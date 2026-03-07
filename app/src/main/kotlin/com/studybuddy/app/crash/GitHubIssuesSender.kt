package com.studybuddy.app.crash

import android.content.Context
import android.os.Build
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import org.acra.ReportField
import org.acra.data.CrashReportData
import org.acra.sender.ReportSender
import org.acra.sender.ReportSenderException
import org.json.JSONArray
import org.json.JSONObject

class GitHubIssuesSender(
    @Suppress("UNUSED_PARAMETER") context: Context,
    private val repoOwner: String,
    private val repoName: String,
    private val token: String,
) : ReportSender {

    private val baseUrl = "https://api.github.com/repos/$repoOwner/$repoName"

    override fun send(
        context: Context,
        errorContent: CrashReportData,
    ) {
        val stackTrace = errorContent.getString(ReportField.STACK_TRACE) ?: "No stacktrace"
        val fingerprint = CrashFingerprint.generate(stackTrace)
        val fingerprintLabel = "fingerprint:$fingerprint"

        val body = buildReportBody(errorContent, fingerprint)

        val existingIssueNumber = findExistingIssue(fingerprintLabel)

        if (existingIssueNumber != null) {
            addComment(existingIssueNumber, body)
            reopenIssueIfClosed(existingIssueNumber)
        } else {
            val exceptionLine = stackTrace.lines().firstOrNull()?.trim() ?: "Unknown crash"
            val title = "\uD83D\uDCA5 Crash: ${exceptionLine.take(120)}"
            createIssue(title, body, listOf("bug", "crash-report", fingerprintLabel))
        }
    }

    private fun buildReportBody(
        data: CrashReportData,
        fingerprint: String,
    ): String {
        val versionName = data.getString(ReportField.APP_VERSION_NAME) ?: "?"
        val versionCode = data.getString(ReportField.APP_VERSION_CODE) ?: "?"
        val androidVersion = data.getString(ReportField.ANDROID_VERSION) ?: Build.VERSION.RELEASE
        val phoneModel = data.getString(ReportField.PHONE_MODEL) ?: Build.MODEL
        val brand = data.getString(ReportField.BRAND) ?: Build.BRAND
        val stackTrace = data.getString(ReportField.STACK_TRACE) ?: "N/A"
        val availMemSize = data.getString(ReportField.AVAILABLE_MEM_SIZE) ?: "?"
        val totalMemSize = data.getString(ReportField.TOTAL_MEM_SIZE) ?: "?"
        val userComment = data.getString(ReportField.USER_COMMENT) ?: ""
        val logcat = data.getString(ReportField.LOGCAT)?.take(3000) ?: ""
        val customData = data.getString(ReportField.CUSTOM_DATA) ?: ""
        val crashDate = data.getString(ReportField.USER_CRASH_DATE) ?: "?"

        return buildString {
            appendLine("## Crash Report")
            appendLine()
            appendLine("| Field | Value |")
            appendLine("|-------|-------|")
            appendLine("| **App Version** | $versionName ($versionCode) |")
            appendLine("| **Android** | $androidVersion |")
            appendLine("| **Device** | $brand $phoneModel |")
            appendLine("| **RAM** | $availMemSize / $totalMemSize |")
            appendLine("| **Date** | $crashDate |")
            appendLine("| **Fingerprint** | `$fingerprint` |")
            appendLine()

            if (userComment.isNotBlank()) {
                appendLine("### User Comment")
                appendLine(userComment)
                appendLine()
            }

            if (customData.isNotBlank()) {
                appendLine("### Custom Data")
                appendLine("```")
                appendLine(customData)
                appendLine("```")
                appendLine()
            }

            appendLine("### Stacktrace")
            appendLine("```java")
            appendLine(stackTrace.take(5000))
            appendLine("```")
            appendLine()

            if (logcat.isNotBlank()) {
                appendLine("<details><summary>Logcat (last lines)</summary>")
                appendLine()
                appendLine("```")
                appendLine(logcat)
                appendLine("```")
                appendLine()
                appendLine("</details>")
            }
        }
    }

    private fun findExistingIssue(fingerprintLabel: String): Int? {
        val searchUrl = "https://api.github.com/search/issues" +
            "?q=repo:$repoOwner/$repoName+label:\"$fingerprintLabel\"+is:issue" +
            "&per_page=1"

        val response = httpGet(searchUrl)
        val json = JSONObject(response)
        val items = json.optJSONArray("items")
        return if (items != null && items.length() > 0) {
            items.getJSONObject(0).getInt("number")
        } else {
            null
        }
    }

    private fun createIssue(
        title: String,
        body: String,
        labels: List<String>,
    ) {
        val json = JSONObject().apply {
            put("title", title)
            put("body", body)
            put("labels", JSONArray(labels))
        }
        httpPost("$baseUrl/issues", json.toString())
    }

    private fun addComment(
        issueNumber: Int,
        body: String,
    ) {
        val json = JSONObject().apply {
            put("body", "## Additional Occurrence\n\n$body")
        }
        httpPost("$baseUrl/issues/$issueNumber/comments", json.toString())
    }

    private fun reopenIssueIfClosed(issueNumber: Int) {
        val json = JSONObject().apply {
            put("state", "open")
        }
        httpPatch("$baseUrl/issues/$issueNumber", json.toString())
    }

    private fun httpGet(url: String): String {
        val conn = (URL(url).openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            setRequestProperty("Authorization", "Bearer $token")
            setRequestProperty("Accept", "application/vnd.github.v3+json")
            setRequestProperty("User-Agent", "StudyBuddy-CrashReporter")
            connectTimeout = 10_000
            readTimeout = 10_000
        }
        return try {
            conn.inputStream.bufferedReader().readText()
        } finally {
            conn.disconnect()
        }
    }

    private fun httpPost(
        url: String,
        body: String,
    ): String {
        val conn = (URL(url).openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"
            setRequestProperty("Authorization", "Bearer $token")
            setRequestProperty("Accept", "application/vnd.github.v3+json")
            setRequestProperty("Content-Type", "application/json")
            setRequestProperty("User-Agent", "StudyBuddy-CrashReporter")
            connectTimeout = 10_000
            readTimeout = 10_000
            doOutput = true
        }
        return try {
            OutputStreamWriter(conn.outputStream).use { it.write(body) }
            if (conn.responseCode in 200..299) {
                conn.inputStream.bufferedReader().readText()
            } else {
                val errorBody = conn.errorStream?.bufferedReader()?.readText() ?: ""
                throw ReportSenderException(
                    "GitHub API error ${conn.responseCode}: $errorBody",
                )
            }
        } finally {
            conn.disconnect()
        }
    }

    private fun httpPatch(
        url: String,
        body: String,
    ): String {
        val conn = (URL(url).openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"
            setRequestProperty("X-HTTP-Method-Override", "PATCH")
            setRequestProperty("Authorization", "Bearer $token")
            setRequestProperty("Accept", "application/vnd.github.v3+json")
            setRequestProperty("Content-Type", "application/json")
            setRequestProperty("User-Agent", "StudyBuddy-CrashReporter")
            connectTimeout = 10_000
            readTimeout = 10_000
            doOutput = true
        }
        return try {
            OutputStreamWriter(conn.outputStream).use { it.write(body) }
            if (conn.responseCode in 200..299) {
                conn.inputStream.bufferedReader().readText()
            } else {
                ""
            }
        } finally {
            conn.disconnect()
        }
    }
}
