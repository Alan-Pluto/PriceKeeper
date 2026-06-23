package com.pricekeeper.app.domain.repository

import kotlinx.coroutines.flow.Flow

interface CategoryRepository {
    fun observeCategories(): Flow<List<String>>

    suspend fun addCategory(name: String)

    suspend fun renameCategory(oldName: String, newName: String)

    suspend fun deleteCategory(name: String)
}
