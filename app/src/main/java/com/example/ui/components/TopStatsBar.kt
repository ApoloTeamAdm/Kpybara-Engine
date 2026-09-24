package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.CourseEntity
import com.example.data.local.entity.StudentProgressEntity
import com.example.ui.theme.AmberStreak
import com.example.ui.theme.CorrectGreen
import com.example.ui.theme.RubyHeart

@Composable
fun TopStatsBar(
    progress: StudentProgressEntity?,
    courses: List<CourseEntity>,
    activeCourseId: String,
    isOnline: Boolean,
    isSyncing: Boolean,
    onSelectCourse: (String) -> Unit,
    onOpenSyncSheet: () -> Unit
) {
    var expandedMenu by remember { mutableStateOf(false) }
    val activeCourse = courses.find { it.course_id == activeCourseId }

    // Rotação suave se estiver sincronizando
    val infiniteTransition = rememberInfiniteTransition(label = "sync_rotation")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "sync_spin"
    )

    Surface(
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Seletor de Curso Ativo
            Box {
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .clickable { expandedMenu = true }
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Language,
                        contentDescription = "Idioma Ativo",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = activeCourse?.target_language?.uppercase() ?: "EO",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                    Icon(
                        imageVector = Icons.Default.ArrowDropDown,
                        contentDescription = "Mudar Curso",
                        modifier = Modifier.size(16.dp)
                    )
                }

                DropdownMenu(
                    expanded = expandedMenu,
                    onDismissRequest = { expandedMenu = false }
                ) {
                    courses.forEach { c ->
                        DropdownMenuItem(
                            text = {
                                Column {
                                    Text(c.title, fontWeight = FontWeight.SemiBold)
                                    Text(
                                        "${c.source_language} ➔ ${c.target_language.uppercase()} • AQSI ${c.aqsi_score}",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            },
                            onClick = {
                                onSelectCourse(c.course_id)
                                expandedMenu = false
                            },
                            leadingIcon = {
                                Icon(Icons.Default.School, contentDescription = null)
                            }
                        )
                    }
                }
            }

            // Indicador de Conexão & Sincronização Inteligente (Online / Offline)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(
                        if (isOnline) CorrectGreen.copy(alpha = 0.12f)
                        else AmberStreak.copy(alpha = 0.15f)
                    )
                    .clickable { onOpenSyncSheet() }
                    .padding(horizontal = 8.dp, vertical = 5.dp)
                    .testTag("network_sync_pill")
            ) {
                if (isSyncing) {
                    Icon(
                        imageVector = Icons.Default.Sync,
                        contentDescription = "Sincronizando...",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .size(15.dp)
                            .rotate(rotation)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Sync",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                } else if (isOnline) {
                    Icon(
                        imageVector = Icons.Default.CloudDone,
                        contentDescription = "Online",
                        tint = CorrectGreen,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Online",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = CorrectGreen
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.CloudOff,
                        contentDescription = "Offline",
                        tint = AmberStreak,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Offline",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = AmberStreak
                    )
                }
            }

            // Estatísticas de Gamificação (Streak, XP, Vidas)
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Streak
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.LocalFireDepartment,
                        contentDescription = "Dias seguidos",
                        tint = AmberStreak,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(1.dp))
                    Text(
                        text = "${progress?.streak_days ?: 1}",
                        fontWeight = FontWeight.Bold,
                        color = AmberStreak,
                        fontSize = 13.sp
                    )
                }

                // XP
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Bolt,
                        contentDescription = "Pontos de XP",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(1.dp))
                    Text(
                        text = "${progress?.total_xp ?: 0}",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 13.sp
                    )
                }

                // Vidas
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Favorite,
                        contentDescription = "Vidas restantes",
                        tint = RubyHeart,
                        modifier = Modifier.size(17.dp)
                    )
                    Spacer(modifier = Modifier.width(1.dp))
                    Text(
                        text = "${progress?.lives ?: 5}",
                        fontWeight = FontWeight.Bold,
                        color = RubyHeart,
                        fontSize = 13.sp
                    )
                }
            }
        }
    }
}
