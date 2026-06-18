package com.example

import android.app.Application
import android.content.Context
import android.net.Uri
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontFamily
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.BufferedReader
import java.io.InputStreamReader

sealed class AnalysisState {
    object Idle : AnalysisState()
    object Running : AnalysisState()
    object Completed : AnalysisState()
}

class MainViewModel(application: Application) : AndroidViewModel(application) {

    // Aesthetic and Custom Personalization Enums
    enum class AppAccent(
        val displayName: String,
        val primaryColor: Color,
        val secondaryColor: Color,
        val labelBgColor: Color,
        val gradientBrush: Brush? = null
    ) {
        EMERALD("Emerald Shield", Color(0xFF10B981), Color(0xFF047857), Color(0xFFECFDF5)),
        OCEAN("Ocean Wave", Color(0xFF0EA5E9), Color(0xFF0284C7), Color(0xFFF0F9FF)),
        AMETHYST("Amethyst Spark", Color(0xFF8B5CF6), Color(0xFF6D28D9), Color(0xFFF5F3FF)),
        CRIMSON("Crimson Rose", Color(0xFFF43F5E), Color(0xFFBE123C), Color(0xFFFFF1F2)),
        VOLCANO("Blaze Sunrise", Color(0xFFF97316), Color(0xFFC2410C), Color(0xFFFFF7ED)),
        CYBERPUNK("Cyberpunk Dream", Color(0xFFEC4899), Color(0xFF3B82F6), Color(0xFFFFF1F2),
            Brush.linearGradient(listOf(Color(0xFFEC4899), Color(0xFF3B82F6)))
        ),
        TWILIGHT("Twilight Sunset", Color(0xFF8B5CF6), Color(0xFFF43F5E), Color(0xFFF5F3FF),
            Brush.linearGradient(listOf(Color(0xFF8B5CF6), Color(0xFFF43F5E)))
        )
    }

    enum class DisplayFont(val displayName: String, val fontFamily: FontFamily) {
        MONOSPACE("Developer Monospace", FontFamily.Monospace),
        SANS_SERIF("Clean Sans-Serif", FontFamily.SansSerif),
        SERIF("Readable Serif", FontFamily.Serif)
    }

    private val _selectedAccent = MutableStateFlow(AppAccent.EMERALD)
    val selectedAccent: StateFlow<AppAccent> = _selectedAccent.asStateFlow()

    private val _selectedFont = MutableStateFlow(DisplayFont.MONOSPACE)
    val selectedFont: StateFlow<DisplayFont> = _selectedFont.asStateFlow()

    fun selectAccent(accent: AppAccent) {
        _selectedAccent.value = accent
    }

    fun selectFont(font: DisplayFont) {
        _selectedFont.value = font
    }

    private val engine = DomainCheckerEngine()
    private val client = OkHttpClient()

    private val _inputText = MutableStateFlow("")
    val inputText: StateFlow<String> = _inputText.asStateFlow()

    private val _inputLabel = MutableStateFlow("READY")
    val inputLabel: StateFlow<String> = _inputLabel.asStateFlow()

    private val _analysisState = MutableStateFlow<AnalysisState>(AnalysisState.Idle)
    val analysisState: StateFlow<AnalysisState> = _analysisState.asStateFlow()

    private val _progress = MutableStateFlow(0f)
    val progress: StateFlow<Float> = _progress.asStateFlow()

    private val _processedCount = MutableStateFlow(0)
    val processedCount: StateFlow<Int> = _processedCount.asStateFlow()

    private val _totalCount = MutableStateFlow(0)
    val totalCount: StateFlow<Int> = _totalCount.asStateFlow()

    // Logs of domains being verified (latest checked domains prepended)
    private val _logs = MutableStateFlow<List<Pair<String, String>>>(emptyList())
    val logs: StateFlow<List<Pair<String, String>>> = _logs.asStateFlow()

    private val _currentLivePool = MutableStateFlow<List<String>>(emptyList())
    val currentLivePool: StateFlow<List<String>> = _currentLivePool.asStateFlow()

    private val _currentDeadPool = MutableStateFlow<List<String>>(emptyList())
    val currentDeadPool: StateFlow<List<String>> = _currentDeadPool.asStateFlow()

    private val _currentSkippedPool = MutableStateFlow<List<String>>(emptyList())
    val currentSkippedPool: StateFlow<List<String>> = _currentSkippedPool.asStateFlow()

