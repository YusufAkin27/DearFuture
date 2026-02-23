import 'dart:async';

import 'package:audioplayers/audioplayers.dart';
import 'package:flutter/material.dart';
import 'package:url_launcher/url_launcher.dart';

import '../models/public_message_models.dart';
import '../services/api_client.dart';
import '../services/auth_service.dart';
import '../services/public_message_service.dart';
import '../theme/login_theme.dart';

class PublicMessageDetailPage extends StatefulWidget {
  const PublicMessageDetailPage({
    super.key,
    required this.viewToken,
    this.messageId,
    this.initialStarred = false,
  });

  final String viewToken;
  final int? messageId;
  final bool initialStarred;

  @override
  State<PublicMessageDetailPage> createState() =>
      _PublicMessageDetailPageState();
}

class _PublicMessageDetailPageState extends State<PublicMessageDetailPage> {
  final PublicMessageService _service = PublicMessageService();
  PublicMessageService? _authService;
  MessageViewDetail? _detail;
  bool _loading = true;
  String? _error;
  bool _starred = false;
  bool _starLoading = false;

  @override
  void initState() {
    super.initState();
    _starred = widget.initialStarred;
    _initAuth();
    _load();
  }

  Future<void> _initAuth() async {
    final auth = AuthService();
    await auth.loadStoredToken();
    final token = auth.token;
    if (token != null && token.isNotEmpty && mounted) {
      setState(() {
        _authService = PublicMessageService(ApiClient(token: token));
      });
    }
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

  Future<void> _toggleStar() async {
    final id = widget.messageId;
    if (id == null || _authService == null || _starLoading) return;
    setState(() => _starLoading = true);
    try {
      bool ok;
      if (_starred) {
        ok = await _authService!.unstarMessage(id);
      } else {
        ok = await _authService!.starMessage(id);
      }
      if (!mounted) return;
      if (ok) setState(() => _starred = !_starred);
    } catch (_) {}
    if (mounted) setState(() => _starLoading = false);
  }

  Future<void> _openUrl(String url) async {
    final uri = Uri.parse(url);
    if (await canLaunchUrl(uri)) {
      await launchUrl(uri, mode: LaunchMode.externalApplication);
    }
  }

  List<MessageViewContentItem> _filterByType(String type) {
    return _detail!.contents
        .where((c) => (c.type ?? '').toUpperCase() == type)
        .toList();
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: LoginColors.background,
      body: _loading
          ? const Center(
              child: CircularProgressIndicator(color: LoginColors.primaryEnd))
          : _error != null || _detail == null
              ? _buildError()
              : _buildContent(),
    );
  }

