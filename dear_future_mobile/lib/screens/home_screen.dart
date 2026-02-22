import 'package:flutter/material.dart';

import '../theme/login_theme.dart';
import 'demo_screens.dart';
import 'profile_page.dart';
import 'public_messages_page.dart';
import 'settings_page.dart';
import 'subscription_page.dart';

class HomeScreen extends StatefulWidget {
  const HomeScreen({super.key, required this.onLogout});

  final VoidCallback onLogout;

  @override
  State<HomeScreen> createState() => _HomeScreenState();
}

class _HomeScreenState extends State<HomeScreen> {
  int _currentIndex = 0;

  void _goToSubscriptionTab() {
    setState(() => _currentIndex = 4);
  }

  static const List<_NavItem> _navItems = [
    _NavItem(icon: Icons.send_rounded, label: 'Mesajlar'),
    _NavItem(icon: Icons.bookmark_outline_rounded, label: 'Kaydetme'),
    _NavItem(icon: Icons.settings_outlined, label: 'Ayarlar'),
    _NavItem(icon: Icons.person_outline_rounded, label: 'Profil'),
    _NavItem(icon: Icons.card_membership_rounded, label: 'Abonelik'),
  ];

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: LoginColors.background,
      body: IndexedStack(
        index: _currentIndex,
        children: [
          const PublicMessagesPage(),
          const SavedDemoPage(),
          SettingsPage(onLogout: widget.onLogout, onManageSubscription: _goToSubscriptionTab),
          ProfilePage(onLogout: widget.onLogout, onManageSubscription: _goToSubscriptionTab),
          const SubscriptionPage(),
        ],
      ),
      bottomNavigationBar: Container(
        decoration: BoxDecoration(
          color: LoginColors.surface,
          border: Border(top: BorderSide(color: LoginColors.border)),
        ),
        child: SafeArea(
          child: Padding(
            padding: const EdgeInsets.symmetric(vertical: 8),
            child: Row(
              mainAxisAlignment: MainAxisAlignment.spaceAround,
              children: List.generate(_navItems.length, (index) {
                final item = _navItems[index];
                final selected = _currentIndex == index;
                return InkWell(
                  onTap: () => setState(() => _currentIndex = index),
                  borderRadius: BorderRadius.circular(12),
                  child: Padding(
                    padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 8),
                    child: Column(
                      mainAxisSize: MainAxisSize.min,
                      children: [
                        Icon(
                          item.icon,
                          size: 24,
                          color: selected ? LoginColors.primaryEnd : LoginColors.textMuted,
                        ),
                        const SizedBox(height: 4),
                        Text(
                          item.label,
                          style: TextStyle(
                            fontSize: 11,
                            color: selected ? LoginColors.primaryEnd : LoginColors.textMuted,
                            fontWeight: selected ? FontWeight.w600 : FontWeight.w500,
                          ),
                        ),
                      ],
                    ),
                  ),
                );
              }),
            ),
          ),
        ),
      ),
    );
  }
}

class _NavItem {
  const _NavItem({required this.icon, required this.label});
  final IconData icon;
  final String label;
}

