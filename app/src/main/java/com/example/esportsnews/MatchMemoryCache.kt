package com.example.esportsnews

import java.time.Instant

// All effective request parameters, including the contract required before accepting data.
internal data class MatchPageKey(
    val game: String?,
    val status: String?,
    val tier: String?,
    val team: String?,
    val beginAtGte: String?,
    val beginAtLt: String?,
    val page: Int,
    val limit: Int = 15,
    val searchVersion: Int? = if (team != null || beginAtGte != null) 1 else null
)

internal data class RecentMatchData<T>(
    val value: T,
    val queriedAt: Instant,
    val receivedNanos: Long,
    val revalidationFailed: Boolean = false
) {
    fun isFresh(nowNanos: Long): Boolean =
        !revalidationFailed && nowNanos - receivedNanos in 0L until MATCH_FRESH_NANOS
}

internal const val MATCH_FRESH_NANOS = 30_000_000_000L
private const val MATCH_RETAIN_NANOS = 600_000_000_000L

// Main-thread confined, bounded history. Active screen data remains available after eviction.
internal class MatchMemoryCache<K, V>(
    private val maxEntries: Int,
    private val nowNanos: () -> Long
) {
    private val entries = LinkedHashMap<K, RecentMatchData<V>>(16, 0.75f, true)

    fun get(key: K): RecentMatchData<V>? {
        prune()
        return entries[key]
    }

    fun put(key: K, value: V, queriedAt: Instant): RecentMatchData<V> {
        prune()
        val entry = RecentMatchData(value, queriedAt, nowNanos())
        entries[key] = entry
        while (entries.size > maxEntries) entries.remove(entries.keys.first())
        return entry
    }

    fun removeWhere(predicate: (K) -> Boolean) {
        entries.keys.removeAll(predicate)
    }

    fun markStale(predicate: (K) -> Boolean) {
        entries.replaceAll { key, entry ->
            if (predicate(key)) entry.copy(revalidationFailed = true) else entry
        }
    }

    fun prune() {
        val now = nowNanos()
        entries.values.removeAll { now - it.receivedNanos !in 0L until MATCH_RETAIN_NANOS }
    }
}
