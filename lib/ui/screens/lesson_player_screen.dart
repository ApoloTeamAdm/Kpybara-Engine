import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import '../theme/kpy_theme.dart';
import '../widgets/kpy_mascot_widget.dart';
import '../widgets/top_stats_bar.dart';
import '../primitives/selection_primitive.dart';
import '../primitives/sequence_primitive.dart';
import '../primitives/hotspot_image_primitive.dart';
import '../primitives/canvas_grid_primitive.dart';

/// Modelo de Exercício Genérico para o Player
enum ExerciseType {
  selection,
  sequence,
  hotspot,
  grid,
}

class LessonExerciseData {
  final String id;
  final ExerciseType type;
  final String prompt;
  final String targetSentence;
  final String? audioText;
  final List<String> options;
  final int correctOptionIndex;
  final List<SequenceItem>? sequenceItems;
  final List<HotspotPoint>? hotspots;
  final String? correctHotspotId;
  final List<List<String>>? gridLetters;
  final Set<GridCellCoordinate>? gridTargets;

  LessonExerciseData({
    required this.id,
    required this.type,
    required this.prompt,
    required this.targetSentence,
    this.audioText,
    this.options = const [],
    this.correctOptionIndex = 0,
    this.sequenceItems,
    this.hotspots,
    this.correctHotspotId,
    this.gridLetters,
    this.gridTargets,
  });
}

/// Estado da Lição do Aluno
class LessonState {
  final int currentIndex;
  final List<LessonExerciseData> exercises;
  final int hearts;
  final int xpEarned;
  final int streakDays;
  final bool isAnswerChecked;
  final bool? isLastAnswerCorrect;
  final String? feedbackMessage;
  final KpyMascotState mascotState;
  final ConnectivityStatus connectivity;

  LessonState({
    required this.currentIndex,
    required this.exercises,
    this.hearts = 5,
    this.xpEarned = 140,
    this.streakDays = 5,
    this.isAnswerChecked = false,
    this.isLastAnswerCorrect,
    this.feedbackMessage,
    this.mascotState = KpyMascotState.idle,
    this.connectivity = ConnectivityStatus.online,
  });

  double get progressRatio =>
      exercises.isEmpty ? 0.0 : (currentIndex) / exercises.length;

  LessonExerciseData get currentExercise => exercises[currentIndex];
  bool get isCompleted => currentIndex >= exercises.length;

  LessonState copyWith({
    int? currentIndex,
    List<LessonExerciseData>? exercises,
    int? hearts,
    int? xpEarned,
    int? streakDays,
    bool? isAnswerChecked,
    bool? isLastAnswerCorrect,
    String? feedbackMessage,
    KpyMascotState? mascotState,
    ConnectivityStatus? connectivity,
  }) {
    return LessonState(
      currentIndex: currentIndex ?? this.currentIndex,
      exercises: exercises ?? this.exercises,
      hearts: hearts ?? this.hearts,
      xpEarned: xpEarned ?? this.xpEarned,
      streakDays: streakDays ?? this.streakDays,
      isAnswerChecked: isAnswerChecked ?? this.isAnswerChecked,
      isLastAnswerCorrect: isLastAnswerCorrect ?? this.isLastAnswerCorrect,
      feedbackMessage: feedbackMessage ?? this.feedbackMessage,
      mascotState: mascotState ?? this.mascotState,
      connectivity: connectivity ?? this.connectivity,
    );
  }
}

/// [TELA PRINCIPAL: LESSON PLAYER SCREEN]
///
/// Interface de execução de exercícios da lição com barra de progresso,
/// HUD superior, sintetizador TTS nativo, renderizadores primitivos universais
/// e BottomSheet de feedback reativo com o mascote Kpy.
class LessonPlayerScreen extends StatefulWidget {
  final List<LessonExerciseData>? customExercises;
  final VoidCallback? onLessonFinished;

  const LessonPlayerScreen({
    Key? key,
    this.customExercises,
    this.onLessonFinished,
  }) : super(key: key);

  @override
  State<LessonPlayerScreen> createState() => _LessonPlayerScreenState();
}

class _LessonPlayerScreenState extends State<LessonPlayerScreen> {
  late LessonState _state;

  // Resposta local do exercício corrente
  int? _selectedOptionIndex;
  List<SequenceItem>? _reorderedSequence;
  String? _selectedHotspotId;
  final Set<GridCellCoordinate> _selectedGridCells = {};
  bool _isPlayingAudio = false;

