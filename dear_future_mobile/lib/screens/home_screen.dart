import 'package:flutter/material.dart';

import '../theme/login_theme.dart';
import 'demo_screens.dart';
import 'new_message_page.dart';
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
    setState(() => _currentIndex = 5);
  }

  static const List<_NavItem> _navItems = [
    _NavItem(icon: Icons.send_rounded, label: 'Mesajlar'),
    _NavItem(icon: Icons.edit_note_rounded, label: 'Yeni Mesaj'),
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
          PublicMessagesPage(onLogout: widget.onLogout),
          NewMessagePage(onLogout: widget.onLogout),
          const SavedDemoPage(),
          SettingsPage(onLogout: widget.onLogout, onManageSubscription: _goToSubscriptionTab),
          ProfilePage(onLogout: widget.onLogout, onManageSubscription: _goToSubscriptionTab),
          SubscriptionPage(onLogout: widget.onLogout),
        ],
      ),
      bottomNavigationBar: Container(
        decoration: BoxDecoration(
          color: LoginColors.surface,
          border: Border(top: BorderSide(color: LoginColors.border)),
        ),
        child: SafeArea(
          child: Padding(
            padding: const EdgeInsets.symmetric(vertical: 6),
            child: Row(
              mainAxisAlignment: MainAxisAlignment.spaceAround,
              children: List.generate(_navItems.length, (index) {
                final item = _navItems[index];
                final selected = _currentIndex == index;
                return Expanded(
                  child: InkWell(
                    onTap: () => setState(() => _currentIndex = index),
                    borderRadius: BorderRadius.circular(8),
                    child: Padding(
                      padding: const EdgeInsets.symmetric(vertical: 6),
                      child: Column(
                        mainAxisSize: MainAxisSize.min,
                        children: [
                          Icon(
                            item.icon,
                            size: 22,
                            color: selected ? LoginColors.primaryEnd : LoginColors.textMuted,
                          ),
                          const SizedBox(height: 3),
                          Text(
                            item.label,
                            style: TextStyle(
                              fontSize: 10,
                              color: selected ? LoginColors.primaryEnd : LoginColors.textMuted,
                              fontWeight: selected ? FontWeight.w600 : FontWeight.w500,
                            ),
                            maxLines: 1,
                            overflow: TextOverflow.ellipsis,
                          ),
                        ],
                      ),
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

