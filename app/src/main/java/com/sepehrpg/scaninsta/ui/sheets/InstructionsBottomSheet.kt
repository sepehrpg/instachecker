package com.sepehrpg.scaninsta.ui.sheets

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.sepehrpg.scaninsta.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InstructionsBottomSheet(
    sheetState: SheetState,
    onDismiss: () -> Unit
) {
    val uriHandler = LocalUriHandler.current
    val videoTutorialUrl = "https://github.com/sepehrpg/instachecker" // <--  TODO: Change this to your actual video URL

    ModalBottomSheet(
        containerColor = Color.White,
        modifier = Modifier.navigationBarsPadding(),
        sheetState = sheetState,
        onDismissRequest = onDismiss,
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                item {
                    // Header
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
                    ) {
                        Icon(painterResource(R.drawable.baseline_help_outline_24), contentDescription = "Instructions", tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("How to Download Your Data", style = MaterialTheme.typography.headlineSmall)
                    }
                }

                // NEW: Video Tutorial Link Button
                item {
                    OutlinedButton(
                        onClick = { uriHandler.openUri(videoTutorialUrl) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(painterResource(R.drawable.baseline_play_circle_outline_24), contentDescription = "Video Tutorial")
                        Spacer(Modifier.width(8.dp))
                        Text("Watch Video Tutorial")
                    }
                    Spacer(Modifier.height(24.dp))
                }

                // Step-by-step guide
                item { InstructionStep("1", "Open the Instagram app and go to your **Profile**.") }
                item { InstructionStep("2", "Tap the **☰ Menu** icon, then go to **Settings and Privacy** → **Accounts Centre**.") }
                item { InstructionStep("3", "Select **Your information and permissions**, then **Download your information**.") }
                item { InstructionStep("4", "Choose **Request a download**, select your profile, and tap **Next**.") }
                item { InstructionStep("5", "Select the **\"Some of your information\"** option.") }
                item { InstructionStep("6", "Scroll down and select ONLY **\"Followers and following\"**. -> Next -> Download to Device") }
                item {
                    InstructionStep(
                        "7",
                        "⚠️ **IMPORTANT**: Scroll to the bottom. In the format options, tap **HTML** and change the format to **JSON**.",
                        isImportant = true
                    )
                }
                item { InstructionStep("8", "Tap **Submit request**. It may take a few minutes for Instagram to prepare your file.") }
                item { InstructionStep("9", "Once the `.zip` file is downloaded, return to this app and upload it.") }
            }
        }
    }
}

@Composable
private fun InstructionStep(number: String, text: String, isImportant: Boolean = false) {
    Row(
        modifier = Modifier.padding(bottom = 16.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(CircleShape)
                .background(if (isImportant) MaterialTheme.colorScheme.primaryContainer else Color.LightGray.copy(alpha = 0.3f)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = number,
                fontWeight = FontWeight.Bold,
                color = if (isImportant) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
        }
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = buildAnnotatedString {
                val parts = text.split("**")
                parts.forEachIndexed { index, part ->
                    if (index % 2 == 1) { // Odd parts are bold
                        withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                            append(part)
                        }
                    } else {
                        append(part)
                    }
                }
            },
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(top = 2.dp)
        )
    }
}