package com.pricekeeper.app.data.repository

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.pricekeeper.app.domain.repository.CategoryRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.categoryDataStore by preferencesDataStore(name = "categories")

@Singleton
class CategoryRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : CategoryRepository {

    override fun observeCategories(): Flow<List<String>> {
        return context.categoryDataStore.data.map { prefs ->
            prefs.currentCategories()
        }
    }

    override suspend fun addCategory(name: String) {
        val cleanName = name.trim()
        if (cleanName.isBlank()) return
        context.categoryDataStore.edit { prefs ->
            prefs[CATEGORIES_KEY] = (prefs.currentCategories() + cleanName).toSet()
            prefs[INITIALIZED_KEY] = true
        }
    }

    override suspend fun renameCategory(oldName: String, newName: String) {
        val cleanOld = oldName.trim()
        val cleanNew = newName.trim()
        if (cleanOld.isBlank() || cleanNew.isBlank()) return
        context.categoryDataStore.edit { prefs ->
            val categories = prefs.currentCategories().map {
                if (it == cleanOld) cleanNew else it
            }
            prefs[CATEGORIES_KEY] = categories.toSet()
            prefs[INITIALIZED_KEY] = true
        }
    }

    override suspend fun deleteCategory(name: String) {
        val cleanName = name.trim()
        if (cleanName == UNCATEGORIZED) return
        context.categoryDataStore.edit { prefs ->
            prefs[CATEGORIES_KEY] = prefs.currentCategories().filterNot { it == cleanName }.toSet()
            prefs[INITIALIZED_KEY] = true
        }
    }

    private fun androidx.datastore.preferences.core.Preferences.currentCategories(): List<String> {
        val source = if (this[INITIALIZED_KEY] == true) {
            this[CATEGORIES_KEY].orEmpty().toList()
        } else {
            DEFAULT_CATEGORIES
        }
        return source
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .distinct()
            .ifEmpty { listOf(UNCATEGORIZED) }
    }

    companion object {
        const val UNCATEGORIZED = "未分类"

        val DEFAULT_CATEGORIES = listOf(
            "生鲜",
            "饮料",
            "日化",
            "粮油调味",
            "乳制品",
            "零食",
            "酒类",
            "其他",
            UNCATEGORIZED
        )

        private val CATEGORIES_KEY = stringSetPreferencesKey("category_names")
        private val INITIALIZED_KEY = booleanPreferencesKey("category_names_initialized")
    }
}
