package com.gchristov.newsfeed.android.feed.feature

import android.content.Intent
import android.util.Log
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.Composable
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.gchristov.newsfeed.android.common.compose.CommonComposeActivity
import com.gchristov.newsfeed.android.common.compose.elements.AppBar
import com.gchristov.newsfeed.android.common.compose.elements.AppBlockingError
import com.gchristov.newsfeed.android.common.compose.elements.AppCircularProgressIndicator
import com.gchristov.newsfeed.android.common.compose.elements.AppIcon
import com.gchristov.newsfeed.android.common.compose.elements.AppIconButton
import com.gchristov.newsfeed.android.common.compose.elements.AppImage
import com.gchristov.newsfeed.android.common.compose.elements.AppNonBlockingError
import com.gchristov.newsfeed.android.common.compose.elements.AppPullRefresh
import com.gchristov.newsfeed.android.common.compose.elements.AppScreen
import com.gchristov.newsfeed.android.common.compose.elements.AppText
import com.gchristov.newsfeed.android.common.compose.elements.BlockingError
import com.gchristov.newsfeed.android.common.compose.elements.NonBlockingError
import com.gchristov.newsfeed.android.common.compose.elements.list.AppGroupedList
import com.gchristov.newsfeed.android.common.compose.elements.list.AppListRow
import com.gchristov.newsfeed.android.common.compose.elements.list.items
import com.gchristov.newsfeed.android.common.compose.elements.rememberRipple
import com.gchristov.newsfeed.android.common.compose.elements.search.AppSearchBar
import com.gchristov.newsfeed.android.common.compose.elements.toUiBlockingError
import com.gchristov.newsfeed.android.common.compose.elements.toUiNonBlockingError
import com.gchristov.newsfeed.android.common.compose.theme.Theme
import com.gchristov.newsfeed.android.common.navigation.Navigator
import com.gchristov.newsfeed.multiplatform.common.kotlin.di.DependencyInjector
import com.gchristov.newsfeed.multiplatform.common.kotlin.di.inject
import com.gchristov.newsfeed.multiplatform.common.mvvm.createViewModelFactory
import com.gchristov.newsfeed.multiplatform.feed.data.model.DecoratedFeedItem
import com.gchristov.newsfeed.multiplatform.feed.data.model.SectionedFeed
import com.gchristov.newsfeed.multiplatform.feed.feature.FeedViewModel
import com.gchristov.newsfeed.multiplatform.feed.feature.SearchWidgetState
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.Random
import com.gchristov.newsfeed.android.common.design.R as DesignR

class FeedActivity : CommonComposeActivity() {
    private val viewModel by viewModels<FeedViewModel> { createViewModelFactory { DependencyInjector.inject() } }
    private val navigator: Navigator by lazy { DependencyInjector.inject(arg = this) }

    private val feedItemDateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    private val feedSectionDateFormat = SimpleDateFormat("MMM, yyyy", Locale.getDefault())

    @Composable
    override fun Content() = FeedScreen(
        viewModel = viewModel,
        feedItemDateFormatter = { ms -> feedItemDateFormat.format(ms) },
        feedSectionDateFormatter = { ms -> feedSectionDateFormat.format(ms) },
        onFeedItemClick = { item ->
            navigator.openPost(postId = item.raw.itemId)
        }
    )

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        setIntent(intent)
        viewModel.loadNextPage()
    }

    override fun onResume() {
        super.onResume()
        viewModel.redecorateContent()
    }
}

