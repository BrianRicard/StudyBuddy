package com.studybuddy.core.domain.model

data class RewardItem(
    val id: String,
    val category: RewardCategory,
    val name: String,
    val icon: String,
    val cost: Int,
    val tier: AvatarTier = AvatarTier.STARTER,
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

enum class AvatarTier(val label: String, val minCost: Int, val maxCost: Int) {
    STARTER("Starter", 0, 25),
    COMMON("Common", 30, 75),
    RARE("Rare", 100, 200),
    EPIC("Epic", 300, 500),
    LEGENDARY("Legendary", 750, 1500),
}
