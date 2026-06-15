package com.example.esportsnews.model

import androidx.annotation.Keep

@Keep
data class Team(
    val id: Int,
    val name: String,
    val acronym: String?,
    val image_url: String?,
    val dark_mode_image_url: String? = null,
    val location: String? = null
)