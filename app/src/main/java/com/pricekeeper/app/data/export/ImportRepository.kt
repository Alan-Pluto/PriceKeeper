package com.pricekeeper.app.data.export

import java.io.InputStream

interface ImportRepository {
    suspend fun importData(inputStream: InputStream, strategy: ImportConflictStrategy): ImportResult
}
