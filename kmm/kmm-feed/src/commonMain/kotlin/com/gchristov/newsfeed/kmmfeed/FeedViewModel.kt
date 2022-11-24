package com.gchristov.newsfeed.kmmfeed

import com.gchristov.newsfeed.kmmcommonmvvm.CommonViewModel
import com.gchristov.newsfeed.kmmfeeddata.FeedRepository
import com.gchristov.newsfeed.kmmfeeddata.model.SectionedFeed
import com.gchristov.newsfeed.kmmfeeddata.model.hasNextPage
import com.gchristov.newsfeed.kmmfeeddata.usecase.GetSectionedFeedUseCase
import com.gchristov.newsfeed.kmmfeeddata.usecase.RedecorateSectionedFeedUseCase
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.filter


class FeedViewModel(
    dispatcher: CoroutineDispatcher,
    private val feedRepository: FeedRepository,
    private val getSectionedFeedUseCase: GetSectionedFeedUseCase,
    private val redecorateSectionedFeedUseCase: RedecorateSectionedFeedUseCase
) : CommonViewModel<FeedViewModel.State>(
    dispatcher = dispatcher,
    initialState = State()
) {
    private val searchQueryFlow = MutableStateFlow("")

    // Added to existing state instead of creating new variables?
    // ----
    // mutableStateOf("") for search
    // mutableStateOf(value = SearchWidgetState.CLOSED) for widget state

    init {
        loadFeedWithStoredSearchQuery()
        observeSearchQuery()
    }

    private fun loadFeedWithStoredSearchQuery() {
        launchUiCoroutine {
            val savedSearchQuery = feedRepository.searchQuery()
            setState { copy(searchQuery = savedSearchQuery) }
            loadNextPage()
        }
    }

    /**
     * Observer function placed on the search text being updated from the UI.
     *
     * The MutableStateFlow, representing the sequence of keystrokes being emitted
     * by the user, is debounced every 500ms so that downstream services (API,
     * repository...) are not hit an unnecessary number of times and only when relevant
     * results can be returned
     */
    @OptIn(FlowPreview::class)
    private fun observeSearchQuery() {
        launchUiCoroutine {
            searchQueryFlow
                .debounce(DEBOUNCE_INTERVAL_MS)
                .filter { text -> text.isNotEmpty() }
                .collect { debouncedText ->
                    setState { copy(searchQuery = debouncedText) }
                    loadNextPage(startFromFirst = true)
                    feedRepository.saveSearchQuery(debouncedText)
                }
        }
    }

    fun onSearchTextChanged(newQuery: String) {
        searchQueryFlow.value = newQuery
        setState{ copy(searchQuery = newQuery) }
    }

    fun onSearchStateChanged(newSearchState: SearchWidgetState) {
        setState {
            copy(
                searchWidgetState = newSearchState
            )
        }
    }

    fun redecorateContent() {
        state.value.sectionedFeed?.let { sectionedFeed ->
            launchUiCoroutine {
                val redecorated = redecorateSectionedFeedUseCase(sectionedFeed)
                setState { copy(sectionedFeed = redecorated) }
            }
        }
    }

    fun refreshContent() {
        // Clear cache when user explicitly requests a refresh
        launchUiCoroutine { feedRepository.clearCache() }
        loadNextPage()
    }

    fun loadNextPage(startFromFirst: Boolean = true) {
        val nextPage = if (startFromFirst) 1 else state.value.sectionedFeed?.currentPage!!.plus(1)
        val hasNextPage = if (startFromFirst) true else state.value.sectionedFeed?.hasNextPage()!!
        // Check if we have reached the end of the list
        if (!hasNextPage) {
            setState { copy(reachedEnd = true) }
            return
        }
        // Do not load more if we're already loading
        if (state.value.loadingMore) {
            return
        }
        setState {
            copy(
                loading = startFromFirst,
                loadingMore = !startFromFirst,
                reachedEnd = false,
                blockingError = null,
                nonBlockingError = null,
            )
        }
        launchUiCoroutine {
            try {
                val feedUpdate = getSectionedFeedUseCase(
                    pageId = nextPage,
                    feedQuery = state.value.searchQuery,
                    currentFeed = state.value.sectionedFeed,
                    // Only request cache if we're starting from the first page
                    onCache = if (startFromFirst) { cache ->
                        setState { copy(sectionedFeed = cache) }
                        launchUiCoroutine { feedRepository.clearCache() }
                    } else null
                )
                setState {
                    copy(
                        loading = false,
                        loadingMore = false,
                        sectionedFeed = feedUpdate,
                    )
                }
            } catch (error: Exception) {
                error.printStackTrace()
                val blocking = if (state.value.sectionedFeed == null) error else null
                val nonBlocking = if (blocking == null) error else null
                setState {
                    copy(
                        loading = false,
                        loadingMore = false,
                        blockingError = blocking,
                        nonBlockingError = nonBlocking
                    )
                }
            }
        }
    }

    fun dismissNonBlockingError() {
        setState { copy(nonBlockingError = null) }
    }

    data class State(
        val loading: Boolean = false,
        val loadingMore: Boolean = false,
        val reachedEnd: Boolean = false,
        val blockingError: Throwable? = null,
        val nonBlockingError: Throwable? = null,
        val sectionedFeed: SectionedFeed? = null,
        val searchQuery: String = "brexit,fintech",

        // Separate or here?
        val searchWidgetState: SearchWidgetState = SearchWidgetState.CLOSED,
        val searchTextState: String = ""
    )
}

private const val DEBOUNCE_INTERVAL_MS = 500L
