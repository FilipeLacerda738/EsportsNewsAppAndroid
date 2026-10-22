package com.example.esportsnews.feature.matchdetail

import com.example.esportsnews.data.remote.dto.TeamDetail

internal fun mapWinnerName(winnerId: Int?, teamA: TeamDetail?, teamB: TeamDetail?): String? {
    if (winnerId == null) return null
    return listOfNotNull(teamA, teamB).firstOrNull { it.pandascoreId == winnerId }?.name
}
