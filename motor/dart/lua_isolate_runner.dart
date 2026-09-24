// ignore_for_file: avoid_print
import 'dart:async';
import 'dart:isolate';

/// Resposta unificada do motor de avaliação de exercícios (EvaluationResult)
/// Localização: motor/dart/lua_isolate_runner.dart
class EvaluationResult {
  final bool isCorrect;
  final double scoreRatio;
  final String feedbackMessage;
  final String? nextRecommendedLessonId;
  final int srsIntervalDays;
  final bool usedFallback;
  final String? telemetryError;

  EvaluationResult({
    required this.isCorrect,
    required this.scoreRatio,
    required this.feedbackMessage,
    this.nextRecommendedLessonId,
    required this.srsIntervalDays,
    required this.usedFallback,
    this.telemetryError,
  });

  Map<String, dynamic> toJson() => {
    'is_correct': isCorrect,
    'score_ratio': scoreRatio,
    'feedback_message': feedbackMessage,
    'next_recommended_lesson_id': nextRecommendedLessonId,
    'srs_interval_days': srsIntervalDays,
    'used_fallback': usedFallback,
    'telemetry_error': telemetryError,
  };
}

/// Mensagem enviada para o Isolate de execução Lua
class LuaExecutionRequest {
  final String luaScript;
  final String studentInput;
  final String expectedAnswer;
  final String exerciseType;
  final SendPort replyPort;

  LuaExecutionRequest({
    required this.luaScript,
    required this.studentInput,
    required this.expectedAnswer,
    required this.exerciseType,
    required this.replyPort,
  });
}

/// Motor de Scripting isolado em Dart Isolate com sandbox e timeout rígido de 300ms
class LuaIsolateRunner {
  static const int executionTimeoutMs = 300;

  /// Executa o script em um Isolate separado para garantir 0 impacto no framerate da UI (120 FPS cravados)
  static Future<EvaluationResult> evaluate({
    required String luaScript,
    required String studentInput,
    required String expectedAnswer,
    required String exerciseType,
  }) async {
    final receivePort = ReceivePort();
    Isolate? isolate;

    try {
      final request = LuaExecutionRequest(
        luaScript: luaScript,
        studentInput: studentInput,
        expectedAnswer: expectedAnswer,
        exerciseType: exerciseType,
        replyPort: receivePort.sendPort,
      );

      isolate = await Isolate.spawn(_isolatedLuaEntrypoint, request);

      final result = await (receivePort.first as Future<dynamic>).timeout(
        const Duration(milliseconds: executionTimeoutMs),
        onTimeout: () {
          throw TimeoutException('Lua execution exceeded 300ms strict sandbox limit');
        },
      );

      return result as EvaluationResult;
    } catch (e) {
      // Falha ou timeout: o isolamento protege o loop principal da UI
      return EvaluationResult(
        isCorrect: false,
        scoreRatio: 0.0,
        feedbackMessage: 'Erro na execução isolada do script.',
        srsIntervalDays: 1,
        usedFallback: true,
        telemetryError: e.toString(),
      );
    } finally {
      isolate?.kill(priority: Isolate.immediate);
      receivePort.close();
    }
  }

  /// Ponto de entrada do Dart Isolate
  static void _isolatedLuaEntrypoint(LuaExecutionRequest request) {
    try {
      // Sandbox: no ambiente real FFI Lua, as tabelas `os`, `io`, `package` e `debug`
      // são expurgadas antes de executar `luaL_loadstring`.
      // Aqui simulamos a execução protegida da lógica customizada:
      final cleanInput = request.studentInput.trim().toLowerCase();
      final cleanExpected = request.expectedAnswer.trim().toLowerCase();

      final isExact = cleanInput == cleanExpected;
      final score = isExact ? 1.0 : (cleanInput.contains(cleanExpected) ? 0.7 : 0.0);

      // Dynamic Branching (Árvore de aprendizado adaptativa):
      String? nextLesson;
      if (!isExact) {
        nextLesson = 'reinforcement_branch_${request.exerciseType.toLowerCase()}';
      }

      final result = EvaluationResult(
        isCorrect: isExact,
        scoreRatio: score,
        feedbackMessage: isExact
            ? 'Resposta excelente!'
            : 'Atenção à concordância esperada: $cleanExpected',
        nextRecommendedLessonId: nextLesson,
        srsIntervalDays: isExact ? 3 : 1,
        usedFallback: false,
        telemetryError: null,
      );

      request.replyPort.send(result);
    } catch (err) {
      request.replyPort.send(
        EvaluationResult(
          isCorrect: false,
          scoreRatio: 0.0,
          feedbackMessage: 'Falha de sintaxe na VM.',
          srsIntervalDays: 1,
          usedFallback: true,
          telemetryError: err.toString(),
        ),
      );
    }
  }
}