  Widget _buildError() {
    return Center(
      child: Padding(
        padding: const EdgeInsets.all(24),
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            const Icon(Icons.error_outline_rounded,
                size: 56, color: LoginColors.textMuted),
            const SizedBox(height: 16),
            Text(_error ?? 'Mesaj bulunamadı.',
                style: const TextStyle(color: LoginColors.textLightGray),
                textAlign: TextAlign.center),
            const SizedBox(height: 16),
            TextButton(
              onPressed: () => Navigator.of(context).pop(),
              child: const Text('Geri dön',
                  style: TextStyle(color: LoginColors.primaryEnd)),
            ),
          ],
        ),
      ),
    );
  }

  Widget _buildContent() {
    final texts = _filterByType('TEXT');
    final images = _filterByType('IMAGE');
    final videos = _filterByType('VIDEO');
    final audios = _filterByType('AUDIO');
    final files = _filterByType('FILE');

    return CustomScrollView(
      slivers: [
        _buildAppBar(),
        SliverToBoxAdapter(
          child: Padding(
            padding: const EdgeInsets.fromLTRB(20, 8, 20, 32),
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                _buildHeaderCard(),
                if (texts.isNotEmpty) ...[
                  const SizedBox(height: 20),
                  ...texts.map((c) => Padding(
                        padding: const EdgeInsets.only(bottom: 12),
                        child: _buildTextContent(c),
                      )),
                ],
                if (images.isNotEmpty) ...[
                  const SizedBox(height: 20),
                  _buildSectionHeader(
                      Icons.photo_library_rounded, 'Fotoğraflar', images.length),
                  const SizedBox(height: 12),
                  _buildImageGrid(images),
                ],
                if (videos.isNotEmpty) ...[
                  const SizedBox(height: 20),
                  _buildSectionHeader(
                      Icons.videocam_rounded, 'Videolar', videos.length),
                  const SizedBox(height: 12),
                  ...videos.map((c) => Padding(
                        padding: const EdgeInsets.only(bottom: 10),
                        child: _buildVideoTile(c),
                      )),
                ],
                if (audios.isNotEmpty) ...[
                  const SizedBox(height: 20),
                  _buildSectionHeader(
                      Icons.audiotrack_rounded, 'Ses Kayıtları', audios.length),
                  const SizedBox(height: 12),
                  ...audios.map((c) => Padding(
                        padding: const EdgeInsets.only(bottom: 10),
                        child: _InlineAudioPlayer(item: c),
                      )),
                ],
                if (files.isNotEmpty) ...[
                  const SizedBox(height: 20),
                  _buildSectionHeader(
                      Icons.folder_rounded, 'Dosyalar', files.length),
                  const SizedBox(height: 12),
                  ...files.map((c) => Padding(
                        padding: const EdgeInsets.only(bottom: 10),
                        child: _buildFileTile(c),
                      )),
                ],
                if (_detail!.contents.isEmpty) ...[
                  const SizedBox(height: 20),
                  _buildEmptyContents(),
                ],
              ],
            ),
          ),
        ),
      ],
    );
  }

  SliverAppBar _buildAppBar() {
    return SliverAppBar(
      backgroundColor: LoginColors.surface,
      pinned: true,
      elevation: 0,
      leading: IconButton(
        icon: const Icon(Icons.arrow_back_rounded),
        color: LoginColors.textWhite,
        onPressed: () =>
            Navigator.of(context).pop(_starred != widget.initialStarred),
      ),
      title: const Text(
        'Mesaj Detayı',
        style: TextStyle(
            fontSize: 18,
            fontWeight: FontWeight.w600,
            color: LoginColors.textWhite),
      ),
      centerTitle: true,
      actions: [
        if (widget.messageId != null && _authService != null)
          IconButton(
            onPressed: _starLoading ? null : _toggleStar,
            icon: _starLoading
                ? const SizedBox(
                    width: 22,
                    height: 22,
                    child: CircularProgressIndicator(
                        strokeWidth: 2, color: LoginColors.primaryEnd),
                  )
                : Icon(
                    _starred ? Icons.star_rounded : Icons.star_outline_rounded,
                    color: _starred
                        ? const Color(0xFFFFD600)
                        : LoginColors.textMuted,
                    size: 28,
                  ),
          ),
      ],
    );
  }

  Widget _buildHeaderCard() {
    final d = _detail!;
    return Container(
      width: double.infinity,
      padding: const EdgeInsets.all(20),
      decoration: BoxDecoration(
        gradient: LinearGradient(
          begin: Alignment.topLeft,
          end: Alignment.bottomRight,
          colors: [
            LoginColors.surface,
            LoginColors.primaryEnd.withValues(alpha: 0.08),
          ],
        ),
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
                  width: 40,
                  height: 40,
                  decoration: BoxDecoration(
                    color: LoginColors.primaryEnd.withValues(alpha: 0.2),
                    borderRadius: BorderRadius.circular(12),
                  ),
                  alignment: Alignment.center,
                  child: Text(
                    d.senderName![0].toUpperCase(),
                    style: const TextStyle(
                        fontSize: 18,
                        fontWeight: FontWeight.w700,
                        color: LoginColors.primaryEnd),
                  ),
                ),
                const SizedBox(width: 12),
                Expanded(
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      Text(
                        d.senderName!,
                        style: const TextStyle(
                            fontSize: 17,
                            fontWeight: FontWeight.w700,
                            color: LoginColors.textWhite),
                      ),
                      const SizedBox(height: 2),
                      const Text('Gönderen',
                          style: TextStyle(
                              fontSize: 12, color: LoginColors.textMuted)),
                    ],
                  ),
                ),
              ],
            ),
            const SizedBox(height: 16),
            Container(height: 1, color: LoginColors.border),
            const SizedBox(height: 12),
          ],
          Row(
            children: [
              const Icon(Icons.schedule_rounded,
                  size: 18, color: LoginColors.textMuted),
              const SizedBox(width: 8),
              Text(
                PublicMessageItem.formatDateTime(d.scheduledAt),
                style: const TextStyle(
                    fontSize: 14, color: LoginColors.textLightGray),
              ),
            ],
          ),
        ],
      ),
    );
  }

  Widget _buildSectionHeader(IconData icon, String title, int count) {
    return Row(
      children: [
        Icon(icon, size: 20, color: LoginColors.primaryEnd),
        const SizedBox(width: 8),
        Text(title,
            style: const TextStyle(
                fontSize: 16,
                fontWeight: FontWeight.w700,
                color: LoginColors.textWhite)),
        const SizedBox(width: 8),
        Container(
          padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 2),
          decoration: BoxDecoration(
            color: LoginColors.primaryEnd.withValues(alpha: 0.2),
            borderRadius: BorderRadius.circular(10),
          ),
          child: Text('$count',
              style: const TextStyle(
                  fontSize: 12,
                  fontWeight: FontWeight.w600,
                  color: LoginColors.primaryEnd)),
        ),
      ],
    );
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
      child: SelectableText(
        text,
        style: const TextStyle(
            fontSize: 15, color: LoginColors.textWhite, height: 1.6),
      ),
    );
  }

  // ──────────── PHOTOS ────────────

  Widget _buildImageGrid(List<MessageViewContentItem> images) {
    if (images.length == 1) return _buildSingleImage(images[0]);
    return GridView.builder(
      shrinkWrap: true,
      physics: const NeverScrollableScrollPhysics(),
      itemCount: images.length,
      gridDelegate: const SliverGridDelegateWithFixedCrossAxisCount(
        crossAxisCount: 2,
        crossAxisSpacing: 10,
        mainAxisSpacing: 10,
        childAspectRatio: 1,
      ),
      itemBuilder: (context, index) {
        final url = images[index].fileUrl;
        if (url == null || url.isEmpty) return const SizedBox.shrink();
        return GestureDetector(
          onTap: () => _openImageViewer(images, index),
          child: Container(
            decoration: BoxDecoration(
              borderRadius: BorderRadius.circular(12),
              border: Border.all(color: LoginColors.border),
            ),
            child: ClipRRect(
              borderRadius: BorderRadius.circular(12),
              child: Image.network(url,
                  fit: BoxFit.cover,
                  loadingBuilder: (_, child, p) => p == null
                      ? child
                      : Container(
                          color: LoginColors.surface,
                          alignment: Alignment.center,
                          child: const CircularProgressIndicator(
                              strokeWidth: 2, color: LoginColors.primaryEnd)),
                  errorBuilder: (_, __, ___) => Container(
                      color: LoginColors.surface,
                      alignment: Alignment.center,
                      child: const Icon(Icons.broken_image_outlined,
                          size: 32, color: LoginColors.textMuted))),
            ),
          ),
        );
      },
    );
  }

  Widget _buildSingleImage(MessageViewContentItem c) {
    final url = c.fileUrl;
    if (url == null || url.isEmpty) return const SizedBox.shrink();
    return GestureDetector(
      onTap: () => _openImageViewer([c], 0),
      child: Container(
        constraints: const BoxConstraints(maxHeight: 300),
        decoration: BoxDecoration(
          borderRadius: BorderRadius.circular(14),
          border: Border.all(color: LoginColors.border),
        ),
        child: ClipRRect(
          borderRadius: BorderRadius.circular(14),
          child: Image.network(url,
              width: double.infinity,
              fit: BoxFit.cover,
              loadingBuilder: (_, child, p) => p == null
                  ? child
                  : Container(
                      height: 200,
                      color: LoginColors.surface,
                      alignment: Alignment.center,
                      child: const CircularProgressIndicator(
                          strokeWidth: 2, color: LoginColors.primaryEnd)),
              errorBuilder: (_, __, ___) => Container(
                  height: 160,
                  color: LoginColors.surface,
                  alignment: Alignment.center,
                  child: const Icon(Icons.broken_image_outlined,
                      size: 48, color: LoginColors.textMuted))),
        ),
      ),
    );
  }

  void _openImageViewer(List<MessageViewContentItem> images, int initial) {
    Navigator.of(context).push(MaterialPageRoute<void>(
      builder: (_) => _FullScreenImageViewer(
        images: images.map((e) => e.fileUrl ?? '').toList(),
        initialIndex: initial,
        onDownload: _openUrl,
      ),
    ));
  }

  // ──────────── VIDEO ────────────

  Widget _buildVideoTile(MessageViewContentItem c) {
    final url = c.fileUrl;
    final name = c.fileName ?? 'Video';
    return Container(
      decoration: BoxDecoration(
        color: LoginColors.surface,
        borderRadius: BorderRadius.circular(14),
        border: Border.all(color: LoginColors.border),
      ),
      child: Column(
        children: [
          InkWell(
            onTap: url != null && url.isNotEmpty ? () => _openUrl(url) : null,
            borderRadius: const BorderRadius.vertical(top: Radius.circular(14)),
            child: Padding(
              padding: const EdgeInsets.all(14),
              child: Row(
                children: [
                  Container(
                    width: 48,
                    height: 48,
                    decoration: BoxDecoration(
                      color: const Color(0xFFE53935).withValues(alpha: 0.15),
                      borderRadius: BorderRadius.circular(12),
                    ),
                    alignment: Alignment.center,
                    child: const Icon(Icons.play_circle_filled_rounded,
                        size: 26, color: Color(0xFFE53935)),
                  ),
                  const SizedBox(width: 14),
                  Expanded(
                    child: Column(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        Text(name,
                            style: const TextStyle(
                                fontSize: 14,
                                fontWeight: FontWeight.w600,
                                color: LoginColors.textWhite),
                            maxLines: 1,
                            overflow: TextOverflow.ellipsis),
                        const SizedBox(height: 3),
                        const Text('Videoyu oynatmak için dokunun',
                            style: TextStyle(
                                fontSize: 12, color: LoginColors.textMuted)),
                      ],
                    ),
                  ),
                  const Icon(Icons.play_arrow_rounded,
                      size: 24, color: LoginColors.textMuted),
                ],
              ),
            ),
          ),
          if (url != null && url.isNotEmpty)
            Container(
              decoration: BoxDecoration(
                border: Border(top: BorderSide(color: LoginColors.border)),
              ),
              child: InkWell(
                onTap: () => _openUrl(url),
                borderRadius:
                    const BorderRadius.vertical(bottom: Radius.circular(14)),
                child: const Padding(
                  padding: EdgeInsets.symmetric(vertical: 10),
                  child: Row(
                    mainAxisAlignment: MainAxisAlignment.center,
                    children: [
                      Icon(Icons.download_rounded,
                          size: 18, color: LoginColors.primaryEnd),
                      SizedBox(width: 6),
                      Text('İndir / Tarayıcıda aç',
                          style: TextStyle(
                              fontSize: 13,
                              fontWeight: FontWeight.w500,
                              color: LoginColors.primaryEnd)),
                    ],
                  ),
                ),
              ),
            ),
        ],
      ),
    );
  }

  // ──────────── FILE ────────────

  Widget _buildFileTile(MessageViewContentItem c) {
    final url = c.fileUrl;
    final name = c.fileName ?? 'Dosya';
    final size = c.fileSize;
    final sizeStr = _formatFileSize(size);
    final ext = _fileExtension(name);
    final fileColor = _fileColor(ext);

    return Container(
      decoration: BoxDecoration(
        color: LoginColors.surface,
        borderRadius: BorderRadius.circular(14),
        border: Border.all(color: LoginColors.border),
      ),
      child: Column(
        children: [
          Padding(
            padding: const EdgeInsets.all(14),
            child: Row(
              children: [
                Container(
                  width: 48,
                  height: 48,
                  decoration: BoxDecoration(
                    color: fileColor.withValues(alpha: 0.15),
                    borderRadius: BorderRadius.circular(12),
                  ),
                  alignment: Alignment.center,
                  child: Column(
                    mainAxisAlignment: MainAxisAlignment.center,
                    children: [
                      Icon(_fileIcon(ext), size: 22, color: fileColor),
                      if (ext.isNotEmpty)
                        Text(ext.toUpperCase(),
                            style: TextStyle(
                                fontSize: 8,
                                fontWeight: FontWeight.w700,
                                color: fileColor)),
                    ],
                  ),
                ),
                const SizedBox(width: 14),
                Expanded(
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      Text(name,
                          style: const TextStyle(
                              fontSize: 14,
                              fontWeight: FontWeight.w600,
                              color: LoginColors.textWhite),
                          maxLines: 1,
                          overflow: TextOverflow.ellipsis),
                      if (sizeStr != null) ...[
                        const SizedBox(height: 3),
                        Text(sizeStr,
                            style: const TextStyle(
                                fontSize: 12, color: LoginColors.textMuted)),
                      ],
                    ],
                  ),
                ),
              ],
            ),
          ),
          if (url != null && url.isNotEmpty)
            Container(
              decoration: BoxDecoration(
                border: Border(top: BorderSide(color: LoginColors.border)),
              ),
              child: InkWell(
                onTap: () => _openUrl(url),
                borderRadius:
                    const BorderRadius.vertical(bottom: Radius.circular(14)),
                child: const Padding(
                  padding: EdgeInsets.symmetric(vertical: 10),
                  child: Row(
                    mainAxisAlignment: MainAxisAlignment.center,
                    children: [
                      Icon(Icons.download_rounded,
                          size: 18, color: LoginColors.primaryEnd),
                      SizedBox(width: 6),
                      Text('İndir',
                          style: TextStyle(
                              fontSize: 13,
                              fontWeight: FontWeight.w500,
                              color: LoginColors.primaryEnd)),
                    ],
                  ),
                ),
              ),
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
      child: const Column(
        children: [
          Icon(Icons.inbox_rounded, size: 48, color: LoginColors.textMuted),
          SizedBox(height: 12),
          Text('Bu mesajda görüntülenecek içerik yok.',
              style: TextStyle(fontSize: 14, color: LoginColors.textLightGray),
              textAlign: TextAlign.center),
        ],
      ),
    );
  }

  // ──────────── Helpers ────────────

  String? _formatFileSize(int? bytes) {
    if (bytes == null || bytes <= 0) return null;
    if (bytes < 1024) return '$bytes B';
    if (bytes < 1024 * 1024) return '${(bytes / 1024).toStringAsFixed(1)} KB';
    return '${(bytes / (1024 * 1024)).toStringAsFixed(1)} MB';
  }

  String _fileExtension(String name) {
    final idx = name.lastIndexOf('.');
    if (idx < 0 || idx == name.length - 1) return '';
    return name.substring(idx + 1).toLowerCase();
  }

  IconData _fileIcon(String ext) {
    switch (ext) {
      case 'pdf':
        return Icons.picture_as_pdf_rounded;
      case 'zip':
      case 'rar':
      case '7z':
      case 'tar':
      case 'gz':
        return Icons.folder_zip_rounded;
      case 'doc':
      case 'docx':
        return Icons.description_rounded;
      case 'xls':
      case 'xlsx':
        return Icons.table_chart_rounded;
      case 'ppt':
      case 'pptx':
        return Icons.slideshow_rounded;
      default:
        return Icons.insert_drive_file_rounded;
    }
  }

  Color _fileColor(String ext) {
    switch (ext) {
      case 'pdf':
        return const Color(0xFFE53935);
      case 'zip':
      case 'rar':
      case '7z':
      case 'tar':
      case 'gz':
        return const Color(0xFFFF9800);
      case 'doc':
      case 'docx':
        return const Color(0xFF1976D2);
      case 'xls':
      case 'xlsx':
        return const Color(0xFF388E3C);
      case 'ppt':
      case 'pptx':
        return const Color(0xFFD84315);
      default:
        return LoginColors.primaryEnd;
    }
  }
}

