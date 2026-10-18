package com.example.esportsnews

internal enum class UpdateCheckState { Idle, Checking, UpToDate, Available, Failed }

// Only stable major.minor.patch versions are supported; prereleases are not upgrades.
internal fun compareStableVersions(candidate: String?, installed: String?): Int? {
    val pattern = Regex("v?([0-9]+)\\.([0-9]+)\\.([0-9]+)")
    val candidateParts = pattern.matchEntire(candidate?.trim() ?: return null)
        ?.groupValues?.drop(1)?.map { it.toBigInteger() } ?: return null
    val installedParts = pattern.matchEntire(installed?.trim() ?: return null)
        ?.groupValues?.drop(1)?.map { it.toBigInteger() } ?: return null
    for (index in candidateParts.indices) {
        val comparison = candidateParts[index].compareTo(installedParts[index])
        if (comparison != 0) return comparison
    }
    return 0
}

internal fun evaluateAppUpdate(installed: String, candidate: AppVersion?): UpdateCheckState {
    val comparison = compareStableVersions(candidate?.version, installed)
        ?: return UpdateCheckState.Failed
    if (!isExternalWebUrl(candidate?.downloadUrl)) return UpdateCheckState.Failed
    return if (comparison > 0) UpdateCheckState.Available else UpdateCheckState.UpToDate
}
