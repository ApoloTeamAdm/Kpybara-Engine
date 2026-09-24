package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.Exercise
import com.example.domain.model.ExerciseType
import com.example.domain.model.PairItem
import com.example.ui.ActiveLessonSession
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LessonPlayerScreen(
    session: ActiveLessonSession,
    onClose: () -> Unit,
    onSpeak: (String) -> Unit,
    onSelectToken: (String) -> Unit,
    onDeselectToken: (String) -> Unit,
    onSelectOption: (Int) -> Unit,
    onMatchPair: (String, String) -> Unit,
    onUpdateTextInput: (String) -> Unit,
    onSubmitAnswer: () -> Unit,
    onNextExercise: () -> Unit
) {
    if (session.isFinished) {
        LessonFinishedDialog(session = session, onFinish = onClose)
        return
    }

    val currentEx = session.exercises.getOrNull(session.currentIndex) ?: return
    val progressRatio = (session.currentIndex + 1).toFloat() / session.exercises.size

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    LinearProgressIndicator(
                        progress = { progressRatio },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(10.dp)
                            .clip(RoundedCornerShape(5.dp)),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onClose,
                        modifier = Modifier.testTag("close_lesson_button")
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Sair da lição")
                    }
                },
                actions = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(end = 16.dp)
                    ) {
                        Icon(
                            Icons.Default.Favorite,
                            contentDescription = "Vidas",
                            tint = RubyHeart,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "5",
                            fontWeight = FontWeight.Bold,
                            color = RubyHeart
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            LessonBottomBar(
                session = session,
                onSubmit = onSubmitAnswer,
                onNext = onNextExercise
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp, vertical = 12.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Cabeçalho do Tipo de Exercício e Enunciado
            Text(
                text = getExerciseTypeTitle(currentEx.type),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = currentEx.prompt,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Renderizador específico por tipo de exercício
            when (currentEx.type) {
                ExerciseType.WORD_ORDERING -> {
                    WordOrderingExerciseView(
                        session = session,
                        onSelectToken = onSelectToken,
                        onDeselectToken = onDeselectToken
                    )
                }

                ExerciseType.MULTIPLE_CHOICE -> {
                    MultipleChoiceExerciseView(
                        exercise = currentEx,
                        selectedIndex = session.selectedOptionIndex,
                        onSelect = onSelectOption
                    )
                }

                ExerciseType.PAIR_MATCHING -> {
                    PairMatchingExerciseView(
                        pairs = currentEx.payload.pairs,
                        matchedPairs = session.matchedPairs,
                        onMatch = onMatchPair
                    )
                }

                ExerciseType.FILL_IN_BLANK -> {
                    FillInBlankExerciseView(
                        session = session,
                        exercise = currentEx,
                        onSelectToken = onSelectToken,
                        onDeselectToken = onDeselectToken
                    )
                }

                ExerciseType.LISTENING_COMPREHENSION -> {
                    ListeningExerciseView(
                        exercise = currentEx,
                        selectedIndex = session.selectedOptionIndex,
                        onSpeak = onSpeak,
                        onSelectOption = onSelectOption
                    )
                }

                ExerciseType.PRONUNCIATION_SPEECH -> {
                    PronunciationExerciseView(
                        exercise = currentEx,
                        onSpeak = onSpeak
                    )
                }
            }
        }
    }
}

// =============================================================================
// SUB-VIEWS DOS 6 TIPOS DE EXERCÍCIO
// =============================================================================

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun WordOrderingExerciseView(
    session: ActiveLessonSession,
    onSelectToken: (String) -> Unit,
    onDeselectToken: (String) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        // Área de Resposta Selecionada
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .defaultMinSize(minHeight = 110.dp)
                .clip(RoundedCornerShape(16.dp))
                .border(2.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(16.dp)),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
        ) {
            FlowRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (session.selectedTokens.isEmpty()) {
                    Text(
                        text = "Toque nas palavras abaixo para construir a frase...",
                        color = MaterialTheme.colorScheme.outline,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(8.dp)
                    )
                }
                session.selectedTokens.forEach { token ->
                    TokenChip(text = token, onClick = { onDeselectToken(token) }, isSelected = true)
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Banco de Palavras Disponíveis
        Text(
            text = "Banco de Palavras:",
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(10.dp))

        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            session.availableTokens.forEach { token ->
                TokenChip(text = token, onClick = { onSelectToken(token) }, isSelected = false)
            }
        }
    }
}

@Composable
fun TokenChip(text: String, onClick: () -> Unit, isSelected: Boolean) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant),
        shadowElevation = if (isSelected) 0.dp else 2.dp,
        modifier = Modifier
            .clickable { onClick() }
            .testTag("token_chip_$text")
    ) {
        Text(
            text = text,
            fontWeight = FontWeight.Medium,
            fontSize = 15.sp,
            color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
        )
    }
}

