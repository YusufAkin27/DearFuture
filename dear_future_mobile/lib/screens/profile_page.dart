import 'package:flutter/material.dart';
import 'package:image_picker/image_picker.dart';

import '../models/profile_models.dart';
import '../services/api_client.dart';
import '../services/auth_service.dart';
import '../services/profile_service.dart';
import '../theme/login_theme.dart';

class ProfilePage extends StatefulWidget {
  const ProfilePage({super.key, this.onLogout, this.onManageSubscription});

  final VoidCallback? onLogout;
  final VoidCallback? onManageSubscription;

  @override
  State<ProfilePage> createState() => _ProfilePageState();
}

class _ProfilePageState extends State<ProfilePage> {
  final AuthService _auth = AuthService();
  ApiClient? _apiClient;
  ProfileService? _profileService;
  ProfileData? _profile;
  MessageQuotaData? _quota;
  bool _loading = true;
  String? _error;
  bool _photoLoading = false;
  bool _personalInfoEditing = false;
  final TextEditingController _nameController = TextEditingController();
  final TextEditingController _surnameController = TextEditingController();

  @override
  void initState() {
    super.initState();
    _initAndLoad();
  }

  @override
  void dispose() {
    _nameController.dispose();
    _surnameController.dispose();
    super.dispose();
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
    setState(() {
      _apiClient = ApiClient(token: token, onUnauthorized: widget.onLogout);
      _profileService = ProfileService(_apiClient!);
    });
    await _loadData();
  }

  Future<void> _loadData() async {
    if (_profileService == null) return;
    setState(() {
      _loading = true;
      _error = null;
    });
    try {
      final profile = await _profileService!.getProfile();
      final quota = await _profileService!.getMessageQuota();
      if (!mounted) return;
      setState(() {
        _profile = profile;
        _quota = quota;
        _loading = false;
      });
    } catch (e) {
      if (!mounted) return;
      setState(() {
        _loading = false;
        _error = 'Bilgiler yüklenemedi.';
      });
    }
  }

