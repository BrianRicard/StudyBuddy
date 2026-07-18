package com.studybuddy.core.domain.model.conjugation

import com.studybuddy.core.domain.model.conjugation.ConjugationTense.FUTUR
import com.studybuddy.core.domain.model.conjugation.ConjugationTense.IMPARFAIT
import com.studybuddy.core.domain.model.conjugation.ConjugationTense.PRESENT

/**
 * Builds a person→form map from six space-separated forms in canonical order:
 * je, tu, il/elle, nous, vous, ils/elles.
 */
private fun conjugation(forms: String): Map<ConjugationPerson, String> {
    val parts = forms.split(' ')
    require(parts.size == 6) { "expected 6 forms, got ${parts.size}: \"$forms\"" }
    return ConjugationPerson.entries.zip(parts).toMap()
}

/**
 * Conjugation tables for the Verb Quest and the Atelier des Verbes.
 *
 * The verb content itself is French only (the app UI around it is localized).
 * Boss sentences are story-flavoured (Verb Quest verbs only): the child is on
 * a quest to bring back the Grumpy King's smile.
 */
object FrenchVerbs {

    val ETRE = ConjugationVerb(
        id = "etre",
        infinitive = "être",
        group = VerbGroup.AUXILIAIRE,
        tenses = mapOf(
            PRESENT to conjugation("suis es est sommes êtes sont"),
            FUTUR to conjugation("serai seras sera serons serez seront"),
            IMPARFAIT to conjugation("étais étais était étions étiez étaient"),
        ),
        bossSentences = listOf(
            "Je suis ton ami",
            "Nous sommes des héros",
            "Tu es vraiment super",
        ),
    )

    val AVOIR = ConjugationVerb(
        id = "avoir",
        infinitive = "avoir",
        group = VerbGroup.AUXILIAIRE,
        tenses = mapOf(
            PRESENT to conjugation("ai as a avons avez ont"),
            FUTUR to conjugation("aurai auras aura aurons aurez auront"),
            IMPARFAIT to conjugation("avais avais avait avions aviez avaient"),
        ),
        bossSentences = listOf(
            "J'ai un grand sourire",
            "Nous avons du courage",
            "Ils ont des étoiles",
        ),
    )

    val AIMER = ConjugationVerb(
        id = "aimer",
        infinitive = "aimer",
        group = VerbGroup.PREMIER_GROUPE,
        tenses = mapOf(
            PRESENT to conjugation("aime aimes aime aimons aimez aiment"),
            FUTUR to conjugation("aimerai aimeras aimera aimerons aimerez aimeront"),
            IMPARFAIT to conjugation("aimais aimais aimait aimions aimiez aimaient"),
        ),
        bossSentences = listOf(
            "J'aime chanter avec toi",
            "Nous aimons les fleurs",
            "Vous aimez la musique",
        ),
    )

    val ALLER = ConjugationVerb(
        id = "aller",
        infinitive = "aller",
        group = VerbGroup.TROISIEME_GROUPE,
        tenses = mapOf(
            PRESENT to conjugation("vais vas va allons allez vont"),
            FUTUR to conjugation("irai iras ira irons irez iront"),
            IMPARFAIT to conjugation("allais allais allait allions alliez allaient"),
        ),
        bossSentences = listOf(
            "Je vais au château",
            "Nous allons très vite",
            "Tu vas gagner",
        ),
    )

    val FAIRE = ConjugationVerb(
        id = "faire",
        infinitive = "faire",
        group = VerbGroup.TROISIEME_GROUPE,
        tenses = mapOf(
            PRESENT to conjugation("fais fais fait faisons faites font"),
            FUTUR to conjugation("ferai feras fera ferons ferez feront"),
            IMPARFAIT to conjugation("faisais faisais faisait faisions faisiez faisaient"),
        ),
        bossSentences = listOf(
            "Je fais de la magie",
            "Nous faisons la fête",
            "Vous faites un gâteau",
        ),
    )

    val DIRE = ConjugationVerb(
        id = "dire",
        infinitive = "dire",
        group = VerbGroup.TROISIEME_GROUPE,
        tenses = mapOf(
            PRESENT to conjugation("dis dis dit disons dites disent"),
            FUTUR to conjugation("dirai diras dira dirons direz diront"),
            IMPARFAIT to conjugation("disais disais disait disions disiez disaient"),
        ),
        bossSentences = listOf(
            "Je dis que tu es super",
            "Nous disons bonjour au roi",
            "Vous dites des mots magiques",
        ),
    )

