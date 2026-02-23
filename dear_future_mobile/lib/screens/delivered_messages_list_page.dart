import 'package:flutter/material.dart';

import '../models/profile_models.dart';
import '../services/api_client.dart';
import '../services/auth_service.dart';
import '../services/profile_service.dart';
import '../theme/login_theme.dart';
import 'public_message_detail_page.dart';

class DeliveredMessagesListPage extends StatefulWidget {
  const DeliveredMessagesListPage({super.key});

  @override
  State<DeliveredMessagesListPage> createState() => _DeliveredMessagesListPageState();
}

class _DeliveredMessagesListPageState extends State<DeliveredMessagesListPage> {
  final AuthService _auth = AuthService();
  ProfileService? _profileService;
  List<DeliveredMessageItem> _messages = [];
  bool _loading = true;
  String? _error;

  @override
  void initState() {
    super.initState();
    _initAndLoad();
  }

  Future<void> _initAndLoad() async {
    await _auth.loadStoredToken();
    if (!mounted) return;
    final token = _auth.token;
    if (token == null || token.isEmpty) {
      setState(() {
        _loading = false;
        _error = 'Oturum bulunamadı.';
      });
      return;
    }
    _profileService = ProfileService(ApiClient(token: token));
    await _loadMessages();
  }

  Future<void> _loadMessages() async {
    setState(() {
      _loading = true;
      _error = null;
    });
    try {
      final list = _profileService != null ? await _profileService!.getDeliveredMessages() : <DeliveredMessageItem>[];
      if (!mounted) return;
      setState(() {
        _messages = list;
        _loading = false;
      });
    } catch (e) {
      if (!mounted) return;
      setState(() {
        _loading = false;
        _error = 'İletilen mesajlar yüklenemedi.';
      });
    }
  }

  String _formatDate(DateTime? d) {
    if (d == null) return '—';
    if (d.year < 2000) return '—'; // Geçersiz/epoch tarih
    return '${d.day.toString().padLeft(2, '0')}.${d.month.toString().padLeft(2, '0')}.${d.year} ${d.hour.toString().padLeft(2, '0')}:${d.minute.toString().padLeft(2, '0')}';
  }

  String _formatDateShort(DateTime? d) {
    if (d == null || d.year < 2000) return '';
    final now = DateTime.now();
    final today = DateTime(now.year, now.month, now.day);
    final msgDay = DateTime(d.year, d.month, d.day);
    if (msgDay == today) {
      return '${d.hour.toString().padLeft(2, '0')}:${d.minute.toString().padLeft(2, '0')}';
    }
    return '${d.day}.${d.month}.${d.year}';
  }

  /// İçerik türüne göre etiket ve ikon (contentTypes null-safe)
  List<_ContentTypeChip> _contentTypeChips(DeliveredMessageItem m) {
    final types = (m.contentTypes ?? const []).map((s) => s.toUpperCase()).toSet();
    final list = <_ContentTypeChip>[];
    if (types.contains('TEXT')) list.add(const _ContentTypeChip(label: 'Metin', icon: Icons.text_snippet_rounded));
    if (types.contains('IMAGE')) list.add(const _ContentTypeChip(label: 'Fotoğraf', icon: Icons.photo_library_rounded));
    if (types.contains('VIDEO')) list.add(const _ContentTypeChip(label: 'Video', icon: Icons.videocam_rounded));
    if (types.contains('FILE')) list.add(const _ContentTypeChip(label: 'Dosya', icon: Icons.attach_file_rounded));
    if (types.contains('AUDIO')) list.add(const _ContentTypeChip(label: 'Ses', icon: Icons.audiotrack_rounded));
    return list;
  }

