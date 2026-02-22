import 'package:flutter/material.dart';
import 'package:url_launcher/url_launcher.dart';

import '../models/public_message_models.dart';
import '../services/public_message_service.dart';
import '../theme/login_theme.dart';

class PublicMessageDetailPage extends StatefulWidget {
  const PublicMessageDetailPage({super.key, required this.viewToken});

  final String viewToken;

  @override
  State<PublicMessageDetailPage> createState() => _PublicMessageDetailPageState();
}

class _PublicMessageDetailPageState extends State<PublicMessageDetailPage> {
  final PublicMessageService _service = PublicMessageService();
  MessageViewDetail? _detail;
  bool _loading = true;
  String? _error;

  @override
  void initState() {
    super.initState();
    _load();
  }

  Future<void> _load() async {
    setState(() {
      _loading = true;
      _error = null;
    });
    try {
      final detail = await _service.getMessageView(widget.viewToken);
      if (!mounted) return;
      setState(() {
        _detail = detail;
        _loading = false;
        if (detail == null) _error = 'Mesaj yüklenemedi.';
      });
    } catch (e) {
      if (!mounted) return;
      setState(() {
        _loading = false;
        _error = 'Mesaj yüklenemedi.';
      });
    }
  }


  Future<void> _openUrl(String url) async {
    final uri = Uri.parse(url);
    if (await canLaunchUrl(uri)) {
      await launchUrl(uri, mode: LaunchMode.externalApplication);
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: LoginColors.background,
      appBar: AppBar(
        backgroundColor: LoginColors.surface,
        elevation: 0,
        leading: IconButton(
          icon: const Icon(Icons.arrow_back_rounded),
          color: LoginColors.textWhite,
          onPressed: () => Navigator.of(context).pop(),
        ),
        title: Text(
          'Mesaj detayı',
          style: TextStyle(fontSize: 18, fontWeight: FontWeight.w600, color: LoginColors.textWhite),
        ),
        centerTitle: true,
      ),
      body: _loading
          ? Center(child: CircularProgressIndicator(color: LoginColors.primaryEnd))
          : _error != null || _detail == null
              ? Center(
                  child: Padding(
                    padding: const EdgeInsets.all(24),
                    child: Column(
                      mainAxisAlignment: MainAxisAlignment.center,
                      children: [
                        Text(_error ?? 'Mesaj bulunamadı.', style: TextStyle(color: LoginColors.textLightGray), textAlign: TextAlign.center),
                        const SizedBox(height: 16),
                        TextButton(
                          onPressed: () => Navigator.of(context).pop(),
                          child: Text('Geri dön', style: TextStyle(color: LoginColors.primaryEnd)),
                        ),
                      ],
                    ),
                  ),
                )
              : SingleChildScrollView(
                  padding: const EdgeInsets.fromLTRB(20, 16, 20, 32),
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      _buildHeaderCard(),
                      const SizedBox(height: 24),
                      Text(
                        'İçerikler',
                        style: TextStyle(
                          fontSize: 17,
                          fontWeight: FontWeight.w700,
                          color: LoginColors.textWhite,
                        ),
                      ),
                      const SizedBox(height: 14),
                      if (_detail!.contents.isEmpty)
                        _buildEmptyContents()
                      else
                        ..._detail!.contents.map((c) => Padding(
                              padding: const EdgeInsets.only(bottom: 14),
                              child: _buildContentItem(c),
                            )),
                    ],
                  ),
                ),
    );
  }

  Widget _buildHeaderCard() {
    final d = _detail!;
    return Container(
      width: double.infinity,
      padding: const EdgeInsets.all(20),
      decoration: BoxDecoration(
        color: LoginColors.surface,
        borderRadius: BorderRadius.circular(16),
        border: Border.all(color: LoginColors.border),
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          if (d.senderName != null && d.senderName!.isNotEmpty) ...[
            Row(
              children: [
                Container(
                  padding: const EdgeInsets.symmetric(horizontal: 10, vertical: 4),
                  decoration: BoxDecoration(
                    color: LoginColors.primaryEnd.withValues(alpha: 0.2),
                    borderRadius: BorderRadius.circular(8),
                  ),
                  child: Text(
                    'Gönderen',
                    style: TextStyle(fontSize: 11, fontWeight: FontWeight.w600, color: LoginColors.primaryEnd),
                  ),
                ),
              ],
            ),
            const SizedBox(height: 8),
            Text(
              d.senderName!,
              style: TextStyle(fontSize: 18, fontWeight: FontWeight.w700, color: LoginColors.textWhite),
            ),
            const SizedBox(height: 12),
          ],
          Row(
            children: [
              Icon(Icons.calendar_today_rounded, size: 18, color: LoginColors.textMuted),
              const SizedBox(width: 8),
              Text(
                PublicMessageItem.formatDateTime(d.scheduledAt),
                style: TextStyle(fontSize: 14, color: LoginColors.textLightGray),
              ),
            ],
          ),
        ],
      ),
    );
  }

  Widget _buildEmptyContents() {
    return Container(
      width: double.infinity,
      padding: const EdgeInsets.symmetric(vertical: 32, horizontal: 20),
      decoration: BoxDecoration(
        color: LoginColors.surface,
        borderRadius: BorderRadius.circular(14),
        border: Border.all(color: LoginColors.border),
      ),
      child: Column(
        children: [
          Icon(Icons.inbox_rounded, size: 48, color: LoginColors.textMuted),
          const SizedBox(height: 12),
          Text(
            'Bu mesajda görüntülenecek içerik yok.',
            style: TextStyle(fontSize: 14, color: LoginColors.textLightGray),
            textAlign: TextAlign.center,
          ),
        ],
      ),
    );
  }

  Widget _buildContentItem(MessageViewContentItem c) {
    final type = (c.type ?? '').toUpperCase();
    switch (type) {
      case 'TEXT':
        return _buildTextContent(c);
      case 'IMAGE':
        return _buildImageContent(c);
      case 'VIDEO':
        return _buildVideoContent(c);
      case 'FILE':
        return _buildFileContent(c);
      case 'AUDIO':
        return _buildAudioContent(c);
      default:
        return const SizedBox.shrink();
    }
  }

  Widget _buildTextContent(MessageViewContentItem c) {
    final text = c.textContent ?? '';
    if (text.isEmpty) return const SizedBox.shrink();
    return Container(
      width: double.infinity,
      padding: const EdgeInsets.all(18),
      decoration: BoxDecoration(
        color: LoginColors.surface,
        borderRadius: BorderRadius.circular(14),
        border: Border.all(color: LoginColors.border),
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Row(
            children: [
              Icon(Icons.text_snippet_rounded, size: 20, color: LoginColors.primaryEnd),
              const SizedBox(width: 8),
              Text('Metin', style: TextStyle(fontSize: 12, fontWeight: FontWeight.w600, color: LoginColors.textMuted)),
            ],
          ),
          const SizedBox(height: 12),
          Text(
            text,
            style: TextStyle(fontSize: 15, color: LoginColors.textWhite, height: 1.55),
          ),
        ],
      ),
    );
  }

  Widget _buildImageContent(MessageViewContentItem c) {
    final url = c.fileUrl;
    if (url == null || url.isEmpty) return const SizedBox.shrink();
    return Container(
      decoration: BoxDecoration(
        borderRadius: BorderRadius.circular(14),
        border: Border.all(color: LoginColors.border),
        boxShadow: [
          BoxShadow(
            color: Colors.black.withValues(alpha: 0.2),
            blurRadius: 12,
            offset: const Offset(0, 4),
          ),
        ],
      ),
      child: ClipRRect(
        borderRadius: BorderRadius.circular(14),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.stretch,
          children: [
            Container(
              color: LoginColors.surface,
              padding: const EdgeInsets.symmetric(horizontal: 14, vertical: 10),
              child: Row(
                children: [
                  Icon(Icons.photo_library_rounded, size: 20, color: LoginColors.primaryEnd),
                  const SizedBox(width: 8),
                  Text('Fotoğraf', style: TextStyle(fontSize: 12, fontWeight: FontWeight.w600, color: LoginColors.textMuted)),
                ],
              ),
            ),
            Image.network(
              url,
              width: double.infinity,
              fit: BoxFit.contain,
              loadingBuilder: (context, child, progress) {
                if (progress == null) return child;
                return Container(
                  height: 220,
                  alignment: Alignment.center,
                  color: LoginColors.surface,
                  child: SizedBox(
                    width: 40,
                    height: 40,
                    child: CircularProgressIndicator(strokeWidth: 2, color: LoginColors.primaryEnd),
                  ),
                );
              },
              errorBuilder: (_, __, ___) => Container(
                height: 160,
                color: LoginColors.surface,
                alignment: Alignment.center,
                padding: const EdgeInsets.all(20),
                child: Column(
                  mainAxisSize: MainAxisSize.min,
                  children: [
                    Icon(Icons.broken_image_outlined, size: 48, color: LoginColors.textMuted),
                    const SizedBox(height: 8),
                    Text('Görsel yüklenemedi', style: TextStyle(fontSize: 14, color: LoginColors.textLightGray)),
                    if (url.isNotEmpty) ...[
                      const SizedBox(height: 12),
                      TextButton.icon(
                        onPressed: () => _openUrl(url),
                        icon: const Icon(Icons.open_in_new_rounded, size: 18),
                        label: const Text('Tarayıcıda aç'),
                        style: TextButton.styleFrom(foregroundColor: LoginColors.primaryEnd),
                      ),
                    ],
                  ],
                ),
              ),
            ),
          ],
        ),
      ),
    );
  }

  Widget _buildVideoContent(MessageViewContentItem c) {
    final url = c.fileUrl;
    final name = c.fileName ?? 'Video';
    if (url == null || url.isEmpty) return const SizedBox.shrink();
    return Material(
      color: LoginColors.surface,
      borderRadius: BorderRadius.circular(14),
      child: InkWell(
        onTap: () => _openUrl(url),
        borderRadius: BorderRadius.circular(14),
        child: Container(
          padding: const EdgeInsets.all(18),
          decoration: BoxDecoration(
            borderRadius: BorderRadius.circular(14),
            border: Border.all(color: LoginColors.border),
          ),
          child: Row(
            children: [
              Container(
                width: 56,
                height: 56,
                decoration: BoxDecoration(
                  color: LoginColors.primaryEnd.withValues(alpha: 0.2),
                  borderRadius: BorderRadius.circular(12),
                ),
                alignment: Alignment.center,
                child: Icon(Icons.play_circle_filled_rounded, size: 36, color: LoginColors.primaryEnd),
              ),
              const SizedBox(width: 16),
              Expanded(
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Row(
                      children: [
                        Icon(Icons.videocam_rounded, size: 18, color: LoginColors.primaryEnd),
                        const SizedBox(width: 6),
                        Text('Video', style: TextStyle(fontSize: 12, fontWeight: FontWeight.w600, color: LoginColors.textMuted)),
                      ],
                    ),
                    const SizedBox(height: 4),
                    Text(
                      name,
                      style: TextStyle(fontSize: 15, fontWeight: FontWeight.w600, color: LoginColors.textWhite),
                      overflow: TextOverflow.ellipsis,
                    ),
                    const SizedBox(height: 4),
                    Text(
                      'Videoyu açmak için dokunun',
                      style: TextStyle(fontSize: 12, color: LoginColors.textLightGray),
                    ),
                  ],
                ),
              ),
              Icon(Icons.open_in_new_rounded, size: 22, color: LoginColors.primaryEnd),
            ],
          ),
        ),
      ),
    );
  }

  Widget _buildFileContent(MessageViewContentItem c) {
    final url = c.fileUrl;
    final name = c.fileName ?? 'Dosya';
    final size = c.fileSize;
    final sizeStr = size != null && size > 0
        ? (size < 1024 ? '$size B' : (size < 1024 * 1024 ? '${(size / 1024).toStringAsFixed(1)} KB' : '${(size / (1024 * 1024)).toStringAsFixed(1)} MB'))
        : null;
    return Material(
      color: LoginColors.surface,
      borderRadius: BorderRadius.circular(14),
      child: InkWell(
        onTap: url != null && url.isNotEmpty ? () => _openUrl(url) : null,
        borderRadius: BorderRadius.circular(14),
        child: Container(
          padding: const EdgeInsets.all(18),
          decoration: BoxDecoration(
            borderRadius: BorderRadius.circular(14),
            border: Border.all(color: LoginColors.border),
          ),
          child: Row(
            children: [
              Container(
                width: 56,
                height: 56,
                decoration: BoxDecoration(
                  color: LoginColors.primaryEnd.withValues(alpha: 0.2),
                  borderRadius: BorderRadius.circular(12),
                ),
                alignment: Alignment.center,
                child: Icon(Icons.insert_drive_file_rounded, size: 28, color: LoginColors.primaryEnd),
              ),
              const SizedBox(width: 16),
              Expanded(
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Row(
                      children: [
                        Icon(Icons.description_rounded, size: 18, color: LoginColors.primaryEnd),
                        const SizedBox(width: 6),
                        Text('Dosya', style: TextStyle(fontSize: 12, fontWeight: FontWeight.w600, color: LoginColors.textMuted)),
                      ],
                    ),
                    const SizedBox(height: 4),
                    Text(
                      name,
                      style: TextStyle(fontSize: 15, fontWeight: FontWeight.w600, color: LoginColors.textWhite),
                      overflow: TextOverflow.ellipsis,
                    ),
                    if (sizeStr != null) ...[
                      const SizedBox(height: 2),
                      Text(sizeStr, style: TextStyle(fontSize: 12, color: LoginColors.textLightGray)),
                    ],
                  ],
                ),
              ),
              if (url != null && url.isNotEmpty)
                Icon(Icons.open_in_new_rounded, size: 22, color: LoginColors.primaryEnd),
            ],
          ),
        ),
      ),
    );
  }

  Widget _buildAudioContent(MessageViewContentItem c) {
    final url = c.fileUrl;
    final name = c.fileName ?? 'Ses kaydı';
    return Material(
      color: LoginColors.surface,
      borderRadius: BorderRadius.circular(14),
      child: InkWell(
        onTap: url != null && url.isNotEmpty ? () => _openUrl(url) : null,
        borderRadius: BorderRadius.circular(14),
        child: Container(
          padding: const EdgeInsets.all(18),
          decoration: BoxDecoration(
            borderRadius: BorderRadius.circular(14),
            border: Border.all(color: LoginColors.border),
          ),
          child: Row(
            children: [
              Container(
                width: 56,
                height: 56,
                decoration: BoxDecoration(
                  color: LoginColors.primaryEnd.withValues(alpha: 0.2),
                  borderRadius: BorderRadius.circular(12),
                ),
                alignment: Alignment.center,
                child: Icon(Icons.mic_rounded, size: 28, color: LoginColors.primaryEnd),
              ),
              const SizedBox(width: 16),
              Expanded(
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Row(
                      children: [
                        Icon(Icons.audiotrack_rounded, size: 18, color: LoginColors.primaryEnd),
                        const SizedBox(width: 6),
                        Text('Ses kaydı', style: TextStyle(fontSize: 12, fontWeight: FontWeight.w600, color: LoginColors.textMuted)),
                      ],
                    ),
                    const SizedBox(height: 4),
                    Text(
                      name,
                      style: TextStyle(fontSize: 15, fontWeight: FontWeight.w600, color: LoginColors.textWhite),
                      overflow: TextOverflow.ellipsis,
                    ),
                    const SizedBox(height: 2),
                    Text(
                      'Dinlemek için dokunun',
                      style: TextStyle(fontSize: 12, color: LoginColors.textLightGray),
                    ),
                  ],
                ),
              ),
              if (url != null && url.isNotEmpty)
                Icon(Icons.play_circle_outline_rounded, size: 32, color: LoginColors.primaryEnd),
            ],
          ),
        ),
      ),
    );
  }
}
