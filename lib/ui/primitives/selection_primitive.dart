import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import '../theme/kpy_theme.dart';

enum SelectionMode {
  multipleChoice, // Escolha única ou múltipla entre opções
  pairMatching,   // Associação de pares (esquerda <-> direita)
  fillInTheBlank, // Preenchimento de lacunas com opções sugeridas
}

class PairItem {
  final String id;
  final String text;
  PairItem({required this.id, required this.text});
}

/// [PRIMITIVA UNIVERSAL 1: SELECTION PRIMITIVE]
///
/// Gerencia exercícios de múltipla escolha, pares e lacunas.
/// Otimizado para alto desempenho em dispositivos low-end com microinterações
/// táteis (HapticFeedback.lightImpact) e transições de cor fluidas sem rebuild global.
class SelectionPrimitive extends StatefulWidget {
  final SelectionMode mode;
  final String? questionPrompt;
  final List<String> options;
  final int? selectedOptionIndex;
  final ValueChanged<int>? onOptionSelected;

  // Para Pair Matching
  final List<PairItem>? leftPairs;
  final List<PairItem>? rightPairs;
  final Map<String, String>? completedPairMatches; // leftId -> rightId
  final Function(String leftId, String rightId)? onPairMatched;

  // Para Fill In The Blank
  final String? sentenceTemplate; // Ex: "A capivara adora comer {blank} no lago."
  final String? filledValue;
  final ValueChanged<String>? onBlankFilled;

  const SelectionPrimitive({
    Key? key,
    this.mode = SelectionMode.multipleChoice,
    this.questionPrompt,
    this.options = const [],
    this.selectedOptionIndex,
    this.onOptionSelected,
    this.leftPairs,
    this.rightPairs,
    this.completedPairMatches,
    this.onPairMatched,
    this.sentenceTemplate,
    this.filledValue,
    this.onBlankFilled,
  }) : super(key: key);

  @override
  State<SelectionPrimitive> createState() => _SelectionPrimitiveState();
}

class _SelectionPrimitiveState extends State<SelectionPrimitive> {
  String? _selectedLeftPairId;
  String? _selectedRightPairId;

  void _triggerHaptic() {
    HapticFeedback.lightImpact();
  }

  @override
  Widget build(BuildContext context) {
    switch (widget.mode) {
      case SelectionMode.multipleChoice:
        return _buildMultipleChoice(context);
      case SelectionMode.pairMatching:
        return _buildPairMatching(context);
      case SelectionMode.fillInTheBlank:
        return _buildFillInBlank(context);
    }
  }

  // --- 1. MÚLTIPLA ESCOLHA ---
  Widget _buildMultipleChoice(BuildContext context) {
    final isDark = Theme.of(context).brightness == Brightness.dark;

    return Column(
      crossAxisAlignment: CrossAxisAlignment.stretch,
      mainAxisSize: MainAxisSize.min,
      children: [
        if (widget.questionPrompt != null) ...[
          Text(
            widget.questionPrompt!,
            style: Theme.of(context).textTheme.titleLarge?.copyWith(
              fontSize: 18,
              height: 1.3,
            ),
          ),
          const SizedBox(height: 16),
        ],
        ListView.separated(
          shrinkWrap: true,
          physics: const NeverScrollableScrollPhysics(),
          itemCount: widget.options.length,
          separatorBuilder: (_, __) => const SizedBox(height: 12),
          itemBuilder: (context, index) {
            final isSelected = widget.selectedOptionIndex == index;
            final optionText = widget.options[index];

            return _SelectionOptionCard(
              index: index,
              text: optionText,
              isSelected: isSelected,
              isDark: isDark,
              onTap: () {
                _triggerHaptic();
                widget.onOptionSelected?.call(index);
              },
            );
          },
        ),
      ],
    );
  }

