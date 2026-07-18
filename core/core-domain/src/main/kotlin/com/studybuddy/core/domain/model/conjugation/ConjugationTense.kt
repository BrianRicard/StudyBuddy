package com.studybuddy.core.domain.model.conjugation

/**
 * The tenses available for conjugation practice.
 *
 * Enum names are a persistence contract: Atelier review rows store them as
 * strings — never rename an entry.
 *
 * @property displayName French label shown to the child (the verb content is
 * French only; the UI around it is localized).
 */
enum class ConjugationTense(val displayName: String) {
    PRESENT("présent"),
    FUTUR("futur"),
    IMPARFAIT("imparfait"),
}
