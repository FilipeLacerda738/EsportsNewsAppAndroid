package com.example.esportsnews.feature.matchdetail

internal fun shouldPollDetail(
    active: Boolean, status: String?, requestActive: Boolean, hasError: Boolean
): Boolean = active && status == "running" && !requestActive && !hasError
