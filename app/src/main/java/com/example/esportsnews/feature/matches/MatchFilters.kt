package com.example.esportsnews.feature.matches

import com.example.esportsnews.R

enum class GameFilter(val apiValue: String?, val label: String) {
    ALL(null, ""), CS2("CSGO", "CS2"), VALORANT("VALORANT", "VALORANT")
}
enum class StatusFilter(val apiValue: String?, val labelRes: Int) {
    ALL(null, R.string.filter_all), LIVE("running", R.string.status_live),
    UPCOMING("not_started", R.string.status_upcoming), FINISHED("finished", R.string.status_finished)
}
enum class TierFilter(val apiValue: String?) {
    ALL(null), S("S"), A("A"), B("B"), C("C")
}