  // --- 2. ASSOCIAÇÃO DE PARES ---
  Widget _buildPairMatching(BuildContext context) {
    final leftList = widget.leftPairs ?? [];
    final rightList = widget.rightPairs ?? [];
    final matches = widget.completedPairMatches ?? {};

    return Column(
      crossAxisAlignment: CrossAxisAlignment.stretch,
      mainAxisSize: MainAxisSize.min,
      children: [
        if (widget.questionPrompt != null) ...[
          Text(
            widget.questionPrompt!,
            style: Theme.of(context).textTheme.titleMedium,
          ),
          const SizedBox(height: 14),
        ],
        Row(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            // Coluna Esquerda
            Expanded(
              child: Column(
                children: leftList.map((item) {
                  final isMatched = matches.containsKey(item.id);
                  final isSelected = _selectedLeftPairId == item.id;
                  return _PairChip(
                    text: item.text,
                    isSelected: isSelected,
                    isMatched: isMatched,
                    onTap: isMatched
                        ? null
                        : () {
                            _triggerHaptic();
                            setState(() {
                              _selectedLeftPairId = item.id;
                              _checkPairCompletion();
                            });
                          },
                  );
                }).toList(),
              ),
            ),
            const SizedBox(width: 12),
            // Coluna Direita
            Expanded(
              child: Column(
                children: rightList.map((item) {
                  final isMatched = matches.containsValue(item.id);
                  final isSelected = _selectedRightPairId == item.id;
                  return _PairChip(
                    text: item.text,
                    isSelected: isSelected,
                    isMatched: isMatched,
                    onTap: isMatched
                        ? null
                        : () {
                            _triggerHaptic();
                            setState(() {
                              _selectedRightPairId = item.id;
                              _checkPairCompletion();
                            });
                          },
                  );
                }).toList(),
              ),
            ),
          ],
        ),
      ],
    );
  }

  void _checkPairCompletion() {
    if (_selectedLeftPairId != null && _selectedRightPairId != null) {
      widget.onPairMatched?.call(_selectedLeftPairId!, _selectedRightPairId!);
      setState(() {
        _selectedLeftPairId = null;
        _selectedRightPairId = null;
      });
    }
  }

  // --- 3. PREENCHIMENTO DE LACUNA ---
  Widget _buildFillInBlank(BuildContext context) {
    final template = widget.sentenceTemplate ?? "Complete: {blank}";
    final parts = template.split("{blank}");
    final isDark = Theme.of(context).brightness == Brightness.dark;

    return Column(
      crossAxisAlignment: CrossAxisAlignment.stretch,
      mainAxisSize: MainAxisSize.min,
      children: [
        // Frase com a lacuna estilizada
        Container(
          padding: const EdgeInsets.all(16.0),
          decoration: BoxDecoration(
            color: isDark ? KpyColors.paperCardDark : KpyColors.paperCardLight,
            borderRadius: KpyTheme.cardRadius,
            border: Border.all(
              color: isDark ? KpyColors.paperBorderDark : KpyColors.paperBorderLight,
              width: 2.0,
            ),
          ),
          child: Wrap(
            crossAxisAlignment: WrapCrossAlignment.center,
            spacing: 6.0,
            runSpacing: 8.0,
            children: [
              if (parts.isNotEmpty && parts[0].isNotEmpty)
                Text(
                  parts[0],
                  style: Theme.of(context).textTheme.titleMedium?.copyWith(fontSize: 18),
                ),
              // Slot da Lacuna
              Container(
                constraints: const BoxConstraints(minWidth: 80, minHeight: 36),
                padding: const EdgeInsets.symmetric(horizontal: 14.0, vertical: 6.0),
                decoration: BoxDecoration(
                  color: widget.filledValue != null
                      ? KpyColors.leafGreen.withOpacity(0.15)
                      : (isDark ? KpyColors.paperDark : const Color(0xFFEDE6D8)),
                  borderRadius: BorderRadius.circular(8),
                  border: Border.all(
                    color: widget.filledValue != null
                        ? KpyColors.leafGreen
                        : KpyColors.honeyYellow,
                    width: 2.0,
                  ),
                ),
                child: Text(
                  widget.filledValue ?? "______",
                  style: TextStyle(
                    fontSize: 17,
                    fontWeight: FontWeight.w700,
                    color: widget.filledValue != null
                        ? (isDark ? Colors.white : KpyColors.leafGreen)
                        : KpyColors.inkMutedLight,
                  ),
                ),
              ),
              if (parts.length > 1 && parts[1].isNotEmpty)
                Text(
                  parts[1],
                  style: Theme.of(context).textTheme.titleMedium?.copyWith(fontSize: 18),
                ),
            ],
          ),
        ),
        const SizedBox(height: 24),
        // Banco de Opções para preencher
        Text(
          "Selecione o termo correspondente:",
          style: Theme.of(context).textTheme.bodyMedium,
        ),
        const SizedBox(height: 12),
        Wrap(
          spacing: 10.0,
          runSpacing: 10.0,
          children: widget.options.map((option) {
            final isUsed = widget.filledValue == option;
            return ActionChip(
              label: Text(
                option,
                style: TextStyle(
                  fontSize: 15,
                  fontWeight: FontWeight.w600,
                  color: isUsed ? Colors.white : null,
                ),
              ),
              backgroundColor: isUsed ? KpyColors.capybaraBrown : null,
              padding: const EdgeInsets.symmetric(horizontal: 14, vertical: 10),
              elevation: isUsed ? 2 : 0,
              onPressed: () {
                _triggerHaptic();
                widget.onBlankFilled?.call(option);
              },
            );
          }).toList(),
        ),
      ],
    );
  }
}

/// Card de opção estilizado para seleção única/múltipla com feedback tátil e visual acolhedor
class _SelectionOptionCard extends StatelessWidget {
  final int index;
  final String text;
  final bool isSelected;
  final bool isDark;
  final VoidCallback onTap;

  const _SelectionOptionCard({
    Key? key,
    required this.index,
    required this.text,
    required this.isSelected,
    required this.isDark,
    required this.onTap,
  }) : super(key: key);

