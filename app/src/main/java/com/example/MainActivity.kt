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
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            // Force Light Mode as requested by User (beautiful modern light ui)
            MyApplicationTheme(darkTheme = false, dynamicColor = false) {
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

    // Dialog state
    var showHelpDialog by remember { mutableStateOf(false) }
    var showFullscreenDialog by remember { mutableStateOf(false) }
    var fullscreenTitle by remember { mutableStateOf("") }
    var fullscreenText by remember { mutableStateOf("") }

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

    // Light Neutral Slate Background
    Column(
        modifier = modifier
            .background(Color(0xFFF8FAFC))
    ) {
        // App header bar (Center Aligned, Pure White Banner)
        Surface(
            color = Color.White,
            shadowElevation = 1.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 14.dp, horizontal = 16.dp)
            ) {
                // Centered portion
                Row(
                    modifier = Modifier.align(Alignment.Center),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Success Verification",
                        tint = Color(0xFF10B981),
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "uBlockOrigin Filters Domain Checker",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1E293B)
                        ),
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Help Button (Replaces Preset & Guides users)
                IconButton(
                    onClick = { showHelpDialog = true },
                    modifier = Modifier
                        .size(36.dp)
                        .align(Alignment.CenterEnd)
                ) {
                    Icon(
                        imageVector = Icons.Default.HelpOutline,
                        contentDescription = "Help Guide",
                        tint = Color(0xFF64748B)
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
            // Feature Description Hero Card (Soft Emerald Green container)
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFECFDF5)
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color(0xFFA7F3D0).copy(alpha = 0.5f), RoundedCornerShape(12.dp))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Dns,
                        contentDescription = "Engine Icon",
                        tint = Color(0xFF047857),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Domain Verification Engine",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF065F46)
                            )
                        )
                        Text(
                            text = "Quad-Layer Logic: DNS + Wildcard Trap + HTTP/S Fetch",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = Color(0xFF047857)
                            )
                        )
                    }
                }
            }

            // Input Custom Board
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                ),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(16.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "INPUT (PASTE LINK OR TEXT)",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF475569)
                            )
                        )
                        
                        // Status Token Badge
                        Box(
                            modifier = Modifier
                                .background(
                                    color = when (inputLabel) {
                                        "READY" -> Color(0xFFF1F5F9)
                                        "FETCHING...", "ANALYZING...", "LOADING..." -> Color(0xFFFEF3C7)
                                        "LOADED" -> Color(0xFFECFDF5)
                                        "DONE" -> Color(0xFFD1FAE5)
                                        "HTTP ERROR", "FETCH FAILED", "OPEN FILE ERROR" -> Color(0xFFFEE2E2)
                                        else -> Color(0xFFF1F5F9)
                                    },
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = inputLabel,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    color = when (inputLabel) {
                                        "READY" -> Color(0xFF475569)
                                        "FETCHING...", "ANALYZING...", "LOADING..." -> Color(0xFFB45309)
                                        "LOADED" -> Color(0xFF047857)
                                        "DONE" -> Color(0xFF065F46)
                                        "HTTP ERROR", "FETCH FAILED", "OPEN FILE ERROR" -> Color(0xFFB91C1C)
                                        else -> Color(0xFF475569)
                                    }
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Text Field Container
                    OutlinedTextField(
                        value = inputText,
                        onValueChange = { viewModel.onInputTextChange(it) },
                        placeholder = {
                            Text(
                                "Paste GitHub raw filters link, local lists content, or specific uBO host files...",
                                style = MaterialTheme.typography.bodyMedium.copy(color = Color(0xFF94A3B8))
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color(0xFFF8FAFC),
                            unfocusedContainerColor = Color(0xFFF8FAFC),
                            focusedBorderColor = Color(0xFFCBD5E1),
                            unfocusedBorderColor = Color(0xFFE2E8F0),
                            focusedTextColor = Color(0xFF1E293B),
                            unfocusedTextColor = Color(0xFF334155)
                        ),
                        singleLine = false,
                        textStyle = MaterialTheme.typography.bodyMedium.copy(
                            fontFamily = FontFamily.Monospace,
                            fontSize = 12.sp
                        )
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Run and file controls
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // File text upload
                        OutlinedButton(
                            onClick = { filePickerLauncher.launch("text/*") },
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = Color(0xFF0F172A)
                            ),
                            border = BorderStroke(1.dp, Color(0xFFCBD5E1))
                        ) {
                            Icon(
                                imageVector = Icons.Default.UploadFile,
                                contentDescription = "Upload .txt",
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                "Upload .txt", 
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                            )
                        }

                        // Clear Button
                        OutlinedButton(
                            onClick = { viewModel.clearInput() },
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = Color(0xFF64748B)
                            ),
                            border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                        ) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "Clear content",
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                "CLEAR", 
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Solid Run Call Button
                    Button(
                        onClick = { viewModel.runEngine() },
                        enabled = inputText.isNotEmpty() && analysisState != AnalysisState.Running,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF22C55E), // Vibrant Green
                            contentColor = Color.White,
                            disabledContainerColor = Color(0xFFE2E8F0),
                            disabledContentColor = Color(0xFF94A3B8)
                        )
                    ) {
                        if (analysisState == AnalysisState.Running) {
                            CircularProgressIndicator(
                                color = Color.White,
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text("RUNNING ANALYSIS...", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                        } else {
                            Icon(
                                imageVector = Icons.Default.PlayArrow, 
                                contentDescription = "Start run",
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("RUN ANALYSIS", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.ExtraBold))
                        }
                    }
                }
            }

            // Verification Log Progress Block
            AnimatedVisibility(
                visible = analysisState != AnalysisState.Idle,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(16.dp)),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "LIVE VERIFICATION LOG",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF64748B)
                                )
                            )
                            if (analysisState == AnalysisState.Running) {
                                CircularProgressIndicator(
                                    color = Color(0xFF22C55E),
                                    modifier = Modifier.size(16.dp),
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Box(
                                    modifier = Modifier
                                        .background(Color(0xFFECFDF5), RoundedCornerShape(4.dp))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        "DONE",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = Color(0xFF047857),
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 9.sp
                                        )
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Numeric Progress
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (analysisState == AnalysisState.Completed) "Done" else "Verifying live status...",
                                style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF64748B))
                            )
                            Text(
                                text = "$processedCount / $totalCount (${(progress * 100).toInt()}%)",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF0F172A)
                                )
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        // Slim Elegant Progress indicator
                        LinearProgressIndicator(
                            progress = { progress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(CircleShape),
                            color = Color(0xFF22C55E),
                            trackColor = Color(0xFFF1F5F9)
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Scrollable terminal items
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp)
                                .background(Color(0xFFF8FAFC), RoundedCornerShape(10.dp))
                                .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(10.dp))
                                .padding(8.dp)
                        ) {
                            if (logs.isEmpty()) {
                                Text(
                                    "No logs currently recorded, launch analysis to start...",
                                    fontSize = 11.sp,
                                    color = Color(0xFF94A3B8),
                                    modifier = Modifier.align(Alignment.Center)
                                )
                            } else {
                                LazyColumn(
                                    modifier = Modifier.fillMaxSize(),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    items(logs) { log ->
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            // Badge pill
                                            val badgeBg = when (log.second) {
                                                "live" -> Color(0xFFDCFCE7)
                                                "dead" -> Color(0xFFFEE2E2)
                                                else -> Color(0xFFFEF3C7) // skipped
                                            }
                                            val badgeText = when (log.second) {
                                                "live" -> Color(0xFF15803D)
                                                "dead" -> Color(0xFFB91C1C)
                                                else -> Color(0xFFB45309) // skipped
                                            }
                                            Box(
                                                modifier = Modifier
                                                    .background(badgeBg, RoundedCornerShape(4.dp))
                                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                                            ) {
                                                Text(
                                                    text = log.second.uppercase(),
                                                    style = MaterialTheme.typography.labelSmall.copy(
                                                        fontSize = 8.sp,
                                                        fontWeight = FontWeight.ExtraBold,
                                                        color = badgeText
                                                    )
                                                )
                                            }

                                            Spacer(modifier = Modifier.width(8.dp))

                                            Text(
                                                text = log.first,
                                                fontSize = 11.sp,
                                                fontFamily = FontFamily.Monospace,
                                                maxLines = 1,
                                                color = Color(0xFF334155),
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // Summary & Sort Section
                        val liveCount = currentLivePool.size
                        val deadCount = currentDeadPool.size
                        val skipCount = currentSkippedPool.size

                        if (analysisState == AnalysisState.Completed) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 12.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(8.dp))
                                        .background(Color(0xFFF8FAFC), RoundedCornerShape(8.dp))
                                        .padding(8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                        Text(
                                            text = "$liveCount Live",
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF15803D),
                                            fontSize = 12.sp
                                        )
                                        Text(
                                            text = "$deadCount Dead",
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFFB91C1C),
                                            fontSize = 12.sp
                                        )
                                        Text(
                                            text = "$skipCount Skipped",
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFFB45309),
                                            fontSize = 12.sp
                                        )
                                    }

                                    Button(
                                        onClick = { viewModel.toggleSort() },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = Color.White,
                                            contentColor = Color(0xFF0F172A)
                                        ),
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                        shape = RoundedCornerShape(6.dp),
                                        border = BorderStroke(1.dp, Color(0xFFCBD5E1))
                                    ) {
                                        Icon(
                                            imageVector = if (isSorted) Icons.Default.Restore else Icons.Default.Sort,
                                            contentDescription = "Sort Icon",
                                            modifier = Modifier.size(13.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            if (isSorted) "Original Order" else "Sort A-Z",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // RESULTS INDIVIDUAL CARDS (UP AND DOWN STACKING - SCREENSHOT 2)
            if (analysisState == AnalysisState.Completed) {
                val liveToShow = if (isSorted) currentLivePool.sorted() else currentLivePool
                val deadToShow = if (isSorted) currentDeadPool.sorted() else currentDeadPool
                val skippedToShow = if (isSorted) currentSkippedPool.sorted() else currentSkippedPool

                // 1. Dead Domains Result Card
                if (deadToShow.isNotEmpty()) {
                    ResultCard(
                        title = "DEAD DOMAINS",
                        badgeText = "${deadToShow.size}",
                        accentColor = Color(0xFFEF4444), // Crimson Red
                        icon = {
                            Icon(
                                imageVector = Icons.Default.Cancel,
                                contentDescription = "Dead Icon",
                                tint = Color(0xFFEF4444),
                                modifier = Modifier.size(18.dp)
                            )
                        },
                        content = deadToShow.joinToString("\n"),
                        onFullscreen = {
                            fullscreenTitle = "DEAD DOMAINS (${deadToShow.size})"
                            fullscreenText = deadToShow.joinToString("\n")
                            showFullscreenDialog = true
                        },
                        onCopy = {
                            clipboardManager.setText(AnnotatedString(deadToShow.joinToString("\n")))
                            Toast.makeText(context, "Copied Dead list to Clipboard!", Toast.LENGTH_SHORT).show()
                        }
                    )
                }

                // 2. Skipped Wildcards Card
                if (skippedToShow.isNotEmpty()) {
                    ResultCard(
                        title = "SKIPPED WILDCARDS",
                        badgeText = "${skippedToShow.size}",
                        accentColor = Color(0xFFF59E0B), // Vibrant Amber
                        icon = {
                            Icon(
                                imageVector = Icons.Default.SkipNext,
                                contentDescription = "Skipped Icon",
                                tint = Color(0xFFF59E0B),
                                modifier = Modifier.size(18.dp)
                            )
                        },
                        content = skippedToShow.joinToString("\n"),
                        onFullscreen = {
                            fullscreenTitle = "SKIPPED WILDCARDS (${skippedToShow.size})"
                            fullscreenText = skippedToShow.joinToString("\n")
                            showFullscreenDialog = true
                        },
                        onCopy = {
                            clipboardManager.setText(AnnotatedString(skippedToShow.joinToString("\n")))
                            Toast.makeText(context, "Copied Skipped list successfully!", Toast.LENGTH_SHORT).show()
                        }
                    )
                }

                // 3. Live Comma List Card
                ResultCard(
                    title = "LIVE (COMMA)",
                    badgeText = "${liveToShow.size}",
                    accentColor = Color(0xFF3B82F6), // Premium Blue
                    icon = {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Live Icon",
                            tint = Color(0xFF3B82F6),
                            modifier = Modifier.size(18.dp)
                        )
                    },
                    content = if (liveToShow.isEmpty()) "Empty" else liveToShow.joinToString(","),
                    onFullscreen = {
                        fullscreenTitle = "LIVE (COMMA)"
                        fullscreenText = if (liveToShow.isEmpty()) "Empty" else liveToShow.joinToString(",")
                        showFullscreenDialog = true
                    },
                    onCopy = {
                        clipboardManager.setText(AnnotatedString(if (liveToShow.isEmpty()) "Empty" else liveToShow.joinToString(",")))
                        Toast.makeText(context, "Copied Live Comma list!", Toast.LENGTH_SHORT).show()
                    }
                )

                // 4. Live Pipe List Card
                ResultCard(
                    title = "LIVE (PIPE)",
                    badgeText = "${liveToShow.size}",
                    accentColor = Color(0xFF8B5CF6), // Royal Purple
                    icon = {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Live Icon",
                            tint = Color(0xFF8B5CF6),
                            modifier = Modifier.size(18.dp)
                        )
                    },
                    content = if (liveToShow.isEmpty()) "Empty" else liveToShow.joinToString("|"),
                    onFullscreen = {
                        fullscreenTitle = "LIVE (PIPE)"
                        fullscreenText = if (liveToShow.isEmpty()) "Empty" else liveToShow.joinToString("|")
                        showFullscreenDialog = true
                    },
                    onCopy = {
                        clipboardManager.setText(AnnotatedString(if (liveToShow.isEmpty()) "Empty" else liveToShow.joinToString("|")))
                        Toast.makeText(context, "Copied Live Pipe list!", Toast.LENGTH_SHORT).show()
                    }
                )

                // 5. Processed filters card
                if (isUboFilter) {
                    ResultCard(
                        title = "PROCESSED UBO CLEANSED FILTER",
                        badgeText = null,
                        accentColor = Color(0xFF10B981), // Emerald Green
                        icon = {
                            Icon(
                                imageVector = Icons.Default.Brush,
                                contentDescription = "Brush Icon",
                                tint = Color(0xFF10B981),
                                modifier = Modifier.size(18.dp)
                            )
                        },
                        content = processedFilter,
                        onFullscreen = {
                            fullscreenTitle = "PROCESSED UBO CLEANSED FILTER"
                            fullscreenText = processedFilter
                            showFullscreenDialog = true
                        },
                        onCopy = {
                            clipboardManager.setText(AnnotatedString(processedFilter))
                            Toast.makeText(context, "Copied Processed filter rules!", Toast.LENGTH_SHORT).show()
                        }
                    )
                }
            }
        }
    }

    // Help Dialog Implementation
    if (showHelpDialog) {
        AlertDialog(
            onDismissRequest = { showHelpDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.HelpOutline,
                        contentDescription = null,
                        tint = Color(0xFF10B981),
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "About Domain Checker",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }
            },
            text = {
                Column(
                    modifier = Modifier
                        .verticalScroll(rememberScrollState())
                        .padding(vertical = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "By BlazeFTL",
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF10B981),
                        style = MaterialTheme.typography.bodyMedium
                    )

                    Text(
                        text = "This utility performs high-fidelity domain resolution, optimized specifically for lists, uBO hosts, and cosmetic/network filters.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF334155)
                    )

                    HorizontalDivider(color = Color(0xFFE2E8F0))

                    Text(
                        text = "Capabilities & Features:",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF0F172A)
                    )

                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("• Cleans and optimizes list entries or advanced uBlock cosmetic/network filter lists.", fontSize = 13.sp, color = Color(0xFF475569))
                        Text("• Safely resolves DNS via HTTPS Google DNS-over-HTTPS (DoH), bypassing cellular carrier/ISP DNS hijacking.", fontSize = 13.sp, color = Color(0xFF475569))
                        Text("• Performs parallel checks to query responsive servers and filter out dead hostnames cleanly and fast.", fontSize = 13.sp, color = Color(0xFF475569))
                        Text("• Keeps at least one reference/backup server alive when replacing list items of a fully dead host list.", fontSize = 13.sp, color = Color(0xFF475569))
                        Text("• Supports fullscreen output boxes to browse, copy, or manipulate large lists easily.", fontSize = 13.sp, color = Color(0xFF475569))
                    }

                    HorizontalDivider(color = Color(0xFFE2E8F0))

                    Text(
                        text = "How to Use:",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF0F172A)
                    )
                    Text(
                        text = "1. Paste rules or domain lists in the text box, or upload any local text file via '+ Upload .txt'.\n" +
                               "2. Click 'RUN ANALYSIS'.\n" +
                               "3. Review progress in the real-time queue log.\n" +
                               "4. Browse output cards or enlarge key areas to copy easily.",
                        fontSize = 13.sp,
                        color = Color(0xFF475569)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { showHelpDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981))
                ) {
                    Text("GOT IT")
                }
            }
        )
    }

    // Fullscreen View Dialog Implementation (Magnificent 95% width, 90% height modal with line numbers!)
    if (showFullscreenDialog) {
        Dialog(
            onDismissRequest = { showFullscreenDialog = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth(0.95f)
                    .fillMaxHeight(0.9f)
                    .clip(RoundedCornerShape(24.dp)),
                color = Color.White,
                shadowElevation = 8.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(20.dp)
                ) {
                    // Header Area of Fullscreen Dialog
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = fullscreenTitle,
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color(0xFF1E293B)
                                )
                            )
                            Text(
                                text = "Detailed analysis list output",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = Color(0xFF64748B)
                                )
                            )
                        }

                        // Circular Close Button at top-right
                        IconButton(
                            onClick = { showFullscreenDialog = false },
                            modifier = Modifier
                                .background(Color(0xFFF1F5F9), CircleShape)
                                .size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close Fullscreen",
                                tint = Color(0xFF64748B),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    HorizontalDivider(color = Color(0xFFE2E8F0))

                    Spacer(modifier = Modifier.height(16.dp))

                    // Large Spacious Monospace Viewer Content Area
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .background(Color(0xFFF8FAFC), RoundedCornerShape(12.dp))
                            .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(12.dp))
                            .padding(14.dp)
                    ) {
                        val innerScrollState = rememberScrollState()
                        val lines = fullscreenText.split("\n")
                        Row(modifier = Modifier.fillMaxSize()) {
                            // Line numbers column
                            Text(
                                text = (1..lines.size).joinToString("\n") { it.toString().padStart(3, ' ') },
                                fontFamily = FontFamily.Monospace,
                                fontSize = 12.sp,
                                color = Color(0xFF94A3B8),
                                lineHeight = 20.sp,
                                modifier = Modifier.padding(end = 12.dp)
                            )

                            // Vertical divider
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .width(1.dp)
                                    .background(Color(0xFFE2E8F0))
                            )

                            Spacer(modifier = Modifier.width(12.dp))

                            // Actual text content
                            Text(
                                text = fullscreenText,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 12.sp,
                                color = Color(0xFF1E293B),
                                lineHeight = 20.sp,
                                modifier = Modifier
                                    .weight(1f)
                                    .verticalScroll(innerScrollState)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Action buttons at the bottom
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Copy Button with Solid Border outline
                        OutlinedButton(
                            onClick = {
                                clipboardManager.setText(AnnotatedString(fullscreenText))
                                Toast.makeText(context, "Copied list content!", Toast.LENGTH_SHORT).show()
                            },
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, Color(0xFFCBD5E1)),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF1E293B)),
                            modifier = Modifier.height(44.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = "Copy Icon",
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Copy Content", fontWeight = FontWeight.Bold)
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        // Clear solid Close button
                        Button(
                            onClick = { showFullscreenDialog = false },
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F172A)),
                            modifier = Modifier.height(44.dp)
                        ) {
                            Text("Close View", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// REUSABLE HIGH-FIDELITY RESULT CARD COMPOSABLE
// ==========================================
@Composable
fun ResultCard(
    title: String,
    badgeText: String?,
    accentColor: Color,
    icon: @Composable () -> Unit,
    content: String,
    onFullscreen: () -> Unit,
    onCopy: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min)
        ) {
            // Elegant Left vertical accent strip
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(6.dp)
                    .background(accentColor)
            )

            // Content Column
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(16.dp)
            ) {
                // Header Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        icon()
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFF1E293B),
                                letterSpacing = 0.5.sp
                            )
                        )
                        if (badgeText != null) {
                            Box(
                                modifier = Modifier
                                    .background(accentColor.copy(alpha = 0.12f), RoundedCornerShape(12.dp))
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = badgeText,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.ExtraBold,
                                        color = accentColor
                                    )
                                )
                            }
                        }
                    }

                    // Flawlessly Aligned Utility Action Icons (Exact 36dp sizes)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        IconButton(
                            onClick = onFullscreen,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.OpenInFull,
                                contentDescription = "Fullscreen",
                                tint = Color(0xFF64748B),
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        IconButton(
                            onClick = onCopy,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = "Copy Content",
                                tint = Color(0xFF64748B),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Beautiful scrolling inner monospace text area
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 130.dp)
                        .background(Color(0xFFF8FAFC), RoundedCornerShape(10.dp))
                        .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(10.dp))
                        .padding(10.dp)
                ) {
                    val innerScrollState = rememberScrollState()
                    Text(
                        text = content.ifEmpty { "Empty list" },
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        color = Color(0xFF334155),
                        lineHeight = 16.sp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .verticalScroll(innerScrollState)
                    )
                }
            }
        }
    }
}
