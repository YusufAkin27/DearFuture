import 'package:flutter/material.dart';

import '../services/api_client.dart';
import '../services/auth_service.dart';
import '../services/profile_service.dart';
import '../theme/login_theme.dart';

/// Hesap dondurma ve kalıcı silme işlemleri; ana ayarlarda göz önünde olmasın diye ayrı sayfada.
class AccountDangerZonePage extends StatefulWidget {
  const AccountDangerZonePage({
    super.key,
    this.onLogout,
  });

  final VoidCallback? onLogout;

  @override
  State<AccountDangerZonePage> createState() => _AccountDangerZonePageState();
}

class _AccountDangerZonePageState extends State<AccountDangerZonePage> {
  final AuthService _auth = AuthService();
  ProfileService? _profileService;

  final _freezeController = TextEditingController();
  final _deleteController = TextEditingController();
  static const _freezeConfirm = 'DONDUR';
  static const _deleteConfirm = 'SİL';

  @override
  void initState() {
    super.initState();
    _init();
  }

  @override
  void dispose() {
    _freezeController.dispose();
    _deleteController.dispose();
    super.dispose();
  }

  Future<void> _init() async {
    await _auth.loadStoredToken();
    if (!mounted) return;
    final token = _auth.token;
    if (token != null && token.isNotEmpty) {
      _profileService = ProfileService(ApiClient(token: token, onUnauthorized: widget.onLogout));
    }
  }

  Future<void> _deactivateAccount() async {
    if (_freezeController.text.trim().toUpperCase() != _freezeConfirm) {
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(content: Text('Onaylamak için DONDUR yazın.'), backgroundColor: Colors.red),
      );
      return;
    }
    final confirm = await showDialog<bool>(
      context: context,
      builder: (ctx) => AlertDialog(
        backgroundColor: LoginColors.surface,
        title: Text('Hesabı dondur', style: TextStyle(color: LoginColors.textWhite)),
        content: Text(
          'Hesabınız devre dışı bırakılacak. Giriş yapamazsınız. Yeniden açmak için destek ile iletişime geçmeniz gerekir. Devam etmek istiyor musunuz?',
          style: TextStyle(color: LoginColors.textLightGray),
        ),
        actions: [
          TextButton(onPressed: () => Navigator.of(ctx).pop(false), child: Text('İptal', style: TextStyle(color: LoginColors.textLightGray))),
          TextButton(onPressed: () => Navigator.of(ctx).pop(true), child: Text('Dondur', style: TextStyle(color: Colors.orange))),
        ],
      ),
    );
    if (confirm != true || _profileService == null) return;
    final ok = await _profileService!.deactivateAccount();
    if (!mounted) return;
    if (ok) widget.onLogout?.call();
  }

  Future<void> _deleteAccount() async {
    if (_deleteController.text.trim().toUpperCase() != _deleteConfirm) {
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(content: Text('Kalıcı silmek için SİL yazın.'), backgroundColor: Colors.red),
      );
      return;
    }
    final confirm = await showDialog<bool>(
      context: context,
      builder: (ctx) => AlertDialog(
        backgroundColor: LoginColors.surface,
        title: Text('Hesabı kalıcı sil', style: TextStyle(color: LoginColors.textWhite)),
        content: Text(
          'Tüm verileriniz kalıcı olarak silinecek. Bu işlem geri alınamaz. Emin misiniz?',
          style: TextStyle(color: LoginColors.textLightGray),
        ),
        actions: [
          TextButton(onPressed: () => Navigator.of(ctx).pop(false), child: Text('İptal', style: TextStyle(color: LoginColors.textLightGray))),
          TextButton(onPressed: () => Navigator.of(ctx).pop(true), child: Text('Sil', style: TextStyle(color: Colors.red))),
        ],
      ),
    );
    if (confirm != true || _profileService == null) return;
    final ok = await _profileService!.deleteAccount();
    if (!mounted) return;
    if (ok) widget.onLogout?.call();
  }

  Widget _section(String title, List<Widget> children) {
    return Container(
      padding: const EdgeInsets.all(20),
      decoration: BoxDecoration(
        color: LoginColors.surface,
        borderRadius: BorderRadius.circular(16),
        border: Border.all(color: LoginColors.border),
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Text(title, style: TextStyle(fontSize: 16, fontWeight: FontWeight.w700, color: LoginColors.textWhite)),
          const SizedBox(height: 16),
          ...children,
        ],
      ),
    );
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: LoginColors.background,
      appBar: AppBar(
        backgroundColor: LoginColors.surface,
        foregroundColor: LoginColors.textWhite,
        title: const Text('Hesap dondurma ve silme', style: TextStyle(color: LoginColors.textWhite)),
        elevation: 0,
      ),
      body: ListView(
        padding: const EdgeInsets.fromLTRB(20, 20, 20, 32),
        children: [
          Text(
            'Bu sayfadaki işlemler geri alınamaz. Hesabınızı dondurmak veya kalıcı olarak silmek istediğinizde buradan işlem yapabilirsiniz.',
            style: TextStyle(fontSize: 14, color: LoginColors.textMuted),
          ),
          const SizedBox(height: 24),
          _section('Hesabı dondur', [
            Text(
              'Hesabınız devre dışı bırakılır, giriş yapamazsınız. Verileriniz silinmez. Yeniden açmak için destek ile iletişime geçin.',
              style: TextStyle(color: LoginColors.textLightGray, fontSize: 14),
            ),
            const SizedBox(height: 8),
            Text('Onaylamak için DONDUR yazın', style: TextStyle(color: LoginColors.textMuted, fontSize: 12)),
            const SizedBox(height: 8),
            TextField(
              controller: _freezeController,
              decoration: InputDecoration(
                hintText: _freezeConfirm,
                filled: true,
                fillColor: LoginColors.background,
                border: OutlineInputBorder(borderRadius: BorderRadius.circular(8), borderSide: BorderSide(color: Colors.orange.shade700)),
              ),
              style: TextStyle(color: LoginColors.textWhite),
            ),
            const SizedBox(height: 12),
            SizedBox(
              width: double.infinity,
              child: OutlinedButton(
                onPressed: _deactivateAccount,
                style: OutlinedButton.styleFrom(foregroundColor: Colors.orange, side: BorderSide(color: Colors.orange)),
                child: const Text('Hesabı dondur'),
              ),
            ),
          ]),
          const SizedBox(height: 20),
          _section('Hesap silme', [
            Text(
              'Tüm verileriniz kalıcı olarak silinir. Bu işlem geri alınamaz.',
              style: TextStyle(color: LoginColors.textLightGray, fontSize: 14),
            ),
            const SizedBox(height: 8),
            Text('Kalıcı silmek için SİL yazın', style: TextStyle(color: LoginColors.textMuted, fontSize: 12)),
            const SizedBox(height: 8),
            TextField(
              controller: _deleteController,
              decoration: InputDecoration(
                hintText: _deleteConfirm,
                filled: true,
                fillColor: LoginColors.background,
                border: OutlineInputBorder(borderRadius: BorderRadius.circular(8), borderSide: BorderSide(color: Colors.red)),
              ),
              style: TextStyle(color: LoginColors.textWhite),
            ),
            const SizedBox(height: 12),
            SizedBox(
              width: double.infinity,
              child: FilledButton(
                onPressed: _deleteAccount,
                style: FilledButton.styleFrom(backgroundColor: Colors.red, foregroundColor: Colors.white),
                child: const Text('Hesabı kalıcı sil'),
              ),
            ),
          ]),
        ],
      ),
    );
  }
}
