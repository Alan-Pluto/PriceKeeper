package com.pricekeeper.app.feature.manual

import android.content.Context
import android.location.Geocoder
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLDecoder
import java.nio.charset.StandardCharsets
import java.util.Locale

data class StoreLocationInfo(
    val latitude: Double,
    val longitude: Double,
    val region: String,
    val address: String,
    val mapUrl: String? = null
)

enum class MapShareLinkType {
    Short,
    Long
}

sealed interface StoreLocationParseResult {
    data class Parsed(
        val latitude: Double,
        val longitude: Double,
        val mapUrl: String
    ) : StoreLocationParseResult

    data class LinkOnly(
        val mapUrl: String
    ) : StoreLocationParseResult

    data class Invalid(
        val message: String
    ) : StoreLocationParseResult
}

data class ExpandedMapShare(
    val url: String,
    val bodySnippet: String? = null
)

fun detectMapShareLinkType(rawValue: String): MapShareLinkType? {
    val mapUrl = normalizeMapUrl(rawValue) ?: return null
    return if (SHORT_MAP_LINK_REGEX.containsMatchIn(mapUrl)) {
        MapShareLinkType.Short
    } else {
        MapShareLinkType.Long
    }
}

suspend fun parseMapShareLocation(rawValue: String): StoreLocationParseResult {
    return parseMapShareLocation(rawValue, ::expandMapShare)
}

internal suspend fun parseMapShareLocation(
    rawValue: String,
    expandUrl: suspend (String) -> ExpandedMapShare?
): StoreLocationParseResult {
    val originalMapUrl = normalizeMapUrl(rawValue)
        ?: return StoreLocationParseResult.Invalid("请粘贴以 http:// 或 https:// 开头的地图分享链接")
    val linkType = detectMapShareLinkType(originalMapUrl) ?: MapShareLinkType.Long

    // 短链本身通常没有经纬度，需要先请求重定向后的长链；长链则直接解析，避免无意义网络请求。
    val expanded = if (linkType == MapShareLinkType.Short) {
        expandUrl(originalMapUrl)
    } else {
        null
    }
    val resolvedMapUrl = expanded?.url ?: originalMapUrl

    return (parseStoreCoordinates(resolvedMapUrl)
        ?: expanded?.bodySnippet?.let(::parseStoreCoordinates))?.let { (latitude, longitude) ->
        StoreLocationParseResult.Parsed(
            latitude = latitude,
            longitude = longitude,
            mapUrl = resolvedMapUrl
        )
    } ?: StoreLocationParseResult.LinkOnly(
        // 解析失败时保存用户原始输入，尤其是短链失败时仍可让地图 App 自行打开。
        mapUrl = originalMapUrl
    )
}

fun parseStoreCoordinates(rawValue: String): Pair<Double, Double>? {
    val decoded = URLDecoder.decode(rawValue, StandardCharsets.UTF_8.name()).trim()

    parseAmapPoiParam(decoded)?.let { return it }

    val commaMatch = Regex("(-?\\d+(?:\\.\\d+)?)[,，]\\s*(-?\\d+(?:\\.\\d+)?)").find(decoded)
    if (commaMatch != null) {
        val first = commaMatch.groupValues[1].toDoubleOrNull()
        val second = commaMatch.groupValues[2].toDoubleOrNull()
        return coordinatesOrNull(latitude = first, longitude = second)
            ?: coordinatesOrNull(latitude = second, longitude = first)
    }

    val latFirst = Regex(
        "(?:lat|latitude)=(-?\\d+(?:\\.\\d+)?).*?(?:lng|lon|longitude)=(-?\\d+(?:\\.\\d+)?)",
        RegexOption.IGNORE_CASE
    ).find(decoded)
    if (latFirst != null) {
        return coordinatesOrNull(
            latitude = latFirst.groupValues[1].toDoubleOrNull(),
            longitude = latFirst.groupValues[2].toDoubleOrNull()
        )
    }

    val lngFirst = Regex(
        "(?:lng|lon|longitude)=(-?\\d+(?:\\.\\d+)?).*?(?:lat|latitude)=(-?\\d+(?:\\.\\d+)?)",
        RegexOption.IGNORE_CASE
    ).find(decoded)
    if (lngFirst != null) {
        return coordinatesOrNull(
            latitude = lngFirst.groupValues[2].toDoubleOrNull(),
            longitude = lngFirst.groupValues[1].toDoubleOrNull()
        )
    }

    val jsonLatFirst = Regex(
        "[\"']?(?:lat|latitude)[\"']?\\s*:\\s*[\"']?(-?\\d+(?:\\.\\d+)?)[\"']?[\\s\\S]*?[\"']?(?:lng|lon|longitude)[\"']?\\s*:\\s*[\"']?(-?\\d+(?:\\.\\d+)?)[\"']?",
        RegexOption.IGNORE_CASE
    ).find(decoded)
    if (jsonLatFirst != null) {
        return coordinatesOrNull(
            latitude = jsonLatFirst.groupValues[1].toDoubleOrNull(),
            longitude = jsonLatFirst.groupValues[2].toDoubleOrNull()
        )
    }

    val jsonLngFirst = Regex(
        "[\"']?(?:lng|lon|longitude)[\"']?\\s*:\\s*[\"']?(-?\\d+(?:\\.\\d+)?)[\"']?[\\s\\S]*?[\"']?(?:lat|latitude)[\"']?\\s*:\\s*[\"']?(-?\\d+(?:\\.\\d+)?)[\"']?",
        RegexOption.IGNORE_CASE
    ).find(decoded)
    if (jsonLngFirst != null) {
        return coordinatesOrNull(
            latitude = jsonLngFirst.groupValues[2].toDoubleOrNull(),
            longitude = jsonLngFirst.groupValues[1].toDoubleOrNull()
        )
    }

    return null
}

