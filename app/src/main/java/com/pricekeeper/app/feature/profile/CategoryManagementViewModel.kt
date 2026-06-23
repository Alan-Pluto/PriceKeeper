package com.pricekeeper.app.feature.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pricekeeper.app.domain.repository.CategoryRepository
import com.pricekeeper.app.domain.repository.GoodsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CategoryManagementViewModel @Inject constructor(
    private val categoryRepository: CategoryRepository,
    private val goodsRepository: GoodsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CategoryManagementUiState())
    val uiState: StateFlow<CategoryManagementUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                categoryRepository.observeCategories(),
                goodsRepository.observeAllCategories()
            ) { managedCategories, dbCategories ->
                (managedCategories + dbCategories)
                    .map { it.trim() }
                    .filter { it.isNotBlank() }
                    .distinct()
            }.collect { categories ->
                _uiState.update { it.copy(categories = categories, isLoading = false) }
            }
        }
    }

    fun onNewCategoryNameChange(name: String) {
        _uiState.update { it.copy(newCategoryName = name) }
    }

    fun onEditNameChange(name: String) {
        _uiState.update { it.copy(editName = name) }
    }

    fun startAdd() {
        _uiState.update { it.copy(isAdding = true, newCategoryName = "") }
    }

    fun cancelAdd() {
        _uiState.update { it.copy(isAdding = false, newCategoryName = "") }
    }

    fun confirmAdd() {
        val name = _uiState.value.newCategoryName.trim()
        if (name.isBlank()) return

        viewModelScope.launch {
            categoryRepository.addCategory(name)
            _uiState.update { it.copy(isAdding = false, newCategoryName = "") }
        }
    }

    fun startEdit(category: String) {
        _uiState.update { it.copy(editingCategory = category, editName = category) }
    }

    fun cancelEdit() {
        _uiState.update { it.copy(editingCategory = null, editName = "") }
    }

    fun confirmEdit() {
        val oldName = _uiState.value.editingCategory ?: return
        val newName = _uiState.value.editName.trim()
        if (newName.isBlank() || newName == oldName) {
            _uiState.update { it.copy(editingCategory = null, editName = "") }
            return
        }

        viewModelScope.launch {
            categoryRepository.renameCategory(oldName, newName)
            goodsRepository.renameCategory(oldName, newName)
            _uiState.update { it.copy(editingCategory = null, editName = "") }
        }
    }

    fun deleteCategory(category: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            categoryRepository.deleteCategory(category)
            goodsRepository.moveCategory(category)
            _uiState.update { it.copy(isLoading = false) }
        }
    }
}
