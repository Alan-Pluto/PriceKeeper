package com.pricekeeper.app.feature.navigation

import android.content.Context
import android.content.Intent
import android.net.Uri

data class MapDestination(
    val name: String,
    val address: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val mapUrl: String? = null
)

fun openMapRoutePlan(context: Context, destination: MapDestination) {
    val latitude = destination.latitude
    val longitude = destination.longitude
    if (latitude != null && longitude != null) {
        val amapIntent = Intent(Intent.ACTION_VIEW, buildAmapRoutePlanUri(latitude, longitude, destination.name))
            .setPackage(AMAP_PACKAGE_NAME)
        try {
            context.startActivity(amapIntent)
            return
        } catch (_: Exception) {
            // 未安装高德或 URI 不被支持时，继续使用系统通用 geo 协议兜底。
        }
    }

    val fallbackUri = when {
        latitude != null && longitude != null ->
            Uri.parse("geo:$latitude,$longitude?q=$latitude,$longitude(${Uri.encode(destination.name)})")
        !destination.mapUrl.isNullOrBlank() -> Uri.parse(destination.mapUrl)
        else -> Uri.parse("geo:0,0?q=${Uri.encode(destination.address ?: destination.name)}")
    }
    try {
        context.startActivity(Intent(Intent.ACTION_VIEW, fallbackUri))
    } catch (_: Exception) {
        if (!destination.mapUrl.isNullOrBlank() && fallbackUri.toString() != destination.mapUrl) {
            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(destination.mapUrl)))
        }
    }
}

internal fun buildAmapRoutePlanUri(latitude: Double, longitude: Double, name: String): Uri {
    return Uri.parse(
        // 打开高德“路线规划页”，不直接开始导航；t=0 只是默认显示驾车路线，用户可自行切换方式。
        "amapuri://route/plan/?sourceApplication=PriceKeeper" +
            "&dlat=$latitude" +
            "&dlon=$longitude" +
            "&dname=${Uri.encode(name)}" +
            "&dev=0" +
            "&t=0"
    )
}

private const val AMAP_PACKAGE_NAME = "com.autonavi.minimap"
