package com.example.esportsnews

import com.google.gson.Gson
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AppUpdatesTest {
    private val gson = Gson()
    private val releaseUrl = "https://example.org/releases/latest"

    @Test
    fun stableVersionsCompareNumericallyAtEachComponent() {
        for ((candidate, installed) in listOf(
            "1.10.0" to "1.9.9",
            "2.0.0" to "1.99.99",
            "1.0.10" to "1.0.9",
            "1.0.99999999999999999999" to "1.0.9"
        )) {
            assertTrue(compareStableVersions(candidate, installed)!! > 0)
            assertTrue(compareStableVersions(installed, candidate)!! < 0)
        }
    }

    @Test
    fun prefixAndWhitespaceDoNotCauseFalseUpdates() {
        assertEquals(0, compareStableVersions(" v1.2.3 ", "1.2.3"))
        assertEquals(0, compareStableVersions("1.2.3", "v1.2.3"))
    }

    @Test
    fun invalidAndPrereleaseVersionsAreNotCompared() {
        for (invalid in listOf(null, "", "latest", "1.2", "1.2.3.4", "1.2.3-beta", "1.2.3+build", "-1.2.3")) {
            assertNull(compareStableVersions(invalid, "1.0.0"))
            assertNull(compareStableVersions("1.0.0", invalid))
        }
    }

    @Test
    fun onlyNewerValidReleasesAreOffered() {
        assertEquals(UpdateCheckState.Available, evaluateAppUpdate("1.9.0", AppVersion("v1.10.0", releaseUrl, null)))
        assertEquals(UpdateCheckState.UpToDate, evaluateAppUpdate("1.10.0", AppVersion("1.9.0", releaseUrl, "")))
        assertEquals(UpdateCheckState.UpToDate, evaluateAppUpdate("1.10.0", AppVersion("v1.10.0", releaseUrl, "")))
    }

    @Test
    fun missingAndNullPayloadFieldsFailSafely() {
        assertEquals(UpdateCheckState.Failed, evaluateAppUpdate("1.0.0", null))
        for (json in listOf("{}", """{"version":null,"download_url":null}""", """{"version":"2.0.0"}""")) {
            assertEquals(UpdateCheckState.Failed, evaluateAppUpdate("1.0.0", gson.fromJson(json, AppVersion::class.java)))
        }
        val validWithoutNotes = gson.fromJson(
            """{"version":"2.0.0","download_url":"$releaseUrl"}""", AppVersion::class.java
        )
        assertEquals(UpdateCheckState.Available, evaluateAppUpdate("1.0.0", validWithoutNotes))
        assertNull(validWithoutNotes.releaseNotes)
    }

    @Test
    fun unsafeOrMissingReleaseLinksAreRejected() {
        for (url in listOf(null, "", "/release", "javascript:alert(1)", "file:///app.apk", "https://user:password@example.org/release", "https://")) {
            assertEquals(UpdateCheckState.Failed, evaluateAppUpdate("1.0.0", AppVersion("2.0.0", url, "")))
        }
    }
}
