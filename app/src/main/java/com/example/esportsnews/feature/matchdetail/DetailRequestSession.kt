package com.example.esportsnews.feature.matchdetail

// A screen owns a session; refreshing creates a request without changing that ownership.
internal class DetailRequestSession {
    var session: Long = 0L
        private set
    var matchId: Int? = null
        private set
    private var request: Long = 0L

    fun open(id: Int): Long {
        ++session
        ++request
        matchId = id
        return session
    }

    fun nextRequest(): Long = ++request

    fun owns(expectedSession: Long): Boolean = matchId != null && session == expectedSession

    fun accepts(expectedSession: Long, expectedRequest: Long): Boolean =
        owns(expectedSession) && request == expectedRequest

    fun close(expectedSession: Long): Boolean {
        if (!owns(expectedSession)) return false
        ++session
        ++request
        matchId = null
        return true
    }
}