  @override
  Widget build(BuildContext context) {
    final borderColor = isSelected
        ? KpyColors.honeyYellow
        : (isDark ? KpyColors.paperBorderDark : KpyColors.paperBorderLight);

    final bgColor = isSelected
        ? KpyColors.honeyYellow.withOpacity(isDark ? 0.25 : 0.15)
        : (isDark ? KpyColors.paperCardDark : KpyColors.paperCardLight);

    return Material(
      color: Colors.transparent,
      borderRadius: KpyTheme.cardRadius,
      child: InkWell(
        onTap: onTap,
        borderRadius: KpyTheme.cardRadius,
        splashColor: KpyColors.honeyYellow.withOpacity(0.2),
        highlightColor: KpyColors.honeyYellow.withOpacity(0.1),
        child: AnimatedContainer(
          duration: const Duration(milliseconds: 180),
          curve: Curves.easeOut,
          constraints: const BoxConstraints(minHeight: KpyTheme.minTouchTargetSize),
          padding: const EdgeInsets.symmetric(horizontal: 16.0, vertical: 14.0),
          decoration: BoxDecoration(
            color: bgColor,
            borderRadius: KpyTheme.cardRadius,
            border: Border.all(color: borderColor, width: isSelected ? 2.5 : 1.5),
            boxShadow: [
              if (isSelected)
                BoxShadow(
                  color: KpyColors.honeyYellow.withOpacity(0.2),
                  blurRadius: 6,
                  offset: const Offset(0, 2),
                ),
            ],
          ),
          child: Row(
            children: [
              // Badge de índice doodle A, B, C, D
              Container(
                width: 32,
                height: 32,
                alignment: Alignment.Center,
                decoration: BoxDecoration(
                  color: isSelected
                      ? KpyColors.honeyYellow
                      : (isDark ? const Color(0xFF332B25) : const Color(0xFFEFE8DA)),
                  borderRadius: BorderRadius.circular(8),
                ),
                child: Text(
                  String.fromCharCode(65 + index), // A, B, C...
                  style: TextStyle(
                    fontWeight: FontWeight.w800,
                    fontSize: 14,
                    color: isSelected
                        ? Colors.black87
                        : (isDark ? Colors.white70 : KpyColors.inkTextLight),
                  ),
                ),
              ),
              const SizedBox(width: 14),
              Expanded(
                child: Text(
                  text,
                  style: TextStyle(
                    fontSize: 16,
                    fontWeight: isSelected ? FontWeight.w700 : FontWeight.w500,
                    color: isDark ? KpyColors.inkTextDark : KpyColors.inkTextLight,
                  ),
                ),
              ),
              if (isSelected)
                const Icon(
                  Icons.check_circle_rounded,
                  color: KpyColors.honeyYellow,
                  size: 22,
                ),
            ],
          ),
        ),
      ),
    );
  }
}

/// Chip interativo para o exercício de associação de pares
class _PairChip extends StatelessWidget {
  final String text;
  final bool isSelected;
  final bool isMatched;
  final VoidCallback? onTap;

  const _PairChip({
    Key? key,
    required this.text,
    required this.isSelected,
    required this.isMatched,
    this.onTap,
  }) : super(key: key);

  @override
  Widget build(BuildContext context) {
    final isDark = Theme.of(context).brightness == Brightness.dark;

    Color bg;
    Color border;
    Color textColor;

    if (isMatched) {
      bg = KpyColors.softSuccessBg;
      border = KpyColors.softSuccess;
      textColor = KpyColors.leafGreen;
    } else if (isSelected) {
      bg = KpyColors.honeyYellow.withOpacity(0.2);
      border = KpyColors.honeyYellow;
      textColor = isDark ? Colors.white : KpyColors.inkTextLight;
    } else {
      bg = isDark ? KpyColors.paperCardDark : KpyColors.paperCardLight;
      border = isDark ? KpyColors.paperBorderDark : KpyColors.paperBorderLight;
      textColor = isDark ? KpyColors.inkTextDark : KpyColors.inkTextLight;
    }

    return Padding(
      padding: const EdgeInsets.only(bottom: 10.0),
      child: InkWell(
        onTap: onTap,
        borderRadius: BorderRadius.circular(10),
        child: AnimatedContainer(
          duration: const Duration(milliseconds: 150),
          constraints: const BoxConstraints(minHeight: KpyTheme.minTouchTargetSize),
          padding: const EdgeInsets.symmetric(horizontal: 12.0, vertical: 12.0),
          alignment: Alignment.center,
          decoration: BoxDecoration(
            color: bg,
            borderRadius: BorderRadius.circular(10),
            border: Border.all(color: border, width: isSelected || isMatched ? 2.0 : 1.5),
          ),
          child: Text(
            text,
            textAlign: TextAlign.center,
            style: TextStyle(
              fontSize: 14,
              fontWeight: isMatched || isSelected ? FontWeight.w700 : FontWeight.w500,
              color: textColor,
            ),
          ),
        ),
      ),
    );
  }
}
