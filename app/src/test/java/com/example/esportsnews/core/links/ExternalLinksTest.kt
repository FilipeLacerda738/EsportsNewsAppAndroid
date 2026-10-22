package com.example.esportsnews.core.links

import com.example.esportsnews.core.links.isExternalWebUrl

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ExternalLinksTest {
    @Test
    fun acceptsAbsoluteWebLinks() {
        listOf(
            "https://example.com/watch?v=123#live",
            "http://example.com:8080/stream",
            "HTTPS://example.com/video",
            "https://[2001:db8::1]/video"
        ).forEach { assertTrue(it, isExternalWebUrl(it)) }
    }

    @Test
    fun rejectsUnsafeOrMalformedLinks() {
        listOf(
            null, "", " ", "/watch/123", "//example.com/watch", "https:///watch",
            "https://", "javascript:alert(1)", "file:///tmp/video", "intent://example.com",
            "https://user:password@example.com/", "https://example.com@other.example/",
            "https://example.com\\@other.example/", "https://example.com/a b",
            "https://example.com/\npath", "https://example.com:99999/"
        ).forEach { assertFalse(it, isExternalWebUrl(it)) }
    }
}
