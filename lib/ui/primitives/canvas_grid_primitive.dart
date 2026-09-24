import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import '../theme/kpy_theme.dart';

class GridCellCoordinate {
  final int row;
  final int col;
  const GridCellCoordinate(this.row, this.col);

  @override
  bool operator ==(Object other) =>
      identical(this, other) ||
      other is GridCellCoordinate && runtimeType == other.runtimeType && row == other.row && col == other.col;

  @override
  int get hashCode => row.hashCode ^ col.hashCode;
}

/// [PRIMITIVA UNIVERSAL 4: CANVAS GRID PRIMITIVE]
///
/// Matriz 2D interativa ($N \times M$) para tabuleiros de lógica, caça-palavras,
/// circuitos lógicos ou palavras cruzadas micro-learning.
/// Altamente responsiva e eficiente em aparelhos de entrada.
class CanvasGridPrimitive extends StatelessWidget {
  final int rows;
  final int cols;
  final String? prompt;
  final List<List<String>> cellValues; // Letras ou símbolos em cada célula
  final Set<GridCellCoordinate> selectedCells;
  final Set<GridCellCoordinate> solvedCells; // Células já acertadas
  final ValueChanged<GridCellCoordinate> onCellTapped;

  const CanvasGridPrimitive({
    Key? key,
    this.rows = 5,
    this.cols = 5,
    this.prompt,
    required this.cellValues,
    required this.selectedCells,
    this.solvedCells = const {},
    required this.onCellTapped,
  }) : super(key: key);

  @override
  Widget build(BuildContext context) {
    final isDark = Theme.of(context).brightness == Brightness.dark;

    return Column(
      crossAxisAlignment: CrossAxisAlignment.stretch,
      mainAxisSize: MainAxisSize.min,
      children: [
        if (prompt != null) ...[
          Text(
            prompt!,
            style: Theme.of(context).textTheme.titleMedium?.copyWith(
              fontWeight: FontWeight.w700,
            ),
          ),
          const SizedBox(height: 12),
        ],
        // Container da Matriz Centralizada
        Center(
          child: Container(
            padding: const EdgeInsets.all(8.0),
            decoration: BoxDecoration(
              color: isDark ? KpyColors.paperCardDark : KpyColors.paperCardLight,
              borderRadius: KpyTheme.cardRadius,
              border: Border.all(
                color: isDark ? KpyColors.paperBorderDark : KpyColors.paperBorderLight,
                width: 2.0,
              ),
              boxShadow: [
                BoxShadow(
                  color: Colors.black.withOpacity(0.04),
                  blurRadius: 4,
                  offset: const Offset(0, 2),
                ),
              ],
            ),
            child: Column(
              mainAxisSize: MainAxisSize.min,
              children: List.generate(rows, (r) {
                return Row(
                  mainAxisSize: MainAxisSize.min,
                  children: List.generate(cols, (c) {
                    final coord = GridCellCoordinate(r, c);
                    final char = (r < cellValues.length && c < cellValues[r].length)
                        ? cellValues[r][c]
                        : "";
                    final isSelected = selectedCells.contains(coord);
                    final isSolved = solvedCells.contains(coord);

                    return _GridCellWidget(
                      char: char,
                      isSelected: isSelected,
                      isSolved: isSolved,
                      isDark: isDark,
                      onTap: () {
                        HapticFeedback.selectionClick();
                        onCellTapped(coord);
                      },
                    );
                  }),
                );
              }),
            ),
          ),
        ),
      ],
    );
  }
}

class _GridCellWidget extends StatelessWidget {
  final String char;
  final bool isSelected;
  final bool isSolved;
  final bool isDark;
  final VoidCallback onTap;

  const _GridCellWidget({
    Key? key,
    required this.char,
    required this.isSelected,
    required this.isSolved,
    required this.isDark,
    required this.onTap,
  }) : super(key: key);

  @override
  Widget build(BuildContext context) {
    Color bg;
    Color border;
    Color textCol;

    if (isSolved) {
      bg = KpyColors.softSuccessBg;
      border = KpyColors.softSuccess;
      textCol = KpyColors.leafGreen;
    } else if (isSelected) {
      bg = KpyColors.honeyYellow;
      border = KpyColors.capybaraBrown;
      textCol = Colors.black87;
    } else {
      bg = isDark ? const Color(0xFF2E2722) : const Color(0xFFFAF5EC);
      border = isDark ? KpyColors.paperBorderDark : KpyColors.paperBorderLight;
      textCol = isDark ? KpyColors.inkTextDark : KpyColors.inkTextLight;
    }

    return Padding(
      padding: const EdgeInsets.all(3.0),
      child: Material(
        color: Colors.transparent,
        child: InkWell(
          onTap: onTap,
          borderRadius: BorderRadius.circular(8),
          child: AnimatedContainer(
            duration: const Duration(milliseconds: 140),
            width: 48, // Mínimo acessível 48x48
            height: 48,
            alignment: Alignment.center,
            decoration: BoxDecoration(
              color: bg,
              borderRadius: BorderRadius.circular(8),
              border: Border.all(color: border, width: isSelected ? 2.5 : 1.5),
            ),
            child: Text(
              char,
              style: TextStyle(
                fontSize: 18,
                fontWeight: FontWeight.w800,
                color: textCol,
              ),
            ),
          ),
        ),
      ),
    );
  }
}
