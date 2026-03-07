package com.studybuddy.feature.reading.questions

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.studybuddy.core.domain.model.ReadingQuestion
import com.studybuddy.core.domain.model.ReadingQuestionType
import com.studybuddy.core.ui.R as CoreUiR
import com.studybuddy.feature.reading.R

private val CorrectColor = Color(0xFF4CAF50)
private val IncorrectColor = Color(0xFFE53935)

@Composable
fun QuestionsScreen(
    onNavigateBack: () -> Unit,
    onNavigateToResults: (String, Int, Int, Long, Long, Boolean, Int) -> Unit,
    viewModel: QuestionsViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is QuestionsEffect.NavigateToResults -> onNavigateToResults(
                    effect.passageId,
                    effect.score,
                    effect.totalQuestions,
                    effect.readingTimeMs,
                    effect.questionsTimeMs,
                    effect.allCorrectFirstTry,
                    effect.tier,
                )
            }
        }
    }

    QuestionsContent(
        state = state,
        onIntent = viewModel::onIntent,
        onNavigateBack = onNavigateBack,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun QuestionsContent(
    state: QuestionsState,
    onIntent: (QuestionsIntent) -> Unit,
    onNavigateBack: () -> Unit,
) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        stringResource(
                            R.string.reading_question_progress,
                            state.currentQuestionIndex + 1,
                            state.totalQuestions,
                        ),
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(CoreUiR.string.navigate_back),
                        )
                    }
                },
            )
        },
    ) { padding ->
        if (state.isLoading || state.passage == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
        ) {
            // Progress bar
            LinearProgressIndicator(
                progress = {
                    (state.currentQuestionIndex + 1).toFloat() / state.totalQuestions.coerceAtLeast(1)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
            )

            // Collapsible passage
            PassageSection(
                passageText = state.passage.passage,
                isExpanded = state.showPassageExpanded,
                forceExpanded = state.currentQuestion?.type == ReadingQuestionType.FIND_IN_TEXT,
                onToggle = { onIntent(QuestionsIntent.TogglePassage) },
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Question card
            val question = state.currentQuestion
            if (question != null) {
                QuestionCard(
                    question = question,
                    selectedAnswer = state.selectedAnswer,
                    isRevealed = state.isAnswerRevealed,
                    passageText = state.passage.passage,
                    onSelectAnswer = { onIntent(QuestionsIntent.SelectAnswer(it)) },
                    onConfirm = { onIntent(QuestionsIntent.ConfirmAnswer) },
                )
            }

            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

@Composable
private fun PassageSection(
    passageText: String,
    isExpanded: Boolean,
    forceExpanded: Boolean,
    onToggle: () -> Unit,
) {
    val showExpanded = isExpanded || forceExpanded

    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onToggle,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        ),
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    text = stringResource(R.string.reading_tap_to_reread),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f),
                )
                Icon(
                    imageVector = if (showExpanded) {
                        Icons.Default.ExpandLess
                    } else {
                        Icons.Default.ExpandMore
                    },
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                )
            }

            AnimatedVisibility(visible = showExpanded) {
                Text(
                    text = passageText,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 8.dp),
                )
            }
        }
    }
}

@Composable
private fun QuestionCard(
    question: ReadingQuestion,
    selectedAnswer: String?,
    isRevealed: Boolean,
    passageText: String,
    onSelectAnswer: (String) -> Unit,
    onConfirm: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = question.questionText,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Medium,
            )

            Spacer(modifier = Modifier.height(16.dp))

            when (question.type) {
                ReadingQuestionType.MULTIPLE_CHOICE -> {
                    MultipleChoiceOptions(
                        options = question.options ?: emptyList(),
                        selectedAnswer = selectedAnswer,
                        correctAnswer = question.correctAnswer,
                        isRevealed = isRevealed,
                        onSelect = onSelectAnswer,
                    )
                }
                ReadingQuestionType.TRUE_FALSE -> {
                    TrueFalseOptions(
                        selectedAnswer = selectedAnswer,
                        correctAnswer = question.correctAnswer,
                        isRevealed = isRevealed,
                        onSelect = onSelectAnswer,
                    )
                }
                ReadingQuestionType.FIND_IN_TEXT -> {
                    FindInTextOptions(
                        passageText = passageText,
                        selectedAnswer = selectedAnswer,
                        correctAnswer = question.correctAnswer,
                        isRevealed = isRevealed,
                        onSelect = onSelectAnswer,
                    )
                }
            }

            if (isRevealed) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = question.explanation,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            if (!isRevealed && selectedAnswer != null) {
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = onConfirm,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(stringResource(R.string.reading_confirm))
                }
            }
        }
    }
}