@Composable
fun MultipleChoiceExerciseView(
    exercise: Exercise,
    selectedIndex: Int?,
    onSelect: (Int) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        exercise.payload.options.forEachIndexed { index, option ->
            val isSelected = selectedIndex == index

            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
                ),
                border = BorderStroke(
                    if (isSelected) 2.dp else 1.dp,
                    if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onSelect(index) }
                    .testTag("mc_option_$index")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = isSelected,
                        onClick = { onSelect(index) }
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = option,
                        fontSize = 16.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

@Composable
fun PairMatchingExerciseView(
    pairs: List<PairItem>,
    matchedPairs: Map<String, String>,
    onMatch: (String, String) -> Unit
) {
    var selectedLeft by remember { mutableStateOf<String?>(null) }
    var selectedRight by remember { mutableStateOf<String?>(null) }

    val leftItems = remember(pairs) { pairs.map { it.left }.shuffled() }
    val rightItems = remember(pairs) { pairs.map { it.right }.shuffled() }

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Toque no termo em Esperanto e depois em sua tradução:",
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(14.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Coluna Esquerda
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                leftItems.forEach { left ->
                    val isMatched = matchedPairs.containsKey(left)
                    val isSelected = selectedLeft == left

                    PairCard(
                        text = left,
                        isSelected = isSelected,
                        isMatched = isMatched,
                        onClick = {
                            if (!isMatched) {
                                selectedLeft = left
                                selectedRight?.let { right ->
                                    // Verifica se o par está correto
                                    val isCorrect = pairs.any { it.left == left && it.right == right }
                                    if (isCorrect) onMatch(left, right)
                                    selectedLeft = null
                                    selectedRight = null
                                }
                            }
                        }
                    )
                }
            }

            // Coluna Direita
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                rightItems.forEach { right ->
                    val isMatched = matchedPairs.containsValue(right)
                    val isSelected = selectedRight == right

                    PairCard(
                        text = right,
                        isSelected = isSelected,
                        isMatched = isMatched,
                        onClick = {
                            if (!isMatched) {
                                selectedRight = right
                                selectedLeft?.let { left ->
                                    val isCorrect = pairs.any { it.left == left && it.right == right }
                                    if (isCorrect) onMatch(left, right)
                                    selectedLeft = null
                                    selectedRight = null
                                }
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun PairCard(text: String, isSelected: Boolean, isMatched: Boolean, onClick: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = when {
            isMatched -> CorrectGreenContainer
            isSelected -> MaterialTheme.colorScheme.primaryContainer
            else -> MaterialTheme.colorScheme.surface
        },
        border = BorderStroke(
            1.5.dp,
            when {
                isMatched -> CorrectGreen
                isSelected -> MaterialTheme.colorScheme.primary
                else -> MaterialTheme.colorScheme.outlineVariant
            }
        ),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = !isMatched) { onClick() }
    ) {
        Box(
            modifier = Modifier.padding(14.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                fontWeight = FontWeight.SemiBold,
                fontSize = 15.sp,
                textAlign = TextAlign.Center,
                color = when {
                    isMatched -> CorrectGreen
                    isSelected -> MaterialTheme.colorScheme.primary
                    else -> MaterialTheme.colorScheme.onSurface
                }
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FillInBlankExerciseView(
    session: ActiveLessonSession,
    exercise: Exercise,
    onSelectToken: (String) -> Unit,
    onDeselectToken: (String) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Frase: ",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                if (session.selectedTokens.isNotEmpty()) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.clickable { onDeselectToken(session.selectedTokens.first()) }
                    ) {
                        Text(
                            text = session.selectedTokens.first(),
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                } else {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.outlineVariant,
                        modifier = Modifier.padding(horizontal = 4.dp)
                    ) {
                        Text(
                            text = " [ _____ ] ",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }
                Text(
                    text = " tagon!",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "Escolha o termo correto:",
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(10.dp))

        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            session.availableTokens.forEach { token ->
                TokenChip(text = token, onClick = { onSelectToken(token) }, isSelected = false)
            }
        }
    }
}

@Composable
fun ListeningExerciseView(
    exercise: Exercise,
    selectedIndex: Int?,
    onSpeak: (String) -> Unit,
    onSelectOption: (Int) -> Unit
) {
    val audioText = exercise.payload.audio_text ?: exercise.payload.target_sentence ?: "Saluton"

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Botão de Áudio TTS Nativo
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                IconButton(
                    onClick = { onSpeak(audioText) },
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary)
                        .testTag("tts_play_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                        contentDescription = "Ouvir áudio",
                        tint = Color.White,
                        modifier = Modifier.size(32.dp)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "Toque para ouvir a pronúncia nativa",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Opções de Resposta
        exercise.payload.options.forEachIndexed { index, option ->
            val isSelected = selectedIndex == index
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
                ),
                border = BorderStroke(
                    if (isSelected) 2.dp else 1.dp,
                    if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .clickable { onSelectOption(index) }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(selected = isSelected, onClick = { onSelectOption(index) })
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(option, fontSize = 15.sp, fontWeight = FontWeight.Medium)
                }
            }
        }
    }
}

@Composable
fun PronunciationExerciseView(
    exercise: Exercise,
    onSpeak: (String) -> Unit
) {
    val targetPhrase = exercise.payload.target_sentence ?: exercise.payload.audio_text ?: "Dankon"
    var isSimulatingMic by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "\"$targetPhrase\"",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(8.dp))

                IconButton(onClick = { onSpeak(targetPhrase) }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                        contentDescription = "Ouvir modelo",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Botão de Captura de Voz / Fala
        IconButton(
            onClick = { isSimulatingMic = !isSimulatingMic },
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(if (isSimulatingMic) RubyHeart else MaterialTheme.colorScheme.primary)
                .testTag("speech_record_button")
        ) {
            Icon(
                imageVector = Icons.Default.Mic,
                contentDescription = "Falar agora",
                tint = Color.White,
                modifier = Modifier.size(36.dp)
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = if (isSimulatingMic) "Capturando fala... pronúncia validada!" else "Toque no microfone e pronuncie a frase",
            fontSize = 13.sp,
            color = if (isSimulatingMic) RubyHeart else MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Medium
        )
    }
}

// =============================================================================
// BARRA INFERIOR COM RESULTADOS E DIAGNÓSTICO PEDAGÓGICO DE IA
// =============================================================================

@Composable
fun LessonBottomBar(
    session: ActiveLessonSession,
    onSubmit: () -> Unit,
    onNext: () -> Unit
) {
    val eval = session.currentEvaluation

    Surface(
        color = when {
            eval == null -> MaterialTheme.colorScheme.surface
            eval.is_correct -> CorrectGreenContainer
            else -> ErrorRedContainer
        },
        tonalElevation = 8.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            if (eval != null) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (eval.is_correct) Icons.Default.CheckCircle else Icons.Default.Cancel,
                        contentDescription = null,
                        tint = if (eval.is_correct) CorrectGreen else ErrorRed,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = if (eval.is_correct) "Excelente! Resposta correta" else "Atenção à regra gramatical",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = if (eval.is_correct) CorrectGreen else ErrorRed
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = eval.feedback_message,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )

                // Diagnóstico Pedagógico da IA Gemini em Tempo Real
                if (!eval.is_correct && !session.aiDiagnostic.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Card(
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = "Diagnóstico IA",
                                tint = IndigoAccent,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "Diagnóstico Pedagógico da IA:",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = IndigoAccent
                                )
                                Text(
                                    text = session.aiDiagnostic,
                                    fontSize = 13.sp,
                                    lineHeight = 17.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Button(
                    onClick = onNext,
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (eval.is_correct) CorrectGreen else ErrorRed
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("continue_lesson_button")
                ) {
                    Text(
                        text = "Continuar",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            } else {
                Button(
                    onClick = onSubmit,
                    enabled = !session.isEvaluating,
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("verify_answer_button")
                ) {
                    if (session.isEvaluating) {
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    } else {
                        Text(
                            text = "Verificar",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun LessonFinishedDialog(
    session: ActiveLessonSession,
    onFinish: () -> Unit
) {
    val accuracy = if (session.exercises.isEmpty()) 100 else ((session.score / session.exercises.size) * 100).toInt()
    val needsReinforcement = accuracy < 75

    AlertDialog(
        onDismissRequest = onFinish,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Celebration,
                    contentDescription = null,
                    tint = AmberStreak,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "Lição Concluída!")
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Você concluiu: ${session.lessonTitle}",
                    fontWeight = FontWeight.SemiBold
                )
                Text(text = "Precisão final: $accuracy%")
                Text(text = "+25 XP adicionados ao seu perfil!")

                if (needsReinforcement) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Surface(
                        color = AmberStreak.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(
                                text = "Árvore Adaptativa Ativada:",
                                fontWeight = FontWeight.Bold,
                                color = AmberStreak,
                                fontSize = 12.sp
                            )
                            Text(
                                text = "Identificamos erros de concordância. O motor recomendou o 'Nó de Reforço: Acusativo' para solidificar o conceito!",
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onFinish,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text("Voltar à Trilha")
            }
        }
    )
}

fun getExerciseTypeTitle(type: ExerciseType): String = when (type) {
    ExerciseType.WORD_ORDERING -> "ORDENAÇÃO DE PALAVRAS"
    ExerciseType.MULTIPLE_CHOICE -> "MÚLTIPLA ESCOLHA"
    ExerciseType.PAIR_MATCHING -> "CORRESPONDÊNCIA DE PARES"
    ExerciseType.FILL_IN_BLANK -> "PREENCHA A LACUNA"
    ExerciseType.LISTENING_COMPREHENSION -> "COMPREENSÃO AUDITIVA"
    ExerciseType.PRONUNCIATION_SPEECH -> "PRÁTICA DE PRONÚNCIA"
}
