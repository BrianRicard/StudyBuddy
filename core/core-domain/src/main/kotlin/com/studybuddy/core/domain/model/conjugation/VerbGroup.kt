package com.studybuddy.core.domain.model.conjugation

/**
 * French school classification of verbs, used to organise the Atelier grid.
 *
 * @property displayName French label as taught in école élémentaire.
 */
enum class VerbGroup(val displayName: String) {
    AUXILIAIRE("auxiliaires"),
    PREMIER_GROUPE("1er groupe"),
    DEUXIEME_GROUPE("2e groupe"),
    TROISIEME_GROUPE("3e groupe"),
}
