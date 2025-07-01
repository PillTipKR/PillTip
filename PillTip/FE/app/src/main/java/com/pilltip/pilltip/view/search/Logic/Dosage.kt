package com.pilltip.pilltip.view.search.Logic

fun mapPeriod(koreanPeriod: String): String {
    return when (koreanPeriod) {
        "오전" -> "AM"
        "오후" -> "PM"
        else -> koreanPeriod
    }
}
