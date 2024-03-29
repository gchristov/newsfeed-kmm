package com.gchristov.newsfeed.multiplatform.post.data

import com.gchristov.newsfeed.multiplatform.post.data.util.ReadingTimeCalculator
import kotlin.test.Test
import kotlin.test.assertEquals

class ReadingTimeCalculatorTest {

    @Test
    fun returnsZeroMinutesWhenNoWords() {
        val totalWordCount = 0;
        runTest(totalWordCount = totalWordCount, expectedReadingTimeMinutes = 0)
    }

    @Test
    fun returnsZeroMinutesWhenNegativeWords() {
        val totalWordCount = -20;
        runTest(totalWordCount = totalWordCount, expectedReadingTimeMinutes = 0)
    }

    @Test
    fun takesMinimumOneMinuteWhenLessThanTwoHundredWords() {
        // given
        // 1 minute is 200 words
        val totalWordCount = 50;
        runTest(totalWordCount = totalWordCount, expectedReadingTimeMinutes = 1)
    }

    @Test
    fun takesMultipleMinutesForExactlyTwoHundredWords() {
        // given
        // 1 minute is 200 words
        val totalWordCount = 200;
        runTest(totalWordCount = totalWordCount, expectedReadingTimeMinutes = 1)
    }

    @Test
    fun roundsToExtraMinuteWhenPassingFewSecondsInThreshold() {
        // given
        // 1 minute is 200 words, so instead minutes and seconds,
        // the count would be rounded to the greatest minute once
        // it passes 1m 30s
        val totalWordCount = 320;
        runTest(totalWordCount = totalWordCount, expectedReadingTimeMinutes = 2)
    }

    private fun runTest(totalWordCount: Int, expectedReadingTimeMinutes: Int) {
        val readingTimeMinutes = ReadingTimeCalculator.calculateReadingTimeMinutes(totalWordCount)
        assertEquals(expectedReadingTimeMinutes, readingTimeMinutes)
    }
}