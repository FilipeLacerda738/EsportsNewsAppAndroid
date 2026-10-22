package com.example.esportsnews.app.navigation

internal object AppRoutes {
    const val MATCHES = "matches_list"
    const val SETTINGS = "settings"
    const val DETAIL = "match_detail/{matchId}"
    fun detail(id: Int) = "match_detail/$id"
}
