import 'package:flutter/material.dart';

import '../models/profile_models.dart';
import '../services/api_client.dart';
import '../services/auth_service.dart';
import '../services/profile_service.dart';
import '../theme/login_theme.dart';
import 'account_danger_zone_page.dart';
import 'contact_page.dart';
import 'delivered_messages_list_page.dart';

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
  bool _loading = true;
  String? _error;

  String _locale = 'tr';
  bool _emailNotifications = true;
  bool _marketingEmails = false;
  bool _savingGeneral = false;
  bool _savingNotifications = false;

  @override
  void initState() {
    super.initState();
    _initAndLoad();
  }

  Future<void> _initAndLoad() async {
    await _auth.loadStoredToken();
    if (!mounted) return;
    final token = _auth.token;
    if (token != null && token.isNotEmpty) {
      _apiClient = ApiClient(token: token, onUnauthorized: widget.onLogout);
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
      if (_profileService != null) {
        profile = await _profileService!.getProfile();
      }
      if (!mounted) return;
      setState(() {
        _profile = profile;
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
                              icon: Icon(Icons.arrow_drop_down_rounded, color: LoginColors.textWhite),
                              style: const TextStyle(color: LoginColors.textWhite, fontSize: 15),
                              items: const [
                                DropdownMenuItem(value: 'tr', child: Text('Türkçe', style: TextStyle(color: LoginColors.textWhite))),
                                DropdownMenuItem(value: 'en', child: Text('English', style: TextStyle(color: LoginColors.textWhite))),
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
                            style: FilledButton.styleFrom(backgroundColor: LoginColors.primaryEnd, foregroundColor: Colors.white),
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
                            style: FilledButton.styleFrom(backgroundColor: LoginColors.primaryEnd, foregroundColor: Colors.white),
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
                        Text(
                          'Size iletilen mesajlarınızı görüntüleyin ve içeriklerini okuyun.',
                          style: TextStyle(color: LoginColors.textLightGray, fontSize: 14),
                        ),
                        const SizedBox(height: 12),
                        SizedBox(
                          width: double.infinity,
                          child: FilledButton.icon(
                            onPressed: () {
                              Navigator.of(context).push(
                                MaterialPageRoute<void>(
                                  builder: (context) => const DeliveredMessagesListPage(),
                                ),
                              );
                            },
                            icon: const Icon(Icons.inbox_rounded, size: 20),
                            label: const Text('İletilen mesajlarımı görüntüle'),
                            style: FilledButton.styleFrom(
                              backgroundColor: LoginColors.primaryEnd,
                              foregroundColor: Colors.white,
                              padding: const EdgeInsets.symmetric(vertical: 14),
                            ),
                          ),
                        ),
                      ]),
                      const SizedBox(height: 20),
                      _section('İletişim', [
                        Text(
                          'Sorularınız veya önerileriniz için bizimle iletişime geçebilirsiniz.',
                          style: TextStyle(color: LoginColors.textLightGray, fontSize: 14),
                        ),
                        const SizedBox(height: 12),
                        SizedBox(
                          width: double.infinity,
                          child: FilledButton.icon(
                            onPressed: () {
                              Navigator.of(context).push(
                                MaterialPageRoute<void>(
                                  builder: (context) => const ContactPage(),
                                ),
                              );
                            },
                            icon: const Icon(Icons.mail_outline_rounded, size: 20),
                            label: const Text('Bizimle iletişime geçin'),
                            style: FilledButton.styleFrom(
                              backgroundColor: LoginColors.primaryEnd,
                              foregroundColor: Colors.white,
                              padding: const EdgeInsets.symmetric(vertical: 14),
                            ),
                          ),
                        ),
                      ]),
                      const SizedBox(height: 20),
                      _section('Hesap güvenliği', [
                        Text(
                          'Hesabınızı dondurmak veya kalıcı olarak silmek isterseniz aşağıdaki bağlantıdan ilerleyebilirsiniz.',
                          style: TextStyle(color: LoginColors.textLightGray, fontSize: 14),
                        ),
                        const SizedBox(height: 12),
                        Material(
                          color: LoginColors.background,
                          borderRadius: BorderRadius.circular(12),
                          child: InkWell(
                            onTap: () {
                              Navigator.of(context).push(
                                MaterialPageRoute<void>(
                                  builder: (context) => AccountDangerZonePage(onLogout: widget.onLogout),
                                ),
                              );
                            },
                            borderRadius: BorderRadius.circular(12),
                            child: Container(
                              padding: const EdgeInsets.symmetric(horizontal: 14, vertical: 14),
                              decoration: BoxDecoration(
                                borderRadius: BorderRadius.circular(12),
                                border: Border.all(color: LoginColors.border),
                              ),
                              child: Row(
                                children: [
                                  Icon(Icons.warning_amber_rounded, size: 22, color: Colors.orange.shade700),
                                  const SizedBox(width: 12),
                                  Expanded(
                                    child: Text(
                                      'Hesap dondurma ve silme',
                                      style: TextStyle(fontSize: 14, color: LoginColors.textWhite),
                                    ),
                                  ),
                                  Icon(Icons.chevron_right_rounded, size: 22, color: LoginColors.textMuted),
                                ],
                              ),
                            ),
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