    val CHANTER = ConjugationVerb(
        id = "chanter",
        infinitive = "chanter",
        group = VerbGroup.PREMIER_GROUPE,
        tenses = mapOf(
            PRESENT to conjugation("chante chantes chante chantons chantez chantent"),
            FUTUR to conjugation("chanterai chanteras chantera chanterons chanterez chanteront"),
            IMPARFAIT to conjugation("chantais chantais chantait chantions chantiez chantaient"),
        ),
    )

    val JOUER = ConjugationVerb(
        id = "jouer",
        infinitive = "jouer",
        group = VerbGroup.PREMIER_GROUPE,
        tenses = mapOf(
            PRESENT to conjugation("joue joues joue jouons jouez jouent"),
            FUTUR to conjugation("jouerai joueras jouera jouerons jouerez joueront"),
            IMPARFAIT to conjugation("jouais jouais jouait jouions jouiez jouaient"),
        ),
    )

    val MANGER = ConjugationVerb(
        id = "manger",
        infinitive = "manger",
        group = VerbGroup.PREMIER_GROUPE,
        tenses = mapOf(
            PRESENT to conjugation("mange manges mange mangeons mangez mangent"),
            FUTUR to conjugation("mangerai mangeras mangera mangerons mangerez mangeront"),
            IMPARFAIT to conjugation("mangeais mangeais mangeait mangions mangiez mangeaient"),
        ),
    )

    val DONNER = ConjugationVerb(
        id = "donner",
        infinitive = "donner",
        group = VerbGroup.PREMIER_GROUPE,
        tenses = mapOf(
            PRESENT to conjugation("donne donnes donne donnons donnez donnent"),
            FUTUR to conjugation("donnerai donneras donnera donnerons donnerez donneront"),
            IMPARFAIT to conjugation("donnais donnais donnait donnions donniez donnaient"),
        ),
    )

    val REGARDER = ConjugationVerb(
        id = "regarder",
        infinitive = "regarder",
        group = VerbGroup.PREMIER_GROUPE,
        tenses = mapOf(
            PRESENT to conjugation("regarde regardes regarde regardons regardez regardent"),
            FUTUR to conjugation("regarderai regarderas regardera regarderons regarderez regarderont"),
            IMPARFAIT to conjugation("regardais regardais regardait regardions regardiez regardaient"),
        ),
    )

    val ECOUTER = ConjugationVerb(
        id = "ecouter",
        infinitive = "écouter",
        group = VerbGroup.PREMIER_GROUPE,
        tenses = mapOf(
            PRESENT to conjugation("écoute écoutes écoute écoutons écoutez écoutent"),
            FUTUR to conjugation("écouterai écouteras écoutera écouterons écouterez écouteront"),
            IMPARFAIT to conjugation("écoutais écoutais écoutait écoutions écoutiez écoutaient"),
        ),
    )

    val FINIR = ConjugationVerb(
        id = "finir",
        infinitive = "finir",
        group = VerbGroup.DEUXIEME_GROUPE,
        tenses = mapOf(
            PRESENT to conjugation("finis finis finit finissons finissez finissent"),
            FUTUR to conjugation("finirai finiras finira finirons finirez finiront"),
            IMPARFAIT to conjugation("finissais finissais finissait finissions finissiez finissaient"),
        ),
    )

    val CHOISIR = ConjugationVerb(
        id = "choisir",
        infinitive = "choisir",
        group = VerbGroup.DEUXIEME_GROUPE,
        tenses = mapOf(
            PRESENT to conjugation("choisis choisis choisit choisissons choisissez choisissent"),
            FUTUR to conjugation("choisirai choisiras choisira choisirons choisirez choisiront"),
            IMPARFAIT to conjugation("choisissais choisissais choisissait choisissions choisissiez choisissaient"),
        ),
    )

    val GRANDIR = ConjugationVerb(
        id = "grandir",
        infinitive = "grandir",
        group = VerbGroup.DEUXIEME_GROUPE,
        tenses = mapOf(
            PRESENT to conjugation("grandis grandis grandit grandissons grandissez grandissent"),
            FUTUR to conjugation("grandirai grandiras grandira grandirons grandirez grandiront"),
            IMPARFAIT to conjugation("grandissais grandissais grandissait grandissions grandissiez grandissaient"),
        ),
    )

    val VENIR = ConjugationVerb(
        id = "venir",
        infinitive = "venir",
        group = VerbGroup.TROISIEME_GROUPE,
        tenses = mapOf(
            PRESENT to conjugation("viens viens vient venons venez viennent"),
            FUTUR to conjugation("viendrai viendras viendra viendrons viendrez viendront"),
            IMPARFAIT to conjugation("venais venais venait venions veniez venaient"),
        ),
    )

