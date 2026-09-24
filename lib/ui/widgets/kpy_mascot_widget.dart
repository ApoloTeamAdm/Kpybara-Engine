import 'dart:math' as math;
import 'package:flutter/material.dart';
import '../theme/kpy_theme.dart';

/// [ESTADOS REATIVOS DO MASCOTE KPY]
enum KpyMascotState {
  idle,           // Tranquilo, piscando na tela principal
  success,        // Celebrando com óculos de proteção maker e melancia
  tryAgain,       // Coçando a cabeça com olhar simpático e acolhedor (sem punição)
  streakMilestone,// Fogo nos olhos, celebrando ofensiva épica
  offlineMode,    // Mochilinha de acampamento, indicando modo autônomo/offline
}

/// [WIDGET DO MASCOTE KPY EM PIXEL-ART / DOODLE VETORIAL]
///
/// Desenvolvido sob medida com [CustomPainter] otimizado e [RepaintBoundary].
/// Elimina carregamento de PNGs/SVGs pesados na memória, garantindo 60/90 FPS
/// em celulares modestos (Moto G22 / Moto G5) com pegada de RAM < 1 MB.
class KpyMascotWidget extends StatefulWidget {
  final KpyMascotState state;
  final double size;
  final String? speechBubbleText;
  final VoidCallback? onTap;

  const KpyMascotWidget({
    Key? key,
    this.state = KpyMascotState.idle,
    this.size = 120.0,
    this.speechBubbleText,
    this.onTap,
  }) : super(key: key);

  @override
  State<KpyMascotWidget> createState() => _KpyMascotWidgetState();
}

class _KpyMascotWidgetState extends State<KpyMascotWidget> with SingleTickerProviderStateMixin {
  late final AnimationController _animController;
  late final Animation<double> _blinkAnimation;
  late final Animation<double> _bounceAnimation;

  @override
  void initState() {
    super.initState();
    _animController = AnimationController(
      vsync: this,
      duration: const Duration(milliseconds: 2400),
    )..repeat(reverse: true);

    _blinkAnimation = TweenSequence<double>([
      TweenSequenceItem(tween: ConstantTween<double>(1.0), weight: 85),
      TweenSequenceItem(tween: Tween<double>(begin: 1.0, end: 0.1), weight: 5),
      TweenSequenceItem(tween: Tween<double>(begin: 0.1, end: 1.0), weight: 5),
      TweenSequenceItem(tween: ConstantTween<double>(1.0), weight: 5),
    ]).animate(_animController);

    _bounceAnimation = Tween<double>(begin: 0.0, end: 4.0).animate(
      CurvedAnimation(parent: _animController, curve: Curves.easeInOut),
    );
  }