@Composable
private fun MultipleChoiceOptions(
    options: List<String>,
    selectedAnswer: String?,
    correctAnswer: String,
    isRevealed: Boolean,
    onSelect: (String) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        options.forEachIndexed { index, option ->
            val indexStr = index.toString()
            val isSelected = selectedAnswer == indexStr
            val isCorrect = correctAnswer == indexStr

            val borderColor by animateColorAsState(
                targetValue = when {
                    isRevealed && isCorrect -> CorrectColor
                    isRevealed && isSelected && !isCorrect -> IncorrectColor
                    isSelected -> MaterialTheme.colorScheme.primary
                    else -> MaterialTheme.colorScheme.outline
                },
                label = "optionBorder",
            )

            val containerColor by animateColorAsState(
                targetValue = when {
                    isRevealed && isCorrect -> CorrectColor.copy(alpha = 0.1f)
                    isRevealed && isSelected && !isCorrect -> IncorrectColor.copy(alpha = 0.1f)
                    isSelected -> MaterialTheme.colorScheme.primaryContainer
                    else -> MaterialTheme.colorScheme.surface
                },
                label = "optionContainer",
            )

            OutlinedCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(enabled = !isRevealed) { onSelect(indexStr) },
                border = BorderStroke(2.dp, borderColor),
                colors = CardDefaults.outlinedCardColors(containerColor = containerColor),
            ) {
                Text(
                    text = option,
                    modifier = Modifier.padding(12.dp),
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }
    }
}

@Composable
private fun TrueFalseOptions(
    selectedAnswer: String?,
    correctAnswer: String,
    isRevealed: Boolean,
    onSelect: (String) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        listOf(
            "true" to stringResource(R.string.reading_true),
            "false" to stringResource(R.string.reading_false),
        ).forEach { (value, label) ->
            val isSelected = selectedAnswer == value
            val isCorrect = correctAnswer == value

            val borderColor by animateColorAsState(
                targetValue = when {
                    isRevealed && isCorrect -> CorrectColor
                    isRevealed && isSelected && !isCorrect -> IncorrectColor
                    isSelected -> MaterialTheme.colorScheme.primary
                    else -> MaterialTheme.colorScheme.outline
                },
                label = "tfBorder",
            )

            val containerColor by animateColorAsState(
                targetValue = when {
                    isRevealed && isCorrect -> CorrectColor.copy(alpha = 0.1f)
                    isRevealed && isSelected && !isCorrect -> IncorrectColor.copy(alpha = 0.1f)
                    isSelected -> MaterialTheme.colorScheme.primaryContainer
                    else -> MaterialTheme.colorScheme.surface
                },
                label = "tfContainer",
            )

            OutlinedCard(
                modifier = Modifier
                    .weight(1f)
                    .clickable(enabled = !isRevealed) { onSelect(value) },
                border = BorderStroke(2.dp, borderColor),
                colors = CardDefaults.outlinedCardColors(containerColor = containerColor),
            ) {
                Text(
                    text = label,
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth(),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                )
            }
        }
    }
}

@Composable
private fun FindInTextOptions(
    passageText: String,
    selectedAnswer: String?,
    correctAnswer: String,
    isRevealed: Boolean,
    onSelect: (String) -> Unit,
) {
    val sentences = com.studybuddy.feature.reading.detail.ReadingDetailViewModel.splitSentences(passageText)

    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        sentences.forEachIndexed { index, sentence ->
            val indexStr = index.toString()
            val isSelected = selectedAnswer == indexStr
            val isCorrect = correctAnswer == indexStr

            val bgColor by animateColorAsState(
                targetValue = when {
                    isRevealed && isCorrect -> CorrectColor.copy(alpha = 0.2f)
                    isRevealed && isSelected && !isCorrect -> IncorrectColor.copy(alpha = 0.2f)
                    isSelected -> MaterialTheme.colorScheme.primaryContainer
                    else -> Color.Transparent
                },
                label = "fitBg",
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(enabled = !isRevealed) { onSelect(indexStr) }
                    .then(
                        Modifier
                            .background(bgColor, RoundedCornerShape(4.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                    ),
            ) {
                Text(
                    text = sentence,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }
    }
}
