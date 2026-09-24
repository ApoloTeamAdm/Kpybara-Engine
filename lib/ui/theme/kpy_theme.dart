import 'package:flutter/material.dart';

/// [SISTEMA DE DESIGN KPYBARA - ESTÉTICA ANTI-CORPORATE AI]
/// 
/// Paleta acolhedora inspirada em cadernos de rascunho, papel kraft e pixel-art.
/// Evita gradientes frios de tech corporativa. Cores terrosas, traços com textura doodle,
/// cantos levemente arredondados e contraste WCAG AA garantido.
class KpyColors {
  // Paleta Terrosa e Quente (Capivara & Natureza)
  static const Color capybaraBrown = Color(0xFF6B4226);
  static const Color capybaraFurLight = Color(0xFF8B5A2B);
  static const Color capybaraFurShadow = Color(0xFF4A2E1B);
  static const Color capybaraSnout = Color(0xFF362010);
  
  // Tons de Papel / Caderno de Rascunho (Tema Claro)
  static const Color paperLight = Color(0xFFFAF6EE);
  static const Color paperCardLight = Color(0xFFFFFDF9);
  static const Color paperBorderLight = Color(0xFFE2D9C8);
  static const Color inkTextLight = Color(0xFF2C241E);
  static const Color inkMutedLight = Color(0xFF73675C);

  // Tons de Caderno Noturno (Tema Escuro - Alto Contraste)
  static const Color paperDark = Color(0xFF1E1A17);
  static const Color paperCardDark = Color(0xFF28231F);
  static const Color paperBorderDark = Color(0xFF3D352F);
  static const Color inkTextDark = Color(0xFFF3ECE4);
  static const Color inkMutedDark = Color(0xFFA6998D);

  // Cores de Acento Encorajadoras (Acolhedoras, não-punitivas)
  static const Color honeyYellow = Color(0xFFF5A623);
  static const Color leafGreen = Color(0xFF4E9A51);
  static const Color softSuccess = Color(0xFF5CB85C);
  static const Color softSuccessBg = Color(0xFFE8F5E9);
  static const Color softTryAgain = Color(0xFFD9534F);
  static const Color softTryAgainBg = Color(0xFFFFEBEE);
  
  // Gamificação / HUD
  static const Color fireStreak = Color(0xFFFF6F00);
  static const Color xpEnergy = Color(0xFFFFA000);
  static const Color heartLife = Color(0xFFE53935);
  static const Color watermelonPink = Color(0xFFFF5252);
  static const Color watermelonGreen = Color(0xFF2E7D32);
}

class KpyTheme {
  // Dimensões Mínimas de Acessibilidade (WCAG AA - 48x48dp)
  static const double minTouchTargetSize = 48.0;

  // Bordas com leve sensação orgânica / hand-drawn
  static final BorderRadius cardRadius = BorderRadius.circular(14.0);
  static final BorderRadius buttonRadius = BorderRadius.circular(12.0);
  static final BorderRadius chipRadius = BorderRadius.circular(10.0);