fun parseStoreCoordinates(uri: Uri): Pair<Double, Double>? {
    val target = uri.getQueryParameter("q") ?: uri.schemeSpecificPart ?: uri.toString()
    return parseStoreCoordinates(target)
}

fun normalizeMapUrl(rawValue: String): String? {
    val value = rawValue.trim()
    if (!value.startsWith("http://") && !value.startsWith("https://")) return null
    return value
}

suspend fun expandMapShareUrl(rawValue: String): String? = withContext(Dispatchers.IO) {
    expandMapShare(rawValue)?.url
}

suspend fun expandMapShare(rawValue: String): ExpandedMapShare? = withContext(Dispatchers.IO) {
    val initialUrl = normalizeMapUrl(rawValue) ?: return@withContext null
    runCatching {
        var currentUrl = initialUrl
        var bodySnippet: String? = null
        repeat(MAX_REDIRECTS) {
            val connection = (URL(currentUrl).openConnection() as HttpURLConnection).apply {
                instanceFollowRedirects = false
                connectTimeout = 5_000
                readTimeout = 5_000
                requestMethod = "GET"
                setRequestProperty(
                    "User-Agent",
                    "Mozilla/5.0 (Linux; Android 14) PriceKeeper/1.0"
                )
            }
            connection.use {
                val location = connection.getHeaderField("Location")
                if (connection.responseCode in 300..399 && !location.isNullOrBlank()) {
                    currentUrl = URL(URL(currentUrl), location).toString()
                    // 高德短链第一跳通常已经带 p=POI,lat,lng；能解析就立即保存这条干净长链，
                    // 不再继续跳到 m.amap.com/callAPP，避免后续链接过长且依赖浏览器中转。
                    if (parseStoreCoordinates(currentUrl) != null) {
                        return@runCatching ExpandedMapShare(currentUrl)
                    }
                    return@repeat
                }
                if (connection.responseCode in 200..299) {
                    bodySnippet = connection.inputStream.bufferedReader().use { it.readText().take(MAX_BODY_SNIPPET_CHARS) }
                }
                return@runCatching ExpandedMapShare(currentUrl, bodySnippet)
            }
        }
        ExpandedMapShare(currentUrl, bodySnippet)
    }.getOrNull()
}

fun resolveStoreLocation(
    context: Context,
    latitude: Double,
    longitude: Double
): StoreLocationInfo {
    val fallback = StoreLocationInfo(
        latitude = latitude,
        longitude = longitude,
        region = "",
        address = "$latitude,$longitude"
    )
    return try {
        val geocoder = Geocoder(context, Locale.CHINA)
        @Suppress("DEPRECATION")
        val address = geocoder.getFromLocation(latitude, longitude, 1)?.firstOrNull() ?: return fallback
        val region = listOfNotNull(address.adminArea, address.locality, address.subLocality)
            .joinToString("")
            .ifBlank { address.featureName.orEmpty() }
        StoreLocationInfo(
            latitude = latitude,
            longitude = longitude,
            region = region,
            address = address.getAddressLine(0) ?: fallback.address
        )
    } catch (_: Exception) {
        fallback
    }
}

private fun parseAmapPoiParam(decoded: String): Pair<Double, Double>? {
    val pValue = Regex("[?&]p=([^&#]+)").find(decoded)?.groupValues?.getOrNull(1)
        ?: Regex("^p=([^&#]+)").find(decoded)?.groupValues?.getOrNull(1)
        ?: return null
    val parts = pValue.split(",")
    if (parts.size < 3) return null
    return coordinatesOrNull(
        latitude = parts[1].toDoubleOrNull(),
        longitude = parts[2].toDoubleOrNull()
    )
}

private fun coordinatesOrNull(latitude: Double?, longitude: Double?): Pair<Double, Double>? {
    if (latitude == null || longitude == null) return null
    if (latitude !in -90.0..90.0 || longitude !in -180.0..180.0) return null
    return latitude to longitude
}

private const val MAX_REDIRECTS = 6
private const val MAX_BODY_SNIPPET_CHARS = 64_000

// 短链识别集中放在这里：后续若要支持腾讯地图等短链，只需要补充这个正则。
private val SHORT_MAP_LINK_REGEX = Regex(
    pattern = "^https?://(?:surl\\.amap\\.com|j\\.map\\.baidu\\.com)/\\S+$",
    option = RegexOption.IGNORE_CASE
)

private inline fun <T : HttpURLConnection, R> T.use(block: (T) -> R): R {
    return try {
        block(this)
    } finally {
        disconnect()
    }
}
