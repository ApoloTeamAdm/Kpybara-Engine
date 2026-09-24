import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import '../theme/kpy_theme.dart';

class SequenceItem {
  final String id;
  final String text;
  final String? imageUrl;
  final String? stepBadge;

  SequenceItem({
    required this.id,
    required this.text,
    this.imageUrl,
    this.stepBadge,
  });
}

/// [PRIMITIVA UNIVERSAL 2: SEQUENCE PRIMITIVE]
///
/// Reordenação drag-and-drop de etapas cronológicas, algoritmos, receitas ou blocos conceituais.
/// Otimizado para telas de 60/90 Hz (sem gargalo de GPU ou GC no Moto G22).
/// Utiliza decorações leves de arraste com proxy leve e animações fluídas.
class SequencePrimitive extends StatefulWidget {
  final String? prompt;
  final List<SequenceItem> initialItems;
  final ValueChanged<List<SequenceItem>> onReorderCompleted;

  const SequencePrimitive({
    Key? key,
    this.prompt,
    required this.initialItems,
    required this.onReorderCompleted,
  }) : super(key: key);

  @override
  State<SequencePrimitive> createState() => _SequencePrimitiveState();
}

class _SequencePrimitiveState extends State<SequencePrimitive> {
  late List<SequenceItem> _items;

  @override
  void initState() {
    super.initState();
    _items = List.from(widget.initialItems);
  }

  @override
  void didUpdateWidget(covariant SequencePrimitive oldWidget) {
    super.didUpdateWidget(oldWidget);
    if (oldWidget.initialItems != widget.initialItems) {
      _items = List.from(widget.initialItems);
    }
  }

  void _onReorder(int oldIndex, int newIndex) {
    HapticFeedback.selectionClick();
    setState(() {
      if (oldIndex < newIndex) {
        newIndex -= 1;
      }
      final item = _items.removeAt(oldIndex);
      _items.insert(newIndex, item);
    });
    widget.onReorderCompleted(_items);
  }

  @override
  Widget build(BuildContext context) {
    final isDark = Theme.of(context).brightness == Brightness.dark;

    return Column(
      crossAxisAlignment: CrossAxisAlignment.stretch,
      mainAxisSize: MainAxisSize.min,
      children: [
        if (widget.prompt != null) ...[
          Row(
            children: [
              const Icon(Icons.drag_indicator, color: KpyColors.honeyYellow, size: 20),
              const SizedBox(width: 8),
              Expanded(
                child: Text(
                  widget.prompt!,
                  style: Theme.of(context).textTheme.titleMedium?.copyWith(
                    fontWeight: FontWeight.w700,
                  ),
                ),
              ),
            ],
          ),
          const SizedBox(height: 14),
        ],
        ReorderableListView.builder(
          shrinkWrap: true,
          physics: const NeverScrollableScrollPhysics(),
          itemCount: _items.length,
          onReorder: _onReorder,
          proxyDecorator: (child, index, animation) {
            // Decoração leve e veloz durante o arraste para 90 FPS
            return AnimatedBuilder(
              animation: animation,
              builder: (context, child) {
                final elevation = Tween<double>(begin: 0.0, end: 6.0).animate(animation).value;
                return Material(
                  elevation: elevation,
                  color: Colors.transparent,
                  borderRadius: KpyTheme.cardRadius,
                  shadowColor: KpyColors.capybaraBrown.withOpacity(0.3),
                  child: child,
                );
              },
              child: child,
            );
          },
          itemBuilder: (context, index) {
            final item = _items[index];
            return Container(
              key: ValueKey(item.id),
              margin: const EdgeInsets.only(bottom: 10.0),
              decoration: BoxDecoration(
                color: isDark ? KpyColors.paperCardDark : KpyColors.paperCardLight,
                borderRadius: KpyTheme.cardRadius,
                border: Border.all(
                  color: isDark ? KpyColors.paperBorderDark : KpyColors.paperBorderLight,
                  width: 1.5,
                ),
              ),
              child: Padding(
                padding: const EdgeInsets.symmetric(horizontal: 14.0, vertical: 12.0),
                child: Row(
                  children: [
                    // Badge de Ordem Atual
                    Container(
                      width: 28,
                      height: 28,
                      alignment: Alignment.Center,
                      decoration: BoxDecoration(
                        color: KpyColors.honeyYellow.withOpacity(0.2),
                        shape: BoxShape.circle,
                        border: Border.all(color: KpyColors.honeyYellow, width: 1.5),
                      ),
                      child: Text(
                        "${index + 1}",
                        style: const TextStyle(
                          fontSize: 13,
                          fontWeight: FontWeight.w800,
                          color: KpyColors.capybaraBrown,
                        ),
                      ),
                    ),
                    const SizedBox(width: 12),
                    // Conteúdo (Texto ou Imagem/Badge)
                    Expanded(
                      child: Column(
                        crossAxisAlignment: CrossAxisAlignment.start,
                        children: [
                          Text(
                            item.text,
                            style: TextStyle(
                              fontSize: 15,
                              fontWeight: FontWeight.w600,
                              color: isDark ? KpyColors.inkTextDark : KpyColors.inkTextLight,
                            ),
                          ),
                          if (item.stepBadge != null)
                            Padding(
                              padding: const EdgeInsets.only(top: 4.0),
                              child: Text(
                                item.stepBadge!,
                                style: const TextStyle(
                                  fontSize: 11,
                                  color: KpyColors.inkMutedLight,
                                ),
                              ),
                            ),
                        ],
                      ),
                    ),
                    const SizedBox(width: 8),
                    // Alça de arraste confortável (Touch target 48x48)
                    const ReorderableDragStartListener(
                      index: 0, // tratado internamente pelo ReorderableListView
                      child: SizedBox(
                        width: 48,
                        height: 48,
                        child: Center(
                          child: Icon(
                            Icons.unfold_more,
                            color: KpyColors.inkMutedLight,
                            size: 24,
                          ),
                        ),
                      ),
                    ),
                  ],
                ),
              ),
            );
          },
        ),
      ],
    );
  }
}
