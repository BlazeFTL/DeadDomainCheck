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
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Header Title with Checked Icon
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Success Verification",
                        tint = Color(0xFF10B981),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "uBlockOrigin Filters Domain Checker",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1E293B)
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Help Button (Replaces Preset & Guides users)
                IconButton(
                    onClick = { showHelpDialog = true },
                    modifier = Modifier.size(36.dp)
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

                // 1. Dead Domains Result Card (RED BORDER AND SOFT BACKGROUND)
                if (deadToShow.isNotEmpty()) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, Color(0xFFFCA5A5), RoundedCornerShape(16.dp)),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF2F2))
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Cancel, // Red clean cancel mark
                                        contentDescription = "Dead icon",
                                        tint = Color(0xFFDC2626),
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "DEAD DOMAINS (${deadToShow.size})",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color(0xFFDC2626)
                                    )
                                }

                                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    // Fullscreen Action Button
                                    IconButton(
                                        onClick = {
                                            fullscreenTitle = "DEAD DOMAINS (${deadToShow.size})"
                                            fullscreenText = deadToShow.joinToString("\n")
                                            showFullscreenDialog = true
                                        },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.OpenInFull,
                                            contentDescription = "Expand Fullscreen",
                                            tint = Color(0xFFDC2626),
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                    
                                    // Copy Action Button
                                    TextButton(
                                        onClick = {
                                            clipboardManager.setText(AnnotatedString(deadToShow.joinToString("\n")))
                                            Toast.makeText(context, "Copied Dead list to Clipboard!", Toast.LENGTH_SHORT).show()
                                        },
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                                    ) {
                                        Text("COPY", color = Color(0xFFDC2626), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = deadToShow.joinToString("\n"),
                                fontFamily = FontFamily.Monospace,
                                fontSize = 11.sp,
                                color = Color(0xFF991B1B),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(max = 120.dp)
                                    .verticalScroll(rememberScrollState())
                                    .background(Color.White, RoundedCornerShape(8.dp))
                                    .border(1.dp, Color(0xFFFEE2E2), RoundedCornerShape(8.dp))
                                    .padding(8.dp)
                            )
                        }
                    }
                }

                // 2. Skipped Wildcards Card (AMBER BORDER AND SOFT BACKGROUND)
                if (skippedToShow.isNotEmpty()) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, Color(0xFFFDE68A), RoundedCornerShape(16.dp)),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFBEB))
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.SkipNext,
                                        contentDescription = "Skipped Icon",
                                        tint = Color(0xFFD97706),
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "SKIPPED WILDCARDS (${skippedToShow.size})",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color(0xFFD97706)
                                    )
                                }

                                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    // Fullscreen action
                                    IconButton(
                                        onClick = {
                                            fullscreenTitle = "SKIPPED WILDCARDS (${skippedToShow.size})"
                                            fullscreenText = skippedToShow.joinToString("\n")
                                            showFullscreenDialog = true
                                        },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.OpenInFull,
                                            contentDescription = "Expand Fullscreen",
                                            tint = Color(0xFFD97706),
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }

                                    // Copy action
                                    TextButton(
                                        onClick = {
                                            clipboardManager.setText(AnnotatedString(skippedToShow.joinToString("\n")))
                                            Toast.makeText(context, "Copied Skipped list successfully!", Toast.LENGTH_SHORT).show()
                                        },
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                                    ) {
                                        Text("COPY", color = Color(0xFFD97706), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = skippedToShow.joinToString("\n"),
                                fontFamily = FontFamily.Monospace,
                                fontSize = 11.sp,
                                color = Color(0xFF92400E),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(max = 100.dp)
                                    .verticalScroll(rememberScrollState())
                                    .background(Color.White, RoundedCornerShape(8.dp))
                                    .border(1.dp, Color(0xFFFEF3C7), RoundedCornerShape(8.dp))
                                    .padding(8.dp)
                            )
                        }
                    }
                }

                // 3. Live Comma List Card (SKY BLUE BORDER AND SOFT BACKGROUND)
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, Color(0xFFBAE6FD), RoundedCornerShape(16.dp)),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF0F9FF))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "LIVE (COMMA)", 
                                fontSize = 11.sp, 
                                fontWeight = FontWeight.ExtraBold, 
                                color = Color(0xFF0284C7)
                            )
                            
                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                val textValue = if (liveToShow.isEmpty()) "Empty" else liveToShow.joinToString(",")
                                
                                // Fullscreen action
                                IconButton(
                                    onClick = {
                                        fullscreenTitle = "LIVE (COMMA)"
                                        fullscreenText = textValue
                                        showFullscreenDialog = true
                                    },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.OpenInFull,
                                        contentDescription = "Expand Fullscreen",
                                        tint = Color(0xFF0284C7),
                                        modifier = Modifier.size(14.dp)
                                    )
                                }

                                // Copy action
                                TextButton(
                                    onClick = {
                                        clipboardManager.setText(AnnotatedString(textValue))
                                        Toast.makeText(context, "Copied Live Comma list!", Toast.LENGTH_SHORT).show()
                                    },
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Text("COPY", color = Color(0xFF0284C7), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        val formattedCommaText = if (liveToShow.isEmpty()) "Empty" else liveToShow.joinToString(",")
                        Text(
                            text = formattedCommaText,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            color = Color(0xFF0369A1),
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 100.dp)
                                .verticalScroll(rememberScrollState())
                                .background(Color.White, RoundedCornerShape(8.dp))
                                .border(1.dp, Color(0xFFE0F2FE), RoundedCornerShape(8.dp))
                                .padding(8.dp)
                        )
                    }
                }

                // 4. Live Pipe List Card (VIOLET/PURPLE BORDER AND SOFT BACKGROUND)
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, Color(0xFFDDD6FE), RoundedCornerShape(16.dp)),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F3FF))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "LIVE (PIPE)", 
                                fontSize = 11.sp, 
                                fontWeight = FontWeight.ExtraBold, 
                                color = Color(0xFF7C3AED)
                            )
                            
                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                val textValue = if (liveToShow.isEmpty()) "Empty" else liveToShow.joinToString("|")
                                
                                // Fullscreen action
                                IconButton(
                                    onClick = {
                                        fullscreenTitle = "LIVE (PIPE)"
                                        fullscreenText = textValue
                                        showFullscreenDialog = true
                                    },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.OpenInFull,
                                        contentDescription = "Expand Fullscreen",
                                        tint = Color(0xFF7C3AED),
                                        modifier = Modifier.size(14.dp)
                                    )
                                }

                                // Copy action
                                TextButton(
                                    onClick = {
                                        clipboardManager.setText(AnnotatedString(textValue))
                                        Toast.makeText(context, "Copied Live Pipe list!", Toast.LENGTH_SHORT).show()
                                    },
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Text("COPY", color = Color(0xFF7C3AED), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        val formattedPipeText = if (liveToShow.isEmpty()) "Empty" else liveToShow.joinToString("|")
                        Text(
                            text = formattedPipeText,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            color = Color(0xFF6D28D9),
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 100.dp)
                                .verticalScroll(rememberScrollState())
                                .background(Color.White, RoundedCornerShape(8.dp))
                                .border(1.dp, Color(0xFFEDE9FE), RoundedCornerShape(8.dp))
                                .padding(8.dp)
                        )
                    }
                }

                // 5. Processed filters card - only shown optionally if detected uBO format (MINT EMERALD GREEN CARD)
                if (isUboFilter) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, Color(0xFFA7F3D0), RoundedCornerShape(16.dp)),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFECFDF5))
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Brush,
                                        contentDescription = "Shield Clean",
                                        tint = Color(0xFF047857),
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "PROCESSED UBO CLEANSED FILTER",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color(0xFF047857)
                                    )
                                }

                                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    // Fullscreen action
                                    IconButton(
                                        onClick = {
                                            fullscreenTitle = "PROCESSED UBO CLEANSED FILTER"
                                            fullscreenText = processedFilter
                                            showFullscreenDialog = true
                                        },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.OpenInFull,
                                            contentDescription = "Expand Fullscreen",
                                            tint = Color(0xFF047857),
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }

                                    // Copy action
                                    TextButton(
                                        onClick = {
                                            clipboardManager.setText(AnnotatedString(processedFilter))
                                            Toast.makeText(context, "Copied Processed filter rules!", Toast.LENGTH_SHORT).show()
                                        },
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                                    ) {
                                        Text("COPY", color = Color(0xFF047857), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = processedFilter,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 11.sp,
                                color = Color(0xFF065F46),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(max = 140.dp)
                                    .verticalScroll(rememberScrollState())
                                    .background(Color.White, RoundedCornerShape(8.dp))
                                    .border(1.dp, Color(0xFFD1FAE5), RoundedCornerShape(8.dp))
                                    .padding(8.dp)
                            )
                        }
                    }
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

    // Fullscreen View Dialog Implementation
    if (showFullscreenDialog) {
        AlertDialog(
            onDismissRequest = { showFullscreenDialog = false },
            title = {
                Text(
                    text = fullscreenTitle,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f, fill = false)
                            .heightIn(max = 420.dp)
                            .background(Color(0xFFF8FAFC), RoundedCornerShape(8.dp))
                            .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(8.dp))
                            .padding(12.dp)
                    ) {
                        val innerScrollState = rememberScrollState()
                        Text(
                            text = fullscreenText,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 12.sp,
                            color = Color(0xFF1E293B),
                            modifier = Modifier
                                .fillMaxWidth()
                                .verticalScroll(innerScrollState)
                        )
                    }
                }
            },
            confirmButton = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        onClick = {
                            clipboardManager.setText(AnnotatedString(fullscreenText))
                            Toast.makeText(context, "Copied content to clipboard!", Toast.LENGTH_SHORT).show()
                        }
                    ) {
                        Icon(imageVector = Icons.Default.ContentCopy, contentDescription = "Copy")
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("COPY ALL")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = { showFullscreenDialog = false },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F172A))
                    ) {
                        Text("CLOSE")
                    }
                }
            }
        )
    }
}