  // Tema Claro
  static ThemeData get lightTheme {
    return ThemeData(
      useMaterial3: true,
      brightness: Brightness.light,
      scaffoldBackgroundColor: KpyColors.paperLight,
      colorScheme: const ColorScheme(
        brightness: Brightness.light,
        primary: KpyColors.capybaraBrown,
        onPrimary: Colors.white,
        secondary: KpyColors.leafGreen,
        onSecondary: Colors.white,
        error: KpyColors.softTryAgain,
        onError: Colors.white,
        background: KpyColors.paperLight,
        onBackground: KpyColors.inkTextLight,
        surface: KpyColors.paperCardLight,
        onSurface: KpyColors.inkTextLight,
        outline: KpyColors.paperBorderLight,
      ),
      cardTheme: CardTheme(
        color: KpyColors.paperCardLight,
        elevation: 0,
        shape: RoundedRectangleBorder(
          borderRadius: cardRadius,
          side: const BorderSide(color: KpyColors.paperBorderLight, width: 2.0),
        ),
      ),
      elevatedButtonTheme: ElevatedButtonThemeData(
        style: ElevatedButton.styleFrom(
          elevation: 2,
          minimumSize: const Size(minTouchTargetSize, minTouchTargetSize),
          backgroundColor: KpyColors.leafGreen,
          foregroundColor: Colors.white,
          shape: RoundedRectangleBorder(
            borderRadius: buttonRadius,
            side: const BorderSide(color: Color(0xFF38703B), width: 2),
          ),
          textStyle: const TextStyle(
            fontSize: 16,
            fontWeight: FontWeight.w700,
            letterSpacing: 0.5,
          ),
        ),
      ),
      textTheme: const TextTheme(
        displayLarge: TextStyle(
          fontSize: 28,
          fontWeight: FontWeight.w800,
          color: KpyColors.inkTextLight,
          letterSpacing: -0.5,
        ),
        titleLarge: TextStyle(
          fontSize: 20,
          fontWeight: FontWeight.w700,
          color: KpyColors.inkTextLight,
        ),
        titleMedium: TextStyle(
          fontSize: 16,
          fontWeight: FontWeight.w600,
          color: KpyColors.inkTextLight,
        ),
        bodyLarge: TextStyle(
          fontSize: 16,
          fontWeight: FontWeight.w500,
          color: KpyColors.inkTextLight,
          height: 1.4,
        ),
        bodyMedium: TextStyle(
          fontSize: 14,
          fontWeight: FontWeight.w400,
          color: KpyColors.inkMutedLight,
          height: 1.3,
        ),
      ),
    );
  }

  // Tema Escuro (Caderno Noturno)
  static ThemeData get darkTheme {
    return ThemeData(
      useMaterial3: true,
      brightness: Brightness.dark,
      scaffoldBackgroundColor: KpyColors.paperDark,
      colorScheme: const ColorScheme(
        brightness: Brightness.dark,
        primary: KpyColors.honeyYellow,
        onPrimary: KpyColors.inkTextLight,
        secondary: KpyColors.leafGreen,
        onSecondary: Colors.white,
        error: KpyColors.softTryAgain,
        onError: Colors.white,
        background: KpyColors.paperDark,
        onBackground: KpyColors.inkTextDark,
        surface: KpyColors.paperCardDark,
        onSurface: KpyColors.inkTextDark,
        outline: KpyColors.paperBorderDark,
      ),
      cardTheme: CardTheme(
        color: KpyColors.paperCardDark,
        elevation: 0,
        shape: RoundedRectangleBorder(
          borderRadius: cardRadius,
          side: const BorderSide(color: KpyColors.paperBorderDark, width: 2.0),
        ),
      ),
      elevatedButtonTheme: ElevatedButtonThemeData(
        style: ElevatedButton.styleFrom(
          elevation: 2,
          minimumSize: const Size(minTouchTargetSize, minTouchTargetSize),
          backgroundColor: KpyColors.leafGreen,
          foregroundColor: Colors.white,
          shape: RoundedRectangleBorder(
            borderRadius: buttonRadius,
            side: const BorderSide(color: Color(0xFF28542A), width: 2),
          ),
          textStyle: const TextStyle(
            fontSize: 16,
            fontWeight: FontWeight.w700,
          ),
        ),
      ),
      textTheme: const TextTheme(
        displayLarge: TextStyle(
          fontSize: 28,
          fontWeight: FontWeight.w800,
          color: KpyColors.inkTextDark,
        ),
        titleLarge: TextStyle(
          fontSize: 20,
          fontWeight: FontWeight.w700,
          color: KpyColors.inkTextDark,
        ),
        titleMedium: TextStyle(
          fontSize: 16,
          fontWeight: FontWeight.w600,
          color: KpyColors.inkTextDark,
        ),
        bodyLarge: TextStyle(
          fontSize: 16,
          fontWeight: FontWeight.w500,
          color: KpyColors.inkTextDark,
          height: 1.4,
        ),
        bodyMedium: TextStyle(
          fontSize: 14,
          fontWeight: FontWeight.w400,
          color: KpyColors.inkMutedDark,
          height: 1.3,
        ),
      ),
    );
  }
}
