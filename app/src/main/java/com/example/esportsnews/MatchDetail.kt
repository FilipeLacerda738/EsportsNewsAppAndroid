package com.example.esportsnews.model

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class MatchDetail(
    val id: Int,
    val game: String,
    val status: String,
    @SerializedName("number_of_games") val numberOfGames: Int,
    @SerializedName("begin_at") val beginAt: String?,
    @SerializedName("team_a_score") val teamAScore: Int,
    @SerializedName("team_b_score") val teamBScore: Int,
    @SerializedName("stream_url") val streamUrl: String?,
    val streams: List<Stream>? = null,
    val tier: String? = null,
    val prizepool: String? = null,
    val league: League?,
    @SerializedName("team_a") val teamA: TeamDetail?,
    @SerializedName("team_b") val teamB: TeamDetail?,
    val games: List<GameMap>
)

@Keep
data class TeamDetail(
    val id: Int,
    val name: String,
    val acronym: String?,
    @SerializedName("image_url") val imageUrl: String?,
    @SerializedName("dark_mode_image_url") val darkModeImageUrl: String? = null,
    val location: String? = null,
    val players: List<Player>
)

@Keep
data class Player(
    val id: Int,
    val name: String,
    @SerializedName("first_name") val firstName: String?,
    @SerializedName("last_name") val lastName: String?,
    @SerializedName("image_url") val imageUrl: String?
)

@Keep
data class GameMap(
    val id: Int,
    val position: Int,
    val status: String,
    @SerializedName("map_name") val mapName: String?,
    @SerializedName("winner_id") val winnerId: Int?
)