  @override
  void initState() {
    super.initState();
    final defaultExercises = [
      LessonExerciseData(
        id: "ex_01_mult",
        type: ExerciseType.selection,
        prompt: "Como se diz 'A capivara nada no lago' em Esperanto?",
        targetSentence: "La kapibaro nagas en la lago.",
        audioText: "La kapibaro nagas en la lago.",
        options: [
          "La kapibaro kuras sur la strato.",
          "La kapibaro nagas en la lago.",
          "La kato dormas sur la tablo.",
          "La birdo kantas en la arbo.",
        ],
        correctOptionIndex: 1,
      ),
      LessonExerciseData(
        id: "ex_02_seq",
        type: ExerciseType.sequence,
        prompt: "Ordene os passos do ciclo de vida da folha na natureza:",
        targetSentence: "Broto -> Folha Verde -> Amarelecimento -> Decomposição",
        sequenceItems: [
          SequenceItem(id: "s1", text: "Broto germinando no galho", stepBadge: "Início"),
          SequenceItem(id: "s2", text: "Folha verde realizando fotossíntese"),
          SequenceItem(id: "s3", text: "Folha amarelada caindo ao solo"),
          SequenceItem(id: "s4", text: "Nutrientes devolvidos à terra", stepBadge: "Reciclagem"),
        ],
      ),
      LessonExerciseData(
        id: "ex_03_spot",
        type: ExerciseType.hotspot,
        prompt: "Toque no sensor ótico principal no diagrama esquemático:",
        targetSentence: "Ponto B (Sensor)",
        hotspots: [
          HotspotPoint(id: "p1", label: "A", normalizedX: 0.25, normalizedY: 0.35, description: "Fonte de Energia"),
          HotspotPoint(id: "p2", label: "B", normalizedX: 0.65, normalizedY: 0.40, description: "Sensor Ótico"),
          HotspotPoint(id: "p3", label: "C", normalizedX: 0.50, normalizedY: 0.75, description: "Microcontrolador"),
        ],
        correctHotspotId: "p2",
      ),
    ];

    _state = LessonState(
      currentIndex: 0,
      exercises: widget.customExercises ?? defaultExercises,
    );
  }

  void _playTtsAudio(String text) async {
    setState(() => _isPlayingAudio = true);
    HapticFeedback.lightImpact();
    // Simulação síncrona/assíncrona do NativeTtsEngine
    await Future.delayed(const Duration(milliseconds: 900));
    if (mounted) {
      setState(() => _isPlayingAudio = false);
    }
  }

  void _checkAnswer() {
    final currentEx = _state.currentExercise;
    bool isCorrect = false;

    switch (currentEx.type) {
      case ExerciseType.selection:
        isCorrect = (_selectedOptionIndex == currentEx.correctOptionIndex);
        break;
      case ExerciseType.sequence:
        isCorrect = true; // Demonstração aceita
        break;
      case ExerciseType.hotspot:
        isCorrect = (_selectedHotspotId == currentEx.correctHotspotId);
        break;
      case ExerciseType.grid:
        isCorrect = true;
        break;
    }

    HapticFeedback.mediumImpact();

    setState(() {
      _state = _state.copyWith(
        isAnswerChecked: true,
        isLastAnswerCorrect: isCorrect,
        hearts: isCorrect ? _state.hearts : (_state.hearts - 1).clamp(0, 5),
        xpEarned: isCorrect ? _state.xpEarned + 15 : _state.xpEarned,
        feedbackMessage: isCorrect
            ? "Boa! Mandou bem demais! A capivara Kpy tá orgulhosa."
            : "Eita, essa foi por pouco! Sem estresse, vamos revisar juntos.",
        mascotState: isCorrect ? KpyMascotState.success : KpyMascotState.tryAgain,
      );
    });
  }

  void _proceedNext() {
    if (_state.currentIndex + 1 < _state.exercises.length) {
      setState(() {
        _selectedOptionIndex = null;
        _reorderedSequence = null;
        _selectedHotspotId = null;
        _selectedGridCells.clear();
        _state = _state.copyWith(
          currentIndex: _state.currentIndex + 1,
          isAnswerChecked: false,
          isLastAnswerCorrect: null,
          feedbackMessage: null,
          mascotState: KpyMascotState.idle,
        );
      });
    } else {
      // Lição completa!
      setState(() {
        _state = _state.copyWith(
          mascotState: KpyMascotState.streakMilestone,
          feedbackMessage: "Parabéns! Lição concluída com sucesso!",
        );
      });
      widget.onLessonFinished?.call();
    }
  }

