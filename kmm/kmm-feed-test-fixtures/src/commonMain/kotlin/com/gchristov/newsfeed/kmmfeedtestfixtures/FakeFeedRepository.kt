package com.gchristov.newsfeed.kmmfeedtestfixtures

import com.gchristov.newsfeed.kmmcommontest.FakeResponse
import com.gchristov.newsfeed.kmmcommontest.execute
import com.gchristov.newsfeed.kmmfeeddata.FeedRepository
import com.gchristov.newsfeed.kmmfeeddata.model.DecoratedFeedPage
import com.gchristov.newsfeed.kmmpostdata.PostRepository

class FakeFeedRepository(
    private val postRepository: PostRepository,
    val feedPages: List<DecoratedFeedPage>? = null,
    val feedPageCache: DecoratedFeedPage? = null
) : FeedRepository {
    var feedResponse: FakeResponse = FakeResponse.CompletesNormally
    var feedLoadMoreResponse: FakeResponse = FakeResponse.CompletesNormally

    private var _cacheCleared = false
    private var _pageIndex = 0
    private var _lastSearchQuery = "brexit,fintech"

    override suspend fun feedPage(
        pageId: Int,
        feedQuery: String
    ): DecoratedFeedPage {
        val fakeResponse = if (_pageIndex == 0) feedResponse else feedLoadMoreResponse
        val indexToLoad = _pageIndex
        if (fakeResponse !is FakeResponse.Error) {
            // Errors should retry loading the same page so do not advance the current index
            _pageIndex++
        }
        return fakeResponse.execute(requireNotNull(feedPages)[indexToLoad])
    }

    override suspend fun redecorateFeedPage(feedPage: DecoratedFeedPage): DecoratedFeedPage {
        return feedPage.copy(items = feedPage.items.map {
            it.copy(favouriteTimestamp = postRepository.favouriteTimestamp(it.raw.itemId))
        })
    }

    override suspend fun cachedFeedPage(): DecoratedFeedPage? {
        return feedPageCache
    }

    override suspend fun clearCache() {
        _cacheCleared = true
    }

    override suspend fun saveSearchQuery(searchQuery: String) {
        _lastSearchQuery = searchQuery
    }

    override suspend fun searchQuery(): String {
        return _lastSearchQuery
    }

    fun resetCurrentPage() {
        _pageIndex = 0
    }

    fun assertCacheCleared() = _cacheCleared
}