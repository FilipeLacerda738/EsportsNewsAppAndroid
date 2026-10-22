package com.example.esportsnews.core.network

import com.example.esportsnews.R
import com.example.esportsnews.core.network.SearchUnavailableException

import com.example.esportsnews.core.network.requestErrorMessage

import com.google.gson.JsonParseException
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.*
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException
import java.net.SocketTimeoutException

class RequestErrorsTest {
    @Test
    fun expectedFailuresHaveSpecificLocalizedMessages() {
        assertEquals(R.string.error_timeout, requestErrorMessage(SocketTimeoutException()))
        assertEquals(R.string.error_connection, requestErrorMessage(IOException()))
        assertEquals(R.string.error_invalid_response, requestErrorMessage(JsonParseException("synthetic")))
        assertEquals(R.string.error_search_unavailable, requestErrorMessage(SearchUnavailableException()))
        for (code in listOf(401, 403)) assertEquals(R.string.error_access_denied, httpMessage(code))
        assertEquals(R.string.error_not_found, httpMessage(404))
        assertEquals(R.string.error_timeout, httpMessage(504))
        assertEquals(R.string.error_rate_limit, httpMessage(429))
        assertEquals(R.string.error_server_unavailable, httpMessage(503))
        assertEquals(R.string.error_request_failed, httpMessage(400))
    }

    @Test
    fun programmingErrorsAndCancellationAreNotConvertedToNetworkFailures() {
        assertThrows(IllegalStateException::class.java) { requestErrorMessage(IllegalStateException("synthetic")) }
        assertThrows(java.util.concurrent.CancellationException::class.java) {
            requestErrorMessage(java.util.concurrent.CancellationException())
        }
    }

    private fun httpMessage(code: Int): Int =
        requestErrorMessage(HttpException(Response.error<Any>(code, "".toResponseBody())))
}
