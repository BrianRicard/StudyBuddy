package com.studybuddy.core.domain.model

data class RewardItem(
    val id: String,
    val category: RewardCategory,
    val name: String,
    val icon: String,
    val cost: Int,
    val description: String? = null,
)

enum class RewardCategory {
    CHARACTER,
    HAT,
    FACE,
    OUTFIT,
    PET,
    THEME,
    EFFECT,
    SOUND,
    TITLE,
}