// ──────────── Inline Audio Player ────────────

class _InlineAudioPlayer extends StatefulWidget {
  const _InlineAudioPlayer({required this.item});
  final MessageViewContentItem item;

  @override
  State<_InlineAudioPlayer> createState() => _InlineAudioPlayerState();
}

class _InlineAudioPlayerState extends State<_InlineAudioPlayer> {
  final AudioPlayer _player = AudioPlayer();
  PlayerState _playerState = PlayerState.stopped;
  Duration _duration = Duration.zero;
  Duration _position = Duration.zero;
  StreamSubscription<Duration>? _posSub;
  StreamSubscription<Duration>? _durSub;
  StreamSubscription<PlayerState>? _stateSub;

  @override
  void initState() {
    super.initState();
    _durSub = _player.onDurationChanged.listen((d) {
      if (mounted) setState(() => _duration = d);
    });
    _posSub = _player.onPositionChanged.listen((p) {
      if (mounted) setState(() => _position = p);
    });
    _stateSub = _player.onPlayerStateChanged.listen((s) {
      if (mounted) setState(() => _playerState = s);
    });
  }

  @override
  void dispose() {
    _posSub?.cancel();
    _durSub?.cancel();
    _stateSub?.cancel();
    _player.dispose();
    super.dispose();
  }

