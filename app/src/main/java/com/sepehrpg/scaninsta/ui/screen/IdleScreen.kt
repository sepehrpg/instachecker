package com.sepehrpg.scaninsta.ui.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.sepehrpg.scaninsta.R
import com.sepehrpg.scaninsta.ui.common.FileUploadButton


@Composable
fun IdleScreen(onSelectFile: () -> Unit, onHelpClick: () -> Unit) {
    val supportUrl = "http://sepehrpg.ir"
    val uriHandler = LocalUriHandler.current

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top,
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 60.dp)
    ) {

        Image(
            painter = painterResource(R.drawable.logo_instagram),
            contentDescription = "Upload File",
            modifier = Modifier.size(90.dp),
        )
        Spacer(modifier = Modifier.height(20.dp))
        Text(
            text = "Instagram Unfollowers Checker",
            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = "Upload your Instagram data to see who doesn't follow you back",
            style = MaterialTheme.typography.bodySmall,
            textAlign = TextAlign.Center,
            color = Color.Gray
        )
        Spacer(modifier = Modifier.height(15.dp))

        FileUploadButton(onClick = onSelectFile)

        Spacer(modifier = Modifier.height(24.dp))
        TextButton(onClick = onHelpClick) {
            Icon(
                painterResource(R.drawable.baseline_help_24),
                contentDescription = "Help",
                tint = MaterialTheme.colorScheme.primary,
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = "How to get the data file?",
                color = Color.Gray,
                style = MaterialTheme.typography.bodyMedium
            )

        }

        Box(
            modifier = Modifier
                .fillMaxWidth().fillMaxHeight()
                .padding(vertical = 16.dp),
            contentAlignment = Alignment.BottomCenter
        ) {
            Text(
                text = buildAnnotatedString {
                    append("Support: ")
                    withStyle(
                        style = SpanStyle(
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold,
                            textDecoration = TextDecoration.Underline
                        )
                    ) {
                        append("sepehrpg.ir")
                    }
                },
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.clickable { uriHandler.openUri(supportUrl) }
            )
        }
    }
}