package com.example.esportsnews.feature.matchdetail

import com.example.esportsnews.core.links.isExternalWebUrl

import com.example.esportsnews.data.remote.dto.Stream
import java.net.URI
import java.util.Locale

internal fun orderedStreams(streams: List<Stream?>?, language: String): List<Stream> {
    val preferred = language.substringBefore('-').substringBefore('_').lowercase(Locale.ROOT)
    return streams.orEmpty().filterNotNull().filter { isExternalWebUrl(it.raw_url) }
        .sortedWith(compareByDescending<Stream> {
            it.language?.substringBefore('-')?.substringBefore('_')?.lowercase(Locale.ROOT) == preferred
        }.thenByDescending { it.main == true }.thenByDescending { it.official == true })
        .distinctBy { it.raw_url }
}

internal fun streamPlatform(url: String?): String {
    if (!isExternalWebUrl(url)) return "Web"
    val host = URI(url).host.lowercase(Locale.ROOT)
    fun matches(domain: String) = host == domain || host.endsWith(".$domain")
    return when {
        matches("twitch.tv") -> "Twitch"
        matches("youtube.com") || matches("youtu.be") -> "YouTube"
        matches("kick.com") -> "Kick"
        else -> "Web"
    }
}

internal fun countryFlagUrl(country: String?): String? = country?.trim()
    ?.takeIf { it.matches(Regex("[a-zA-Z]{2}")) }
    ?.let { "https://flagcdn.com/w40/${it.lowercase(Locale.ROOT)}.png" }

internal fun streamChannel(url: String?): String? {
    if (!isExternalWebUrl(url)) return null
    val uri = URI(url)
    val host = uri.host.lowercase(Locale.ROOT)
    val segments = uri.path.orEmpty().trim('/').split('/').filter { it.isNotBlank() }
    if (segments.size != 1) return null
    val channel = segments.single()
    return when {
        host in listOf("twitch.tv", "www.twitch.tv", "m.twitch.tv") &&
            channel.lowercase(Locale.ROOT) !in listOf("directory", "videos", "search", "downloads", "settings", "subscriptions", "inventory", "wallet", "jobs", "turbo", "login", "signup") &&
            channel.matches(Regex("[a-zA-Z0-9_]{1,25}")) -> channel
        host in listOf("kick.com", "www.kick.com") &&
            channel.lowercase(Locale.ROOT) !in listOf("categories", "search", "following", "browse", "dashboard", "login", "signup") &&
            channel.matches(Regex("[a-zA-Z0-9_-]+")) -> channel
        streamPlatform(url) == "YouTube" && channel.startsWith("@") && channel.length > 1 -> channel
        else -> null
    }
}
