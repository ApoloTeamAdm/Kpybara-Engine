import 'package:flutter/material.dart';
import '../theme/kpy_theme.dart';

enum ConnectivityStatus {
  online,       // 🟢 Online
  offlineLocal, // 🟡 Offline Local (Modo Autônomo)
  syncingDeltas,// 🔄 Sincronizando Deltas RFC 6902
}

/// [BARRA SUPERIOR DE ESTATÍSTICAS E HUD - TOP STATS BAR]
///
/// Exibe a ofensiva de dias (Streak 🔥), XP acumulado ⚡, Vidas ❤️
/// e o badge reativo de conectividade de rede sem engasgos na renderização.
class TopStatsBar extends StatelessWidget {
  final int streakDays;
  final int xpEarned;
  final int hearts;
  final ConnectivityStatus connectivity;
  final VoidCallback? onConnectivityTap;

  const TopStatsBar({
    Key? key,
    this.streakDays = 5,
    this.xpEarned = 140,
    this.hearts = 5,
    this.connectivity = ConnectivityStatus.online,
    this.onConnectivityTap,
  }) : super(key: key);

  @override
  Widget build(BuildContext context) {
    final isDark = Theme.of(context).brightness == Brightness.dark;

    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 16.0, vertical: 8.0),
      decoration: BoxDecoration(
        color: isDark ? KpyColors.paperDark : KpyColors.paperLight,
        border: Border(
          bottom: BorderSide(
            color: isDark ? KpyColors.paperBorderDark : KpyColors.paperBorderLight,
            width: 1.5,
          ),
        ),
      ),
      child: Row(
        mainAxisAlignment: MainAxisAlignment.spaceBetween,
        children: [
          // 1. Ofensiva de Dias (Streak) 🔥
          _buildStatChip(
            icon: Icons.local_fire_department_rounded,
            iconColor: KpyColors.fireStreak,
            text: "$streakDays",
            isDark: isDark,
          ),

          // 2. XP Acumulado ⚡
          _buildStatChip(
            icon: Icons.bolt_rounded,
            iconColor: KpyColors.xpEnergy,
            text: "$xpEarned XP",
            isDark: isDark,
          ),

          // 3. Vidas / Energia ❤️
          _buildStatChip(
            icon: Icons.favorite_rounded,
            iconColor: KpyColors.heartLife,
            text: "$hearts",
            isDark: isDark,
          ),

          // 4. Badge Reativo de Conectividade
          InkWell(
            onTap: onConnectivityTap,
            borderRadius: BorderRadius.circular(8),
            child: _buildConnectivityBadge(isDark),
          ),
        ],
      ),
    );
  }

  Widget _buildStatChip({
    required IconData icon,
    required Color iconColor,
    required String text,
    required bool isDark,
  }) {
    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 4),
      decoration: BoxDecoration(
        color: isDark ? KpyColors.paperCardDark : KpyColors.paperCardLight,
        borderRadius: BorderRadius.circular(8),
        border: Border.all(
          color: isDark ? KpyColors.paperBorderDark : KpyColors.paperBorderLight,
          width: 1.0,
        ),
      ),
      child: Row(
        mainAxisSize: MainAxisSize.min,
        children: [
          Icon(icon, color: iconColor, size: 18),
          const SizedBox(width: 4),
          Text(
            text,
            style: TextStyle(
              fontWeight: FontWeight.w800,
              fontSize: 13,
              color: isDark ? KpyColors.inkTextDark : KpyColors.inkTextLight,
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildConnectivityBadge(bool isDark) {
    Color badgeColor;
    IconData iconData;
    String label;

    switch (connectivity) {
      case ConnectivityStatus.online:
        badgeColor = KpyColors.leafGreen;
        iconData = Icons.wifi_rounded;
        label = "Online";
        break;
      case ConnectivityStatus.offlineLocal:
        badgeColor = KpyColors.honeyYellow;
        iconData = Icons.wifi_off_rounded;
        label = "Offline";
        break;
      case ConnectivityStatus.syncingDeltas:
        badgeColor = const Color(0xFF3498DB);
        iconData = Icons.sync_rounded;
        label = "Syncing";
        break;
    }

    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 4),
      decoration: BoxDecoration(
        color: badgeColor.withOpacity(0.15),
        borderRadius: BorderRadius.circular(8),
        border: Border.all(color: badgeColor, width: 1.0),
      ),
      child: Row(
        mainAxisSize: MainAxisSize.min,
        children: [
          Icon(iconData, color: badgeColor, size: 14),
          const SizedBox(width: 4),
          Text(
            label,
            style: TextStyle(
              fontSize: 11,
              fontWeight: FontWeight.w700,
              color: badgeColor,
            ),
          ),
        ],
      ),
    );
  }
}
