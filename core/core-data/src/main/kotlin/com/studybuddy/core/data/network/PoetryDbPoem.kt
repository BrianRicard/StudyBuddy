package com.studybuddy.core.data.network

import kotlinx.serialization.Serializable

@Serializable
data class PoetryDbPoem(
    val title: String,
    val author: String,
    val lines: List<String>,
    val linecount: String,
)
