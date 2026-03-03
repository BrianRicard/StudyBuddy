package com.studybuddy.feature.dictee.add

import org.json.JSONArray
import org.json.JSONObject

object DicteeFileParser {

    fun parseTextFile(content: String): Result<List<String>> = runCatching {
        val words = content.lines()
            .map { it.trim() }
            .filter { it.isNotBlank() }
        require(words.isNotEmpty()) { "File contains no words" }
        words
    }

    fun parseCsvFile(content: String): Result<List<String>> = runCatching {
        val words = content.lines()
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .map { line -> line.split(",", ";").first().trim() }
            .filter { it.isNotBlank() }
        require(words.isNotEmpty()) { "CSV file contains no words" }
        words
    }

    fun parseJsonFile(content: String): Result<List<String>> = runCatching {
        val trimmed = content.trim()
        val words = if (trimmed.startsWith("[")) {
            parseJsonArray(JSONArray(trimmed))
        } else {
            val obj = JSONObject(trimmed)
            val arr = obj.getJSONArray("words")
            parseJsonArray(arr)
        }
        require(words.isNotEmpty()) { "JSON file contains no words" }
        words
    }

    private fun parseJsonArray(arr: JSONArray): List<String> = (0 until arr.length())
        .map { arr.getString(it).trim() }
        .filter { it.isNotBlank() }
}
