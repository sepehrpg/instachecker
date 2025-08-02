package com.sepehrpg.scaninsta.ui
import android.content.Context
import android.net.Uri
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.zip.ZipInputStream
import androidx.lifecycle.ViewModel
import com.example.database.model.PageEntity
import com.example.database.model.UserEntity
import com.sepehrpg.scaninsta.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

enum class SortOrder {
    ASC, // Ascending A-Z
    DESC // Descending Z-A
}

sealed class MainActivityUiState {
    data object Idle : MainActivityUiState()
    data object Loading : MainActivityUiState()
    data object Success : MainActivityUiState()
    data class Error(val message: String) : MainActivityUiState()
}

@HiltViewModel
class MainActivityViewModel @Inject constructor(
    private val repository: UserRepository
) : ViewModel() {

    private val _selectedPageId = MutableStateFlow<Int?>(null)
    private val _sortOrder = MutableStateFlow(SortOrder.ASC)

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    val sortOrder: StateFlow<SortOrder> = _sortOrder

    @OptIn(ExperimentalCoroutinesApi::class)
    val unfollowers: StateFlow<List<UserEntity>> = _selectedPageId.flatMapLatest { pageId ->
        if (pageId != null) {
            repository.getUnfollowersForPage(pageId)
        } else {
            flowOf(emptyList())
        }
    }
        // Combine with sort order first
        .combine(_sortOrder) { list, order ->
            when (order) {
                SortOrder.ASC -> list.sortedBy { it.username.lowercase() }
                SortOrder.DESC -> list.sortedByDescending { it.username.lowercase() }
            }
        }
        // THEN, combine the sorted list with the search query to filter it
        .combine(_searchQuery) { sortedList, query ->
            if (query.isBlank()) {
                sortedList // If query is blank, return the full sorted list
            } else {
                // Otherwise, filter the list based on the query
                sortedList.filter { user ->
                    user.username.contains(query, ignoreCase = true)
                }
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val allPages: StateFlow<List<PageEntity>>
    private val _uiState = MutableStateFlow<MainActivityUiState>(MainActivityUiState.Idle)
    val uiState: StateFlow<MainActivityUiState> = _uiState

    init {
        allPages = repository.allPages.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
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

    // NEW: Function to be called from the UI when the search text changes
    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    fun setSortOrder(order: SortOrder) {
        _sortOrder.value = order
    }

    // ... (The rest of your ViewModel functions remain unchanged)
    fun processZipFile(context: Context, uri: Uri, pageName: String) {
        _uiState.value = MainActivityUiState.Loading
        viewModelScope.launch(Dispatchers.IO) {
            try {
                var followersJsonString: String? = null
                var followingJsonString: String? = null

                context.contentResolver.openInputStream(uri)?.use { inputStream ->
                    ZipInputStream(inputStream).use { zipInputStream ->
                        var entry = zipInputStream.nextEntry
                        while (entry != null) {
                            when {
                                entry.name.endsWith("followers_1.json") -> followersJsonString = readZipEntry(zipInputStream)
                                entry.name.endsWith("following.json") -> followingJsonString = readZipEntry(zipInputStream)
                            }
                            entry = zipInputStream.nextEntry
                        }
                    }
                }

                if (followersJsonString == null || followingJsonString == null) {
                    _uiState.value =
                        MainActivityUiState.Error("Could not find the file following.json or followers_1.json")
                    return@launch
                }

                val newPageId = repository.analyzeAndStoreUserData(followersJsonString!!, followingJsonString!!, pageName)
                selectPage(newPageId)
                _uiState.value = MainActivityUiState.Success

            } catch (e: Exception) {
                _uiState.value =
                    MainActivityUiState.Error("An error occurred while processing the file: ${e.message}")
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

    private fun readZipEntry(zipInputStream: ZipInputStream): String {
        val reader = BufferedReader(InputStreamReader(zipInputStream))
        return reader.readText()
    }

    fun resetState() {
        _uiState.value = MainActivityUiState.Idle
    }

    fun returnToSuccessState() {
        _uiState.value = MainActivityUiState.Success
    }
}