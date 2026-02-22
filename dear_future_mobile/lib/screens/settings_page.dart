import 'package:flutter/material.dart';

import '../models/profile_models.dart';
import '../services/api_client.dart';
import '../services/auth_service.dart';
import '../services/profile_service.dart';
import '../theme/login_theme.dart';

class SettingsPage extends StatefulWidget {
  const SettingsPage({
    super.key,
    this.onLogout,
    this.onManageSubscription,
  });

  final VoidCallback? onLogout;
  final VoidCallback? onManageSubscription;

  @override
  State<SettingsPage> createState() => _SettingsPageState();
}

class _SettingsPageState extends State<SettingsPage> {
  final AuthService _auth = AuthService();
  ApiClient? _apiClient;
  ProfileService? _profileService;
  ProfileData? _profile;
  List<DeliveredMessageItem> _deliveredMessages = [];
  bool _loading = true;
  String? _error;

  String _locale = 'tr';
  bool _emailNotifications = true;
  bool _marketingEmails = false;
  bool _savingGeneral = false;
  bool _savingNotifications = false;

  final _freezeController = TextEditingController();
  final _deleteController = TextEditingController();
  static const _freezeConfirm = 'DONDUR';
  static const _deleteConfirm = 'SİL';

  @override
  void initState() {
    super.initState();
    _initAndLoad();
  }

  @override
  void dispose() {
    _freezeController.dispose();
    _deleteController.dispose();
    super.dispose();
  }

  Future<void> _initAndLoad() async {
    await _auth.loadStoredToken();
    if (!mounted) return;
    final token = _auth.token;
    if (token != null && token.isNotEmpty) {
      _apiClient = ApiClient(token: token);
      _profileService = ProfileService(_apiClient!);
    }
    await _loadData();
  }

  Future<void> _loadData() async {
    setState(() {
      _loading = true;
      _error = null;
    });
    try {
      ProfileData? profile;
      List<DeliveredMessageItem> messages = [];
      if (_profileService != null) {
        profile = await _profileService!.getProfile();
        messages = await _profileService!.getDeliveredMessages();
      }
      if (!mounted) return;
      setState(() {
        _profile = profile;
        _deliveredMessages = messages;
        _locale = profile?.locale ?? 'tr';
        _emailNotifications = profile?.emailNotifications ?? true;
        _marketingEmails = profile?.marketingEmails ?? false;
        _loading = false;
      });
    } catch (e) {
      if (!mounted) return;
      setState(() {
        _loading = false;
        _error = 'Ayarlar yüklenemedi.';
      });
    }
  }

  String _formatDate(DateTime? d) {
    if (d == null) return '—';
    return '${d.day}.${d.month}.${d.year}';
  }

  String get _subscriptionEndText {
    if (_profile == null) return '—';
    final plan = _profile!.subscriptionPlanName ?? 'Ücretsiz';
    final endsAt = _profile!.subscriptionEndsAt;
    if (endsAt == null) return 'Mevcut plan: $plan';
    return 'Mevcut plan: $plan – Bitiş: ${_formatDate(endsAt)}';
  }

  Future<void> _saveGeneral() async {
    if (_profileService == null) return;
    setState(() => _savingGeneral = true);
    try {
      final ok = await _profileService!.updateSettings(locale: _locale);
      if (mounted) {
        setState(() => _savingGeneral = false);
        if (ok) ScaffoldMessenger.of(context).showSnackBar(const SnackBar(content: Text('Kaydedildi.')));
      }
    } catch (_) {
      if (mounted) setState(() => _savingGeneral = false);
    }
  }

