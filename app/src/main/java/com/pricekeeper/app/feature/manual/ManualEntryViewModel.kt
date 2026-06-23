package com.pricekeeper.app.feature.manual

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pricekeeper.app.domain.repository.CategoryRepository
import com.pricekeeper.app.domain.repository.GoodsRepository
import com.pricekeeper.app.domain.repository.StoreRepository
import com.pricekeeper.app.domain.usecase.AddManualPriceRecordUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ManualEntryViewModel @Inject constructor(
    private val addManualPriceRecordUseCase: AddManualPriceRecordUseCase,
    private val categoryRepository: CategoryRepository,
    goodsRepository: GoodsRepository,
    storeRepository: StoreRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ManualEntryUiState())
    val uiState: StateFlow<ManualEntryUiState> = _uiState.asStateFlow()

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
            }.collect { merged ->
                _uiState.update { it.copy(categories = merged) }
            }
        }
        viewModelScope.launch {
            storeRepository.observeStores().collect { stores ->
                _uiState.update { it.copy(existingStores = stores) }
            }
        }
    }

    fun onGoodsNameChange(name: String) {
        _uiState.update { it.copy(goodsName = name, validationErrors = it.validationErrors - "goodsName") }
    }

    fun onPriceChange(price: String) {
        if (price.isEmpty() || price.matches(Regex("^\\d*\\.?\\d{0,2}$"))) {
            _uiState.update { it.copy(price = price, validationErrors = it.validationErrors - "price") }
        }
    }

    fun onCategoryChange(category: String) {
        _uiState.update { it.copy(category = category, showCategorySheet = false, categorySearchQuery = "") }
    }

    fun onCategorySheetVisibleChange(visible: Boolean) {
        _uiState.update {
            it.copy(
                showCategorySheet = visible,
                categorySearchQuery = if (visible) it.categorySearchQuery else ""
            )
        }
    }

    fun onCategorySearchQueryChange(query: String) {
        _uiState.update { it.copy(categorySearchQuery = query) }
    }

    fun onStoreNameChange(store: String) {
        _uiState.update { it.copy(storeName = store, validationErrors = it.validationErrors - "storeName") }
    }

    fun onStoreSelected(store: com.pricekeeper.app.domain.model.Store) {
        _uiState.update {
            it.copy(
                storeName = store.name,
                storeRegion = store.region,
                storeAddress = store.address ?: "",
                storeLatitude = store.latitude,
                storeLongitude = store.longitude,
                storeMapUrl = store.mapUrl,
                storeLocationInput = "",
                showStoreSheet = false,
                storeSearchQuery = "",
                showNewStoreFields = false
            )
        }
    }

    fun onStoreSheetVisibleChange(visible: Boolean) {
        _uiState.update {
            it.copy(
                showStoreSheet = visible,
                storeSearchQuery = if (visible) it.storeSearchQuery else ""
            )
        }
    }

    fun onStoreSearchQueryChange(query: String) {
        _uiState.update { it.copy(storeSearchQuery = query) }
    }

    fun onNewStoreToggle() {
        _uiState.update {
            it.copy(
                showNewStoreFields = true,
                showStoreSheet = false,
                storeSearchQuery = "",
                storeName = "",
                storeRegion = "",
                storeAddress = "",
                storeLatitude = null,
                storeLongitude = null,
                storeMapUrl = null,
                storeLocationInput = ""
            )
        }
    }

    fun onStoreLocationSelected(latitude: Double, longitude: Double, region: String, address: String, mapUrl: String?) {
        _uiState.update {
            it.copy(
                storeLatitude = latitude,
                storeLongitude = longitude,
                storeRegion = region,
                storeAddress = address,
                storeMapUrl = mapUrl,
                storeLocationInput = "",
                validationErrors = it.validationErrors - "storeLocation"
            )
        }
    }

    fun onStoreLocationInputChange(input: String) {
        _uiState.update {
            it.copy(storeLocationInput = input, validationErrors = it.validationErrors - "storeLocation")
        }
    }

    fun onStoreLocationLinkSaved(mapUrl: String) {
        _uiState.update {
            it.copy(
                storeLatitude = null,
                storeLongitude = null,
                storeRegion = "",
                storeAddress = mapUrl,
                storeMapUrl = mapUrl,
                validationErrors = it.validationErrors - "storeLocation"
            )
        }
    }

    fun onStoreLocationParseFailed(message: String = "未能解析位置，请粘贴地图分享链接") {
        _uiState.update {
            it.copy(
                validationErrors = it.validationErrors + ("storeLocation" to message)
            )
        }
    }

    fun saveAndContinue() {
        performSave(resetForm = true)
    }

    fun saveOnly() {
        performSave(resetForm = false, backAfterSave = true)
    }

    fun onSaveNavigationConsumed() {
        _uiState.update { it.copy(saveSuccess = false, pendingBackAfterSave = false) }
    }

    private fun performSave(resetForm: Boolean, backAfterSave: Boolean = false) {
        val errors = validate()
        if (errors.isNotEmpty()) {
            _uiState.update { it.copy(validationErrors = errors) }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, validationErrors = emptyMap()) }
            val state = _uiState.value
            categoryRepository.addCategory(state.category.ifBlank { "未分类" })
            val result = addManualPriceRecordUseCase(
                goodsName = state.goodsName.trim(),
                storeName = state.storeName.trim(),
                price = state.price.toDouble(),
                goodsCategory = state.category.ifBlank { "未分类" },
                storeRegion = state.storeRegion,
                storeAddress = state.storeAddress.ifBlank { null },
                storeLatitude = state.storeLatitude,
                storeLongitude = state.storeLongitude,
                storeMapUrl = state.storeMapUrl
            )
            if (result.isSuccess) {
                if (resetForm) {
                    _uiState.update {
                        it.copy(
                            isSaving = false,
                            saveSuccess = true,
                            pendingBackAfterSave = backAfterSave,
                            goodsName = "",
                            price = ""
                        )
                    }
                } else {
                    _uiState.update {
                        it.copy(isSaving = false, saveSuccess = true, pendingBackAfterSave = backAfterSave)
                    }
                }
            } else {
                _uiState.update {
                    it.copy(isSaving = false,
                        validationErrors = mapOf("general" to (result.exceptionOrNull()?.message ?: "保存失败")))
                }
            }
        }
    }

    private fun validate(): Map<String, String> {
        val errors = mutableMapOf<String, String>()
        val state = _uiState.value
        if (state.goodsName.isBlank()) errors["goodsName"] = "请输入商品名称"
        val price = state.price.toDoubleOrNull()
        if (state.price.isBlank() || price == null || price <= 0) errors["price"] = "请输入有效价格"
        if (state.storeName.isBlank()) errors["storeName"] = "请选择或输入商店名称"
        if (state.showNewStoreFields &&
            (state.storeLatitude == null || state.storeLongitude == null) &&
            state.storeMapUrl.isNullOrBlank()
        ) {
            errors["storeLocation"] = "请粘贴地图分享链接并点击解析保存"
        }
        return errors
    }
}
