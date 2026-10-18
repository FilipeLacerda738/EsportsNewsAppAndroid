package com.example.esportsnews

import com.google.gson.JsonParseException
import retrofit2.HttpException
import java.io.IOException
import java.net.SocketTimeoutException

internal fun requestErrorMessage(error: Exception): Int = when (error) {
    is SocketTimeoutException -> R.string.error_timeout
    is HttpException -> when (error.code()) {
        401, 403 -> R.string.error_access_denied
        404 -> R.string.error_not_found
        408, 504 -> R.string.error_timeout
        429 -> R.string.error_rate_limit
        in 500..599 -> R.string.error_server_unavailable
        else -> R.string.error_request_failed
    }
    is JsonParseException -> R.string.error_invalid_response
    is IOException -> R.string.error_connection
    else -> throw error
}