  @override
  Widget build(BuildContext context) {
    final isDark = Theme.of(context).brightness == Brightness.dark;
    final ex = _state.currentExercise;

    return Scaffold(
      backgroundColor: isDark ? KpyColors.paperDark : KpyColors.paperLight,
      body: SafeArea(
        child: Column(
          children: [
            // 1. Barra Superior de Gamificação (TopStatsBar)
            TopStatsBar(
              streakDays: _state.streakDays,
              xpEarned: _state.xpEarned,
              hearts: _state.hearts,
              connectivity: _state.connectivity,
              onConnectivityTap: () {
                // Alterna modo offline para testar o Kpy de mochilinha
                setState(() {
                  final nextStatus = _state.connectivity == ConnectivityStatus.online
                      ? ConnectivityStatus.offlineLocal
                      : ConnectivityStatus.online;
                  _state = _state.copyWith(
                    connectivity: nextStatus,
                    mascotState: nextStatus == ConnectivityStatus.offlineLocal
                        ? KpyMascotState.offlineMode
                        : KpyMascotState.idle,
                  );
                });
              },
            ),

            // 2. Barra de Progresso Suave da Lição
            Padding(
              padding: const EdgeInsets.symmetric(horizontal: 16.0, vertical: 8.0),
              child: Row(
                children: [
                  IconButton(
                    icon: const Icon(Icons.close_rounded),
                    onPressed: () => Navigator.of(context).maybePop(),
                    tooltip: "Sair da Lição",
                  ),
                  const SizedBox(width: 8),
                  Expanded(
                    child: ClipRRect(
                      borderRadius: BorderRadius.circular(8),
                      child: LinearProgressIndicator(
                        value: (_state.currentIndex + 1) / _state.exercises.length,
                        minHeight: 10,
                        backgroundColor: isDark ? const Color(0xFF332B25) : const Color(0xFFE2D9C8),
                        valueColor: const AlwaysStoppedAnimation<Color>(KpyColors.leafGreen),
                      ),
                    ),
                  ),
                  const SizedBox(width: 12),
                  Text(
                    "${_state.currentIndex + 1}/${_state.exercises.length}",
                    style: const TextStyle(fontWeight: FontWeight.w700, fontSize: 13),
                  ),
                ],
              ),
            ),

            // 3. Área Central de Exercício (com Mascote e Áudio)
            Expanded(
              child: SingleChildScrollView(
                padding: const EdgeInsets.symmetric(horizontal: 16.0, vertical: 8.0),
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.stretch,
                  children: [
                    // Linha com o Mascote Kpy e Botão de Áudio TTS
                    Row(
                      crossAxisAlignment: CrossAxisAlignment.center,
                      children: [
                        KpyMascotWidget(
                          state: _state.mascotState,
                          size: 72,
                        ),
                        const SizedBox(width: 12),
                        if (ex.audioText != null) ...[
                          ElevatedButton.icon(
                            style: ElevatedButton.styleFrom(
                              backgroundColor: KpyColors.capybaraBrown,
                              foregroundColor: Colors.white,
                              padding: const EdgeInsets.symmetric(horizontal: 14, vertical: 10),
                            ),
                            icon: Icon(
                              _isPlayingAudio ? Icons.volume_up_rounded : Icons.play_arrow_rounded,
                              size: 20,
                            ),
                            label: Text(_isPlayingAudio ? "Ouvindo..." : "Pronúncia"),
                            onPressed: () => _playTtsAudio(ex.audioText!),
                          ),
                        ],
                      ],
                    ),

                    const SizedBox(height: 16),

                    // Renderizador da Primitiva Apropriada
                    _buildActivePrimitive(ex),

                    const SizedBox(height: 100), // Espaço para o BottomSheet fixo
                  ],
                ),
              ),
            ),

            // 4. Painel Inferior de Checagem / BottomSheet de Feedback
            _buildBottomCheckPanel(context, isDark),
          ],
        ),
      ),
    );
  }

  Widget _buildActivePrimitive(LessonExerciseData ex) {
    switch (ex.type) {
      case ExerciseType.selection:
        return SelectionPrimitive(
          mode: SelectionMode.multipleChoice,
          questionPrompt: ex.prompt,
          options: ex.options,
          selectedOptionIndex: _selectedOptionIndex,
          onOptionSelected: (idx) {
            if (!_state.isAnswerChecked) {
              setState(() => _selectedOptionIndex = idx);
            }
          },
        );

      case ExerciseType.sequence:
        return SequencePrimitive(
          prompt: ex.prompt,
          initialItems: ex.sequenceItems ?? [],
          onReorderCompleted: (items) {
            _reorderedSequence = items;
          },
        );

      case ExerciseType.hotspot:
        return HotspotImagePrimitive(
          prompt: ex.prompt,
          hotspots: ex.hotspots ?? [],
          selectedHotspotId: _selectedHotspotId,
          onHotspotSelected: (spot) {
            if (!_state.isAnswerChecked) {
              setState(() => _selectedHotspotId = spot.id);
            }
          },
        );

      case ExerciseType.grid:
        return CanvasGridPrimitive(
          prompt: ex.prompt,
          rows: ex.gridLetters?.length ?? 4,
          cols: ex.gridLetters?.first.length ?? 4,
          cellValues: ex.gridLetters ?? [["A", "B"], ["C", "D"]],
          selectedCells: _selectedGridCells,
          onCellTapped: (coord) {
            if (!_state.isAnswerChecked) {
              setState(() {
                if (_selectedGridCells.contains(coord)) {
                  _selectedGridCells.remove(coord);
                } else {
                  _selectedGridCells.add(coord);
                }
              });
            }
          },
        );
    }
  }

  Widget _buildBottomCheckPanel(BuildContext context, bool isDark) {
    final hasSelection = _selectedOptionIndex != null ||
        _selectedHotspotId != null ||
        _reorderedSequence != null ||
        _selectedGridCells.isNotEmpty;

    final isChecked = _state.isAnswerChecked;
    final isCorrect = _state.isLastAnswerCorrect == true;

    Color panelBg;
    if (isChecked) {
      panelBg = isCorrect ? KpyColors.softSuccessBg : KpyColors.softTryAgainBg;
    } else {
      panelBg = isDark ? KpyColors.paperCardDark : KpyColors.paperCardLight;
    }

    return Container(
      padding: const EdgeInsets.all(16.0),
      decoration: BoxDecoration(
        color: panelBg,
        border: Border(
          top: BorderSide(
            color: isChecked
                ? (isCorrect ? KpyColors.softSuccess : KpyColors.softTryAgain)
                : (isDark ? KpyColors.paperBorderDark : KpyColors.paperBorderLight),
            width: 2.0,
          ),
        ),
      ),
      child: Column(
        mainAxisSize: MainAxisSize.min,
        children: [
          if (isChecked && _state.feedbackMessage != null) ...[
            Row(
              children: [
                Icon(
                  isCorrect ? Icons.check_circle_rounded : Icons.info_outline_rounded,
                  color: isCorrect ? KpyColors.leafGreen : KpyColors.softTryAgain,
                  size: 26,
                ),
                const SizedBox(width: 10),
                Expanded(
                  child: Text(
                    _state.feedbackMessage!,
                    style: TextStyle(
                      fontWeight: FontWeight.w700,
                      fontSize: 14,
                      color: isCorrect ? KpyColors.leafGreen : KpyColors.softTryAgain,
                    ),
                  ),
                ),
              ],
            ),
            const SizedBox(height: 12),
          ],
          SizedBox(
            width: double.infinity,
            height: 52,
            child: ElevatedButton(
              style: ElevatedButton.styleFrom(
                backgroundColor: isChecked
                    ? (isCorrect ? KpyColors.leafGreen : KpyColors.capybaraBrown)
                    : (hasSelection ? KpyColors.leafGreen : Colors.grey.shade400),
                foregroundColor: Colors.white,
                shape: RoundedRectangleBorder(
                  borderRadius: BorderRadius.circular(12),
                ),
              ),
              onPressed: !hasSelection && !isChecked
                  ? null
                  : (isChecked ? _proceedNext : _checkAnswer),
              child: Text(
                isChecked
                    ? (_state.currentIndex + 1 >= _state.exercises.length ? "FINALIZAR LIÇÃO" : "CONTINUAR")
                    : "VERIFICAR RESPOSTA",
                style: const TextStyle(fontSize: 16, fontWeight: FontWeight.w800),
              ),
            ),
          ),
        ],
      ),
    );
  }
}
