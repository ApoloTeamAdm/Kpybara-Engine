// Drift ORM Schema & Repository - Offline-First Storage (< 5MB memory footprint)
// Suporta Cursos Federados, Histórico do Aluno, RFC 6902 JSON Patches e Algoritmo SRS (SM-2)

import 'dart:convert';

/// Contrato dos feeds federados de repositório (index.json)
class FederatedCourseMeta {
  final String courseId;
  final String title;
  final String sourceLanguage;
  final String targetLanguage;
  final String latestVersion;
  final String downloadUrl;
  final double aqsiScore;
  final String? authorWalletLightning;

  FederatedCourseMeta({
    required this.courseId,
    required this.title,
    required this.sourceLanguage,
    required this.targetLanguage,
    required this.latestVersion,
    required this.downloadUrl,
    required this.aqsiScore,
    this.authorWalletLightning,
  });

  factory FederatedCourseMeta.fromJson(Map<String, dynamic> json) {
    return FederatedCourseMeta(
      courseId: json['course_id'] as String,
      title: json['title'] as String,
      sourceLanguage: json['source_language'] as String,
      targetLanguage: json['target_language'] as String,
      latestVersion: json['latest_version'] as String,
      downloadUrl: json['download_url'] as String,
      aqsiScore: (json['aqsi_score'] as num).toDouble(),
      authorWalletLightning: json['author_wallet_lightning'] as String?,
    );
  }
}

/// Estado do Algoritmo de Repetição Espaçada (SuperMemo-2)
class SrsCardState {
  final String cardId;
  final String exerciseId;
  final int repetitions;
  final int intervalDays;
  final double easeFactor;
  final int nextReviewTimestampMs;

  SrsCardState({
    required this.cardId,
    required this.exerciseId,
    required this.repetitions,
    required this.intervalDays,
    required this.easeFactor,
    required this.nextReviewTimestampMs,
  });

  /// Implementação do algoritmo SuperMemo-2 (SM-2)
  /// quality: 0 a 5 (0: apagão, 3: correto com dificuldade, 5: perfeito)
  SrsCardState calculateNext(int quality) {
    int nextRepetitions;
    int nextInterval;
    double nextEaseFactor;

    if (quality >= 3) {
      if (repetitions == 0) {
        nextInterval = 1;
      } else if (repetitions == 1) {
        nextInterval = 6;
      } else {
        nextInterval = (intervalDays * easeFactor).round();
      }
      nextRepetitions = repetitions + 1;
    } else {
      nextRepetitions = 0;
      nextInterval = 1;
    }

    // Fórmula SM-2: EF' = EF + (0.1 - (5 - q) * (0.08 + (5 - q) * 0.02))
    nextEaseFactor = easeFactor + (0.1 - (5 - quality) * (0.08 + (5 - quality) * 0.02));
    if (nextEaseFactor < 1.3) nextEaseFactor = 1.3;

    final nextReviewMs = DateTime.now().millisecondsSinceEpoch + (nextInterval * 86400000);

    return SrsCardState(
      cardId: cardId,
      exerciseId: exerciseId,
      repetitions: nextRepetitions,
      intervalDays: nextInterval,
      easeFactor: nextEaseFactor,
      nextReviewTimestampMs: nextReviewMs,
    );
  }
}

/// Motor de Aplicação de Atualizações Delta (RFC 6902 JSON Patch)
class DriftJsonPatchEngine {
  /// Aplica patches em um mapa serializado sem rebaixar o curso completo
  static Map<String, dynamic> applyPatch(
    Map<String, dynamic> originalJson,
    List<Map<String, dynamic>> patchOps,
  ) {
    final Map<String, dynamic> copy = jsonDecode(jsonEncode(originalJson));
    for (final op in patchOps) {
      final action = op['op'] as String?;
      final path = op['path'] as String?;
      final value = op['value'];

      if (action == 'replace' || action == 'add') {
        _setByPath(copy, path ?? '', value);
      }
    }
    return copy;
  }

  static void _setByPath(Map<String, dynamic> target, String path, dynamic value) {
    final segments = path.split('/').where((s) => s.isNotEmpty).toList();
    if (segments.isEmpty) return;

    dynamic current = target;
    for (int i = 0; i < segments.length - 1; i++) {
      final seg = segments[i];
      if (current is Map<String, dynamic>) {
        current = current[seg];
      }
    }
    final last = segments.last;
    if (current is Map<String, dynamic>) {
      current[last] = value;
    }
  }
}