  Future<void> _saveNotifications() async {
    if (_profileService == null) return;
    setState(() => _savingNotifications = true);
    try {
      final ok = await _profileService!.updateSettings(
        emailNotifications: _emailNotifications,
        marketingEmails: _marketingEmails,
      );
      if (mounted) {
        setState(() => _savingNotifications = false);
        if (ok) ScaffoldMessenger.of(context).showSnackBar(const SnackBar(content: Text('Kaydedildi.')));
      }
    } catch (_) {
      if (mounted) setState(() => _savingNotifications = false);
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

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: LoginColors.background,
      body: RefreshIndicator(
        onRefresh: _loadData,
        color: LoginColors.primaryEnd,
        child: _loading && _profile == null
            ? Center(child: CircularProgressIndicator(color: LoginColors.primaryEnd))
            : _error != null
                ? Center(
                    child: Padding(
                      padding: const EdgeInsets.all(24),
                      child: Column(
                        mainAxisAlignment: MainAxisAlignment.center,
                        children: [
                          Text(_error!, style: TextStyle(color: LoginColors.textLightGray), textAlign: TextAlign.center),
                          const SizedBox(height: 16),
                          TextButton(onPressed: _loadData, child: Text('Tekrar dene', style: TextStyle(color: LoginColors.primaryEnd))),
                        ],
                      ),
                    ),
                  )
                : ListView(
                    padding: const EdgeInsets.fromLTRB(20, 20, 20, 32),
                    children: [
                      _section('Hesap', [
                        _field('E-posta', _profile?.email ?? '—'),
                        const SizedBox(height: 6),
                        Text(
                          'E-posta değişikliği için destek ile iletişime geçin.',
                          style: TextStyle(fontSize: 12, color: LoginColors.textMuted),
                        ),
                      ]),
                      const SizedBox(height: 20),
                      _section('Genel', [
                        Row(
                          children: [
                            Expanded(child: Text('Dil', style: TextStyle(color: LoginColors.textWhite, fontSize: 15))),
                            DropdownButton<String>(
                              value: _locale,
                              dropdownColor: LoginColors.surface,
                              underline: const SizedBox(),
                              items: const [
                                DropdownMenuItem(value: 'tr', child: Text('Türkçe')),
                                DropdownMenuItem(value: 'en', child: Text('English')),
                              ],
                              onChanged: (v) => setState(() => _locale = v ?? 'tr'),
                            ),
                          ],
                        ),
                        const SizedBox(height: 12),
                        SizedBox(
                          width: double.infinity,
                          child: FilledButton(
                            onPressed: _savingGeneral ? null : _saveGeneral,
                            style: FilledButton.styleFrom(backgroundColor: LoginColors.primaryEnd, foregroundColor: Colors.black87),
                            child: _savingGeneral ? const SizedBox(height: 20, width: 20, child: CircularProgressIndicator(strokeWidth: 2)) : const Text('Kaydet'),
                          ),
                        ),
                      ]),
                      const SizedBox(height: 20),
                      _section('Bildirimler', [
                        SwitchListTile(
                          value: _emailNotifications,
                          onChanged: (v) => setState(() => _emailNotifications = v),
                          title: Text('E-posta bildirimleri', style: TextStyle(color: LoginColors.textWhite, fontSize: 15)),
                          subtitle: Text('Planlanmış mesajlarınız hakkında hatırlatma e-postaları alın.', style: TextStyle(color: LoginColors.textLightGray, fontSize: 13)),
                          activeColor: LoginColors.primaryEnd,
                        ),
                        SwitchListTile(
                          value: _marketingEmails,
                          onChanged: (v) => setState(() => _marketingEmails = v),
                          title: Text('Pazarlama e-postaları', style: TextStyle(color: LoginColors.textWhite, fontSize: 15)),
                          subtitle: Text('Kampanya ve yenilikler hakkında bilgi alın.', style: TextStyle(color: LoginColors.textLightGray, fontSize: 13)),
                          activeColor: LoginColors.primaryEnd,
                        ),
                        const SizedBox(height: 8),
                        SizedBox(
                          width: double.infinity,
                          child: FilledButton(
                            onPressed: _savingNotifications ? null : _saveNotifications,
                            style: FilledButton.styleFrom(backgroundColor: LoginColors.primaryEnd, foregroundColor: Colors.black87),
                            child: _savingNotifications ? const SizedBox(height: 20, width: 20, child: CircularProgressIndicator(strokeWidth: 2)) : const Text('Kaydet'),
                          ),
                        ),
                      ]),
                      const SizedBox(height: 20),
                      _section('Abonelik', [
                        Text(_subscriptionEndText, style: TextStyle(color: LoginColors.textLightGray, fontSize: 14)),
                        const SizedBox(height: 8),
                        GestureDetector(
                          onTap: widget.onManageSubscription,
                          child: Text('Yükseltmek için Aboneliği yönet sayfasına gidin.', style: TextStyle(color: LoginColors.primaryEnd, fontSize: 14, fontWeight: FontWeight.w600)),
                        ),
                      ]),
                      const SizedBox(height: 20),
                      _section('İletilen mesajlar', [
                        if (_deliveredMessages.isEmpty)
                          Text('Henüz iletilen mesaj yok.', style: TextStyle(color: LoginColors.textMuted, fontSize: 14))
                        else
                          ..._deliveredMessages.take(10).map((m) => Padding(
                                padding: const EdgeInsets.only(bottom: 8),
                                child: Row(
                                  children: [
                                    Icon(Icons.send_rounded, size: 18, color: LoginColors.primaryEnd),
                                    const SizedBox(width: 10),
                                    Expanded(
                                      child: Text(
                                        'Mesaj #${m.id ?? '?'} – ${_formatDate(m.sentAt ?? m.scheduledAt)}',
                                        style: TextStyle(color: LoginColors.textLightGray, fontSize: 14),
                                      ),
                                    ),
                                  ],
                                ),
                              )),
                        if (_deliveredMessages.length > 10)
                          Text('+ ${_deliveredMessages.length - 10} mesaj daha', style: TextStyle(color: LoginColors.textMuted, fontSize: 12)),
                      ]),
                      const SizedBox(height: 20),
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
                            fillColor: LoginColors.surface,
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
                            fillColor: LoginColors.surface,
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
      ),
    );
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

  Widget _field(String label, String value) {
    return Row(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        SizedBox(width: 100, child: Text(label, style: TextStyle(color: LoginColors.textLightGray, fontSize: 14))),
        Expanded(child: Text(value, style: TextStyle(color: LoginColors.textWhite, fontSize: 14))),
      ],
    );
  }
}
