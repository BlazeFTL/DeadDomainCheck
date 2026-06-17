package com.example

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                Scaffold(
                    modifier = Modifier.fillMaxSize()
                ) { innerPadding ->
                    MainScreen(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MainScreen(
    modifier: Modifier = Modifier,
    viewModel: MainViewModel = viewModel()
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    val inputText by viewModel.inputText.collectAsState()
    val inputLabel by viewModel.inputLabel.collectAsState()
    val analysisState by viewModel.analysisState.collectAsState()
    val progress by viewModel.progress.collectAsState()
    val processedCount by viewModel.processedCount.collectAsState()
    val totalCount by viewModel.totalCount.collectAsState()
    val logs by viewModel.logs.collectAsState()

    val currentLivePool by viewModel.currentLivePool.collectAsState()
    val currentDeadPool by viewModel.currentDeadPool.collectAsState()
    val currentSkippedPool by viewModel.currentSkippedPool.collectAsState()
    val processedFilter by viewModel.processedFilter.collectAsState()
    val isSorted by viewModel.isSorted.collectAsState()
    val isUboFilter by viewModel.isUboFilter.collectAsState()

    // File Picker for opening .txt or similar documents
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            viewModel.loadFromUri(context, uri)
        }
    }

    // Scroll state for the entire screen container
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .background(MaterialTheme.colorScheme.background)
    ) {
        // App header bar with dynamic status badge
        Surface(
            tonalElevation = 4.dp,
            shadowElevation = 2.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 20.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(
                                    brush = Brush.linearGradient(
                                        listOf(
                                            Color(0xFF34D399),
                                            Color(0xFF059669)
                                        )
                                    ),
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .padding(6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Shield,
                                contentDescription = "App Icon",
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Domain Checker",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        )
                    }
                    Text(
                        text = "DNS + Wildcard Trap + HTTP/S Fetch Engine",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }

                // Dynamic Status Badge
                Box(
                    modifier = Modifier
                        .background(
                            color = when (inputLabel) {
                                "READY" -> Color(0xFFE0F2FE)
                                "FETCHING..." -> Color(0xFFFEF3C7)
                                "LOADED" -> Color(0xFFECFDF5)
                                "DONE" -> Color(0xFFD1FAE5)
                                "HTTP ERROR" -> Color(0xFFFEE2E2)
                                else -> MaterialTheme.colorScheme.surfaceVariant
                            },
                            shape = RoundedCornerShape(12.dp)
                        )
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = inputLabel,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = when (inputLabel) {
                                "READY" -> Color(0xFF0369A1)
                                "FETCHING..." -> Color(0xFFD97706)
                                "LOADED" -> Color(0xFF047857)
                                "DONE" -> Color(0xFF065F46)
                                "HTTP ERROR" -> Color(0xFFB91C1C)
                                else -> MaterialTheme.colorScheme.onSurfaceVariant
                            }
                        )
                    )
                }
            }
        }

        // Sub-layout body
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Main Input Card
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                ),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "INPUT FILTER OR LINk",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            )
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            TextButton(
                                onClick = { filePickerLauncher.launch("text/*") },
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.UploadFile,
                                    contentDescription = "Upload .txt",
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Upload .txt")
                            }
                            TextButton(
                                onClick = { viewModel.clearInput() },
                                colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = "Clear",
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("CLEAR")
                            }
                        }
                    }

                    OutlinedTextField(
                        value = inputText,
                        onValueChange = { viewModel.onInputTextChange(it) },
                        placeholder = {
                            Text(
                                "Paste GitHub raw link, local file content, or uBO filters...",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp)
                            .padding(vertical = 8.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                        ),
                        singleLine = false,
                        textStyle = MaterialTheme.typography.bodyMedium.copy(
                            fontFamily = FontFamily.Monospace,
                            fontSize = 12.sp
                        )
                    )

                    // Presets or Samples Drawer (Helper to quickly test in the emulator)
                    Text(
                        text = "TRY PRESETS SAMPLES",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        ),
                        modifier = Modifier.padding(top = 4.dp, bottom = 6.dp)
                    )

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Box(
                            modifier = Modifier
                                .background(
                                    color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f),
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .clickable {
                                    viewModel.onInputTextChange(
                                        "google.com\napple.com\nnonexistentdomain999.xyz\nsome-dead-link-to-test.org\n*wildcard-skip.co.uk\n*another-one*.org"
                                    )
                                }
                                .padding(horizontal = 8.dp, vertical = 6.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Dns, "Preset 1", modifier = Modifier.size(12.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Domains List", fontSize = 11.sp, fontWeight = FontWeight.Medium)
                            }
                        }

                        Box(
                            modifier = Modifier
                                .background(
                                    color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f),
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .clickable {
                                    viewModel.onInputTextChange(
                                        "! Title: Blocklist Filter Sample\nexample.com##.block-adds\ndead-site-example.com##.ad-banner\n||live-site.app^\$domain=apple.com|google.com|dead-site-example.com\n||non-existent-site.xyz^\$domain=apple.com\n*ads*.net##.banner"
                                    )
                                }
                                .padding(horizontal = 8.dp, vertical = 6.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.FilterAlt, "Preset 2", modifier = Modifier.size(12.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("uBlock Filter Rules", fontSize = 11.sp, fontWeight = FontWeight.Medium)
                            }
                        }

                        Box(
                            modifier = Modifier
                                .background(
                                    color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f),
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .clickable {
                                    viewModel.onInputTextChange(
                                        "https://github.com/uBlockOrigin/uAssets/blob/master/filters/filters.txt"
                                    )
                                }
                                .padding(horizontal = 8.dp, vertical = 6.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Link, "Preset 3", modifier = Modifier.size(12.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Raw GitHub URL", fontSize = 11.sp, fontWeight = FontWeight.Medium)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // ACTION RUN BUTTON
                    Button(
                        onClick = { viewModel.runEngine() },
                        enabled = inputText.isNotEmpty() && analysisState != AnalysisState.Running,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        if (analysisState == AnalysisState.Running) {
                            CircularProgressIndicator(
                                color = Color.White,
                                modifier = Modifier
                                    .size(20.dp)
                                    .padding(end = 4.dp),
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("RUNNING ANALYSIS...")
                        } else {
                            Icon(Icons.Default.PlayArrow, "Run")
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("RUN ANALYSIS", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Real-time log or progress panel
            AnimatedVisibility(
                visible = analysisState != AnalysisState.Idle,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "LIVE VERIFICATION LOG",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                )
                            )
                            if (analysisState == AnalysisState.Running) {
                                CircularProgressIndicator(
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(16.dp),
                                    strokeWidth = 2.dp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Progress Indicator
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = if (analysisState == AnalysisState.Completed) "Done" else "Checking domains...",
                                style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                            )
                            Text(
                                text = "$processedCount / $totalCount (${(progress * 100).toInt()}%)",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            )
                        }

                        LinearProgressIndicator(
                            progress = { progress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                                .clip(CircleShape),
                            color = Color(0xFF10B981),
                            trackColor = MaterialTheme.colorScheme.surfaceVariant
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        // Log outputs inner scroll window
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(130.dp)
                                .background(
                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f),
                                    shape = RoundedCornerShape(10.dp)
                                )
                                .border(
                                    width = 1.dp,
                                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                                    shape = RoundedCornerShape(10.dp)
                                )
                                .padding(8.dp)
                        ) {
                            if (logs.isEmpty()) {
                                Text(
                                    "Waiting for queue...",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.align(Alignment.Center)
                                )
                            } else {
                                LazyColumn(
                                    modifier = Modifier.fillMaxSize(),
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    items(logs) { log ->
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            // status pill
                                            Box(
                                                modifier = Modifier
                                                    .background(
                                                        color = when (log.second) {
                                                            "live" -> Color(0xFFD1FAE5)
                                                            "dead" -> Color(0xFFFEE2E2)
                                                            else -> Color(0xFFFEF3C7) // skip
                                                        },
                                                        shape = RoundedCornerShape(4.dp)
                                                    )
                                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                                            ) {
                                                Text(
                                                    text = log.second.uppercase(),
                                                    style = MaterialTheme.typography.labelSmall.copy(
                                                        fontSize = 8.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = when (log.second) {
                                                            "live" -> Color(0xFF065F46)
                                                            "dead" -> Color(0xFF991B1B)
                                                            else -> Color(0xFF92400E)
                                                        }
                                                    )
                                                )
                                            }

                                            Spacer(modifier = Modifier.width(8.dp))

                                            Text(
                                                text = log.first,
                                                fontSize = 11.sp,
                                                fontFamily = FontFamily.Monospace,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // Summary area
                        val liveCount = currentLivePool.size
                        val deadCount = currentDeadPool.size
                        val skipCount = currentSkippedPool.size

                        if (analysisState == AnalysisState.Completed) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 12.dp)
                                    .border(
                                        width = 1.dp,
                                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f),
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    .padding(8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                    Text(
                                        text = "$liveCount Live",
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF10B981),
                                        fontSize = 12.sp
                                    )
                                    Text(
                                        text = "$deadCount Dead",
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFEF4444),
                                        fontSize = 12.sp
                                    )
                                    Text(
                                        text = "$skipCount Skipped",
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFF59E0B),
                                        fontSize = 12.sp
                                    )
                                }

                                Button(
                                    onClick = { viewModel.toggleSort() },
                                    colors = ButtonDefaults.outlinedButtonColors(),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Icon(
                                        imageVector = if (isSorted) Icons.Default.Restore else Icons.Default.SortByAlpha,
                                        contentDescription = "Sort Icon",
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        if (isSorted) "Undo Sort" else "Sort A-Z",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // RESULTS UI SECTION
            if (analysisState == AnalysisState.Completed) {
                val liveToShow = if (isSorted) currentLivePool.sorted() else currentLivePool
                val deadToShow = if (isSorted) currentDeadPool.sorted() else currentDeadPool
                val skippedToShow = if (isSorted) currentSkippedPool.sorted() else currentSkippedPool

                // Dead Domains Result Card
                if (deadToShow.isNotEmpty()) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF5F5))
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Sick,
                                        contentDescription = "Dead",
                                        tint = Color(0xFFCF222E),
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "DEAD DOMAINS (${deadToShow.size})",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color(0xFFCF222E)
                                    )
                                }

                                TextButton(
                                    onClick = {
                                        clipboardManager.setText(AnnotatedString(deadToShow.joinToString("\n")))
                                        Toast.makeText(context, "Copied Dead list!", Toast.LENGTH_SHORT).show()
                                    },
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp)
                                ) {
                                    Text("COPY", color = Color(0xFFCF222E), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = deadToShow.joinToString("\n"),
                                fontFamily = FontFamily.Monospace,
                                fontSize = 11.sp,
                                color = Color(0xFF8C1D24),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(max = 120.dp)
                                    .verticalScroll(rememberScrollState())
                                    .background(Color.White.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                                    .padding(8.dp)
                            )
                        }
                    }
                }

                // Skipped (Wildcard) Card
                if (skippedToShow.isNotEmpty()) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFDF5))
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Forward,
                                        contentDescription = "Skipped",
                                        tint = Color(0xFFD97706),
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "SKIPPED WILDCARDS (${skippedToShow.size})",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color(0xFFD97706)
                                    )
                                }

                                TextButton(
                                    onClick = {
                                        clipboardManager.setText(AnnotatedString(skippedToShow.joinToString("\n")))
                                        Toast.makeText(context, "Copied Skipped list!", Toast.LENGTH_SHORT).show()
                                    },
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp)
                                ) {
                                    Text("COPY", color = Color(0xFFD97706), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = skippedToShow.joinToString("\n"),
                                fontFamily = FontFamily.Monospace,
                                fontSize = 11.sp,
                                color = Color(0xFF92400E),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(max = 100.dp)
                                    .verticalScroll(rememberScrollState())
                                    .background(Color.White.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                                    .padding(8.dp)
                            )
                        }
                    }
                }

                // Live outputs split box
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "LIVE OUTPUT POOLS",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            ),
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            // Live Comma Card
                            Card(
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFF0F9FF))
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text("LIVE (COMMA)", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0284C7))
                                        IconButton(
                                            onClick = {
                                                clipboardManager.setText(AnnotatedString(liveToShow.joinToString(",")))
                                                Toast.makeText(context, "Copied comma list!", Toast.LENGTH_SHORT).show()
                                            },
                                            modifier = Modifier.size(24.dp)
                                        ) {
                                            Icon(Icons.Default.ContentCopy, "Copy comma list", modifier = Modifier.size(12.dp), tint = Color(0xFF0284C7))
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = if (liveToShow.isEmpty()) "Empty" else liveToShow.joinToString(","),
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 11.sp,
                                        color = Color(0xFF0369A1),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(80.dp)
                                            .verticalScroll(rememberScrollState())
                                            .background(Color.White.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                                            .padding(6.dp)
                                    )
                                }
                            }

                            // Live Pipe Card
                            Card(
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F0FF))
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text("LIVE (PIPE)", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF7C3AED))
                                        IconButton(
                                            onClick = {
                                                clipboardManager.setText(AnnotatedString(liveToShow.joinToString("|")))
                                                Toast.makeText(context, "Copied pipe list!", Toast.LENGTH_SHORT).show()
                                            },
                                            modifier = Modifier.size(24.dp)
                                        ) {
                                            Icon(Icons.Default.ContentCopy, "Copy pipe list", modifier = Modifier.size(12.dp), tint = Color(0xFF7C3AED))
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = if (liveToShow.isEmpty()) "Empty" else liveToShow.joinToString("|"),
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 11.sp,
                                        color = Color(0xFF6D28D9),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(80.dp)
                                            .verticalScroll(rememberScrollState())
                                            .background(Color.White.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                                            .padding(6.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // Processed filters card (only shown if detected uBO format)
                if (isUboFilter) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFCF0))
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.CleaningServices,
                                        contentDescription = "Cleansed filters",
                                        tint = Color(0xFFB45309),
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "PROCESSED UBO CLEANSED FILTER",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color(0xFFB45309)
                                    )
                                }

                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    IconButton(
                                        onClick = {
                                            clipboardManager.setText(AnnotatedString(processedFilter))
                                            Toast.makeText(context, "Copied Cleansed filters!", Toast.LENGTH_SHORT).show()
                                        },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(Icons.Default.ContentCopy, "Copy Filters", modifier = Modifier.size(14.dp), tint = Color(0xFFB45309))
                                    }

                                    IconButton(
                                        onClick = {
                                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                                type = "text/plain"
                                                putExtra(Intent.EXTRA_TEXT, processedFilter)
                                            }
                                            context.startActivity(Intent.createChooser(shareIntent, "Share Filters"))
                                        },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(Icons.Default.Share, "Share Filters", modifier = Modifier.size(14.dp), tint = Color(0xFFB45309))
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = processedFilter,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 10.sp,
                                color = Color(0xFF78350F),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(260.dp)
                                    .verticalScroll(rememberScrollState())
                                    .background(Color.White.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                                    .padding(8.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