    val PRENDRE = ConjugationVerb(
        id = "prendre",
        infinitive = "prendre",
        group = VerbGroup.TROISIEME_GROUPE,
        tenses = mapOf(
            PRESENT to conjugation("prends prends prend prenons prenez prennent"),
            FUTUR to conjugation("prendrai prendras prendra prendrons prendrez prendront"),
            IMPARFAIT to conjugation("prenais prenais prenait prenions preniez prenaient"),
        ),
    )

    val VOIR = ConjugationVerb(
        id = "voir",
        infinitive = "voir",
        group = VerbGroup.TROISIEME_GROUPE,
        tenses = mapOf(
            PRESENT to conjugation("vois vois voit voyons voyez voient"),
            FUTUR to conjugation("verrai verras verra verrons verrez verront"),
            IMPARFAIT to conjugation("voyais voyais voyait voyions voyiez voyaient"),
        ),
    )

    val VOULOIR = ConjugationVerb(
        id = "vouloir",
        infinitive = "vouloir",
        group = VerbGroup.TROISIEME_GROUPE,
        tenses = mapOf(
            PRESENT to conjugation("veux veux veut voulons voulez veulent"),
            FUTUR to conjugation("voudrai voudras voudra voudrons voudrez voudront"),
            IMPARFAIT to conjugation("voulais voulais voulait voulions vouliez voulaient"),
        ),
    )

    val POUVOIR = ConjugationVerb(
        id = "pouvoir",
        infinitive = "pouvoir",
        group = VerbGroup.TROISIEME_GROUPE,
        tenses = mapOf(
            PRESENT to conjugation("peux peux peut pouvons pouvez peuvent"),
            FUTUR to conjugation("pourrai pourras pourra pourrons pourrez pourront"),
            IMPARFAIT to conjugation("pouvais pouvais pouvait pouvions pouviez pouvaient"),
        ),
    )

    val PARTIR = ConjugationVerb(
        id = "partir",
        infinitive = "partir",
        group = VerbGroup.TROISIEME_GROUPE,
        tenses = mapOf(
            PRESENT to conjugation("pars pars part partons partez partent"),
            FUTUR to conjugation("partirai partiras partira partirons partirez partiront"),
            IMPARFAIT to conjugation("partais partais partait partions partiez partaient"),
        ),
    )

    val METTRE = ConjugationVerb(
        id = "mettre",
        infinitive = "mettre",
        group = VerbGroup.TROISIEME_GROUPE,
        tenses = mapOf(
            PRESENT to conjugation("mets mets met mettons mettez mettent"),
            FUTUR to conjugation("mettrai mettras mettra mettrons mettrez mettront"),
            IMPARFAIT to conjugation("mettais mettais mettait mettions mettiez mettaient"),
        ),
    )

    val DORMIR = ConjugationVerb(
        id = "dormir",
        infinitive = "dormir",
        group = VerbGroup.TROISIEME_GROUPE,
        tenses = mapOf(
            PRESENT to conjugation("dors dors dort dormons dormez dorment"),
            FUTUR to conjugation("dormirai dormiras dormira dormirons dormirez dormiront"),
            IMPARFAIT to conjugation("dormais dormais dormait dormions dormiez dormaient"),
        ),
    )

    val LIRE = ConjugationVerb(
        id = "lire",
        infinitive = "lire",
        group = VerbGroup.TROISIEME_GROUPE,
        tenses = mapOf(
            PRESENT to conjugation("lis lis lit lisons lisez lisent"),
            FUTUR to conjugation("lirai liras lira lirons lirez liront"),
            IMPARFAIT to conjugation("lisais lisais lisait lisions lisiez lisaient"),
        ),
    )

    /** The six Verb Quest verbs in teaching order (easiest and most common first). */
    val questVerbs: List<ConjugationVerb> = listOf(ETRE, AVOIR, AIMER, ALLER, FAIRE, DIRE)

    /** Every verb available in the Atelier, ordered by group then difficulty. */
    val all: List<ConjugationVerb> = listOf(
        ETRE, AVOIR,
        AIMER, CHANTER, JOUER, MANGER, DONNER, REGARDER, ECOUTER,
        FINIR, CHOISIR, GRANDIR,
        ALLER, FAIRE, DIRE, VENIR, PRENDRE, VOIR, VOULOIR, POUVOIR, PARTIR, METTRE, DORMIR, LIRE,
    )

    private val byId = all.associateBy { it.id }

    fun byId(id: String): ConjugationVerb? = byId[id]
}