    private val _processedFilter = MutableStateFlow("")
    val processedFilter: StateFlow<String> = _processedFilter.asStateFlow()

    private val _isSorted = MutableStateFlow(false)
    val isSorted: StateFlow<Boolean> = _isSorted.asStateFlow()

    private val _isUboFilter = MutableStateFlow(false)
    val isUboFilter: StateFlow<Boolean> = _isUboFilter.asStateFlow()

    fun onInputTextChange(text: String) {
        _inputText.value = text
        // Automatically check if pasted a raw github link
        if (text.startsWith("https://github.com/") && text.contains("/blob/")) {
            fetchFromGitHub(text)
        }
    }

    fun clearInput() {
        _inputText.value = ""
        resetUI()
    }

    fun resetUI() {
        _logs.value = emptyList()
        _analysisState.value = AnalysisState.Idle
        _currentLivePool.value = emptyList()
        _currentDeadPool.value = emptyList()
        _currentSkippedPool.value = emptyList()
        _processedFilter.value = ""
        _isSorted.value = false
        _isUboFilter.value = false
        _progress.value = 0f
        _processedCount.value = 0
        _totalCount.value = 0
        _inputLabel.value = "READY"
        engine.clearCache()
    }

    fun loadFromUri(context: Context, uri: Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            _inputLabel.value = "LOADING..."
            try {
                context.contentResolver.openInputStream(uri)?.use { inputStream ->
                    BufferedReader(InputStreamReader(inputStream)).use { reader ->
                        val content = reader.readText()
                        withContext(Dispatchers.Main) {
                            _inputText.value = content
                            _inputLabel.value = "LOADED"
                        }
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    _inputLabel.value = "OPEN FILE ERROR"
                }
            }
        }
    }

