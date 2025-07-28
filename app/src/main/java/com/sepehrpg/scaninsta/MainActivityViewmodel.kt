package com.sepehrpg.scaninsta

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.zip.ZipInputStream
import androidx.lifecycle.ViewModel
import com.example.database.model.UserEntity
import com.sepehrpg.scaninsta.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject


sealed class MainActivtyUiState {
    object Idle : MainActivtyUiState()
    object Loading : MainActivtyUiState()
    object Success : MainActivtyUiState()
    data class Error(val message: String) : MainActivtyUiState()
}

@HiltViewModel
class MainActivityViewModel @Inject constructor(
   private val repository: UserRepository
) : ViewModel() {

    val unfollowers: StateFlow<List<UserEntity>>
    private val _uiState = MutableStateFlow<MainActivtyUiState>(MainActivtyUiState.Idle)
    val uiState: StateFlow<MainActivtyUiState> = _uiState

    init {
        unfollowers = repository.unfollowers.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        viewModelScope.launch {
            repository.unfollowers.collect { users ->
                if (users.isNotEmpty()) {
                    _uiState.value = MainActivtyUiState.Success
                }
            }
        }
    }


    fun processZipFile(context: Context, uri: Uri) {
        _uiState.value = MainActivtyUiState.Loading

        viewModelScope.launch(Dispatchers.IO) {
            try {
                var followersJsonString: String? = null
                var followingJsonString: String? = null

                context.contentResolver.openInputStream(uri)?.use { inputStream ->
                    ZipInputStream(inputStream).use { zipInputStream ->
                        var entry = zipInputStream.nextEntry
                        while (entry != null) {
                            Log.i("MainActivityViewModel", entry.toString())
                            when {
                                entry.name.endsWith("followers_1.json") -> followersJsonString = readZipEntry(zipInputStream)
                                entry.name.endsWith("following.json") -> followingJsonString = readZipEntry(zipInputStream)
                            }
                            entry = zipInputStream.nextEntry
                        }
                    }
                }

                if (followersJsonString == null || followingJsonString == null) {
                    _uiState.value = MainActivtyUiState.Error("Could not find the file following.json or followers_1.json")
                    return@launch
                }

                repository.analyzeAndStoreUserData(followersJsonString!!, followingJsonString!!)

                _uiState.value = MainActivtyUiState.Success

            } catch (e: Exception) {
                _uiState.value = MainActivtyUiState.Error("An error occurred while processing the file: ${e.message}")
            }
        }
    }

    private fun readZipEntry(zipInputStream: ZipInputStream): String {
        val reader = BufferedReader(InputStreamReader(zipInputStream))
        return reader.readText()
    }

    fun resetState() {
        viewModelScope.launch {
            repository.clearAllData()
            _uiState.value = MainActivtyUiState.Idle
        }
    }
}