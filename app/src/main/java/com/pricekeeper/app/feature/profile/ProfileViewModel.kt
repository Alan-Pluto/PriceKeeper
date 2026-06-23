package com.pricekeeper.app.feature.profile

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pricekeeper.app.core.ui.theme.ThemePreferences
import com.pricekeeper.app.data.export.ExportRepository
import com.pricekeeper.app.data.export.ImportConflictStrategy
import com.pricekeeper.app.data.export.ImportRepository
import com.pricekeeper.app.domain.usecase.GetDashboardStatsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    getDashboardStatsUseCase: GetDashboardStatsUseCase,
    private val exportRepository: ExportRepository,
    private val importRepository: ImportRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            ThemePreferences.observeDarkMode(context).collect { isDark ->
                _uiState.update { it.copy(isDarkMode = isDark) }
            }
        }
        viewModelScope.launch {
            getDashboardStatsUseCase().collect { stats ->
                _uiState.update { it.copy(dashboardStats = stats) }
            }
        }
    }

    fun exportData(uri: Uri) {
        viewModelScope.launch {
            _uiState.update { it.copy(isExporting = true) }
            try {
                val outputStream = context.contentResolver.openOutputStream(uri)
                if (outputStream != null) {
                    val stats = exportRepository.exportAllData(outputStream)
                    _uiState.update { it.copy(isExporting = false, exportResult = stats) }
                } else {
                    _uiState.update { it.copy(isExporting = false) }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isExporting = false) }
            }
        }
    }

    fun importData(uri: Uri, strategy: ImportConflictStrategy) {
        viewModelScope.launch {
            _uiState.update { it.copy(isImporting = true) }
            try {
                val inputStream = context.contentResolver.openInputStream(uri)
                if (inputStream != null) {
                    val result = importRepository.importData(inputStream, strategy)
                    _uiState.update { it.copy(isImporting = false, importResult = result) }
                } else {
                    _uiState.update { it.copy(isImporting = false) }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isImporting = false) }
            }
        }
    }

    fun toggleDarkMode() {
        val newValue = !_uiState.value.isDarkMode
        _uiState.update { it.copy(isDarkMode = newValue) }
        viewModelScope.launch {
            ThemePreferences.setDarkMode(context, newValue)
        }
    }

    fun clearExportResult() {
        _uiState.update { it.copy(exportResult = null) }
    }

    fun clearImportResult() {
        _uiState.update { it.copy(importResult = null) }
    }
}
