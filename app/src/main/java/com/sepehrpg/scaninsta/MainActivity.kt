package com.sepehrpg.scaninsta

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.*
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.database.model.UserEntity
import com.sepehrpg.scaninsta.designsystem.component.AppCustomSearchBarBasicTextField
import com.sepehrpg.scaninsta.designsystem.component.AppExtendedFloatingActionButton
import com.sepehrpg.scaninsta.designsystem.theme.AppTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val viewModel: MainActivityViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AppTheme(){
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppMainScreen(viewModel = viewModel)
                }
            }
        }
    }
}

@Composable
fun AppMainScreen(viewModel: MainActivityViewModel) {

    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val unfollowers by viewModel.unfollowers.collectAsState()

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            viewModel.processZipFile(context, it)
        }
    }
    var text by remember { mutableStateOf("") }
    Scaffold(
        containerColor = Color.White,
        floatingActionButton = {
            if (uiState is MainActivityUiState.Success){
                AppExtendedFloatingActionButton(
                    onClick = { viewModel.resetState() },
                    icon = { Icon(Icons.Default.Add, contentDescription = null) },
                    text = { Text("Upload New File") }
                )
            }
        },
        topBar = {
            if (uiState is MainActivityUiState.Success){
                Box(
                    Modifier
                        .fillMaxWidth()
                        .padding(start = 15.dp, end = 15.dp, top = 25.dp, bottom = 5.dp)){
                    AppCustomSearchBarBasicTextField(
                        value = text,
                        onValueChange = { text = it },
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
                    is MainActivityUiState.Idle -> IdleScreen { filePickerLauncher.launch("application/zip") }
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
}

@Composable
fun IdleScreen(onSelectFile: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top,
        modifier = Modifier.fillMaxSize().padding(top = 60.dp)
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
    }
}

@Composable
fun LoadingScreen() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CircularProgressIndicator(modifier = Modifier.size(64.dp))
        Spacer(modifier = Modifier.height(24.dp))
        Text("Processing...", style = MaterialTheme.typography.titleLarge)
        Text("Please wait a moment...", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
    }
}

@Composable
fun ResultScreen(unfollowers: List<UserEntity>) {
    val context = LocalContext.current

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 5.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${unfollowers.size} users do not follow you back.",
                style = MaterialTheme.typography.labelMedium.copy(Color.Gray),
            )

        }
        Spacer(modifier = Modifier.height(4.dp))

        if (unfollowers.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("🎉 Great! No one has unfollowed you.", style = MaterialTheme.typography.bodyLarge)
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(unfollowers, key = { it.id }) { user ->
                    UserItem(user = user) {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(user.href))
                        context.startActivity(intent)
                    }
                }
            }
        }
    }
}

@Composable
fun UserItem(user: UserEntity, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.3f)),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(45.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFEEEEEE)),
                contentAlignment = Alignment.Center
            ) {

                Image(
                    painter = painterResource(R.drawable.instagram),
                    contentDescription = "instagram",
                    modifier = Modifier.size(24.dp),
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Text(
                text = user.username,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f)
            )

            Icon(
                imageVector = Icons.Outlined.Info,
                contentDescription = "Open Profile",
                tint = Color.Gray,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
fun ErrorScreen(message: String, onRetry: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.padding(16.dp)
    ) {
        Text("⚠️", fontSize = 48.sp)
        Spacer(modifier = Modifier.height(16.dp))
        Text("Error!", style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.error)
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(onClick = onRetry) {
            Text("Try Again")
        }
    }
}



@Composable
fun FileUploadButton(
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .background(Color(0xFFF8F8F8))
            .fillMaxWidth()
            .height(200.dp)
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .dashedBorder(
                color = Color.LightGray,
                strokeWidth = 2.dp,
                cornerRadius = 16.dp,
                dashOn = 10.dp,
                dashOff = 5.dp
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                painter = painterResource(R.drawable.upload),
                contentDescription = "Upload File",
                modifier = Modifier.size(60.dp),
                tint = Color.Gray
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Tap to upload Instagram data .zip",
                color = Color.Gray,
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}

fun Modifier.dashedBorder(
    color: Color,
    strokeWidth: Dp = 1.dp,
    cornerRadius: Dp = 0.dp,
    dashOn: Dp = 10.dp,
    dashOff: Dp = 5.dp
): Modifier = composed {
    // Convert Dp values to Px for drawing
    val density = LocalDensity.current
    val strokeWidthPx = with(density) { strokeWidth.toPx() }
    val cornerRadiusPx = with(density) { cornerRadius.toPx() }
    val dashOnPx = with(density) { dashOn.toPx() }
    val dashOffPx = with(density) { dashOff.toPx() }

    // Create the Stroke object with the dash effect
    val stroke = Stroke(
        width = strokeWidthPx,
        pathEffect = PathEffect.dashPathEffect(
            intervals = floatArrayOf(dashOnPx, dashOffPx),
            phase = 0f
        )
    )

    this.drawBehind {
        drawRoundRect(
            color = color,
            style = stroke,
            cornerRadius = CornerRadius(cornerRadiusPx)
        )
    }
}