  Future<void> _togglePlay() async {
    final url = widget.item.fileUrl;
    if (url == null || url.isEmpty) return;
    if (_playerState == PlayerState.playing) {
      await _player.pause();
    } else {
      await _player.play(UrlSource(url));
    }
  }

  Future<void> _openUrl(String url) async {
    final uri = Uri.parse(url);
    if (await canLaunchUrl(uri)) {
      await launchUrl(uri, mode: LaunchMode.externalApplication);
    }
  }

  String _formatDuration(Duration d) {
    final m = d.inMinutes.remainder(60).toString().padLeft(2, '0');
    final s = d.inSeconds.remainder(60).toString().padLeft(2, '0');
    return '$m:$s';
  }

  @override
  Widget build(BuildContext context) {
    final url = widget.item.fileUrl;
    final name = widget.item.fileName ?? 'Ses kaydı';
    final isPlaying = _playerState == PlayerState.playing;

    return Container(
      decoration: BoxDecoration(
        color: LoginColors.surface,
        borderRadius: BorderRadius.circular(14),
        border: Border.all(color: LoginColors.border),
      ),
      child: Column(
        children: [
          Padding(
            padding: const EdgeInsets.fromLTRB(6, 10, 14, 4),
            child: Row(
              children: [
                IconButton(
                  onPressed: _togglePlay,
                  icon: Icon(
                    isPlaying
                        ? Icons.pause_circle_filled_rounded
                        : Icons.play_circle_filled_rounded,
                    size: 40,
                    color: const Color(0xFFFF9800),
                  ),
                ),
                const SizedBox(width: 4),
                Expanded(
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      Text(name,
                          style: const TextStyle(
                              fontSize: 14,
                              fontWeight: FontWeight.w600,
                              color: LoginColors.textWhite),
                          maxLines: 1,
                          overflow: TextOverflow.ellipsis),
                      const SizedBox(height: 6),
                      SliderTheme(
                        data: SliderThemeData(
                          thumbShape:
                              const RoundSliderThumbShape(enabledThumbRadius: 6),
                          overlayShape:
                              const RoundSliderOverlayShape(overlayRadius: 12),
                          trackHeight: 3,
                          activeTrackColor: const Color(0xFFFF9800),
                          inactiveTrackColor: LoginColors.border,
                          thumbColor: const Color(0xFFFF9800),
                        ),
                        child: Slider(
                          value: _duration.inMilliseconds > 0
                              ? _position.inMilliseconds
                                  .clamp(0, _duration.inMilliseconds)
                                  .toDouble()
                              : 0,
                          max: _duration.inMilliseconds > 0
                              ? _duration.inMilliseconds.toDouble()
                              : 1,
                          onChanged: (v) {
                            _player
                                .seek(Duration(milliseconds: v.toInt()));
                          },
                        ),
                      ),
                    ],
                  ),
                ),
              ],
            ),
          ),
          Padding(
            padding: const EdgeInsets.fromLTRB(62, 0, 14, 6),
            child: Row(
              children: [
                Text(_formatDuration(_position),
                    style: const TextStyle(
                        fontSize: 11, color: LoginColors.textMuted)),
                const Spacer(),
                Text(_formatDuration(_duration),
                    style: const TextStyle(
                        fontSize: 11, color: LoginColors.textMuted)),
              ],
            ),
          ),
          if (url != null && url.isNotEmpty)
            Container(
              decoration: BoxDecoration(
                border: Border(top: BorderSide(color: LoginColors.border)),
              ),
              child: InkWell(
                onTap: () => _openUrl(url),
                borderRadius:
                    const BorderRadius.vertical(bottom: Radius.circular(14)),
                child: const Padding(
                  padding: EdgeInsets.symmetric(vertical: 10),
                  child: Row(
                    mainAxisAlignment: MainAxisAlignment.center,
                    children: [
                      Icon(Icons.download_rounded,
                          size: 18, color: LoginColors.primaryEnd),
                      SizedBox(width: 6),
                      Text('İndir',
                          style: TextStyle(
                              fontSize: 13,
                              fontWeight: FontWeight.w500,
                              color: LoginColors.primaryEnd)),
                    ],
                  ),
                ),
              ),
            ),
        ],
      ),
    );
  }
}