    private fun fetchFromGitHub(githubUrl: String) {
        viewModelScope.launch(Dispatchers.IO) {
            _inputLabel.value = "FETCHING..."
            try {
                // Convert github to raw format
                val rawUrl = githubUrl
                    .replace("github.com", "raw.githubusercontent.com")
                    .replace("/blob/", "/")
                
                val request = Request.Builder().url(rawUrl).build()
                client.newCall(request).execute().use { response ->
                    if (response.isSuccessful) {
                        val body = response.body?.string().orEmpty()
                        withContext(Dispatchers.Main) {
                            _inputText.value = body
                            _inputLabel.value = "READY"
                        }
                    } else {
                        withContext(Dispatchers.Main) {
                            _inputLabel.value = "HTTP ERROR"
                        }
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    _inputLabel.value = "FETCH FAILED"
                }
            }
        }
    }

    fun toggleSort() {
        _isSorted.value = !_isSorted.value
    }

    fun runEngine() {
        val input = _inputText.value.trim()
        if (input.isEmpty()) return

        viewModelScope.launch(Dispatchers.Default) {
            _analysisState.value = AnalysisState.Running
            _inputLabel.value = "ANALYZING..."
            _progress.value = 0f
            _processedCount.value = 0
            _logs.value = emptyList()

            val lines = input.split('\n')
            // Match HTML's UBO detection
            val isUbo = lines.any { line ->
                line.contains("##") || line.contains("#@#") || line.contains("||") || 
                Regex("(?:domain|from)=[^,\\s]+\\|[^,\\s]+").containsMatchIn(line)
            }
            withContext(Dispatchers.Main) {
                _isUboFilter.value = isUbo
            }

            val uniqueToCheck = mutableSetOf<String>()
            val allSkipped = mutableSetOf<String>()

            // First parse pass to collect unique domains
            for (line in lines) {
                val parsed = DomainCheckerEngine.parseLine(line) ?: continue
                for (d in parsed.domains) {
                    val bare = if (d.startsWith("~")) d.drop(1) else d
                    uniqueToCheck.add(bare)
                }
                for (s in parsed.skipped) {
                    allSkipped.add(s)
                }
            }

            val uniqueCheckList = uniqueToCheck.toList()
            val total = uniqueCheckList.size
            withContext(Dispatchers.Main) {
                _totalCount.value = total
                _currentSkippedPool.value = allSkipped.toList()
                // Log all skipped wildcards immediately
                allSkipped.forEach { s ->
                    _logs.value = listOf(Pair(s, "skip")) + _logs.value
                }
            }

            val activeLive = mutableSetOf<String>()
            val activeDead = mutableSetOf<String>()

            if (total > 0) {
                // We run in parallel but controlled chunk batches to avoid resource explosion
                val batchSize = 12
                val countAtom = java.util.concurrent.atomic.AtomicInteger(0)

                // Split uniqueCheckList into batches
                for (i in uniqueCheckList.indices step batchSize) {
                    val endToken = minOf(i + batchSize, total)
                    val chunk = uniqueCheckList.subList(i, endToken)

                    // Execute batch in parallel
                    val chunkResults = chunk.map { bare ->
                        viewModelScope.launch(Dispatchers.IO) {
                            val status = engine.validate(bare)
                            val currentCount = countAtom.incrementAndGet()
                            
                            // Log real-time status update
                            withContext(Dispatchers.Main) {
                                _logs.value = listOf(Pair(bare, status)) + _logs.value
                                _processedCount.value = currentCount
                                _progress.value = currentCount.toFloat() / total
                            }
                            if (status == "live") {
                                synchronized(activeLive) { activeLive.add(bare) }
                            } else {
                                synchronized(activeDead) { activeDead.add(bare) }
                            }
                        }
                    }
                    chunkResults.forEach { it.join() }
                }
            }

            // Second pass for line-by-line filter replacement
            val processedLines = mutableListOf<String>()
            for (line in lines) {
                val parsed = DomainCheckerEngine.parseLine(line)
                if (parsed == null) {
                    processedLines.add(line)
                    continue
                }
                if (parsed.domains.isEmpty()) {
                    processedLines.add(line)
                    continue
                }

                val results = parsed.domains.map { d ->
                    val bare = if (d.startsWith("~")) d.drop(1) else d
                    val isLiveDomain = synchronized(activeLive) { activeLive.contains(bare) }
                    val status = if (isLiveDomain) "live" else "dead"
                    Pair(d, status)
                }

                if (parsed.type == FilterType.NETWORK || parsed.type == FilterType.PLAIN) {
                    val alive = results.filter { it.second == "live" }
                    if (alive.isEmpty()) {
                        if (processedLines.isNotEmpty() && processedLines.last() != "! All Dead Kept One Backup") {
                            processedLines.add("! All Dead Kept One Backup")
                        }
                        processedLines.add(if (parsed.type == FilterType.NETWORK) line else parsed.domains.first())
                    } else {
                        processedLines.add(line)
                    }
                    continue
                }

                if (parsed.type == FilterType.NETWORK_DOMLIST) {
                    val liveSet = results.filter { it.second == "live" }.map { it.first }.toSet()
                    val newDomList = parsed.rawDomList.filter { entry ->
                        if (entry.startsWith("~")) true else liveSet.contains(entry)
                    }
                    val livePosCount = newDomList.filter { !it.startsWith("~") }.size
                    if (livePosCount == 0) {
                        if (processedLines.isNotEmpty() && processedLines.last() != "! All Dead Kept One Backup") {
                            processedLines.add("! All Dead Kept One Backup")
                        }
                        val backup = parsed.rawDomList.find { !it.startsWith("~") } ?: parsed.rawDomList.first()
                        processedLines.add(parsed.original.replace(Regex("(domain|from)=[^,\\s]+"), "${parsed.optionKey}=$backup"))
                    } else {
                        processedLines.add(parsed.original.replace(Regex("(domain|from)=[^,\\s]+"), "${parsed.optionKey}=${newDomList.joinToString("|")}"))
                    }
                    continue
                }

                // Cosmetic replacement
                val alive = results.filter { it.second == "live" }.map { it.first }
                if (alive.isEmpty()) {
                    if (processedLines.isNotEmpty() && processedLines.last() != "! All Dead Kept One Backup") {
                        processedLines.add("! All Dead Kept One Backup")
                    }
                    processedLines.add("${parsed.domains.first()}${parsed.sep}${parsed.ruleSuffix}")
                } else if (alive.size < parsed.domains.size) {
                    processedLines.add("${alive.joinToString(",")}${parsed.sep}${parsed.ruleSuffix}")
                } else {
                    processedLines.add(line)
                }
            }

            withContext(Dispatchers.Main) {
                _currentLivePool.value = activeLive.toList()
                _currentDeadPool.value = activeDead.toList()
                _processedFilter.value = processedLines.joinToString("\n")
                _analysisState.value = AnalysisState.Completed
                _inputLabel.value = "DONE"
            }
        }
    }
}
