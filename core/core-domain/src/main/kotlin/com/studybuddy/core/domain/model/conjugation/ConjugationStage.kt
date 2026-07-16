package com.studybuddy.core.domain.model.conjugation

/**
 * The activities inside a stage, in the order the child plays them.
 */
enum class ConjugationStep {
    LEARN,
    WRITE,
    SPEAK,
    BATTLE,
    BOSS,
}

/**
 * One stop on the Verb Quest path.
 *
 * Story text (titles, narration) lives in string resources keyed by [order];
 * the domain model only carries what the game logic needs.
 *
 * @property friendCharacterId Creature the child cheers up in the battle step.
 * @property bossCharacterId Creature that raps in the boss step.
 */
data class ConjugationStage(
    val order: Int,
    val verb: ConjugationVerb,
    val friendCharacterId: String,
    val bossCharacterId: String,
) {
    val id: String get() = verb.id
}

object ConjugationStages {

    val all: List<ConjugationStage> = listOf(
        ConjugationStage(order = 1, verb = FrenchVerbs.ETRE, friendCharacterId = "bunny", bossCharacterId = "frog"),
        ConjugationStage(order = 2, verb = FrenchVerbs.AVOIR, friendCharacterId = "squirrel", bossCharacterId = "owl"),
        ConjugationStage(order = 3, verb = FrenchVerbs.AIMER, friendCharacterId = "cat", bossCharacterId = "snail"),
        ConjugationStage(order = 4, verb = FrenchVerbs.ALLER, friendCharacterId = "dog", bossCharacterId = "shark"),
        ConjugationStage(order = 5, verb = FrenchVerbs.FAIRE, friendCharacterId = "panda", bossCharacterId = "dragon"),
        ConjugationStage(order = 6, verb = FrenchVerbs.DIRE, friendCharacterId = "ladybug", bossCharacterId = "lion"),
    )

    fun byId(stageId: String): ConjugationStage? = all.firstOrNull { it.id == stageId }
}
