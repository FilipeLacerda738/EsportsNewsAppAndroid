package com.example.esportsnews.data.remote.dto

import androidx.annotation.Keep

@Keep
data class Stream(
    val language: String?,
    val raw_url: String?,
    val embed_url: String?,
    val main: Boolean?,
    val official: Boolean?
)
