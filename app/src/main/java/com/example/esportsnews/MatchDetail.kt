package com.example.esportsnews

import com.google.gson.annotations.SerializedName

data class MatchDetail(
    val id: Int,
    val game: String,
    val status: String,
    @SerializedName("number_of_games") val numberOfGames: Int,
    @SerializedName("begin_at") val beginAt: String?,
    @SerializedName("team_a_score") val teamAScore: Int,
    @SerializedName("team_b_score") val teamBScore: Int,
    @SerializedName("stream_url") val streamUrl: String?,

    val league: League?,

    @SerializedName("team_a") val teamA: TeamDetail?,
    @SerializedName("team_b") val teamB: TeamDetail?,
    val games: List<GameMap>
)

data class TeamDetail(
    val id: Int,
    val name: String,
    val acronym: String?,
    @SerializedName("image_url") val imageUrl: String?,
    val players: List<Player>
)

data class Player(
    val id: Int,
    val name: String,
    @SerializedName("first_name") val firstName: String?,
    @SerializedName("last_name") val lastName: String?,
    @SerializedName("image_url") val imageUrl: String?
)

data class GameMap(
    val id: Int,
    val position: Int,
    val status: String,
    @SerializedName("map_name") val mapName: String?,
    @SerializedName("winner_id") val winnerId: Int?
)