// ──────────── Fullscreen Image Viewer ────────────

class _FullScreenImageViewer extends StatefulWidget {
  const _FullScreenImageViewer({
    required this.images,
    this.initialIndex = 0,
    this.onDownload,
  });

  final List<String> images;
  final int initialIndex;
  final void Function(String url)? onDownload;

  @override
  State<_FullScreenImageViewer> createState() => _FullScreenImageViewerState();
}

class _FullScreenImageViewerState extends State<_FullScreenImageViewer> {
  late PageController _pageController;
  late int _currentIndex;

  @override
  void initState() {
    super.initState();
    _currentIndex = widget.initialIndex;
    _pageController = PageController(initialPage: _currentIndex);
  }

  @override
  void dispose() {
    _pageController.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: Colors.black,
      appBar: AppBar(
        backgroundColor: Colors.black,
        elevation: 0,
        leading: IconButton(
          icon: const Icon(Icons.close_rounded, color: Colors.white),
          onPressed: () => Navigator.of(context).pop(),
        ),
        title: widget.images.length > 1
            ? Text('${_currentIndex + 1} / ${widget.images.length}',
                style: const TextStyle(color: Colors.white70, fontSize: 15))
            : null,
        centerTitle: true,
        actions: [
          IconButton(
            icon: const Icon(Icons.download_rounded, color: Colors.white70),
            onPressed: () {
              final url = widget.images[_currentIndex];
              if (url.isNotEmpty) widget.onDownload?.call(url);
            },
          ),
        ],
      ),
      body: PageView.builder(
        controller: _pageController,
        itemCount: widget.images.length,
        onPageChanged: (i) => setState(() => _currentIndex = i),
        itemBuilder: (_, index) {
          final url = widget.images[index];
          if (url.isEmpty) {
            return const Center(
                child: Icon(Icons.broken_image_outlined,
                    size: 64, color: Colors.white24));
          }
          return InteractiveViewer(
            minScale: 0.5,
            maxScale: 4.0,
            child: Center(
              child: Image.network(url,
                  fit: BoxFit.contain,
                  loadingBuilder: (_, child, p) => p == null
                      ? child
                      : const Center(
                          child: CircularProgressIndicator(
                              strokeWidth: 2, color: LoginColors.primaryEnd)),
                  errorBuilder: (_, __, ___) => const Center(
                        child: Column(
                          mainAxisSize: MainAxisSize.min,
                          children: [
                            Icon(Icons.broken_image_outlined,
                                size: 48, color: Colors.white24),
                            SizedBox(height: 8),
                            Text('Görsel yüklenemedi',
                                style: TextStyle(color: Colors.white38)),
                          ],
                        ),
                      )),
            ),
          );
        },
      ),
      bottomNavigationBar: widget.images.length > 1
          ? SafeArea(
              child: Container(
                height: 56,
                color: Colors.black,
                child: Row(
                  mainAxisAlignment: MainAxisAlignment.center,
                  children: List.generate(
                    widget.images.length,
                    (i) => AnimatedContainer(
                      duration: const Duration(milliseconds: 200),
                      margin: const EdgeInsets.symmetric(horizontal: 4),
                      width: _currentIndex == i ? 20 : 8,
                      height: 8,
                      decoration: BoxDecoration(
                        color: _currentIndex == i
                            ? LoginColors.primaryEnd
                            : Colors.white24,
                        borderRadius: BorderRadius.circular(4),
                      ),
                    ),
                  ),
                ),
              ),
            )
          : null,
    );
  }
}
