package com.example.esportsnews

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import java.net.URI
import java.net.URISyntaxException

internal fun isExternalWebUrl(url: String?): Boolean {
    if (url.isNullOrBlank()) return false
    return try {
        val uri = URI(url)
        (uri.scheme.equals("https", ignoreCase = true) ||
            uri.scheme.equals("http", ignoreCase = true)) &&
            !uri.host.isNullOrBlank() && uri.rawUserInfo == null &&
            uri.port in -1..65535
    } catch (_: URISyntaxException) {
        false
    }
}

fun openExternalWebLink(context: Context, url: String?): Boolean {
    if (!isExternalWebUrl(url)) {
        Toast.makeText(context, R.string.error_external_link, Toast.LENGTH_SHORT).show()
        return false
    }
    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
        addCategory(Intent.CATEGORY_BROWSABLE)
        if (context !is Activity) addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    return try {
        context.startActivity(intent)
        true
    } catch (_: ActivityNotFoundException) {
        Toast.makeText(context, R.string.error_external_link, Toast.LENGTH_SHORT).show()
        false
    } catch (_: SecurityException) {
        Toast.makeText(context, R.string.error_external_link, Toast.LENGTH_SHORT).show()
        false
    }
}
