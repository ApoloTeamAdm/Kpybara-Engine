package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.CourseEntity
import com.example.data.local.entity.LessonEntity
import com.example.data.local.entity.ModuleEntity
import com.example.ui.theme.AmberStreak
import com.example.ui.theme.CorrectGreen
import com.example.ui.theme.EmeraldDark
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.IndigoAccent

@Composable
fun LearnScreen(
    activeCourse: CourseEntity?,
    modules: List<ModuleEntity>,
    lessonsMap: Map<String, List<LessonEntity>>,
    srsDueCount: Int,
    isOnline: Boolean = true,
    totalCacheBytes: Long = 2_450_000L,
    onStartLesson: (String, String) -> Unit,
    onNavigateToSrs: () -> Unit,
    onOpenOfflineSync: () -> Unit = {},
    onDownloadModule: (String, String, String) -> Unit = { _, _, _ -> }
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("learn_screen_scroll"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Hero Card do Curso Ativo
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.horizontalGradient(
                                listOf(
                                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                                    MaterialTheme.colorScheme.surfaceVariant
                                )
                            )
                        )
                        .padding(18.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                color = MaterialTheme.colorScheme.primary,
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = "CEFR ${activeCourse?.cefr_level ?: "A1"}",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Verified,
                                    contentDescription = null,
                                    tint = EmeraldPrimary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "AQSI ${activeCourse?.aqsi_score ?: 94.5}",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = activeCourse?.title ?: "Esperanto Prático para Brasileiros",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 20.sp,
                            lineHeight = 26.sp
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = activeCourse?.description ?: "Aprendizado acelerado 100% offline-first com repetição espaçada e IA federada.",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "v${activeCourse?.version ?: "1.0.0"} • 100% Offline",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.outline
                            )
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.primaryContainer,
                                modifier = Modifier.clickable { onOpenOfflineSync() }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CloudSync,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "${String.format("%.2f", totalCacheBytes / (1024f * 1024f))} MB • Offline",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Alerta de Repetição Espaçada (SRS Memory Matrix)
        if (srsDueCount > 0) {
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = AmberStreak.copy(alpha = 0.15f)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onNavigateToSrs() }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = AmberStreak,
                            modifier = Modifier.size(40.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Psychology,
                                    contentDescription = null,
                                    tint = Color.White
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Matriz SRS: $srsDueCount cartões para revisar",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            Text(
                                text = "O algoritmo SM-2 calculou revisões para fixar palavras-chave na memória de longo prazo.",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = "Revisar agora",
                            tint = AmberStreak
                        )
                    }
                }
            }
        }

        // Lista de Módulos e Árvore de Aprendizado Adaptativa
        items(modules) { module ->
            val lessons = lessonsMap[module.module_id] ?: emptyList()

            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = module.module_title,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(top = 2.dp)
                            ) {
                                Text(
                                    text = "${lessons.count { it.is_completed }} de ${lessons.size} lições",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                if (module.is_downloaded) {
                                    Surface(
                                        shape = RoundedCornerShape(4.dp),
                                        color = CorrectGreen.copy(alpha = 0.15f)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                Icons.Default.CloudDone,
                                                contentDescription = "Áudios e lições no cache",
                                                tint = CorrectGreen,
                                                modifier = Modifier.size(11.dp)
                                            )
                                            Spacer(modifier = Modifier.width(2.dp))
                                            Text(
                                                "Offline",
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = CorrectGreen
                                            )
                                        }
                                    }
                                } else {
                                    IconButton(
                                        onClick = {
                                            onDownloadModule(module.course_id, module.module_id, module.module_title)
                                        },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.Download,
                                            contentDescription = "Baixar módulo",
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        }

                        val completedCount = lessons.count { it.is_completed }
                        val progressRatio = if (lessons.isEmpty()) 0f else completedCount.toFloat() / lessons.size
                        CircularProgressIndicator(
                            progress = { progressRatio },
                            modifier = Modifier.size(36.dp),
                            color = MaterialTheme.colorScheme.primary,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant,
                            strokeWidth = 4.dp
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Caminho de nós da lição (Visual Tree Path)
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        lessons.forEachIndexed { index, lesson ->
                            val isReinforcement = lesson.lesson_id.contains("reforco") || lesson.lesson_title.contains("Reforço")
                            val isNextAvailable = index == 0 || lessons.getOrNull(index - 1)?.is_completed == true

                            LessonNodeItem(
                                lesson = lesson,
                                isAvailable = isNextAvailable || lesson.is_completed || isReinforcement,
                                isReinforcement = isReinforcement,
                                onClick = {
                                    onStartLesson(lesson.lesson_id, lesson.lesson_title)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun LessonNodeItem(
    lesson: LessonEntity,
    isAvailable: Boolean,
    isReinforcement: Boolean,
    onClick: () -> Unit
) {
    val nodeColor = when {
        lesson.is_completed -> EmeraldPrimary
        isReinforcement -> AmberStreak
        isAvailable -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.outlineVariant
    }

    val iconVector = when {
        lesson.is_completed -> Icons.Default.Check
        isReinforcement -> Icons.AutoMirrored.Filled.AltRoute
        isAvailable -> Icons.Default.PlayArrow
        else -> Icons.Default.Lock
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(if (isReinforcement) AmberStreak.copy(alpha = 0.1f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            .clickable(enabled = isAvailable) { onClick() }
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Círculo do Nó de Aprendizado
        Box(
            modifier = Modifier
                .size(46.dp)
                .clip(CircleShape)
                .background(nodeColor),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = iconVector,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (isReinforcement) {
                    Surface(
                        color = AmberStreak,
                        shape = RoundedCornerShape(4.dp),
                        modifier = Modifier.padding(end = 6.dp)
                    ) {
                        Text(
                            text = "RAMIFICAÇÃO DINÂMICA",
                            color = Color.White,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                        )
                    }
                }
                Text(
                    text = lesson.lesson_title,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp
                )
            }

            Text(
                text = if (lesson.is_completed) "Concluído • Nota: ${(lesson.score * 100).toInt()}%"
                else if (isReinforcement) "Nó adaptativo ativado para reforçar conceitos anteriores"
                else if (isAvailable) "Toque para praticar agora"
                else "Bloqueado",
                fontSize = 12.sp,
                color = if (lesson.is_completed) EmeraldDark else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        if (isAvailable) {
            Button(
                onClick = onClick,
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = nodeColor),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                modifier = Modifier.testTag("start_lesson_${lesson.lesson_id}")
            ) {
                Text(
                    text = if (lesson.is_completed) "Praticar" else "Iniciar",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
