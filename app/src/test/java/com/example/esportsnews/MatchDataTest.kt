package com.example.esportsnews

import com.example.esportsnews.model.MatchDetail
import com.example.esportsnews.model.Stream
import com.google.gson.Gson
import com.google.gson.JsonParseException
import org.junit.Assert.*
import org.junit.Test

class MatchDataTest {
    private val gson = Gson()

    @Test
    fun streamOrderingPrefersLanguageThenMainThenOfficial() {
        val other = stream("en", "https://twitch.tv/english", true, true)
        val regular = stream("pt", "https://twitch.tv/regular")
        val official = stream("pt-BR", "https://twitch.tv/official", official = true)
        val main = stream("pt", "https://twitch.tv/main", main = true)
        assertEquals(listOf(main, official, regular, other), orderedStreams(listOf(other, regular, official, main), "pt-BR"))
    }

    @Test
    fun unsafeAndDuplicateStreamsAreOmittedAndUnknownMetadataIsAccepted() {
        val unknown = gson.fromJson("""{"raw_url":"https://example.org/live"}""", Stream::class.java)
        assertNull(unknown.language)
        assertNull(unknown.main)
        assertNull(unknown.official)
        assertEquals(listOf(unknown), orderedStreams(listOf(null, unknown, unknown, stream(null, "javascript:alert(1)")), "es"))
    }

    @Test
    fun platformRequiresRealHostAndNeverInfersOfficialFlag() {
        assertEquals("Web", streamPlatform("https://example.org/twitch.tv"))
        assertEquals("Web", streamPlatform("https://youtube.com.evil.example/watch"))
        assertEquals("Twitch", streamPlatform("https://www.twitch.tv/channel"))
        assertEquals("YouTube", streamPlatform("https://youtu.be/video"))
        assertEquals("Kick", streamPlatform("https://kick.com/channel"))
        assertEquals("channel", streamChannel("https://twitch.tv/channel"))
        assertNull(streamChannel("https://twitch.tv/directory"))
        assertNull(streamChannel("https://youtube.com/watch?v=1"))
        assertEquals("@channel", streamChannel("https://youtube.com/@channel"))
        assertNull(gson.fromJson("""{"raw_url":"https://youtube.com/watch?v=1"}""", Stream::class.java).official)
    }

    @Test
    fun emptyOrInvalidCountriesDoNotRequestFlags() {
        for (country in listOf(null, "", " ", "BRA", "../us", "1x")) assertNull(countryFlagUrl(country))
        assertEquals("https://flagcdn.com/w40/br.png", countryFlagUrl(" BR "))
    }

    @Test
    fun mapWinnerMatchesPandaScoreIdAndNotInternalId() {
        val detail = gson.fromJson(
            """{"id":7,"game":"CSGO","status":"finished",
                "team_a":{"id":11,"pandascore_id":101,"name":"Team A"},
                "team_b":{"id":12,"pandascore_id":102,"name":"Team B"}}""", MatchDetail::class.java
        )
        assertEquals("Team B", mapWinnerName(102, detail.teamA, detail.teamB))
        assertNull(mapWinnerName(12, detail.teamA, detail.teamB))
        assertNull(mapWinnerName(null, detail.teamA, detail.teamB))
        assertNull(mapWinnerName(999, detail.teamA, detail.teamB))
    }

    @Test
    fun absentFormatAndOptionalDetailListsRemainUnknown() {
        for (json in listOf(
            """{"id":7,"game":"CSGO","status":"not_started"}""",
            """{"id":7,"game":"CSGO","status":"not_started","number_of_games":null,"games":null}"""
        )) {
            val detail = gson.fromJson(json, MatchDetail::class.java)
            assertNull(detail.numberOfGames)
            assertTrue(detail.games.orEmpty().isEmpty())
            assertNull(detail.teamA)
            assertEquals(detail, validateMatchDetail(detail, 7))
        }
    }

    @Test
    fun wrongMatchOrMissingRequiredFieldsAreRejected() {
        val valid = gson.fromJson("""{"id":7,"game":"CSGO","status":"running"}""", MatchDetail::class.java)
        assertThrows(JsonParseException::class.java) { validateMatchDetail(valid, 8) }
        for (json in listOf(
            """{"id":7,"game":"CSGO"}""",
            """{"id":7,"game":"CSGO","status":"running","team_a":{"id":1}}""",
            """{"id":7,"game":"CSGO","status":"running","games":[{"id":1}]}"""
        )) {
            val invalid = gson.fromJson(json, MatchDetail::class.java)
            assertThrows(JsonParseException::class.java) { validateMatchDetail(invalid, 7) }
        }
    }

    private fun stream(language: String?, url: String, main: Boolean = false, official: Boolean = false) =
        Stream(language, url, null, main, official)
}
