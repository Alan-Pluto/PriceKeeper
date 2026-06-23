package com.pricekeeper.app.data.export

import java.io.OutputStream

interface ExportRepository {
    suspend fun exportAllData(outputStream: OutputStream): ExportStats
}
