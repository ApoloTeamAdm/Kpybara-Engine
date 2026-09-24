import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import '../theme/kpy_theme.dart';

class HotspotPoint {
  final String id;
  final String label;
  final double normalizedX; // 0.0 a 1.0 (coordenada relativa à imagem)
  final double normalizedY; // 0.0 a 1.0
  final String? description;

  HotspotPoint({
    required this.id,
    required this.label,
    required this.normalizedX,
    required this.normalizedY,
    this.description,
  });
}

/// [PRIMITIVA UNIVERSAL 3: HOTSPOT IMAGE PRIMITIVE]
///
/// Permite interação sobre diagramas científicos, mapas, esquemáticos e circuitos.
/// Inclui suporte a Zoom/Pan sem perda de precisão ou consumo de memória com
/// [InteractiveViewer] e marcadores táteis de alta visibilidade.
class HotspotImagePrimitive extends StatefulWidget {
  final String? prompt;
  final String? imagePlaceholderLabel;
  final String? imageUrl;
  final List<HotspotPoint> hotspots;
  final String? selectedHotspotId;
  final ValueChanged<HotspotPoint> onHotspotSelected;

  const HotspotImagePrimitive({
    Key? key,
    this.prompt,
    this.imagePlaceholderLabel,
    this.imageUrl,
    required this.hotspots,
    this.selectedHotspotId,
    required this.onHotspotSelected,
  }) : super(key: key);

  @override
  State<HotspotImagePrimitive> createState() => _HotspotImagePrimitiveState();
}

class _HotspotImagePrimitiveState extends State<HotspotImagePrimitive> {
  final TransformationController _transController = TransformationController();

  @override
  void dispose() {
    _transController.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    final isDark = Theme.of(context).brightness == Brightness.dark;

    return Column(
      crossAxisAlignment: CrossAxisAlignment.stretch,
      mainAxisSize: MainAxisSize.min,
      children: [
        if (widget.prompt != null) ...[
          Text(
            widget.prompt!,
            style: Theme.of(context).textTheme.titleMedium?.copyWith(
              fontWeight: FontWeight.w700,
            ),
          ),
          const SizedBox(height: 12),
        ],
        // Container de Imagem com Zoom/Pan
        Container(
          height: 260,
          decoration: BoxDecoration(
            color: isDark ? const Color(0xFF141210) : const Color(0xFFF3ECE1),
            borderRadius: KpyTheme.cardRadius,
            border: Border.all(
              color: isDark ? KpyColors.paperBorderDark : KpyColors.paperBorderLight,
              width: 2.0,
            ),
          ),
          child: ClipRRect(
            borderRadius: KpyTheme.cardRadius,
            child: Stack(
              children: [
                InteractiveViewer(
                  transformationController: _transController,
                  minScale: 1.0,
                  maxScale: 3.5,
                  child: LayoutBuilder(
                    builder: (context, constraints) {
                      final boxWidth = constraints.maxWidth;
                      final boxHeight = constraints.maxHeight;

                      return Stack(
                        children: [
                          // Base Visual (Esquemático ou Doodle Ilustrado)
                          Container(
                            width: boxWidth,
                            height: boxHeight,
                            color: Colors.transparent,
                            child: widget.imageUrl != null
                                ? Image.network(
                                    widget.imageUrl!,
                                    fit: BoxFit.contain,
                                    errorBuilder: (_, __, ___) => _buildDoodleDiagram(boxWidth, boxHeight),
                                  )
                                : _buildDoodleDiagram(boxWidth, boxHeight),
                          ),
                          // Pontos Hotspots Sobrepostos
                          ...widget.hotspots.map((spot) {
                            final posX = spot.normalizedX * boxWidth;
                            final posY = spot.normalizedY * boxHeight;
                            final isSelected = widget.selectedHotspotId == spot.id;

                            return Positioned(
                              left: posX - 24,
                              top: posY - 24,
                              child: GestureDetector(
                                onTap: () {
                                  HapticFeedback.mediumImpact();
                                  widget.onHotspotSelected(spot);
                                },
                                child: _HotspotMarker(
                                  label: spot.label,
                                  isSelected: isSelected,
                                ),
                              ),
                            );
                          }).toList(),
                        ],
                      );
                    },
                  ),
                ),
                // Indicador de Zoom & Reset
                Positioned(
                  right: 8,
                  bottom: 8,
                  child: FloatingActionButton.small(
                    heroTag: "btn_reset_zoom",
                    backgroundColor: isDark ? KpyColors.paperCardDark : Colors.white,
                    foregroundColor: KpyColors.inkTextLight,
                    onPressed: () {
                      _transController.value = Matrix4.identity();
                    },
                    child: const Icon(Icons.center_focus_strong, size: 18),
                  ),
                ),
              ],
            ),
          ),
        ),
        const SizedBox(height: 12),
        // Legenda / Feedback do Ponto Selecionado
        if (widget.selectedHotspotId != null) ...[
          _buildSelectedInfoCard(isDark),
        ],
      ],
    );
  }

