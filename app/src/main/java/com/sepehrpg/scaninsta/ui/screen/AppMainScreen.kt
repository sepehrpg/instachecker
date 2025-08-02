package com.sepehrpg.scaninsta.ui.screen

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.sepehrpg.scaninsta.designsystem.component.AppCustomSearchBarBasicTextField
import com.sepehrpg.scaninsta.designsystem.component.AppExtendedFloatingActionButton
import com.sepehrpg.scaninsta.ui.MainActivityUiState
import com.sepehrpg.scaninsta.ui.MainActivityViewModel
import com.sepehrpg.scaninsta.ui.sheets.FilterBottomSheet
import com.sepehrpg.scaninsta.ui.sheets.HistoryBottomSheet
import com.sepehrpg.scaninsta.ui.sheets.InstructionsBottomSheet
import com.sepehrpg.scaninsta.ui.sheets.NewAnalysisBottomSheet
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppMainScreen(viewModel: MainActivityViewModel) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val unfollowers by viewModel.unfollowers.collectAsState()
    val allPages by viewModel.allPages.collectAsState()
    val sortOrder by viewModel.sortOrder.collectAsState()

    val searchQuery by viewModel.searchQuery.collectAsState()

    val newAnalysisSheetState = rememberModalBottomSheetState()
    var isNewAnalysisSheetOpen by remember { mutableStateOf(false) }
    var pageName by remember { mutableStateOf("") }

    val historySheetState = rememberModalBottomSheetState()
    var isHistorySheetOpen by remember { mutableStateOf(false) }

    val filterSheetState = rememberModalBottomSheetState()
    var isFilterSheetOpen by remember { mutableStateOf(false) }

    val instructionsSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var isInstructionsSheetOpen by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            viewModel.processZipFile(context, it, pageName)
            pageName =  ""
        }
    }

    val canGoBack = uiState is MainActivityUiState.Idle && unfollowers.isNotEmpty()
    BackHandler(enabled = canGoBack) {
        viewModel.returnToSuccessState()
    }

    Scaffold(
        containerColor = Color.White,
        floatingActionButton = {
            if (uiState is MainActivityUiState.Success) {
                AppExtendedFloatingActionButton(
                    onClick = { viewModel.resetState() },
                    icon = { Icon(Icons.Default.Add, contentDescription = null) },
                    text = { Text("Upload New File") }
                )
            }
        },
        topBar = {
            if (uiState is MainActivityUiState.Success) {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .padding(start = 15.dp, end = 15.dp, top = 25.dp, bottom = 5.dp)
                ) {
                    AppCustomSearchBarBasicTextField(
                        value = searchQuery,
                        onValueChange = { viewModel.onSearchQueryChanged(it) },
                        onMenuClick = { isHistorySheetOpen = true },
                        onFilterClick = { isFilterSheetOpen = true }
                    )
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            AnimatedContent(targetState = uiState, label = "State Animation") { state ->
                when (state) {
                    is MainActivityUiState.Idle -> IdleScreen(
                        onSelectFile = { isNewAnalysisSheetOpen = true },
                        onHelpClick = { isInstructionsSheetOpen = true }
                    )
                    is MainActivityUiState.Loading -> LoadingScreen()
                    is MainActivityUiState.Success -> ResultScreen(unfollowers = unfollowers)
                    is MainActivityUiState.Error -> ErrorScreen(
                        message = state.message,
                        onRetry = { viewModel.resetState() }
                    )
                }
            }
        }
    }


    if (isNewAnalysisSheetOpen) {
        var pageName by remember { mutableStateOf("") }
        NewAnalysisBottomSheet(
            sheetState = newAnalysisSheetState,
            pageName = pageName,
            onPageNameChange = { pageName = it },
            onDismiss = { isNewAnalysisSheetOpen = false },
            onConfirm = {
                scope.launch { newAnalysisSheetState.hide() }.invokeOnCompletion {
                    if (!newAnalysisSheetState.isVisible) {
                        isNewAnalysisSheetOpen = false
                        filePickerLauncher.launch("application/zip")
                    }
                }
            }
        )
    }

    // Bottom Sheet for showing analysis history
    if (isHistorySheetOpen) {
        HistoryBottomSheet(
            sheetState = historySheetState,
            pages = allPages,
            onDismiss = { isHistorySheetOpen = false },
            onDeletePage = { pageId ->
                viewModel.deletePage(pageId)
            },
            // NEW: Pass the select page function
            onSelectPage = { pageId ->
                viewModel.selectPage(pageId)
                // Close the sheet after selection
                scope.launch { historySheetState.hide() }.invokeOnCompletion {
                    isHistorySheetOpen = false
                }
            }
        )
    }

    // NEW: Bottom Sheet for filtering options
    if (isFilterSheetOpen) {
        FilterBottomSheet(
            sheetState = filterSheetState,
            currentSortOrder = sortOrder,
            onDismiss = { isFilterSheetOpen = false },
            onSortOrderSelected = { newOrder ->
                viewModel.setSortOrder(newOrder)
                scope.launch { filterSheetState.hide() }.invokeOnCompletion {
                    isFilterSheetOpen = false
                }
            }
        )
    }

    if (isInstructionsSheetOpen) {
        InstructionsBottomSheet(
            sheetState = instructionsSheetState,
            onDismiss = { isInstructionsSheetOpen = false }
        )
    }
}