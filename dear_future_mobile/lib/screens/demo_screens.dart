import 'package:flutter/material.dart';

import '../theme/login_theme.dart';

class NewMessageDemoPage extends StatelessWidget {
  const NewMessageDemoPage({super.key});

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: LoginColors.background,
      appBar: AppBar(
        backgroundColor: LoginColors.surface,
        foregroundColor: LoginColors.textWhite,
        title: const Text('Yeni Mesaj'),
        elevation: 0,
      ),
      body: Center(
        child: Padding(
          padding: const EdgeInsets.all(24),
          child: Column(
            mainAxisAlignment: MainAxisAlignment.center,
            children: [
              Icon(Icons.edit_note_rounded, size: 80, color: LoginColors.textMuted),
              const SizedBox(height: 24),
              Text(
                'Yeni mesaj yazma ekranı',
                style: TextStyle(
                  fontSize: 18,
                  fontWeight: FontWeight.w600,
                  color: LoginColors.textWhite,
                ),
              ),
              const SizedBox(height: 8),
              Text(
                'Bu sayfa demo amaçlıdır. İleride mesaj oluşturma formu burada olacak.',
                textAlign: TextAlign.center,
                style: TextStyle(fontSize: 14, color: LoginColors.textLightGray),
              ),
            ],
          ),
        ),
      ),
    );
  }
}

class SavedDemoPage extends StatelessWidget {
  const SavedDemoPage({super.key});

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: LoginColors.background,
      body: SafeArea(
        child: Center(
          child: Column(
            mainAxisAlignment: MainAxisAlignment.center,
            children: [
              Icon(Icons.bookmark_outline_rounded, size: 64, color: LoginColors.textMuted),
              const SizedBox(height: 16),
              Text(
                'Kaydetme',
                style: TextStyle(
                  fontSize: 20,
                  fontWeight: FontWeight.w700,
                  color: LoginColors.textWhite,
                ),
              ),
              const SizedBox(height: 8),
              Text(
                'Kaydettiğin mesajlar burada listelenecek.',
                style: TextStyle(fontSize: 14, color: LoginColors.textLightGray),
              ),
            ],
          ),
        ),
      ),
    );
  }
}

class SettingsDemoPage extends StatelessWidget {
  const SettingsDemoPage({super.key});

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: LoginColors.background,
      body: SafeArea(
        child: Center(
          child: Column(
            mainAxisAlignment: MainAxisAlignment.center,
            children: [
              Icon(Icons.settings_outlined, size: 64, color: LoginColors.textMuted),
              const SizedBox(height: 16),
              Text(
                'Ayarlar',
                style: TextStyle(
                  fontSize: 20,
                  fontWeight: FontWeight.w700,
                  color: LoginColors.textWhite,
                ),
              ),
              const SizedBox(height: 8),
              Text(
                'Bildirimler, tema ve diğer ayarlar burada olacak.',
                textAlign: TextAlign.center,
                style: TextStyle(fontSize: 14, color: LoginColors.textLightGray),
              ),
            ],
          ),
        ),
      ),
    );
  }
}

class ProfileDemoPage extends StatelessWidget {
  const ProfileDemoPage({super.key, this.onLogout});

  final VoidCallback? onLogout;

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: LoginColors.background,
      body: SafeArea(
        child: Center(
          child: Padding(
            padding: const EdgeInsets.all(24),
            child: Column(
              mainAxisAlignment: MainAxisAlignment.center,
              children: [
                Icon(Icons.person_outline_rounded, size: 64, color: LoginColors.textMuted),
                const SizedBox(height: 16),
                Text(
                  'Profil',
                  style: TextStyle(
                    fontSize: 20,
                    fontWeight: FontWeight.w700,
                    color: LoginColors.textWhite,
                  ),
                ),
                const SizedBox(height: 8),
                Text(
                  'Profil bilgilerin ve fotoğraf burada düzenlenecek.',
                  textAlign: TextAlign.center,
                  style: TextStyle(fontSize: 14, color: LoginColors.textLightGray),
                ),
                if (onLogout != null) ...[
                  const SizedBox(height: 32),
                  OutlinedButton.icon(
                    onPressed: onLogout,
                    icon: const Icon(Icons.logout_rounded, size: 20),
                    label: const Text('Çıkış yap'),
                    style: OutlinedButton.styleFrom(
                      foregroundColor: LoginColors.textLightGray,
                      side: BorderSide(color: LoginColors.border),
                    ),
                  ),
                ],
              ],
            ),
          ),
        ),
      ),
    );
  }
}

class SubscriptionDemoPage extends StatelessWidget {
  const SubscriptionDemoPage({super.key});

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: LoginColors.background,
      body: SafeArea(
        child: Center(
          child: Column(
            mainAxisAlignment: MainAxisAlignment.center,
            children: [
              Icon(Icons.card_membership_rounded, size: 64, color: LoginColors.textMuted),
              const SizedBox(height: 16),
              Text(
                'Abonelik',
                style: TextStyle(
                  fontSize: 20,
                  fontWeight: FontWeight.w700,
                  color: LoginColors.textWhite,
                ),
              ),
              const SizedBox(height: 8),
              Text(
                'Ücretsiz, Plus ve Premium planlar burada listelenecek.',
                textAlign: TextAlign.center,
                style: TextStyle(fontSize: 14, color: LoginColors.textLightGray),
              ),
            ],
          ),
        ),
      ),
    );
  }
}
