package com.example

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.devtools.ui.DevToolsScreen
import com.example.ui.CourseEngineViewModel
import com.example.ui.components.OfflineSyncSheet
import com.example.ui.components.TopStatsBar
import com.example.ui.screens.*
import com.example.ui.theme.CourseEngineTheme

enum class AppTab(val title: String, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
    LEARN("Trilha", Icons.Default.School),
    SRS("SRS Memória", Icons.Default.Psychology),
    FEDERATION("Federação", Icons.Default.Hub),
    CURATION("Curadoria IA", Icons.Default.AutoAwesome),
    DEVTOOLS("DevTools", Icons.Default.Terminal)
}

class MainActivity : ComponentActivity() {

    private val viewModel: CourseEngineViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            CourseEngineTheme {
                val context = LocalContext.current

                val activeSession by viewModel.activeSession.collectAsStateWithLifecycle()
                val courses by viewModel.courses.collectAsStateWithLifecycle()
                val activeCourseId by viewModel.activeCourseId.collectAsStateWithLifecycle()
                val modules by viewModel.modules.collectAsStateWithLifecycle()
                val lessonsMap by viewModel.lessons.collectAsStateWithLifecycle()
                val studentProgress by viewModel.studentProgress.collectAsStateWithLifecycle()
                val srsCards by viewModel.srsCards.collectAsStateWithLifecycle()
                val toastMessage by viewModel.toastMessage.collectAsStateWithLifecycle()
                val aqsiResult by viewModel.curationAqsiResult.collectAsStateWithLifecycle()

                // Estados de Rede e Offline
                val isOnline by viewModel.isOnline.collectAsStateWithLifecycle()
                val isSyncing by viewModel.isSyncing.collectAsStateWithLifecycle()
                val lastSyncReport by viewModel.lastSyncReport.collectAsStateWithLifecycle()
                val totalCacheBytes by viewModel.totalCacheBytes.collectAsStateWithLifecycle()
                val currentDownload by viewModel.currentDownload.collectAsStateWithLifecycle()

                var showOfflineSyncSheet by remember { mutableStateOf(false) }
                var currentTab by remember { mutableStateOf(AppTab.LEARN) }

                LaunchedEffect(toastMessage) {
                    toastMessage?.let {
                        Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
                        viewModel.clearToast()
                    }
                }

                // Modal de Gestão Offline & Sincronização Inteligente (RFC 6902)
                if (showOfflineSyncSheet) {
                    val activeCourse = courses.find { it.course_id == activeCourseId }
                    OfflineSyncSheet(
                        isOnline = isOnline,
                        isSyncing = isSyncing,
                        lastSyncReport = lastSyncReport,
                        totalCacheBytes = totalCacheBytes,
                        currentDownload = currentDownload,
                        activeCourse = activeCourse,
                        modules = modules,
                        onDismiss = { showOfflineSyncSheet = false },
                        onTriggerSync = { viewModel.triggerSmartSync() },
                        onDownloadCourse = { courseId -> viewModel.downloadEntireCourse(courseId) },
                        onDownloadModule = { courseId, moduleId, title ->
                            viewModel.downloadSpecificModule(courseId, moduleId, title)
                        },
                        onRemoveModule = { courseId, moduleId ->
                            viewModel.removeModuleDownload(courseId, moduleId)
                        }
                    )
                }

                // Se houver uma sessão de lição ativa, exibe o reprodutor interativo
                val currentSession = activeSession
                if (currentSession != null) {
                    LessonPlayerScreen(
                        session = currentSession,
                        onClose = { viewModel.exitLesson() },
                        onSpeak = { text -> viewModel.speakText(text) },
                        onSelectToken = { token -> viewModel.selectToken(token) },
                        onDeselectToken = { token -> viewModel.deselectToken(token) },
                        onSelectOption = { index -> viewModel.selectOption(index) },
                        onMatchPair = { left, right -> viewModel.matchPair(left, right) },
                        onUpdateTextInput = { text -> viewModel.updateTextInput(text) },
                        onSubmitAnswer = { viewModel.submitAnswer() },
                        onNextExercise = { viewModel.nextExercise() }
                    )
                } else {
                    Scaffold(
                        modifier = Modifier.fillMaxSize(),
                        topBar = {
                            TopStatsBar(
                                progress = studentProgress,
                                courses = courses,
                                activeCourseId = activeCourseId,
                                isOnline = isOnline,
                                isSyncing = isSyncing,
                                onSelectCourse = { viewModel.selectCourse(it) },
                                onOpenSyncSheet = { showOfflineSyncSheet = true }
                            )
                        },
                        bottomBar = {
                            NavigationBar(
                                modifier = Modifier.testTag("main_bottom_nav")
                            ) {
                                AppTab.values().forEach { tab ->
                                    NavigationBarItem(
                                        selected = currentTab == tab,
                                        onClick = { currentTab = tab },
                                        icon = {
                                            if (tab == AppTab.SRS && srsCards.isNotEmpty()) {
                                                BadgedBox(badge = {
                                                    Badge { Text("${srsCards.size}") }
                                                }) {
                                                    Icon(tab.icon, contentDescription = tab.title)
                                                }
                                            } else {
                                                Icon(tab.icon, contentDescription = tab.title)
                                            }
                                        },
                                        label = { Text(tab.title) },
                                        modifier = Modifier.testTag("nav_tab_${tab.name.lowercase()}")
                                    )
                                }
                            }
                        }
                    ) { innerPadding ->
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(innerPadding)
                        ) {
                            when (currentTab) {
                                AppTab.LEARN -> {
                                    val activeCourse = courses.find { it.course_id == activeCourseId }
                                    LearnScreen(
                                        activeCourse = activeCourse,
                                        modules = modules,
                                        lessonsMap = lessonsMap,
                                        srsDueCount = srsCards.size,
                                        isOnline = isOnline,
                                        totalCacheBytes = totalCacheBytes,
                                        onStartLesson = { id, title ->
                                            viewModel.startLesson(id, title)
                                        },
                                        onNavigateToSrs = { currentTab = AppTab.SRS },
                                        onOpenOfflineSync = { showOfflineSyncSheet = true },
                                        onDownloadModule = { courseId, moduleId, title ->
                                            viewModel.downloadSpecificModule(courseId, moduleId, title)
                                        }
                                    )
                                }

                                AppTab.SRS -> {
                                    SrsReviewScreen(
                                        cards = srsCards,
                                        onReviewCard = { card, q -> viewModel.reviewSrsCard(card, q) },
                                        onSpeak = { text -> viewModel.speakText(text) }
                                    )
                                }

                                AppTab.FEDERATION -> {
                                    FederationScreen(
                                        courses = courses,
                                        onApplyDeltaPatch = { courseId ->
                                            viewModel.applySampleDeltaPatch(courseId)
                                        },
                                        onInstallTokiPona = {
                                            viewModel.installTokiPonaCourse()
                                        }
                                    )
                                }

                                AppTab.CURATION -> {
                                    CurationStudioScreen(
                                        aqsiResult = aqsiResult,
                                        onRunAudit = { viewModel.auditCurrentCourseAqsi() }
                                    )
                                }

                                AppTab.DEVTOOLS -> {
                                    DevToolsScreen(
                                        onToast = { msg ->
                                            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
