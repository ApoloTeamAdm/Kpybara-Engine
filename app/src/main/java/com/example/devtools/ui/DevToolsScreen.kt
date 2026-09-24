package com.example.devtools.ui

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.devtools.engine.MultiLanguageScriptRunner
import com.example.devtools.model.DevLanguage
import com.example.devtools.model.DevScriptExecutionResult
import com.example.devtools.model.LanguageExtension
import com.example.kpybara.KpybaraTranspilerPipeline
import com.example.kpybara.KpybaraUniversalCore
import com.example.ui.theme.AmberStreak
import com.example.ui.theme.CorrectGreen
import com.example.ui.theme.EmeraldDark
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.IndigoAccent
import kotlinx.coroutines.launch

enum class DevTab(val label: String, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
    EDITOR("Editor & Sandbox", Icons.Default.Code),
    KPYBARA_CORE("Core C++ & .kpy", Icons.Default.Memory),
    CONSOLE("Terminal & Logs", Icons.Default.Terminal),
    EXTENSIONS("Extensões (8)", Icons.Default.Extension),
    EXPORT("Exportar RFC 6902", Icons.Default.DataObject)
}

@Composable
fun DevToolsScreen(
    onPublishExercise: (String, DevLanguage) -> Unit = { _, _ -> },
    onToast: (String) -> Unit = {}
) {
    val coroutineScope = rememberCoroutineScope()

    var activeTab by remember { mutableStateOf(DevTab.EDITOR) }
    var selectedLanguage by remember { mutableStateOf(DevLanguage.LUA) }
    var codeContent by remember { mutableStateOf(DevLanguage.LUA.defaultSnippet) }

    // Entradas do Test Bench
    var testPrompt by remember { mutableStateOf("Como se diz 'Olá' em Esperanto?") }
    var testTarget by remember { mutableStateOf("Saluton") }
    var testStudentAnswer by remember { mutableStateOf("saluton") }

    var isRunning by remember { mutableStateOf(false) }
    var lastExecutionResult by remember { mutableStateOf<DevScriptExecutionResult?>(null) }

    // Estado do Core C++ Kpybara
    var isRunningKpyCore by remember { mutableStateOf(false) }
    var kpyExecutionResult by remember { mutableStateOf<KpybaraUniversalCore.CoreExecutionResult?>(null) }
    var currentKpyPackage by remember {
        mutableStateOf(
            KpybaraTranspilerPipeline.compileAndPackage(
                exerciseId = "esp_saluton_01",
                sourceLanguage = DevLanguage.LUA,
                sourceCode = DevLanguage.LUA.defaultSnippet,
                prompt = "Como se diz 'Olá' em Esperanto?",
                targetSentence = "Saluton"
            )
        )
    }

    // Registro de Extensões
    var extensions by remember {
        mutableStateOf(
            listOf(
                LanguageExtension(DevLanguage.LUA, "5.4.6", isEnabled = true, memoryFootprintKb = 48, author = "PUC-Rio / Core", supportLevel = "NATIVO"),
                LanguageExtension(DevLanguage.LUAU, "0.620", isEnabled = true, memoryFootprintKb = 64, author = "Roblox Corp / Comunitário", supportLevel = "OFICIAL"),
                LanguageExtension(DevLanguage.JAVASCRIPT, "ES2023", isEnabled = true, memoryFootprintKb = 96, author = "QuickJS / Bellard", supportLevel = "OFICIAL"),
                LanguageExtension(DevLanguage.PYTHON, "3.11-micro", isEnabled = true, memoryFootprintKb = 110, author = "MicroPython / Python Org", supportLevel = "OFICIAL"),
                LanguageExtension(DevLanguage.JAVA, "21-LTS", isEnabled = true, memoryFootprintKb = 140, author = "OpenJDK / JVM Sandbox", supportLevel = "COMUNITÁRIO"),
                LanguageExtension(DevLanguage.C, "C99 Wasm", isEnabled = true, memoryFootprintKb = 18, author = "Clang / LLVM Core", supportLevel = "OFICIAL"),
                LanguageExtension(DevLanguage.CPP, "C++20", isEnabled = true, memoryFootprintKb = 32, author = "LLVM / Clang Toolchain", supportLevel = "COMUNITÁRIO"),
                LanguageExtension(DevLanguage.CSHARP, ".NET 8", isEnabled = true, memoryFootprintKb = 128, author = "Roslyn Micro-Compiler", supportLevel = "COMUNITÁRIO")
            )
        )
    }

    // Atualiza o pacote C++ Kpybara sempre que o código ou alvo mudar
    fun recompileKpyPackage() {
        currentKpyPackage = KpybaraTranspilerPipeline.compileAndPackage(
            exerciseId = "esp_ex_${selectedLanguage.id}_01",
            sourceLanguage = selectedLanguage,
            sourceCode = codeContent,
            prompt = testPrompt,
            targetSentence = testTarget
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Top Header do DevTools
        Surface(
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 3.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(IndigoAccent.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Terminal,
                                contentDescription = null,
                                tint = IndigoAccent,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Kpybara Studio & C++ Core",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 16.sp
                            )
                            Text(
                                text = "Motor C++20 Universal • Transpilação Build-Time • Zero-Overhead",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Badge(containerColor = CorrectGreen.copy(alpha = 0.2f)) {
                        Text("< 1ms • < 5MB", color = CorrectGreen, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Seletor Rápido de Linguagem (Horizontal Chips)
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(DevLanguage.values()) { lang ->
                        val isSelected = selectedLanguage == lang
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                selectedLanguage = lang
                                codeContent = lang.defaultSnippet
                                recompileKpyPackage()
                                onToast("Ambiente configurado para ${lang.displayName}")
                            },
                            label = {
                                Text(
                                    text = lang.displayName,
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            leadingIcon = if (isSelected) {
                                { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp)) }
                            } else null
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Sub-navegação do DevTools
                TabRow(
                    selectedTabIndex = activeTab.ordinal,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    DevTab.values().forEach { tab ->
                        Tab(
                            selected = activeTab == tab,
                            onClick = {
                                activeTab = tab
                                if (tab == DevTab.KPYBARA_CORE) {
                                    recompileKpyPackage()
                                }
                            },
                            text = { Text(tab.label, fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                            icon = { Icon(tab.icon, contentDescription = tab.label, modifier = Modifier.size(16.dp)) }
                        )
                    }
                }
            }
        }

        // Conteúdo da Aba Selecionada
        Box(modifier = Modifier.weight(1f)) {
            when (activeTab) {
                DevTab.EDITOR -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        // Card de Informações da Linguagem
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "${selectedLanguage.displayName} • ${selectedLanguage.runtimeName}",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Text(
                                        text = selectedLanguage.description,
                                        fontSize = 11.sp,
                                        lineHeight = 15.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                OutlinedButton(
                                    onClick = {
                                        codeContent = selectedLanguage.defaultSnippet
                                        recompileKpyPackage()
                                        onToast("Snippet padrão restaurado.")
                                    },
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text("Restaurar", fontSize = 11.sp)
                                }
                            }
                        }

                        // Editor de Código Fonte
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Código do Avaliador (${selectedLanguage.fileExtension})",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                                Text(
                                    text = "${codeContent.lines().size} linhas",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.outline
                                )
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            OutlinedTextField(
                                value = codeContent,
                                onValueChange = {
                                    codeContent = it
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(min = 200.dp, max = 300.dp)
                                    .testTag("dev_code_editor"),
                                textStyle = LocalTextStyle.current.copy(
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 12.sp,
                                    lineHeight = 17.sp
                                ),
                                placeholder = { Text("Escreva a lógica em ${selectedLanguage.displayName}...") }
                            )
                        }

                        // Test Bench: Entradas do Exercício
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(
                                    text = "Bancada de Testes do Exercício (Input Mock)",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )

                                OutlinedTextField(
                                    value = testPrompt,
                                    onValueChange = { testPrompt = it },
                                    label = { Text("Prompt da Questão") },
                                    modifier = Modifier.fillMaxWidth(),
                                    textStyle = LocalTextStyle.current.copy(fontSize = 12.sp)
                                )

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    OutlinedTextField(
                                        value = testTarget,
                                        onValueChange = { testTarget = it },
                                        label = { Text("Gabarito Alvo") },
                                        modifier = Modifier.weight(1f),
                                        textStyle = LocalTextStyle.current.copy(fontSize = 12.sp)
                                    )
                                    OutlinedTextField(
                                        value = testStudentAnswer,
                                        onValueChange = { testStudentAnswer = it },
                                        label = { Text("Resposta do Aluno") },
                                        modifier = Modifier.weight(1f),
                                        textStyle = LocalTextStyle.current.copy(fontSize = 12.sp)
                                    )
                                }
                            }
                        }

                        // Ações Principais: Executar Sandbox e Compilar para C++ Core
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(
                                onClick = {
                                    isRunning = true
                                    coroutineScope.launch {
                                        val res = MultiLanguageScriptRunner.executeScript(
                                            language = selectedLanguage,
                                            scriptCode = codeContent,
                                            prompt = testPrompt,
                                            studentAnswer = testStudentAnswer,
                                            targetSentence = testTarget
                                        )
                                        lastExecutionResult = res
                                        isRunning = false
                                        activeTab = DevTab.CONSOLE
                                        onToast("Script avaliado em ${res.executionTimeMs}ms!")
                                    }
                                },
                                enabled = !isRunning,
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("btn_run_script")
                            ) {
                                if (isRunning) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(16.dp),
                                        color = MaterialTheme.colorScheme.onPrimary,
                                        strokeWidth = 2.dp
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Executando...")
                                } else {
                                    Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Sandbox", fontWeight = FontWeight.Bold)
                                }
                            }

                            Button(
                                onClick = {
                                    recompileKpyPackage()
                                    activeTab = DevTab.KPYBARA_CORE
                                    onToast("Transpilado para C++20 Core (.kpy.json)!")
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = IndigoAccent),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("btn_to_kpybara_core")
                            ) {
                                Icon(Icons.Default.Memory, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Core C++ (.kpy)", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                DevTab.KPYBARA_CORE -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        // Painel de Destaque: C++ Universal Core Runtime & SLA
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.Memory,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(24.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Column {
                                            Text(
                                                text = "Kpybara Universal Core (C++20)",
                                                fontWeight = FontWeight.ExtraBold,
                                                fontSize = 15.sp
                                            )
                                            Text(
                                                text = "Zero-Overhead no Mobile • Comunicação FFI direta",
                                                fontSize = 11.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }

                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = CorrectGreen.copy(alpha = 0.2f)
                                    ) {
                                        Text(
                                            text = "SLA < 1ms",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = CorrectGreen,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                // Grade de Métricas Arquiteturais
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = MaterialTheme.colorScheme.surface,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Column(modifier = Modifier.padding(10.dp)) {
                                            Text("Latência", fontSize = 10.sp, color = MaterialTheme.colorScheme.outline)
                                            Text(
                                                text = if (kpyExecutionResult != null) "${kpyExecutionResult!!.executionTimeMicros} µs (${String.format("%.3f", kpyExecutionResult!!.executionTimeMs)} ms)" else "< 0.1 ms",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 12.sp,
                                                color = CorrectGreen
                                            )
                                            Text("Alvo: < 1.0 ms", fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                    }

                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = MaterialTheme.colorScheme.surface,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Column(modifier = Modifier.padding(10.dp)) {
                                            Text("RAM Total", fontSize = 10.sp, color = MaterialTheme.colorScheme.outline)
                                            Text(
                                                text = if (kpyExecutionResult != null) "${kpyExecutionResult!!.memoryFootprintKb} KB" else "1.24 MB",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 12.sp,
                                                color = IndigoAccent
                                            )
                                            Text("Teto: < 5.0 MB", fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                    }

                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = MaterialTheme.colorScheme.surface,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Column(modifier = Modifier.padding(10.dp)) {
                                            Text("Bytecode", fontSize = 10.sp, color = MaterialTheme.colorScheme.outline)
                                            Text(
                                                text = "${currentKpyPackage.bytecodeSizeBytes} B",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 12.sp,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                            Text("Zero overhead", fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                    }
                                }
                            }
                        }

                        // Botão de Benchmarking no Core C++ em tempo real
                        Button(
                            onClick = {
                                isRunningKpyCore = true
                                coroutineScope.launch {
                                    val bytecode = KpybaraUniversalCore.compileToKpyBytecode(
                                        targetSentence = testTarget,
                                        exerciseId = "esp_benchmark_01"
                                    )
                                    val result = KpybaraUniversalCore.executePrecompiledBytecode(
                                        bytecode = bytecode,
                                        studentAnswer = testStudentAnswer,
                                        targetSentence = testTarget,
                                        exerciseId = "esp_benchmark_01"
                                    )
                                    kpyExecutionResult = result
                                    isRunningKpyCore = false
                                    onToast("Executado no C++ Universal Core em ${result.executionTimeMicros} µs (< 1ms)!")
                                }
                            },
                            enabled = !isRunningKpyCore,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("btn_run_kpy_core")
                        ) {
                            if (isRunningKpyCore) {
                                CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White, strokeWidth = 2.dp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Avaliando no C++ Universal Core...")
                            } else {
                                Icon(Icons.Default.Bolt, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Executar Benchmark no C++ Core Runtime (< 1ms)", fontWeight = FontWeight.Bold)
                            }
                        }

                        if (kpyExecutionResult != null) {
                            val res = kpyExecutionResult!!
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (res.isCorrect) CorrectGreen.copy(alpha = 0.12f) else AmberStreak.copy(alpha = 0.15f),
                                border = BorderStroke(1.dp, if (res.isCorrect) CorrectGreen else AmberStreak),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = if (res.isCorrect) "✓ APROVADO PELO C++ CORE" else "✗ DIVERGÊNCIA NO C++ CORE",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            color = if (res.isCorrect) CorrectGreen else AmberStreak
                                        )
                                        Text(
                                            text = "${res.executionTimeMicros} µs (${String.format("%.4f", res.executionTimeMs)} ms)",
                                            fontWeight = FontWeight.ExtraBold,
                                            fontSize = 12.sp,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = res.feedback,
                                        fontSize = 12.sp,
                                        lineHeight = 16.sp
                                    )
                                    Text(
                                        text = "Branching Adaptativo: next_lesson = '${res.nextLessonId}' • Zero-Overhead Mobile: ${res.zeroOverheadVerified}",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.outline
                                    )
                                }
                            }
                        }

                        // Seção 1: Código C++20 Nativo Gerado
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Código C++20 Gerado (Build-time Transpilation)",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                                Text(
                                    text = "std::string_view • C-ABI",
                                    fontSize = 11.sp,
                                    color = IndigoAccent,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            OutlinedTextField(
                                value = currentKpyPackage.cppNativeSource,
                                onValueChange = {},
                                readOnly = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(min = 160.dp, max = 240.dp),
                                textStyle = LocalTextStyle.current.copy(
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 11.sp,
                                    lineHeight = 15.sp
                                )
                            )
                        }

                        // Seção 2: Bytecode Binário Kpybara (.kpybc)
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Stream de Bytecode Kpybara (Hexadecimal)",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                                Text(
                                    text = "${currentKpyPackage.bytecodeSizeBytes} bytes",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFF1E1E2E),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = currentKpyPackage.bytecodeHex.chunked(2).joinToString(" "),
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 12.sp,
                                    color = Color(0xFF51CF66),
                                    modifier = Modifier.padding(12.dp)
                                )
                            }
                        }

                        // Seção 3: Manifesto Oficial .kpy.json
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Manifesto Final (.kpy.json)",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                                Text(
                                    text = "kpy_spec: 2.0.0",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.outline
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            OutlinedTextField(
                                value = currentKpyPackage.jsonPayload,
                                onValueChange = {},
                                readOnly = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(min = 160.dp, max = 220.dp),
                                textStyle = LocalTextStyle.current.copy(
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 11.sp
                                )
                            )
                        }

                        Button(
                            onClick = {
                                onToast("Pacote .kpy.json exportado com sucesso para a federação!")
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("btn_export_kpy_json")
                        ) {
                            Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Exportar Pacote Pré-compilado (.kpy.json)")
                        }
                    }
                }

                DevTab.CONSOLE -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                    ) {
                        // Cartão de Resumo da Execução
                        if (lastExecutionResult != null) {
                            val res = lastExecutionResult!!
                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = if (res.isSuccess && res.isCorrect) CorrectGreen.copy(alpha = 0.15f)
                                    else if (res.isSuccess) AmberStreak.copy(alpha = 0.15f)
                                    else MaterialTheme.colorScheme.errorContainer
                                ),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = if (res.isSuccess) "STATUS: OK (Avaliado em ${res.executionTimeMs}ms)" else "STATUS: FALHA",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            color = if (res.isSuccess) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.error
                                        )
                                        Text(
                                            text = "${res.memoryUsedKb} KB RAM",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Resultado: is_correct = ${res.isCorrect} • score = ${res.score} • next = '${res.nextLesson}'",
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 12.sp
                                    )
                                    Text(
                                        text = "Feedback retornado: \"${res.feedback}\"",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        } else {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "Nenhum script executado ainda. Vá até a aba 'Editor' e clique em 'Sandbox'.",
                                    fontSize = 12.sp,
                                    modifier = Modifier.padding(14.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Terminal Box (Preto com estilo hacker / DevTools)
                        Text(
                            text = "Terminal Output (Stdout & Profiling)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFF1E1E2E),
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                        ) {
                            val logs = lastExecutionResult?.stdoutLogs ?: listOf("[Terminal] Aguardando inicialização do motor...")
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                items(logs) { logLine ->
                                    val color = when {
                                        logLine.contains("[Erro") || logLine.contains("[TIMEOUT") -> Color(0xFFFF6B6B)
                                        logLine.contains("[OK]") || logLine.contains("passou") -> Color(0xFF51CF66)
                                        logLine.contains("[Aviso") -> Color(0xFFFFD43B)
                                        logLine.contains("[Runner]") -> Color(0xFF74C0FC)
                                        else -> Color(0xFFC5C8C6)
                                    }
                                    Text(
                                        text = logLine,
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 11.sp,
                                        color = color,
                                        lineHeight = 15.sp
                                    )
                                }
                            }
                        }
                    }
                }

                DevTab.EXTENSIONS -> {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        item {
                            Text(
                                text = "Gerenciador de Runtimes & Extensões de Linguagem",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                            Text(
                                text = "A arquitetura CourseEngine suporta Lua 5.4 como padrão e plugins comunitários para outras 7 linguagens com transpilação sob demanda para o Kpybara C++ Core.",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        items(extensions) { ext ->
                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = if (ext.isEnabled) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                                ),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(14.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = ext.language.displayName,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 14.sp
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Surface(
                                                shape = RoundedCornerShape(4.dp),
                                                color = if (ext.supportLevel == "NATIVO") CorrectGreen.copy(alpha = 0.2f) else MaterialTheme.colorScheme.primaryContainer
                                            ) {
                                                Text(
                                                    text = ext.supportLevel,
                                                    fontSize = 9.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
                                                    color = if (ext.supportLevel == "NATIVO") CorrectGreen else MaterialTheme.colorScheme.primary
                                                )
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = "${ext.language.runtimeName} • v${ext.version}",
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            text = "Footprint: ~${ext.memoryFootprintKb} KB RAM • Autor: ${ext.author}",
                                            fontSize = 10.sp,
                                            color = MaterialTheme.colorScheme.outline
                                        )
                                    }

                                    Switch(
                                        checked = ext.isEnabled,
                                        onCheckedChange = { isChecked ->
                                            extensions = extensions.map {
                                                if (it.language == ext.language) it.copy(isEnabled = isChecked) else it
                                            }
                                            onToast("${ext.language.displayName} ${if (isChecked) "ativado" else "desativado"}")
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                DevTab.EXPORT -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Exportador RFC 6902 & Pacote Federado",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                        Text(
                            text = "Gera a atualização delta JSON Patch pronta para versionamento no GitHub ou repositório federado sem rebaixar o curso completo.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        val sampleExportJson = """
[
  {
    "op": "add",
    "path": "/exercises/custom_${selectedLanguage.id}_01",
    "value": {
      "exercise_id": "custom_${selectedLanguage.id}_01",
      "type": "WORD_ORDERING",
      "prompt": "$testPrompt",
      "script_language": "${selectedLanguage.id}",
      "kpy_core": "kpybara_cpp20_universal",
      "bytecode_hex": "${currentKpyPackage.bytecodeHex}",
      "srs_metadata": {
        "difficulty_rating": 2.2,
        "keywords": ["dev", "${selectedLanguage.id}", "comunidade"]
      }
    }
  }
]
                        """.trimIndent()

                        OutlinedTextField(
                            value = sampleExportJson,
                            onValueChange = {},
                            readOnly = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 200.dp, max = 320.dp),
                            textStyle = LocalTextStyle.current.copy(
                                fontFamily = FontFamily.Monospace,
                                fontSize = 11.sp
                            )
                        )

                        Button(
                            onClick = {
                                onToast("JSON Patch copiado e exportado para repositório federado!")
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Copiar Delta Patch RFC 6902")
                        }
                    }
                }
            }
        }
    }
}