@Composable
internal fun FeedScreen(
    viewModel: FeedViewModel,
    feedItemDateFormatter: (Long) -> String,
    feedSectionDateFormatter: (Long) -> String,
    onFeedItemClick: (feedItem: DecoratedFeedItem) -> Unit
) {
    val state = viewModel.state.ld().observeAsState().value

    when {
        state?.blockingError != null -> ErrorState(
            blockingError = state.blockingError!!.toUiBlockingError(),
            onRetry = viewModel::refreshContent
        )

        else -> FeedState(
            feedItemDateFormatter = feedItemDateFormatter,
            feedSectionDateFormatter = feedSectionDateFormatter,
            loading = state?.loading == true,
            reachedEnd = state?.reachedEnd == true,
            feed = state?.sectionedFeed,
            nonBlockingError = state?.nonBlockingError?.toUiNonBlockingError(),
            onNonBlockingErrorDismiss = viewModel::dismissNonBlockingError,
            onRefresh = viewModel::refreshContent,
            onLoadMore = { viewModel.loadNextPage(startFromFirst = false) },
            onFeedItemClick = onFeedItemClick,
            searchWidgetState = state?.searchWidgetState ?: SearchWidgetState.CLOSED,
            onSearchClick = {
                viewModel.onSearchStateChanged(it)
            },
            searchTextState = state?.searchQuery ?: "",
            onSearchTextChange = {
                viewModel.onSearchTextChanged(it)
            }
        )
    }
}

@Composable
private fun FeedState(
    feedItemDateFormatter: (Long) -> String,
    feedSectionDateFormatter: (Long) -> String,
    loading: Boolean,
    reachedEnd: Boolean,
    feed: SectionedFeed?,
    nonBlockingError: NonBlockingError?,
    onNonBlockingErrorDismiss: () -> Unit,
    onRefresh: () -> Unit,
    onLoadMore: () -> Unit,
    onFeedItemClick: (feedItem: DecoratedFeedItem) -> Unit,
    searchWidgetState: SearchWidgetState,
    onSearchClick: (searchState: SearchWidgetState) -> Unit,
    searchTextState: String,
    onSearchTextChange: (String) -> Unit
) {
    AppScreen(
        topBar = {
            MainAppBar(
                searchWidgetState = searchWidgetState,
                searchTextState = searchTextState,
                onSearchTextChange = onSearchTextChange,
                onSearchCloseClicked = {
                    onSearchClick(SearchWidgetState.CLOSED)
                },
                onSearchClicked = {
                    Log.d("open", "Searched Text: $it")
                },
                onSearchTriggered = {
                    onSearchClick(SearchWidgetState.OPENED)
                }
            )
        },
    ) {
        AppPullRefresh(
            loading = loading,
            onRefresh = onRefresh
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                feed?.let { feed ->
                    if (!loading && feed.sections.isEmpty()) {
                        FeedEmpty()
                    } else {
                        FeedContent(
                            feedItemDateFormatter = feedItemDateFormatter,
                            feedSectionDateFormatter = feedSectionDateFormatter,
                            feed = feed,
                            reachedEnd = reachedEnd,
                            nonBlockingError = nonBlockingError,
                            onLoadMore = onLoadMore,
                            onFeedItemClick = onFeedItemClick
                        )
                    }
                }
                nonBlockingError?.let { nonBlockingError ->
                    AppNonBlockingError(
                        nonBlockingError = nonBlockingError,
                        onRetry = onLoadMore,
                        onDismiss = onNonBlockingErrorDismiss
                    )
                }
            }
        }
    }
}

@Composable
private fun MainAppBar(
    searchWidgetState: SearchWidgetState,
    searchTextState: String,
    onSearchTextChange: (String) -> Unit,
    onSearchCloseClicked: () -> Unit,
    onSearchClicked: (String) -> Unit,
    onSearchTriggered: () -> Unit
) {
    when (searchWidgetState) {
        SearchWidgetState.CLOSED -> {
            AppBar(
                title = stringResource(DesignR.string.app_name),
                actions = {
                    AppIconButton(
                        onClick = onSearchTriggered,
                        icon = Icons.Filled.Search,
                        contentDescription = "Search"
                    )
                }
            )
        }

        SearchWidgetState.OPENED -> {
            AppSearchBar(
                text = searchTextState,
                onTextChange = onSearchTextChange,
                onCloseClicked = onSearchCloseClicked,
                onSearchClicked = onSearchClicked
            )
        }
    }
}

@Composable
private fun BoxScope.FeedEmpty() {
    AppText(
        modifier = Modifier.align(Alignment.Center),
        text = stringResource(R.string.feed_empty),
        color = Theme.contentColors.secondary.copy(alpha = 0.6f),
        textAlign = TextAlign.Center
    )
}