  @override
  void dispose() {
    _animController.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    return GestureDetector(
      onTap: widget.onTap,
      child: Column(
        mainAxisSize: MainAxisSize.min,
        children: [
          if (widget.speechBubbleText != null && widget.speechBubbleText!.isNotEmpty)
            _buildSpeechBubble(context),
          RepaintBoundary(
            child: AnimatedBuilder(
              animation: _animController,
              builder: (context, child) {
                return Transform.translate(
                  offset: Offset(0, widget.state == KpyMascotState.idle ? _bounceAnimation.value : 0),
                  child: CustomPaint(
                    size: Size(widget.size, widget.size),
                    painter: _KpyPixelArtPainter(
                      state: widget.state,
                      blinkProgress: _blinkAnimation.value,
                      animProgress: _animController.value,
                    ),
                  ),
                );
              },
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildSpeechBubble(BuildContext context) {
    final isDark = Theme.of(context).brightness == Brightness.dark;
    return Container(
      margin: const EdgeInsets.only(bottom: 8.0),
      padding: const EdgeInsets.symmetric(horizontal: 14.0, vertical: 8.0),
      constraints: const BoxConstraints(maxWidth: 240),
      decoration: BoxDecoration(
        color: isDark ? KpyColors.paperCardDark : KpyColors.paperCardLight,
        borderRadius: BorderRadius.circular(12),
        border: Border.all(
          color: isDark ? KpyColors.paperBorderDark : KpyColors.paperBorderLight,
          width: 2.0,
        ),
        boxShadow: [
          BoxShadow(
            color: Colors.black.withOpacity(0.06),
            offset: const Offset(0, 3),
            blurRadius: 4,
          ),
        ],
      ),
      child: Text(
        widget.speechBubbleText!,
        textAlign: TextAlign.center,
        style: TextStyle(
          fontSize: 13,
          fontWeight: FontWeight.w700,
          color: isDark ? KpyColors.inkTextDark : KpyColors.inkTextLight,
        ),
      ),
    );
  }
}

/// Painter vetorial que desenha o mascote Kpy com proporções estilo Pixel-Art / Hand-Drawn.
class _KpyPixelArtPainter extends CustomPainter {
  final KpyMascotState state;
  final double blinkProgress;
  final double animProgress;

  _KpyPixelArtPainter({
    required this.state,
    required this.blinkProgress,
    required this.animProgress,
  });

  @override
  void paint(Canvas canvas, Size size) {
    final w = size.width;
    final h = size.height;
    final pixelUnit = w / 24.0; // Grid 24x24 pixel art

    final bodyPaint = Paint()..color = KpyColors.capybaraFurLight;
    final darkFurPaint = Paint()..color = KpyColors.capybaraBrown;
    final snoutPaint = Paint()..color = KpyColors.capybaraSnout;
    final shadowPaint = Paint()..color = KpyColors.capybaraFurShadow;
    final whitePaint = Paint()..color = Colors.white;
    final blackPaint = Paint()..color = const Color(0xFF1E140C);

    // 1. Orelhas Pequenas e Redondas
    final leftEar = RRect.fromRectAndRadius(
      Rect.fromLTWH(pixelUnit * 4, pixelUnit * 4, pixelUnit * 3, pixelUnit * 3),
      Radius.circular(pixelUnit),
    );
    final rightEar = RRect.fromRectAndRadius(
      Rect.fromLTWH(pixelUnit * 17, pixelUnit * 4, pixelUnit * 3, pixelUnit * 3),
      Radius.circular(pixelUnit),
    );
    canvas.drawRRect(leftEar, darkFurPaint);
    canvas.drawRRect(rightEar, darkFurPaint);

    // 2. Mochilinha de Acampamento (Estado OFFLINE_MODE)
    if (state == KpyMascotState.offlineMode) {
      final backpackPaint = Paint()..color = const Color(0xFFE67E22);
      final pocketPaint = Paint()..color = const Color(0xFFD35400);
      canvas.drawRRect(
        RRect.fromRectAndRadius(
          Rect.fromLTWH(pixelUnit * 18, pixelUnit * 10, pixelUnit * 5, pixelUnit * 8),
          Radius.circular(pixelUnit),
        ),
        backpackPaint,
      );
      canvas.drawRect(
        Rect.fromLTWH(pixelUnit * 19, pixelUnit * 13, pixelUnit * 3, pixelUnit * 4),
        pocketPaint,
      );
    }

    // 3. Cabeça e Corpo Acolhedor (Formato Capivara Chonky)
    final bodyRect = RRect.fromRectAndRadius(
      Rect.fromLTWH(pixelUnit * 5, pixelUnit * 6, pixelUnit * 14, pixelUnit * 15),
      Radius.circular(pixelUnit * 3.5),
    );
    canvas.drawRRect(bodyRect, bodyPaint);

    // Sombra inferior do corpo
    final bodyShadow = RRect.fromRectAndRadius(
      Rect.fromLTWH(pixelUnit * 6, pixelUnit * 18, pixelUnit * 12, pixelUnit * 3),
      Radius.circular(pixelUnit * 1.5),
    );
    canvas.drawRRect(bodyShadow, shadowPaint);

    // 4. Focinho Quadrado Típico de Capivara
    final snoutRect = RRect.fromRectAndRadius(
      Rect.fromLTWH(pixelUnit * 8, pixelUnit * 12, pixelUnit * 8, pixelUnit * 6),
      Radius.circular(pixelUnit * 1.5),
    );
    canvas.drawRRect(snoutRect, snoutPaint);

    // Narinas
    canvas.drawCircle(Offset(pixelUnit * 10.5, pixelUnit * 14), pixelUnit * 0.6, blackPaint);
    canvas.drawCircle(Offset(pixelUnit * 13.5, pixelUnit * 14), pixelUnit * 0.6, blackPaint);

    // 5. Olhinhos e Expressão de acordo com o Estado
    _drawEyesAndExpressions(canvas, pixelUnit, blackPaint, whitePaint);

    // 6. Acessórios Especiais por Estado
    if (state == KpyMascotState.success) {
      _drawMakerGlassesAndWatermelon(canvas, pixelUnit);
    } else if (state == KpyMascotState.streakMilestone) {
      _drawFireEyes(canvas, pixelUnit);
    } else if (state == KpyMascotState.tryAgain) {
      _drawScratchingHand(canvas, pixelUnit);
    }
  }

  void _drawEyesAndExpressions(Canvas canvas, double u, Paint blackPaint, Paint whitePaint) {
    switch (state) {
      case KpyMascotState.idle:
      case KpyMascotState.offlineMode:
        // Olhinhos serenos e relaxados piscando
        final eyeHeight = math.max(0.4, 2.0 * blinkProgress) * u;
        canvas.drawRRect(
          RRect.fromRectAndRadius(
            Rect.fromLTWH(u * 7.5, u * 9.5 + (2.0 * u - eyeHeight) / 2, u * 1.8, eyeHeight),
            Radius.circular(u * 0.6),
          ),
          blackPaint,
        );
        canvas.drawRRect(
          RRect.fromRectAndRadius(
            Rect.fromLTWH(u * 14.7, u * 9.5 + (2.0 * u - eyeHeight) / 2, u * 1.8, eyeHeight),
            Radius.circular(u * 0.6),
          ),
          blackPaint,
        );
        // Brilho nos olhos
        if (blinkProgress > 0.6) {
          canvas.drawCircle(Offset(u * 8.2, u * 10.0), u * 0.4, whitePaint);
          canvas.drawCircle(Offset(u * 15.4, u * 10.0), u * 0.4, whitePaint);
        }
        break;

      case KpyMascotState.tryAgain:
        // Olhar compreensivo / simpático (sem cara brava ou punitiva)
        // Olho esquerdo normal, olho direito piscando solidário
        canvas.drawCircle(Offset(u * 8.5, u * 10.5), u * 1.0, blackPaint);
        canvas.drawCircle(Offset(u * 8.8, u * 10.2), u * 0.4, whitePaint);

        // Curva suave do olho direito piscando amigável ^-^
        final path = Path()
          ..moveTo(u * 14.5, u * 11)
          ..quadraticBezierTo(u * 15.5, u * 9.5, u * 16.5, u * 11);
        final strokePaint = Paint()
          ..color = blackPaint.color
          ..style = PaintingStyle.stroke
          ..strokeWidth = u * 0.8
          ..strokeCap = StrokeCap.round;
        canvas.drawPath(path, strokePaint);
        break;

      case KpyMascotState.success:
      case KpyMascotState.streakMilestone:
        // Olhinhos animados de alegria ^_^
        final strokePaint = Paint()
          ..color = blackPaint.color
          ..style = PaintingStyle.stroke
          ..strokeWidth = u * 0.8
          ..strokeCap = StrokeCap.round;

        final pathLeft = Path()
          ..moveTo(u * 7.0, u * 11)
          ..quadraticBezierTo(u * 8.5, u * 9.0, u * 10.0, u * 11);
        final pathRight = Path()
          ..moveTo(u * 14.0, u * 11)
          ..quadraticBezierTo(u * 15.5, u * 9.0, u * 17.0, u * 11);

        canvas.drawPath(pathLeft, strokePaint);
        canvas.drawPath(pathRight, strokePaint);
        break;
    }
  }

  void _drawMakerGlassesAndWatermelon(Canvas canvas, double u) {
    // 1. Óculos de Proteção Maker (Amarelo Mel / Neon)
    final gogglePaint = Paint()
      ..color = KpyColors.honeyYellow
      ..style = PaintingStyle.stroke
      ..strokeWidth = u * 0.8;
    final glassLensPaint = Paint()..color = const Color(0x664FC3F7);

    // Lente esquerda e direita
    canvas.drawRRect(
      RRect.fromRectAndRadius(Rect.fromLTWH(u * 6.5, u * 8.5, u * 4.0, u * 3.5), Radius.circular(u)),
      glassLensPaint,
    );
    canvas.drawRRect(
      RRect.fromRectAndRadius(Rect.fromLTWH(u * 6.5, u * 8.5, u * 4.0, u * 3.5), Radius.circular(u)),
      gogglePaint,
    );
    canvas.drawRRect(
      RRect.fromRectAndRadius(Rect.fromLTWH(u * 13.5, u * 8.5, u * 4.0, u * 3.5), Radius.circular(u)),
      glassLensPaint,
    );
    canvas.drawRRect(
      RRect.fromRectAndRadius(Rect.fromLTWH(u * 13.5, u * 8.5, u * 4.0, u * 3.5), Radius.circular(u)),
      gogglePaint,
    );
    // Haste central
    canvas.drawLine(Offset(u * 10.5, u * 10), Offset(u * 13.5, u * 10), gogglePaint);

    // 2. Fatias de Melancia Saborosa
    final rindPaint = Paint()..color = KpyColors.watermelonGreen;
    final fleshPaint = Paint()..color = KpyColors.watermelonPink;
    final seedPaint = Paint()..color = Colors.black;

    // Casca
    final rindPath = Path()
      ..moveTo(u * 8, u * 19)
      ..arcToPoint(Offset(u * 16, u * 19), radius: Radius.circular(u * 4), clockwise: false)
      ..close();
    canvas.drawPath(rindPath, rindPaint);

    // Polpa
    final fleshPath = Path()
      ..moveTo(u * 8.5, u * 18.5)
      ..arcToPoint(Offset(u * 15.5, u * 18.5), radius: Radius.circular(u * 3.5), clockwise: false)
      ..close();
    canvas.drawPath(fleshPath, fleshPaint);

    // Sementinhas pretas
    canvas.drawCircle(Offset(u * 10.5, u * 20), u * 0.3, seedPaint);
    canvas.drawCircle(Offset(u * 12.0, u * 20.5), u * 0.3, seedPaint);
    canvas.drawCircle(Offset(u * 13.5, u * 20), u * 0.3, seedPaint);
  }

  void _drawFireEyes(Canvas canvas, double u) {
    // Foguinho nos olhos celebrando streak milestone 🔥
    final flameOuter = Paint()..color = KpyColors.fireStreak;
    final flameInner = Paint()..color = KpyColors.honeyYellow;

    void drawFlameAt(double cx, double cy) {
      final p = Path()
        ..moveTo(cx - u, cy + u)
        ..quadraticBezierTo(cx - u * 1.5, cy, cx, cy - u * 2)
        ..quadraticBezierTo(cx + u * 1.5, cy, cx + u, cy + u)
        ..close();
      canvas.drawPath(p, flameOuter);

      final pInner = Path()
        ..moveTo(cx - u * 0.5, cy + u * 0.8)
        ..quadraticBezierTo(cx - u * 0.8, cy + u * 0.2, cx, cy - u * 1.0)
        ..quadraticBezierTo(cx + u * 0.8, cy + u * 0.2, cx + u * 0.5, cy + u * 0.8)
        ..close();
      canvas.drawPath(pInner, flameInner);
    }

    drawFlameAt(u * 8.5, u * 10.0);
    drawFlameAt(u * 15.5, u * 10.0);
  }

  void _drawScratchingHand(Canvas canvas, double u) {
    // Patinha coçando a orelha (indicando reflexão / simpatia)
    final pawPaint = Paint()..color = KpyColors.capybaraFurLight;
    final pawBorder = Paint()
      ..color = KpyColors.capybaraBrown
      ..style = PaintingStyle.stroke
      ..strokeWidth = u * 0.5;

    final pawRect = RRect.fromRectAndRadius(
      Rect.fromLTWH(u * 3.5, u * 4.5, u * 3.0, u * 2.5),
      Radius.circular(u * 1.2),
    );
    canvas.drawRRect(pawRect, pawPaint);
    canvas.drawRRect(pawRect, pawBorder);
  }

  @override
  bool shouldRepaint(covariant _KpyPixelArtPainter oldDelegate) {
    return oldDelegate.state != state ||
        oldDelegate.blinkProgress != blinkProgress ||
        oldDelegate.animProgress != animProgress;
  }
}
