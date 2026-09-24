import 'lua_isolate_runner.dart';

/// Avaliador nativo em Dart para fallback transparente com 0% impacto no framerate
class DartNativeFallbackEvaluator {
  /// Assume a avaliação caso o Isolate estoure o timeout de 300ms ou lance exceção
  static EvaluationResult evaluateFallback({
    required String studentInput,
    required String expectedAnswer,
    required String exerciseType,
    String? rawError,
  }) {
    final cleanInput = _normalize(studentInput);
    final cleanExpected = _normalize(expectedAnswer);

    final distance = _levenshteinDistance(cleanInput, cleanExpected);
    final maxLength = cleanInput.length > cleanExpected.length
        ? cleanInput.length
        : cleanExpected.length;

    final similarity = maxLength == 0 ? 1.0 : (1.0 - (distance / maxLength));
    final isCorrect = similarity >= 0.85;

    final srsInterval = isCorrect ? (similarity > 0.95 ? 4 : 2) : 1;
    final nextLesson = isCorrect ? null : 'reinforcement_module_fallback';

    return EvaluationResult(
      isCorrect: isCorrect,
      scoreRatio: double.parse(similarity.toStringAsFixed(2)),
      feedbackMessage: isCorrect
          ? 'Correto (validado por Fallback Nativo)!'
          : 'Quase lá! A forma canônica é: $expectedAnswer',
      nextRecommendedLessonId: nextLesson,
      srsIntervalDays: srsInterval,
      usedFallback: true,
      telemetryError: rawError,
    );
  }

  static String _normalize(String text) {
    return text.trim().toLowerCase().replaceAll(RegExp(r'[^\w\s]'), '');
  }

  static int _levenshteinDistance(String s, String t) {
    if (s == t) return 0;
    if (s.isEmpty) return t.length;
    if (t.isEmpty) return s.length;

    List<int> v0 = List<int>.filled(t.length + 1, 0);
    List<int> v1 = List<int>.filled(t.length + 1, 0);

    for (int i = 0; i <= t.length; i++) {
      v0[i] = i;
    }

    for (int i = 0; i < s.length; i++) {
      v1[0] = i + 1;
      for (int j = 0; j < t.length; j++) {
        int cost = (s[i] == t[j]) ? 0 : 1;
        int minVal = v1[j] + 1;
        if (v0[j + 1] + 1 < minVal) minVal = v0[j + 1] + 1;
        if (v0[j] + cost < minVal) minVal = v0[j] + cost;
        v1[j + 1] = minVal;
      }
      for (int j = 0; j <= t.length; j++) {
        v0[j] = v1[j];
      }
    }
    return v0[t.length];
  }
}