@Composable
private fun FeedContent(
    feedItemDateFormatter: (Long) -> String,
    feedSectionDateFormatter: (Long) -> String,
    feed: SectionedFeed,
    reachedEnd: Boolean,
    nonBlockingError: NonBlockingError?,
    onLoadMore: () -> Unit,
    onFeedItemClick: (feedItem: DecoratedFeedItem) -> Unit
) {
    AppGroupedList {
        feed.sections.forEachIndexed { index, section ->
            group(
                key = section.type.toString(),
                header = @Composable {
                    section.type.toHeader(headerFormatter = feedSectionDateFormatter)
                }
            ) {
                items(
                    items = section.feedItems,
                    key = { feedItem -> feedItem.raw.itemId }) { feedItem ->
                    FeedItemRow(
                        item = feedItem,
                        dateFormatter = feedItemDateFormatter,
                        onFeedItemClick = onFeedItemClick
                    )
                }
            }
            if (index == feed.sections.lastIndex
                && !reachedEnd
                && nonBlockingError == null
            ) {
                item(key = Random().nextInt()) {
                    LoadMoreRow()
                    onLoadMore()
                }
            }
        }
    }
}

@Composable
private fun FeedItemRow(
    item: DecoratedFeedItem,
    dateFormatter: (Long) -> String,
    onFeedItemClick: (feedItem: DecoratedFeedItem) -> Unit
) {
    AppListRow(
        modifier = Modifier
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = rememberRipple()
            ) {
                onFeedItemClick(item)
            }
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            FeedItemThumbnail(thumbnailUrl = item.raw.thumbnail)
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                FeedItemHeader(header = item.raw.headline ?: "--")
                FeedItemDate(
                    timestamp = item.date.toEpochMilliseconds(),
                    dateFormatter = dateFormatter
                )
            }
            if (item.isFavourite()) {
                AppIcon(
                    imageVector = Icons.Filled.Favorite,
                    tint = Theme.contentColors.action,
                    contentDescription = stringResource(R.string.feed_favourite)
                )
            }
        }
    }
}

@Composable
private fun FeedItemThumbnail(thumbnailUrl: String?) {
    // Placeholder container
    Box(
        modifier = Modifier
            .size(46.dp)
            .clip(CircleShape)
            // This is within a surface so invert the background
            .background(Theme.backgrounds.primary)
    ) {
        thumbnailUrl?.let {
            AppImage(
                modifier = Modifier.fillMaxSize(),
                imageUrl = it,
                contentScale = ContentScale.Crop,
            )
        }
    }
}

@Composable
private fun FeedItemHeader(header: String) {
    AppText(
        text = header,
        style = Theme.typography.bodyBold
    )
}

@Composable
private fun FeedItemDate(
    timestamp: Long,
    dateFormatter: (Long) -> String,
) {
    AppText(
        text = dateFormatter(timestamp),
        style = Theme.typography.subtitle,
        color = Theme.contentColors.secondary
    )
}

@Composable
private fun LoadMoreRow() {
    AppListRow {
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            AppCircularProgressIndicator()
        }
    }
}

@Composable
private fun ErrorState(
    blockingError: BlockingError,
    onRetry: () -> Unit
) {
    AppScreen(
        topBar = {
            AppBar(
                title = stringResource(DesignR.string.app_name)
            )
        },
    ) {
        AppBlockingError(
            blockingError = blockingError,
            onRetry = onRetry
        )
    }
}

@Composable
private fun SectionedFeed.SectionType.toHeader(headerFormatter: (Long) -> String) = when (this) {
    is SectionedFeed.SectionType.ThisWeek -> stringResource(R.string.feed_this_week)
    is SectionedFeed.SectionType.LastWeek -> stringResource(R.string.feed_last_week)
    is SectionedFeed.SectionType.ThisMonth -> stringResource(R.string.feed_this_month)
    is SectionedFeed.SectionType.Older -> headerFormatter(date.toEpochMilliseconds())
}

private fun DecoratedFeedItem.isFavourite() = favouriteTimestamp != null
