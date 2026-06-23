package com.pricekeeper.app.data.export

enum class ImportConflictStrategy {
    /** Replace all existing data with imported data. */
    OVERWRITE,
    /** Add new records, skip duplicates (matched by name for goods/stores). */
    MERGE,
    /** Only add new records, skip all existing. */
    SKIP
}