  Future<void> _openDetail(DeliveredMessageItem item) async {
    String? viewToken = item.viewToken;
    if (viewToken == null || viewToken.isEmpty) {
      final id = item.id;
      if (id == null || _profileService == null) return;
      viewToken = await _profileService!.getMessageViewToken(id);
    }
    if (viewToken == null || viewToken.isEmpty || !mounted) return;
    Navigator.of(context).push(
      MaterialPageRoute<void>(
        builder: (context) => PublicMessageDetailPage(
          viewToken: viewToken!,
          messageId: item.id,
        ),
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
        title: const Text('İletilen mesajlar'),
        elevation: 0,
      ),
      body: _loading
          ? const Center(child: CircularProgressIndicator(color: LoginColors.primaryEnd))
          : _error != null
              ? Center(
                  child: Padding(
                    padding: const EdgeInsets.all(24),
                    child: Column(
                      mainAxisAlignment: MainAxisAlignment.center,
                      children: [
                        Icon(Icons.error_outline_rounded, size: 56, color: LoginColors.textMuted),
                        const SizedBox(height: 16),
                        Text(_error!, style: const TextStyle(color: LoginColors.textLightGray, fontSize: 15), textAlign: TextAlign.center),
                        const SizedBox(height: 20),
                        FilledButton.icon(
                          onPressed: _loadMessages,
                          icon: const Icon(Icons.refresh_rounded, size: 20),
                          label: const Text('Tekrar dene'),
                          style: FilledButton.styleFrom(backgroundColor: LoginColors.primaryEnd, foregroundColor: Colors.white),
                        ),
                      ],
                    ),
                  ),
                )
              : _messages.isEmpty
                  ? Center(
                      child: Padding(
                        padding: const EdgeInsets.all(32),
                        child: Column(
                          mainAxisAlignment: MainAxisAlignment.center,
                          children: [
                            Container(
                              padding: const EdgeInsets.all(24),
                              decoration: BoxDecoration(
                                color: LoginColors.surface,
                                shape: BoxShape.circle,
                                boxShadow: [
                                  BoxShadow(
                                    color: LoginColors.primaryEnd.withValues(alpha: 0.15),
                                    blurRadius: 24,
                                    spreadRadius: 2,
                                  ),
                                ],
                              ),
                              child: Icon(Icons.inbox_rounded, size: 56, color: LoginColors.primaryEnd.withValues(alpha: 0.7)),
                            ),
                            const SizedBox(height: 24),
                            Text(
                              'Henüz iletilen mesaj yok',
                              style: TextStyle(fontSize: 18, fontWeight: FontWeight.w600, color: LoginColors.textWhite),
                              textAlign: TextAlign.center,
                            ),
                            const SizedBox(height: 8),
                            Text(
                              'Size zamanlanmış mesajlar iletildiğinde burada listelenecek.',
                              style: TextStyle(fontSize: 14, color: LoginColors.textMuted),
                              textAlign: TextAlign.center,
                            ),
                          ],
                        ),
                      ),
                    )
                  : RefreshIndicator(
                      onRefresh: _loadMessages,
                      color: LoginColors.primaryEnd,
                      child: ListView.builder(
                        padding: const EdgeInsets.fromLTRB(16, 12, 16, 32),
                        itemCount: _messages.length,
                        itemBuilder: (context, index) {
                          final m = _messages[index];
                          final sentAt = m.sentAt ?? m.scheduledAt;
                          final fullPreview = (m.contentPreview ?? '').trim();
                          final preview = fullPreview.length > 80 ? '${fullPreview.substring(0, 80)}...' : fullPreview;
                          final chips = _contentTypeChips(m);
                          return _MessageCard(
                            sentAt: sentAt,
                            formatDate: _formatDate,
                            formatDateShort: _formatDateShort,
                            preview: preview,
                            previewImageUrl: m.previewImageUrl,
                            contentChips: chips,
                            onTap: () => _openDetail(m),
                          );
                        },
                      ),
                    ),
    );
  }
}

class _ContentTypeChip {
  const _ContentTypeChip({required this.label, required this.icon});
  final String label;
  final IconData icon;
}

class _MessageCard extends StatelessWidget {
  const _MessageCard({
    required this.sentAt,
    required this.formatDate,
    required this.formatDateShort,
    required this.preview,
    this.previewImageUrl,
    required this.contentChips,
    required this.onTap,
  });

  final DateTime? sentAt;
  final String Function(DateTime?) formatDate;
  final String Function(DateTime?) formatDateShort;
  final String preview;
  final String? previewImageUrl;
  final List<_ContentTypeChip> contentChips;
  final VoidCallback onTap;

  Widget _buildPlaceholderIcon() {
    return Container(
      width: 56,
      height: 56,
      decoration: BoxDecoration(
        gradient: LinearGradient(
          begin: Alignment.topLeft,
          end: Alignment.bottomRight,
          colors: [
            LoginColors.primaryEnd.withValues(alpha: 0.2),
            LoginColors.primaryEnd.withValues(alpha: 0.08),
          ],
        ),
        borderRadius: BorderRadius.circular(12),
      ),
      alignment: Alignment.center,
      child: const Icon(Icons.mail_rounded, color: LoginColors.primaryEnd, size: 24),
    );
  }

