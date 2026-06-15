package com.example.esportsnews.model

import androidx.annotation.Keep

@Keep
data class League(
    val id: Int,
    val name: String,
    val image_url: String?,
    val dark_mode_image_url: String? = null
)