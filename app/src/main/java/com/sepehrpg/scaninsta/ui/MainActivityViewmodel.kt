package com.sepehrpg.scaninsta.ui

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.database.model.PageEntity
import com.example.database.model.UserEntity
import com.sepehrpg.scaninsta.data.importer.ExportFileSettings
import com.sepehrpg.scaninsta.data.importer.ExportFileSettingsStore
import com.sepehrpg.scaninsta.data.importer.InstagramExportImporter
import com.sepehrpg.scaninsta.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class SortOrder {
    ASC,
    DESC,
}

sealed class MainActivityUiState {
    data object Idle : MainActivityUiState()
    data object Loading : MainActivityUiState()
    data object Success : MainActivityUiState()
    data class Error(val message: String) : MainActivityUiState()
}

@HiltViewModel
class MainActivityViewModel @Inject constructor(
    private val repository: UserRepository,
    private val exportImporter: InstagramExportImporter,
    private val exportFileSettingsStore: ExportFileSettingsStore,
) : ViewModel() {

    private val _selectedPageId = MutableStateFlow<Int?>(null)
    private val _sortOrder = MutableStateFlow(SortOrder.ASC)

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    val sortOrder: StateFlow<SortOrder> = _sortOrder
    val exportFileSettings: StateFlow<ExportFileSettings> = exportFileSettingsStore.settings

    @OptIn(ExperimentalCoroutinesApi::class)
    val unfollowers: StateFlow<List<UserEntity>> = _selectedPageId.flatMapLatest { pageId ->
        if (pageId != null) {
            repository.getUnfollowersForPage(pageId)
        } else {
            flowOf(emptyList())
        }
    }
        .combine(_sortOrder) { list, order ->
            when (order) {
                SortOrder.ASC -> list.sortedBy { it.username.lowercase() }
                SortOrder.DESC -> list.sortedByDescending { it.username.lowercase() }
            }
        }
        .combine(_searchQuery) { sortedList, query ->
            if (query.isBlank()) {
                sortedList
            } else {
                sortedList.filter { user ->
                    user.username.contains(query, ignoreCase = true)
                }
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList(),
        )

    val allPages: StateFlow<List<PageEntity>>
    private val _uiState = MutableStateFlow<MainActivityUiState>(MainActivityUiState.Idle)
    val uiState: StateFlow<MainActivityUiState> = _uiState

    init {
        allPages = repository.allPages.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList(),
        )

        viewModelScope.launch {
            repository.latestPageId.collect { latestId ->
                if (_selectedPageId.value == null) {
                    _selectedPageId.value = latestId
                }
                if (latestId != null) {
                    _uiState.value = MainActivityUiState.Success
                } else {
                    _uiState.value = MainActivityUiState.Idle
                }
            }
        }
    }

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    fun setSortOrder(order: SortOrder) {
        _sortOrder.value = order
    }

    fun processZipFile(context: Context, uri: Uri, pageName: String) {
        _uiState.value = MainActivityUiState.Loading
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val export = context.contentResolver.openInputStream(uri)?.use { input ->
                    exportImporter.importZip(input, exportFileSettings.value)
                } ?: error("The selected file could not be opened.")
                val analysisName = pageName.trim().ifBlank { "Instagram export" }
                val newPageId = repository.analyzeAndStoreUserData(export, analysisName)
                selectPage(newPageId)
                _uiState.value = MainActivityUiState.Success
            } catch (exception: CancellationException) {
                throw exception
            } catch (exception: Exception) {
                _uiState.value =
                    MainActivityUiState.Error(exception.message ?: "The export could not be processed.")
            }
        }
    }

    fun selectPage(pageId: Int) {
        _selectedPageId.value = pageId
    }

    fun deletePage(pageId: Int) {
        viewModelScope.launch {
            if (_selectedPageId.value == pageId) {
                _selectedPageId.value = null
            }
            repository.deletePage(pageId)
        }
    }

    fun updateExportFileSettings(followersFileName: String, followingFileName: String) {
        exportFileSettingsStore.update(followersFileName, followingFileName)
    }

    fun resetExportFileSettings() {
        exportFileSettingsStore.reset()
    }

    fun resetState() {
        _uiState.value = MainActivityUiState.Idle
    }

    fun returnToSuccessState() {
        _uiState.value = MainActivityUiState.Success
    }
}