  Widget _buildSelectedInfoCard(bool isDark) {
    final selectedSpot = widget.hotspots.firstWhere(
      (s) => s.id == widget.selectedHotspotId,
      orElse: () => widget.hotspots.first,
    );

    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 14, vertical: 10),
      decoration: BoxDecoration(
        color: KpyColors.honeyYellow.withOpacity(isDark ? 0.2 : 0.12),
        borderRadius: BorderRadius.circular(10),
        border: Border.all(color: KpyColors.honeyYellow, width: 1.5),
      ),
      child: Row(
        children: [
          const Icon(Icons.place, color: KpyColors.honeyYellow, size: 20),
          const SizedBox(width: 8),
          Expanded(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(
                  "Ponto: ${selectedSpot.label}",
                  style: const TextStyle(fontWeight: FontWeight.w700, fontSize: 13),
                ),
                if (selectedSpot.description != null)
                  Text(
                    selectedSpot.description!,
                    style: const TextStyle(fontSize: 12),
                  ),
              ],
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildDoodleDiagram(double w, double h) {
    return CustomPaint(
      size: Size(w, h),
      painter: _DiagramDoodlePainter(label: widget.imagePlaceholderLabel ?? "Diagrama Interativo"),
    );
  }
}

class _HotspotMarker extends StatelessWidget {
  final String label;
  final bool isSelected;

  const _HotspotMarker({
    Key? key,
    required this.label,
    required this.isSelected,
  }) : super(key: key);

  @override
  Widget build(BuildContext context) {
    return Container(
      width: 48,
      height: 48,
      alignment: Alignment.center,
      child: AnimatedContainer(
        duration: const Duration(milliseconds: 200),
        width: isSelected ? 34 : 26,
        height: isSelected ? 34 : 26,
        decoration: BoxDecoration(
          color: isSelected ? KpyColors.honeyYellow : KpyColors.capybaraBrown,
          shape: BoxShape.circle,
          border: Border.all(color: Colors.white, width: 2.5),
          boxShadow: [
            BoxShadow(
              color: Colors.black.withOpacity(0.3),
              blurRadius: 4,
              offset: const Offset(0, 2),
            ),
          ],
        ),
        child: Center(
          child: Text(
            label,
            style: TextStyle(
              fontSize: isSelected ? 13 : 11,
              fontWeight: FontWeight.w800,
              color: isSelected ? Colors.black87 : Colors.white,
            ),
          ),
        ),
      ),
    );
  }
}

class _DiagramDoodlePainter extends CustomPainter {
  final String label;
  _DiagramDoodlePainter({required this.label});

  @override
  void paint(Canvas canvas, Size size) {
    final strokePaint = Paint()
      ..color = const Color(0xFFC7BCAB)
      ..style = PaintingStyle.stroke
      ..strokeWidth = 2.0;

    // Grid técnico suave
    for (double x = 20; x < size.width; x += 40) {
      canvas.drawLine(Offset(x, 0), Offset(x, size.height), strokePaint);
    }
    for (double y = 20; y < size.height; y += 40) {
      canvas.drawLine(Offset(0, y), Offset(size.width, y), strokePaint);
    }

    // Texto central
    final textSpan = TextSpan(
      text: label,
      style: const TextStyle(
        color: Color(0xFF8F8271),
        fontSize: 14,
        fontWeight: FontWeight.w700,
      ),
    );
    final textPainter = TextPainter(
      text: textSpan,
      textDirection: TextDirection.ltr,
    )..layout();

    textPainter.paint(
      canvas,
      Offset((size.width - textPainter.width) / 2, (size.height - textPainter.height) / 2),
    );
  }

  @override
  bool shouldRepaint(covariant CustomPainter oldDelegate) => false;
}
