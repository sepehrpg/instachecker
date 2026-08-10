package com.sepehrpg.scaninsta.ui.sheets

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.sepehrpg.scaninsta.data.importer.ExportFileSettings
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImportSettingsBottomSheet(
    sheetState: SheetState,
    settings: ExportFileSettings,
    onDismiss: () -> Unit,
    onSave: (followersFileName: String, followingFileName: String) -> Unit,
    onReset: () -> Unit,
) {
    var followersFileName by remember(settings) { mutableStateOf(settings.followersFileName) }
    var followingFileName by remember(settings) { mutableStateOf(settings.followingFileName) }
    val normalizedFollowersName = followersFileName.normalizedFileName()
    val normalizedFollowingName = followingFileName.normalizedFileName()
    val validationMessage = when {
        normalizedFollowersName.isBlank() || normalizedFollowingName.isBlank() ->
            "Both file names are required."
        normalizedFollowersName == normalizedFollowingName ->
            "The two file names must be different."
        else -> null
    }

    ModalBottomSheet(
        modifier = Modifier.navigationBarsPadding(),
        containerColor = Color.White,
        sheetState = sheetState,
        onDismissRequest = onDismiss,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Icon(Icons.Default.Settings, contentDescription = null)
                Text("Import settings", style = MaterialTheme.typography.titleLarge)
            }
            Spacer(Modifier.height(12.dp))
            Text(
                "The app searches every folder in the ZIP automatically. Change these names only if Instagram renames the relationship files. You may include or omit the extension.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(20.dp))
            OutlinedTextField(
                value = followersFileName,
                onValueChange = { followersFileName = it },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                label = { Text("Followers file") },
                supportingText = { Text("Default: followers_1") },
                isError = validationMessage != null,
            )
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = followingFileName,
                onValueChange = { followingFileName = it },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                label = { Text("Following file") },
                supportingText = { Text("Default: following") },
                isError = validationMessage != null,
            )
            if (validationMessage != null) {
                Text(
                    text = validationMessage,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                )
            }
            Spacer(Modifier.height(20.dp))
            Button(
                onClick = {
                    onSave(followersFileName.trim(), followingFileName.trim())
                    onDismiss()
                },
                enabled = validationMessage == null,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Icon(Icons.Default.Done, contentDescription = null)
                Text(" Save settings")
            }
            OutlinedButton(
                onClick = {
                    onReset()
                    onDismiss()
                },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Icon(Icons.Default.Refresh, contentDescription = null)
                Text(" Restore defaults")
            }
            Spacer(Modifier.height(40.dp))
        }
    }
}

private fun String.normalizedFileName(): String {
    val fileName = trim().replace('\\', '/').substringAfterLast('/')
    return fileName.substringBeforeLast('.', missingDelimiterValue = fileName).lowercase(Locale.ROOT)
}
