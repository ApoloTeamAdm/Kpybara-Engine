package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.AQSIScore
import com.example.ui.theme.AmberStreak
import com.example.ui.theme.CorrectGreen
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.IndigoAccent

@Composable
fun CurationStudioScreen(
    aqsiResult: AQSIScore?,
    onRunAudit: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Cabeçalho da Curadoria & IA
        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Estúdio de Curadoria & IA",
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp
                        )
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = IndigoAccent
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "Pipeline de curadoria com Gemini API: calcula o Índice de Qualidade e Acessibilidade Automático (AQSI 0-100), audita distratores, checa o CEFR e diagnostica falhas pedagógicas em tempo real.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = onRunAudit,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = IndigoAccent),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("run_aqsi_audit_button")
                    ) {
                        Icon(Icons.Default.Speed, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Executar Auditoria AQSI no Curso Atual", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Resultado da Auditoria AQSI
        if (aqsiResult != null) {
            item {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.5.dp, CorrectGreen),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Índice AQSI Global",
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.outline
                                )
                                Text(
                                    text = "${aqsiResult.overall} / 100",
                                    fontSize = 28.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = CorrectGreen
                                )
                            }

                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = CorrectGreen
                            ) {
                                Text(
                                    text = aqsiResult.status,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                )
                            }
                        }

                        HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                        // Métricas do AQSI
                        AqsiMetricRow("Precisão & Alinhamento CEFR", aqsiResult.cefr_alignment)
                        Spacer(modifier = Modifier.height(8.dp))
                        AqsiMetricRow("Qualidade dos Distratores (Sem Pegadinhas)", aqsiResult.distractor_quality)
                        Spacer(modifier = Modifier.height(8.dp))
                        AqsiMetricRow("Legibilidade & Clareza no Idioma Fonte", aqsiResult.readability)
                        Spacer(modifier = Modifier.height(8.dp))
                        AqsiMetricRow("Acessibilidade (TTS & Alt-texts)", aqsiResult.accessibility)

                        Spacer(modifier = Modifier.height(14.dp))

                        Text(
                            text = "Notas da Auditoria Automatizada:",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        aqsiResult.audit_notes.forEach { note ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 2.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = CorrectGreen,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(note, fontSize = 12.sp, lineHeight = 16.sp)
                            }
                        }
                    }
                }
            }
        }

        // Especificações de Arquitetura do Microserviço Backend
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Microserviço Backend Python (FastAPI + google-genai)",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "• Endpoint: POST /api/v1/curate-course\n" +
                                "• Validação de Esquema Pydantic v2 estrita com CoursePackage\n" +
                                "• Diagnósticos: POST /api/v1/diagnose-error\n" +
                                "• SDK Oficial: google-genai (SDK Oficial do Google AI)\n" +
                                "• Código-fonte disponível em /backend/curator_service.py",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 18.sp
                    )
                }
            }
        }
    }
}

@Composable
fun AqsiMetricRow(title: String, score: Double) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(title, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface)
            Text("${score.toInt()}%", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        }
        Spacer(modifier = Modifier.height(3.dp))
        LinearProgressIndicator(
            progress = { (score / 100.0).toFloat() },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp),
            color = MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.surfaceVariant
        )
    }
}
