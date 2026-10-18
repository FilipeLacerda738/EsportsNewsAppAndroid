package com.example.esportsnews

import com.example.esportsnews.model.TeamDetail

internal fun mapWinnerName(winnerId: Int?, teamA: TeamDetail?, teamB: TeamDetail?): String? {
    if (winnerId == null) return null
    return listOfNotNull(teamA, teamB).firstOrNull { it.pandascoreId == winnerId }?.name
}