  @override
  Widget build(BuildContext context) {
    return Padding(
      padding: const EdgeInsets.only(bottom: 12),
      child: Material(
        color: LoginColors.surface,
        borderRadius: BorderRadius.circular(16),
        clipBehavior: Clip.antiAlias,
        child: InkWell(
          onTap: onTap,
          child: Container(
            decoration: BoxDecoration(
              borderRadius: BorderRadius.circular(16),
              border: Border.all(color: LoginColors.border),
              gradient: LinearGradient(
                begin: Alignment.topLeft,
                end: Alignment.bottomRight,
                colors: [
                  LoginColors.surface,
                  LoginColors.surface,
                  LoginColors.primaryEnd.withValues(alpha: 0.03),
                ],
              ),
            ),
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Padding(
                  padding: const EdgeInsets.fromLTRB(16, 14, 16, 10),
                  child: Row(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      SizedBox(
                        width: 56,
                        height: 56,
                        child: (previewImageUrl != null && previewImageUrl!.isNotEmpty)
                            ? ClipRRect(
                                borderRadius: BorderRadius.circular(12),
                                child: Image.network(
                                  previewImageUrl!,
                                  width: 56,
                                  height: 56,
                                  fit: BoxFit.cover,
                                  errorBuilder: (_, __, ___) => _buildPlaceholderIcon(),
                                ),
                              )
                            : _buildPlaceholderIcon(),
                      ),
                      const SizedBox(width: 14),
                      Expanded(
                        child: Column(
                          crossAxisAlignment: CrossAxisAlignment.start,
                          children: [
                            Row(
                              children: [
                                Expanded(
                                  child: Text(
                                    sentAt != null ? formatDate(sentAt) : '—',
                                    style: const TextStyle(
                                      fontSize: 15,
                                      fontWeight: FontWeight.w700,
                                      color: LoginColors.textWhite,
                                    ),
                                  ),
                                ),
                                if (sentAt != null)
                                  Container(
                                    padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 4),
                                    decoration: BoxDecoration(
                                      color: LoginColors.primaryEnd.withValues(alpha: 0.12),
                                      borderRadius: BorderRadius.circular(8),
                                    ),
                                    child: Text(
                                      formatDateShort(sentAt),
                                      style: const TextStyle(fontSize: 12, fontWeight: FontWeight.w600, color: LoginColors.primaryEnd),
                                    ),
                                  ),
                              ],
                            ),
                            if (preview.isNotEmpty) ...[
                              const SizedBox(height: 6),
                              Text(
                                preview,
                                style: TextStyle(fontSize: 14, color: LoginColors.textLightGray, height: 1.35),
                                maxLines: 2,
                                overflow: TextOverflow.ellipsis,
                              ),
                            ],
                          ],
                        ),
                      ),
                      const SizedBox(width: 8),
                      Icon(Icons.arrow_forward_ios_rounded, size: 16, color: LoginColors.textMuted),
                    ],
                  ),
                ),
                if (contentChips.isNotEmpty) ...[
                  const Divider(height: 1),
                  Padding(
                    padding: const EdgeInsets.fromLTRB(16, 10, 16, 14),
                    child: Wrap(
                      spacing: 8,
                      runSpacing: 6,
                      children: contentChips.map((chip) {
                        return Container(
                          padding: const EdgeInsets.symmetric(horizontal: 10, vertical: 6),
                          decoration: BoxDecoration(
                            color: LoginColors.background,
                            borderRadius: BorderRadius.circular(8),
                            border: Border.all(color: LoginColors.border),
                          ),
                          child: Row(
                            mainAxisSize: MainAxisSize.min,
                            children: [
                              Icon(chip.icon, size: 16, color: LoginColors.primaryEnd),
                              const SizedBox(width: 6),
                              Text(
                                chip.label,
                                style: const TextStyle(fontSize: 12, fontWeight: FontWeight.w500, color: LoginColors.textLightGray),
                              ),
                            ],
                          ),
                        );
                      }).toList(),
                    ),
                  ),
                ] else
                  const SizedBox(height: 4),
              ],
            ),
          ),
        ),
      ),
    );
  }
}
