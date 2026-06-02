package com.example.esportsnews

data class Match(
    val id: Int,
    val pandascore_id: Int,
    val game: String,
    val status: String,
    val team_a_score: Int,
    val team_b_score: Int,
    val begin_at: String?,
    val stream_url: String?,
    val team_a: Team,
    val team_b: Team,
    val league: League?
)

data class Team(
    val id: Int,
    val name: String,
    val acronym: String?,
    val image_url: String?
)

data class League(
    val id: Int,
    val name: String,
    val image_url: String?
)