package com.example

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.net.InetAddress
import java.util.concurrent.TimeUnit

enum class FilterType {
    COSMETIC,
    NETWORK_DOMLIST,
    NETWORK,
    PLAIN
}

data class ParsedLine(
    val type: FilterType,
    val domains: List<String>,
    val skipped: List<String> = emptyList(),
    val original: String,
    // Used for cosmetic
    val sep: String = "",
    val ruleSuffix: String = "",
    // Used for network_domlist
    val rawDomList: List<String> = emptyList(),
    val optionKey: String = ""
)

class DomainCheckerEngine {

    companion object {
        private val HOSTNAME_RE = Regex("^~?[a-zA-Z0-9]([a-zA-Z0-9\\-]*[a-zA-Z0-9])?(\\.[a-zA-Z0-9]([a-zA-Z0-9\\-]*[a-zA-Z0-9])?)+\\.?$")
        private val COSMETIC_RE = Regex("^([^#]*)(##|#@#|#%#|#@%#)(.*)$")
        private val DOM_OPT_RE = Regex("(?:^|,|\\$)(domain|from)=([^,\\s]+)")
        private val NETWORK_RE = Regex("^\\|\\|([a-zA-Z0-9][a-zA-Z0-9\\-\\.]*[a-zA-Z0-9])[\\^\\$/]")

        fun isValidHostname(d: String): Boolean {
            val bare = if (d.startsWith("~")) d.drop(1) else d
            if (bare.isEmpty() || !bare.contains(".")) return false
            if (bare.endsWith(".")) return false
            return HOSTNAME_RE.matches(bare)
        }

        fun isWildcard(d: String): Boolean {
            return d.contains("*")
        }

        fun parseLine(line: String): ParsedLine? {
            val t = line.trim()
            if (t.isEmpty() || t.startsWith("!") || t.startsWith("[") || t.startsWith("#")) {
                return null
            }

            // Cosmetic filter
            val cosMatch = COSMETIC_RE.matchEntire(t)
            if (cosMatch != null) {
                val rawDomains = cosMatch.groupValues[1]
                val sep = cosMatch.groupValues[2]
                val ruleSuffix = cosMatch.groupValues[3]
                val allTokens = rawDomains.split(',').map { it.trim() }.filter { it.isNotEmpty() }
                val skipped = allTokens.filter { isWildcard(it) }
                val domains = allTokens.filter { !isWildcard(it) && isValidHostname(it) }
                if (domains.isEmpty() && skipped.isEmpty()) return null
                return ParsedLine(
                    type = FilterType.COSMETIC,
                    domains = domains,
                    skipped = skipped,
                    original = t,
                    sep = sep,
                    ruleSuffix = ruleSuffix
                )
            }

            // Network filter with domain= or from= opt
            val domOptMatch = DOM_OPT_RE.find(t)
            if (domOptMatch != null) {
                val optionKey = domOptMatch.groupValues[1]
                val rawDomList = domOptMatch.groupValues[2].split('|').map { it.trim() }.filter { it.isNotEmpty() }
                val checkable = rawDomList.filter { !it.startsWith("~") && isValidHostname(it) }
                if (checkable.isEmpty()) return null
                return ParsedLine(
                    type = FilterType.NETWORK_DOMLIST,
                    domains = checkable,
                    rawDomList = rawDomList,
                    optionKey = optionKey,
                    original = t
                )
            }

            // Standard network filter without domain= option
            val netMatch = NETWORK_RE.find(t)
            if (netMatch != null) {
                val hn = netMatch.groupValues[1]
                if (!isValidHostname(hn)) return null
                return ParsedLine(
                    type = FilterType.NETWORK,
                    domains = listOf(hn),
                    original = t
                )
            }

            // Skip exception rules and $-options without domain
            if (t.startsWith("@@") || t.contains("$")) return null

            // Plain domain list (comma or pipe separated)
            val allTokens = t.split(Regex("[|,]")).map { it.trim() }.filter { it.isNotEmpty() }
            val skipped = allTokens.filter { isWildcard(it) }
            val plainDomains = allTokens.filter { !isWildcard(it) && isValidHostname(it) }
            if (plainDomains.isNotEmpty() || skipped.isNotEmpty()) {
                return ParsedLine(
                    type = FilterType.PLAIN,
                    domains = plainDomains,
                    skipped = skipped,
                    original = t
                )
            }

            return null
        }
    }

    private val cache = mutableMapOf<String, String>()
    private val client = OkHttpClient.Builder()
        .connectTimeout(3500, TimeUnit.MILLISECONDS)
        .readTimeout(3500, TimeUnit.MILLISECONDS)
        .build()

    suspend fun validate(d: String): String = withContext(Dispatchers.IO) {
        val bare = if (d.startsWith("~")) d.drop(1) else d
        if (bare.isEmpty()) return@withContext "dead"
        
        synchronized(cache) {
            val cachedValue = cache[bare]
            if (cachedValue != null) return@withContext cachedValue
        }

        var isLive = false

        // 1. Try HTTP/S connection first (https then http)
        try {
            val request = Request.Builder()
                .url("https://$bare")
                .build()
            client.newCall(request).execute().use { response ->
                isLive = true
            }
        } catch (e: Exception) {
            try {
                val request = Request.Builder()
                    .url("http://$bare")
                    .build()
                client.newCall(request).execute().use { response ->
                    isLive = true
                }
            } catch (e2: Exception) {
                // Ignore failure
            }
        }

        // 2. DNS Resolution via Google DNS-over-HTTPS (DoH) fallback if HTTP connect failed
        // This is safe from client ISP/carrier DNS hijacking.
        if (!isLive) {
            try {
                val request = Request.Builder()
                    .url("https://dns.google/resolve?name=$bare&type=A")
                    .build()
                client.newCall(request).execute().use { response ->
                    if (response.isSuccessful) {
                        val body = response.body?.string()
                        if (body != null && body.contains("\"Answer\"") && body.contains("\"Status\":0")) {
                            isLive = true
                        }
                    }
                }
            } catch (e: Exception) {
                // Ignore failure
            }
        }

        // 3. Last fallback: Native DNS lookup ONLY if both Web and DoH checks fail or have no connectivity
        if (!isLive) {
            try {
                // We only do this if there's active resolution to protect against typical lookups, 
                // but since carrier DNS hijacking could return fake hits, we keep it as a ultra-last fallback
                // or we can completely bypass it to match the HTML code exactly. Let's omit it to be 100% identical.
            } catch (e: Exception) {
                // Ignore
            }
        }

        val status = if (isLive) "live" else "dead"
        synchronized(cache) {
            cache[bare] = status
        }
        status
    }

    fun clearCache() {
        synchronized(cache) {
            cache.clear()
        }
    }
}
