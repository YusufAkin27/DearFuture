import 'package:flutter/material.dart';

import '../models/public_message_models.dart';
import '../services/api_client.dart';
import '../services/auth_service.dart';
import '../services/public_message_service.dart';
import '../theme/login_theme.dart';
import 'public_message_detail_page.dart';

class PublicMessagesPage extends StatefulWidget {
  const PublicMessagesPage({super.key, this.onLogout});

  final VoidCallback? onLogout;

  @override
  State<PublicMessagesPage> createState() => _PublicMessagesPageState();
}

class _PublicMessagesPageState extends State<PublicMessagesPage> {
  static const int _pageSize = 12;
  final AuthService _auth = AuthService();
  PublicMessageService? _service;
  final List<PublicMessageItem> _items = [];
  int _currentPage = 0;
  int _totalPages = 0;
  bool _loading = true;
  bool _loadingMore = false;
  String? _error;

  @override
  void initState() {
    super.initState();
    _initAndLoad();
  }

  Future<void> _initAndLoad() async {
    await _auth.loadStoredToken();
    if (!mounted) return;
    _service = PublicMessageService(ApiClient(token: _auth.token, onUnauthorized: widget.onLogout));
    await _loadPage(0);
  }

  Future<void> _loadPage(int page) async {
    if (_service == null) return;
    if (page > 0 && _loadingMore) return;
    if (page == 0) {
      setState(() {
        _loading = true;
        _error = null;
        _items.clear();
        _currentPage = 0;
      });
    } else {
      setState(() => _loadingMore = true);
    }
    try {
      final result = await _service!.getPublicMessages(page: page, size: _pageSize);
      if (!mounted) return;
      if (result == null) {
        setState(() {
          _loading = false;
          _loadingMore = false;
          if (page == 0) _error = 'Liste yüklenemedi.';
        });
        return;
      }
      setState(() {
        if (page == 0) _items.clear();
        _items.addAll(result.content);
        _currentPage = result.number;
        _totalPages = result.totalPages;
        _loading = false;
        _loadingMore = false;
      });
    } catch (e) {
      if (!mounted) return;
      setState(() {
        _loading = false;
        _loadingMore = false;
        if (page == 0) _error = 'Liste yüklenemedi.';
      });
    }
  }

  void _openDetail(PublicMessageItem item) {
    final token = item.viewToken;
    if (token == null || token.isEmpty) return;
    Navigator.of(context).push(
      MaterialPageRoute<void>(
        builder: (context) => PublicMessageDetailPage(
          viewToken: token,
          messageId: item.id,
          initialStarred: item.starredByMe,
        ),
      ),
    );
  }

  bool _showStarred = false;
  final List<PublicMessageItem> _starredItems = [];
  bool _loadingStarred = false;

  Future<void> _loadStarred() async {
    if (_service == null) return;
    setState(() => _loadingStarred = true);
    try {
      final list = await _service!.getStarredMessages();
      if (!mounted) return;
      setState(() {
        _starredItems.clear();
        _starredItems.addAll(list);
        _loadingStarred = false;
      });
    } catch (_) {
      if (!mounted) return;
      setState(() => _loadingStarred = false);
    }
  }

