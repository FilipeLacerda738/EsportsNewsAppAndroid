package com.example.esportsnews

import com.example.esportsnews.model.Match
import com.google.gson.Gson
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class MatchContractTest {
    private val gson = Gson()

    @Test
    fun missingOrNullTeamsAreAccepted() {
        val missingTeams = gson.fromJson(matchJson(), Match::class.java)
        val nullTeams = gson.fromJson(
            matchJson("\"team_a\":null,\"team_b\":null"),
            Match::class.java
        )

        assertNull(missingTeams.team_a)
        assertNull(missingTeams.team_b)
        assertNull(nullTeams.team_a)
        assertNull(nullTeams.team_b)
    }

    @Test
    fun validTeamsAndMatchFieldsArePreserved() {
        val match = gson.fromJson(
            matchJson(
                """
                "team_a":{"id":11,"name":"Team A","acronym":"TA","image_url":"a.png"},
                "team_b":{"id":12,"name":"Team B","acronym":"TB","image_url":"b.png"}
                """.trimIndent()
            ),
            Match::class.java
        )

        assertNotNull(match.team_a)
        assertEquals("Team A", match.team_a?.name)
        assertEquals("Team B", match.team_b?.name)
        assertEquals(2, match.id)
        assertEquals(1, match.team_a_score)
        assertEquals(0, match.team_b_score)
    }

    private fun matchJson(teamFields: String = "") =
        """
        {
          "id":2,
          "pandascore_id":22,
          "game":"CSGO",
          "status":"running",
          "team_a_score":1,
          "team_b_score":0,
          "begin_at":null,
          "stream_url":null
          ${if (teamFields.isNotBlank()) ",${teamFields.lines().joinToString("\n")}" else ""}
        }
        """.trimIndent()
}
