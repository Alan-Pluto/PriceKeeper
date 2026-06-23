package com.pricekeeper.app.core.navigation

/**
 * Navigation route constants.
 */
object Route {
    const val HOME = "home"
    const val GOODS = "goods"
    const val STORE = "store"
    const val PROFILE = "profile"
    const val GOODS_DETAIL = "goods_detail/{goodsId}"
    const val STORE_DETAIL = "store_detail/{storeId}"
    const val MANUAL_ENTRY = "manual_entry"
    const val RECEIPT_CAPTURE = "receipt_capture"
    const val RECEIPT_RECOGNIZE = "receipt_recognize/{imagePath}"
    const val CATEGORY_MANAGEMENT = "category_management"
    const val ABOUT = "about"

    fun goodsDetail(goodsId: Long) = "goods_detail/$goodsId"
    fun storeDetail(storeId: Long) = "store_detail/$storeId"
    fun receiptRecognize(imagePath: String) =
        "receipt_recognize/${java.net.URLEncoder.encode(imagePath, "UTF-8")}"
}