  Future<void> _pickAndUploadPhoto() async {
    final picker = ImagePicker();
    final xFile = await picker.pickImage(source: ImageSource.gallery, maxWidth: 800, imageQuality: 85);
    if (xFile == null || _profileService == null) return;
    setState(() => _photoLoading = true);
    try {
      final bytes = await xFile.readAsBytes();
      final filename = xFile.name;
      final mimeType = xFile.mimeType;
      final error = await _profileService!.uploadPhotoFromBytes(
        bytes,
        filename.isEmpty ? 'photo.jpg' : filename,
        mimeType: mimeType,
      );
      if (!mounted) return;
      if (error == null) {
        await _loadData();
      } else {
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(
            content: Text(error.length > 80 ? '${error.substring(0, 80)}...' : error),
            backgroundColor: Colors.red.shade700,
          ),
        );
      }
    } catch (e) {
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(
            content: Text(e.toString().length > 80 ? 'Fotoğraf yüklenemedi.' : e.toString()),
            backgroundColor: Colors.red.shade700,
          ),
        );
      }
    } finally {
      if (mounted) setState(() => _photoLoading = false);
    }
  }

  Future<void> _removePhoto() async {
    if (_profileService == null) return;
    setState(() => _photoLoading = true);
    try {
      final ok = await _profileService!.deletePhoto();
      if (!mounted) return;
      if (ok) await _loadData();
    } finally {
      if (mounted) setState(() => _photoLoading = false);
    }
  }

  void _startEditPersonalInfo() {
    if (_profile == null) return;
    _nameController.text = _profile!.firstName ?? '';
    _surnameController.text = _profile!.lastName ?? '';
    setState(() => _personalInfoEditing = true);
  }

  void _cancelEditPersonalInfo() {
    setState(() => _personalInfoEditing = false);
  }

  Future<void> _savePersonalInfo() async {
    if (_profileService == null) return;
    final ok = await _profileService!.updateProfile(
      firstName: _nameController.text.trim().isEmpty ? null : _nameController.text.trim(),
      lastName: _surnameController.text.trim().isEmpty ? null : _surnameController.text.trim(),
    );
    if (!mounted) return;
    setState(() => _personalInfoEditing = false);
    if (ok) await _loadData();
  }

  String _formatDate(DateTime? d) {
    if (d == null) return '—';
    return '${d.day}.${d.month}.${d.year}';
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: LoginColors.background,
      body: SafeArea(
        child: _loading && _profile == null
            ? Center(
                child: CircularProgressIndicator(color: LoginColors.primaryEnd),
              )
            : _error != null
                ? Center(
                    child: Padding(
                      padding: const EdgeInsets.all(24),
                      child: Column(
                        mainAxisAlignment: MainAxisAlignment.center,
                        children: [
                          Text(_error!, style: TextStyle(color: LoginColors.textLightGray), textAlign: TextAlign.center),
                          const SizedBox(height: 16),
                          TextButton(
                            onPressed: _loadData,
                            child: Text('Tekrar dene', style: TextStyle(color: LoginColors.primaryEnd)),
                          ),
                        ],
                      ),
                    ),
                  )
                : CustomScrollView(
                    slivers: [
                      SliverToBoxAdapter(
                        child: Padding(
                          padding: const EdgeInsets.fromLTRB(20, 20, 20, 12),
                          child: Column(
                            crossAxisAlignment: CrossAxisAlignment.start,
                            children: [
                              Text(
                                'Profilim',
                                style: TextStyle(
                                  fontSize: 22,
                                  fontWeight: FontWeight.w700,
                                  color: LoginColors.textWhite,
                                ),
                              ),
                              const SizedBox(height: 4),
                              Text(
                                'Hesap bilgilerinizi, aboneliğinizi ve kişisel verilerinizi buradan yönetin.',
                                style: TextStyle(fontSize: 14, color: LoginColors.textLightGray, height: 1.35),
                              ),
                            ],
                          ),
                        ),
                      ),
                      SliverPadding(
                        padding: const EdgeInsets.symmetric(horizontal: 20),
                        sliver: SliverList(
                          delegate: SliverChildListDelegate([
                            _buildProfileCard(),
                            const SizedBox(height: 16),
                            _buildPersonalInfoCard(),
                            const SizedBox(height: 16),
                            _buildSubscriptionCard(),
                            if (widget.onLogout != null) ...[
                              const SizedBox(height: 24),
                              Center(
                                child: OutlinedButton.icon(
                                  onPressed: widget.onLogout,
                                  icon: const Icon(Icons.logout_rounded, size: 20),
                                  label: const Text('Çıkış yap'),
                                  style: OutlinedButton.styleFrom(
                                    foregroundColor: LoginColors.textLightGray,
                                    side: BorderSide(color: LoginColors.border),
                                  ),
                                ),
                              ),
                              const SizedBox(height: 24),
                            ],
                          ]),
                        ),
                      ),
                    ],
                  ),
      ),
    );
  }

  Widget _buildProfileCard() {
    final p = _profile;
    final hasPhoto = (p?.profilePictureUrl ?? '').isNotEmpty;
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
          Text(
            'Profil bilgileri',
            style: TextStyle(
              fontSize: 16,
              fontWeight: FontWeight.w700,
              color: LoginColors.textWhite,
            ),
          ),
          const SizedBox(height: 2),
          Text(
            'Fotoğraf ve hesap özeti',
            style: TextStyle(fontSize: 13, color: LoginColors.textLightGray),
          ),
          const SizedBox(height: 16),
          Center(
            child: Stack(
              clipBehavior: Clip.none,
              children: [
                SizedBox(
                  width: 96,
                  height: 96,
                  child: _photoLoading
                      ? Center(child: SizedBox(
                          width: 32,
                          height: 32,
                          child: CircularProgressIndicator(strokeWidth: 2, color: LoginColors.primaryEnd),
                        ))
                      : CircleAvatar(
                          radius: 48,
                          backgroundColor: LoginColors.primaryStart.withValues(alpha: 0.4),
                          backgroundImage: hasPhoto ? NetworkImage(p!.profilePictureUrl!) : null,
                          child: hasPhoto
                              ? null
                              : Text(
                                  p?.initial ?? '?',
                                  style: TextStyle(fontSize: 36, fontWeight: FontWeight.w600, color: LoginColors.textWhite),
                                ),
                        ),
                ),
                Positioned(
                  right: -4,
                  bottom: -4,
                  child: Material(
                    color: LoginColors.primaryEnd,
                    shape: const CircleBorder(),
                    child: InkWell(
                      onTap: _photoLoading ? null : _pickAndUploadPhoto,
                      customBorder: const CircleBorder(),
                      child: const Padding(
                        padding: EdgeInsets.all(10),
                        child: Icon(Icons.camera_alt_rounded, size: 20, color: Colors.white),
                      ),
                    ),
                  ),
                ),
              ],
            ),
          ),
          if (hasPhoto && !_photoLoading) ...[
            const SizedBox(height: 12),
            Center(
              child: TextButton(
                onPressed: _removePhoto,
                child: Text(
                  'Fotoğrafı kaldır',
                  style: TextStyle(fontSize: 14, color: LoginColors.textLightGray),
                ),
              ),
            ),
          ],
          const SizedBox(height: 12),
          if (p != null) ...[
            Center(
              child: Text(
                p.displayName,
                style: TextStyle(fontSize: 18, fontWeight: FontWeight.w600, color: LoginColors.textWhite),
              ),
            ),
            const SizedBox(height: 8),
            Row(
              mainAxisAlignment: MainAxisAlignment.center,
              children: [
                Icon(Icons.email_outlined, size: 18, color: LoginColors.textMuted),
                const SizedBox(width: 6),
                Text(
                  p.email ?? '',
                  style: TextStyle(fontSize: 14, color: LoginColors.textLightGray),
                ),
              ],
            ),
            if (p.emailVerified) ...[
              const SizedBox(height: 6),
              Row(
                mainAxisAlignment: MainAxisAlignment.center,
                children: [
                  Icon(Icons.check_circle_rounded, size: 18, color: LoginColors.primaryEnd),
                  const SizedBox(width: 6),
                  Text(
                    'Doğrulanmış',
                    style: TextStyle(fontSize: 14, fontWeight: FontWeight.w500, color: LoginColors.primaryEnd),
                  ),
                ],
              ),
            ],
          ],
        ],
      ),
    );
  }

  Widget _buildPersonalInfoCard() {
    final p = _profile;
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
          Row(
            children: [
              Expanded(
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Text(
                      'Kişisel bilgiler',
                      style: TextStyle(
                        fontSize: 16,
                        fontWeight: FontWeight.w700,
                        color: LoginColors.textWhite,
                      ),
                    ),
                    const SizedBox(height: 2),
                    Text(
                      'Ad, soyad ve üyelik bilgileriniz',
                      style: TextStyle(fontSize: 13, color: LoginColors.textLightGray),
                    ),
                  ],
                ),
              ),
              if (!_personalInfoEditing)
                TextButton.icon(
                  onPressed: _startEditPersonalInfo,
                  icon: const Icon(Icons.edit_rounded, size: 18),
                  label: const Text('Düzenle'),
                  style: TextButton.styleFrom(
                    foregroundColor: LoginColors.primaryEnd,
                    padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 8),
                  ),
                ),
            ],
          ),
          const SizedBox(height: 12),
          if (_personalInfoEditing) ...[
            Text('Ad', style: TextStyle(fontSize: 12, color: LoginColors.textLightGray)),
            const SizedBox(height: 4),
            TextField(
              controller: _nameController,
              decoration: InputDecoration(
                filled: true,
                fillColor: LoginColors.background,
                border: OutlineInputBorder(borderRadius: BorderRadius.circular(10), borderSide: BorderSide(color: LoginColors.border)),
                contentPadding: const EdgeInsets.symmetric(horizontal: 12, vertical: 10),
              ),
              style: TextStyle(color: LoginColors.textWhite, fontSize: 14),
            ),
            const SizedBox(height: 12),
            Text('Soyad', style: TextStyle(fontSize: 12, color: LoginColors.textLightGray)),
            const SizedBox(height: 4),
            TextField(
              controller: _surnameController,
              decoration: InputDecoration(
                filled: true,
                fillColor: LoginColors.background,
                border: OutlineInputBorder(borderRadius: BorderRadius.circular(10), borderSide: BorderSide(color: LoginColors.border)),
                contentPadding: const EdgeInsets.symmetric(horizontal: 12, vertical: 10),
              ),
              style: TextStyle(color: LoginColors.textWhite, fontSize: 14),
            ),
            const SizedBox(height: 16),
            Row(
              children: [
                TextButton(
                  onPressed: _cancelEditPersonalInfo,
                  child: Text('İptal', style: TextStyle(color: LoginColors.textLightGray)),
                ),
                const SizedBox(width: 12),
                FilledButton(
                  onPressed: _savePersonalInfo,
                  style: FilledButton.styleFrom(backgroundColor: LoginColors.primaryEnd, foregroundColor: Colors.black87),
                  child: const Text('Kaydet'),
                ),
              ],
            ),
          ] else ...[
            _row('Ad', p?.firstName ?? '—'),
            _row('Soyad', p?.lastName ?? '—'),
            _row('E-posta', p?.email ?? '—'),
            _row('Üyelik tarihi', _formatDate(p?.createdAt)),
          ],
        ],
      ),
    );
  }

  Widget _row(String label, String value) {
    return Padding(
      padding: const EdgeInsets.symmetric(vertical: 10),
      child: Row(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          SizedBox(
            width: 110,
            child: Text(
              label,
              style: TextStyle(fontSize: 14, color: LoginColors.textLightGray),
            ),
          ),
          Expanded(
            child: Text(
              value,
              style: TextStyle(fontSize: 14, color: LoginColors.textWhite),
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildSubscriptionCard() {
    final q = _quota ?? MessageQuotaData(planCode: 'FREE', planName: 'Ücretsiz', limit: 3, remaining: 3);
    final planName = _profile?.subscriptionPlanName ?? q.planName;
    final limit = _profile?.maxMessagesPerPlan ?? q.limit;
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
          Row(
            children: [
              Icon(Icons.card_membership_rounded, size: 20, color: LoginColors.primaryEnd),
              const SizedBox(width: 8),
              Text(
                'Abonelik',
                style: TextStyle(
                  fontSize: 16,
                  fontWeight: FontWeight.w700,
                  color: LoginColors.textWhite,
                ),
              ),
            ],
          ),
          const SizedBox(height: 2),
          Text(
            'Planınız ve mesaj limitleri',
            style: TextStyle(fontSize: 13, color: LoginColors.textLightGray),
          ),
          const SizedBox(height: 16),
          Text(
            planName.toUpperCase(),
            style: TextStyle(
              fontSize: 18,
              fontWeight: FontWeight.w700,
              color: LoginColors.textWhite,
            ),
          ),
          const SizedBox(height: 4),
          Text(
            'Limit: $limit mesaj',
            style: TextStyle(fontSize: 14, color: LoginColors.textLightGray),
          ),
          const SizedBox(height: 16),
          OutlinedButton(
            onPressed: widget.onManageSubscription,
            style: OutlinedButton.styleFrom(
              foregroundColor: LoginColors.primaryEnd,
              side: BorderSide(color: LoginColors.primaryEnd),
            ),
            child: const Text('Aboneliği yönet'),
          ),
        ],
      ),
    );
  }
}
