package com.example.esportsnews.feature.matches

internal fun shouldPollMatches(
    active: Boolean, status: StatusFilter, page: Int, requestActive: Boolean, hasError: Boolean,
    queryPending: Boolean = false
): Boolean = active && status in listOf(StatusFilter.ALL, StatusFilter.LIVE) && page == 1 && !requestActive && !hasError && !queryPending