  Widget _segmentChip(String label, bool selected, VoidCallback onTap) {
    return Material(
      color: selected ? LoginColors.primaryEnd.withValues(alpha: 0.25) : LoginColors.surface,
      borderRadius: BorderRadius.circular(20),
      child: InkWell(
        onTap: onTap,
        borderRadius: BorderRadius.circular(20),
        child: Padding(
          padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 8),
          child: Text(
            label,
            style: TextStyle(
              fontSize: 14,
              fontWeight: selected ? FontWeight.w600 : FontWeight.w500,
              color: selected ? LoginColors.primaryEnd : LoginColors.textLightGray,
            ),
          ),
        ),
      ),
    );
  }

  Widget _buildStarredList() {
    if (_loadingStarred) {
      return SliverFillRemaining(
        hasScrollBody: false,
        child: Center(child: CircularProgressIndicator(color: LoginColors.primaryEnd)),
      );
    }
    if (_starredItems.isEmpty) {
      return SliverFillRemaining(
        hasScrollBody: false,
        child: Center(
          child: Padding(
            padding: const EdgeInsets.all(32),
            child: Column(
              mainAxisSize: MainAxisSize.min,
              children: [
                Icon(Icons.star_border_rounded, size: 64, color: LoginColors.textMuted),
                const SizedBox(height: 20),
                Text(
                  'Yıldızlı mesajınız yok',
                  style: TextStyle(fontSize: 18, fontWeight: FontWeight.w700, color: LoginColors.textWhite),
                  textAlign: TextAlign.center,
                ),
                const SizedBox(height: 8),
                Text(
                  'Mesaj kartındaki yıldız ikonuna basarak yıldızlayabilirsiniz.',
                  style: TextStyle(fontSize: 14, color: LoginColors.textLightGray),
                  textAlign: TextAlign.center,
                ),
              ],
            ),
          ),
        ),
      );
    }
    return SliverPadding(
      padding: const EdgeInsets.symmetric(horizontal: 20),
      sliver: SliverList(
        delegate: SliverChildBuilderDelegate(
          (context, index) {
            final item = _starredItems[index];
            return Padding(
              padding: const EdgeInsets.only(bottom: 12),
              child: Material(
                color: LoginColors.surface,
                borderRadius: BorderRadius.circular(14),
                child: Padding(
                  padding: const EdgeInsets.all(16),
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      Row(
                        crossAxisAlignment: CrossAxisAlignment.start,
                        children: [
                          Expanded(
                            child: InkWell(
                              onTap: () => _openDetail(item),
                              borderRadius: BorderRadius.circular(8),
                              child: Column(
                                crossAxisAlignment: CrossAxisAlignment.start,
                                children: [
                                  if (item.senderName != null && item.senderName!.isNotEmpty)
                                    Text(
                                      item.senderName!,
                                      style: TextStyle(fontSize: 14, fontWeight: FontWeight.w600, color: LoginColors.primaryEnd),
                                    ),
                                  if (item.senderName != null && item.senderName!.isNotEmpty) const SizedBox(height: 6),
                                  if (item.textPreview != null && item.textPreview!.isNotEmpty)
                                    Text(
                                      item.textPreview!,
                                      style: TextStyle(fontSize: 14, color: LoginColors.textLightGray),
                                      maxLines: 3,
                                      overflow: TextOverflow.ellipsis,
                                    ),
                                  const SizedBox(height: 8),
                                  Text(
                                    PublicMessageItem.formatDateTime(item.sentAt ?? item.scheduledAt),
                                    style: TextStyle(fontSize: 12, color: LoginColors.textMuted),
                                  ),
                                ],
                              ),
                            ),
                          ),
                          IconButton(
                            icon: Icon(Icons.star_rounded, color: LoginColors.primaryEnd, size: 26),
                            onPressed: () => _toggleStar(item),
                            tooltip: 'Yıldızı kaldır',
                          ),
                        ],
                      ),
                    ],
                  ),
                ),
              ),
            );
          },
          childCount: _starredItems.length,
        ),
      ),
    );
  }

  Future<void> _toggleStar(PublicMessageItem item) async {
    final id = item.id;
    if (id == null || _service == null) return;
    final isStarred = item.starredByMe;
    try {
      final ok = isStarred
          ? await _service!.unstarMessage(id)
          : await _service!.starMessage(id);
      if (!mounted) return;
      if (ok) {
        if (_showStarred) {
          if (isStarred) {
            setState(() => _starredItems.removeWhere((e) => e.id == id));
          } else {
            await _loadStarred();
          }
        } else {
          final idx = _items.indexWhere((e) => e.id == id);
          if (idx >= 0) {
            setState(() {
              _items[idx] = PublicMessageItem(
                id: _items[idx].id,
                viewToken: _items[idx].viewToken,
                scheduledAt: _items[idx].scheduledAt,
                sentAt: _items[idx].sentAt,
                senderName: _items[idx].senderName,
                textPreview: _items[idx].textPreview,
                starredByMe: !isStarred,
              );
            });
          }
        }
      }
    } catch (_) {}
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: LoginColors.background,
      body: SafeArea(
        child: _loading && _items.isEmpty
            ? Center(child: CircularProgressIndicator(color: LoginColors.primaryEnd))
            : _error != null && _items.isEmpty
                ? Center(
                    child: Padding(
                      padding: const EdgeInsets.all(24),
                      child: Column(
                        mainAxisAlignment: MainAxisAlignment.center,
                        children: [
                          Text(_error!, style: TextStyle(color: LoginColors.textLightGray), textAlign: TextAlign.center),
                          const SizedBox(height: 16),
                          TextButton(
                            onPressed: () => _loadPage(0),
                            child: Text('Tekrar dene', style: TextStyle(color: LoginColors.primaryEnd)),
                          ),
                        ],
                      ),
                    ),
                  )
                : RefreshIndicator(
                    onRefresh: () => _loadPage(0),
                    color: LoginColors.primaryEnd,
                    child: CustomScrollView(
                      slivers: [
                        SliverToBoxAdapter(
                          child: Padding(
                            padding: const EdgeInsets.fromLTRB(20, 20, 20, 12),
                            child: Column(
                              crossAxisAlignment: CrossAxisAlignment.start,
                              children: [
                                Text(
                                  'Herkese açık mesajlar',
                                  style: TextStyle(
                                    fontSize: 22,
                                    fontWeight: FontWeight.w700,
                                    color: LoginColors.textWhite,
                                  ),
                                ),
                                const SizedBox(height: 4),
                                Text(
                                  'Diğer kullanıcıların paylaştığı mesajları okuyabilirsiniz.',
                                  style: TextStyle(fontSize: 14, color: LoginColors.textLightGray),
                                ),
                                if (_auth.token != null && _auth.token!.isNotEmpty) ...[
                                  const SizedBox(height: 16),
                                  Row(
                                    children: [
                                      _segmentChip('Tümü', !_showStarred, () => setState(() => _showStarred = false)),
                                      const SizedBox(width: 10),
                                      _segmentChip('Yıldızlılarım', _showStarred, () {
                                        setState(() => _showStarred = true);
                                        _loadStarred();
                                      }),
                                    ],
                                  ),
                                ],
                              ],
                            ),
                          ),
                        ),
                        if (_showStarred && _auth.token != null && _auth.token!.isNotEmpty)
                          _buildStarredList()
                        else if (_items.isEmpty)
                          SliverFillRemaining(
                            hasScrollBody: false,
                            child: Center(
                              child: Padding(
                                padding: const EdgeInsets.all(32),
                                child: Column(
                                  mainAxisSize: MainAxisSize.min,
                                  children: [
                                    Icon(Icons.send_rounded, size: 64, color: LoginColors.textMuted),
                                    const SizedBox(height: 20),
                                    Text(
                                      'Henüz public mesaj yok',
                                      style: TextStyle(fontSize: 18, fontWeight: FontWeight.w700, color: LoginColors.textWhite),
                                      textAlign: TextAlign.center,
                                    ),
                                    const SizedBox(height: 8),
                                    Text(
                                      'Herkese açık paylaşılan mesajlar burada listelenecek.',
                                      style: TextStyle(fontSize: 14, color: LoginColors.textLightGray),
                                      textAlign: TextAlign.center,
                                    ),
                                  ],
                                ),
                              ),
                            ),
                          )
                        else
                          SliverPadding(
                            padding: const EdgeInsets.symmetric(horizontal: 20),
                            sliver: SliverList(
                              delegate: SliverChildBuilderDelegate(
                                (context, index) {
                                  if (index == _items.length) {
                                    if (_loadingMore) {
                                      return Padding(
                                        padding: const EdgeInsets.symmetric(vertical: 16),
                                        child: Center(child: SizedBox(height: 24, width: 24, child: CircularProgressIndicator(strokeWidth: 2, color: LoginColors.primaryEnd))),
                                      );
                                    }
                                    if (_currentPage + 1 < _totalPages) {
                                      WidgetsBinding.instance.addPostFrameCallback((_) => _loadPage(_currentPage + 1));
                                      return Padding(
                                        padding: const EdgeInsets.symmetric(vertical: 16),
                                        child: Center(child: SizedBox(height: 24, width: 24, child: CircularProgressIndicator(strokeWidth: 2, color: LoginColors.primaryEnd))),
                                      );
                                    }
                                    return const SizedBox(height: 24);
                                  }
                                  final item = _items[index];
                                  return Padding(
                                    padding: const EdgeInsets.only(bottom: 12),
                                    child: Material(
                                      color: LoginColors.surface,
                                      borderRadius: BorderRadius.circular(14),
                                      child: InkWell(
                                        onTap: () => _openDetail(item),
                                        borderRadius: BorderRadius.circular(14),
                                        child: Padding(
                                          padding: const EdgeInsets.all(16),
                                          child: Column(
                                            crossAxisAlignment: CrossAxisAlignment.start,
                                            children: [
                                              Row(
                                                crossAxisAlignment: CrossAxisAlignment.start,
                                                children: [
                                                  Expanded(
                                                    child: Column(
                                                      crossAxisAlignment: CrossAxisAlignment.start,
                                                      children: [
                                                        if (item.senderName != null && item.senderName!.isNotEmpty)
                                                          Text(
                                                            item.senderName!,
                                                            style: TextStyle(fontSize: 14, fontWeight: FontWeight.w600, color: LoginColors.primaryEnd),
                                                          ),
                                                        if (item.senderName != null && item.senderName!.isNotEmpty) const SizedBox(height: 6),
                                                        if (item.textPreview != null && item.textPreview!.isNotEmpty)
                                                          Text(
                                                            item.textPreview!,
                                                            style: TextStyle(fontSize: 14, color: LoginColors.textLightGray),
                                                            maxLines: 3,
                                                            overflow: TextOverflow.ellipsis,
                                                          ),
                                                        const SizedBox(height: 8),
                                                        Text(
                                                          PublicMessageItem.formatDateTime(item.sentAt ?? item.scheduledAt),
                                                          style: TextStyle(fontSize: 12, color: LoginColors.textMuted),
                                                        ),
                                                      ],
                                                    ),
                                                  ),
                                                  if (_auth.token != null && _auth.token!.isNotEmpty)
                                                    IconButton(
                                                      icon: Icon(
                                                        item.starredByMe ? Icons.star_rounded : Icons.star_border_rounded,
                                                        color: item.starredByMe ? LoginColors.primaryEnd : LoginColors.textMuted,
                                                        size: 26,
                                                      ),
                                                      onPressed: () => _toggleStar(item),
                                                      tooltip: item.starredByMe ? 'Yıldızı kaldır' : 'Yıldızla',
                                                    ),
                                                ],
                                              ),
                                            ],
                                          ),
                                        ),
                                      ),
                                    ),
                                  );
                                },
                                childCount: _items.length + 1,
                              ),
                            ),
                          ),
                      ],
                    ),
                  ),
      ),
    );
  }
}
