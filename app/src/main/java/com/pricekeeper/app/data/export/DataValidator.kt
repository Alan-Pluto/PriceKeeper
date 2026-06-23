package com.pricekeeper.app.data.export

/**
 * Validates imported data integrity before writing to the database.
 */
class DataValidator {

    /**
     * Validate an [ExportBundle]. Returns a list of errors.
     */
    fun validate(bundle: ExportBundle): List<String> {
        val errors = mutableListOf<String>()

        // Version check
        if (bundle.version != ExportBundle.CURRENT_VERSION) {
            // Future version — reject. Older version — warn but allow.
            if (bundle.version > ExportBundle.CURRENT_VERSION) {
                errors.add("文件版本(${bundle.version})高于当前支持版本(${ExportBundle.CURRENT_VERSION})")
                return errors
            }
        }

        // Validate goods
        bundle.goods.forEach { goods ->
            if (goods.name.isBlank()) errors.add("商品名称不能为空: id=${goods.id}")
        }

        // Validate stores
        bundle.stores.forEach { store ->
            if (store.name.isBlank()) errors.add("商店名称不能为空: id=${store.id}")
        }

        // Validate price records
        val goodsIds = bundle.goods.map { it.id }.toSet()
        val storeIds = bundle.stores.map { it.id }.toSet()

        bundle.priceRecords.forEach { record ->
            if (record.price <= 0) errors.add("价格必须>0: goodsId=${record.goodsId}")
            if (record.goodsId !in goodsIds) errors.add("外键缺失: goodsId=${record.goodsId}")
            if (record.storeId !in storeIds) errors.add("外键缺失: storeId=${record.storeId}")
        }

        return errors
    }
}
