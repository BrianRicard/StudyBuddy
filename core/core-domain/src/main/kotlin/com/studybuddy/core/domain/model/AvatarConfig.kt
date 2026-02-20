package com.studybuddy.core.domain.model

data class AvatarConfig(
    val bodyId: String,
    val hatId: String,
    val faceId: String,
    val outfitId: String,
    val petId: String,
    val equippedTitle: String? = null,
) {
    companion object {
        fun default() = AvatarConfig(
            bodyId = "fox",
            hatId = "none",
            faceId = "none",
            outfitId = "default",
            petId = "none",
        )
    }
}
