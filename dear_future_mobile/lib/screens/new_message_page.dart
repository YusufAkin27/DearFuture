import 'dart:async';
import 'dart:io';

import 'package:file_picker/file_picker.dart';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:image_picker/image_picker.dart';
import 'package:path_provider/path_provider.dart';
import 'package:permission_handler/permission_handler.dart';
import 'package:record/record.dart';

import '../models/profile_models.dart';
import '../services/api_client.dart';
import '../services/auth_service.dart';
import '../services/message_service.dart';
import '../services/profile_service.dart';
import '../theme/login_theme.dart';

class NewMessagePage extends StatefulWidget {
  const NewMessagePage({super.key, this.onLogout});

  final VoidCallback? onLogout;

  @override
  State<NewMessagePage> createState() => _NewMessagePageState();
}

class _NewMessagePageState extends State<NewMessagePage> {
  final AuthService _auth = AuthService();
  ApiClient? _apiClient;
  ProfileService? _profileService;
  MessageService? _messageService;

  ProfileData? _profile;
  MessageQuotaData? _quota;
  bool _loading = true;
  String? _error;

  final _contentController = TextEditingController();
  final List<TextEditingController> _recipientControllers = [TextEditingController()];
  DateTime? _scheduledAt;
  bool _isPublic = false;
  bool _submitting = false;

  // Attachments
  final List<_UploadedFile> _photos = [];
  final List<_UploadedFile> _files = [];
  _UploadedFile? _voiceRecording;
  bool _uploadingPhoto = false;
  bool _uploadingFile = false;
  bool _uploadingVoice = false;
  bool _isRecording = false;
  final AudioRecorder _recorder = AudioRecorder();
  Timer? _recordingTimer;
  int _recordingSeconds = 0;

  @override
  void initState() {
    super.initState();
    _initAndLoad();
  }

  @override
  void dispose() {
    _contentController.dispose();
    for (final c in _recipientControllers) {
      c.dispose();
    }
    _recordingTimer?.cancel();
    _recorder.dispose();
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
    _apiClient = ApiClient(token: token, onUnauthorized: widget.onLogout);
    _profileService = ProfileService(_apiClient!);
    _messageService = MessageService(_apiClient!);
    await _loadData();
  }

  Future<void> _loadData() async {
    setState(() {
      _loading = true;
      _error = null;
    });
    try {
      final profile = await _profileService!.getProfile();
      final quota = await _profileService!.getMessageQuota();
      if (!mounted) return;
      if (profile == null) {
        setState(() {
          _loading = false;
          _error = 'Profil yüklenemedi.';
        });
        return;
      }
      final isExpired = profile.subscriptionEndsAt != null &&
          profile.subscriptionEndsAt!.isBefore(DateTime.now());
      setState(() {
        _profile = profile;
        _quota = quota;
        _loading = false;
      });
      if (_effectivePlan != 'FREE' && _recipientControllers.length == 1) {
        _recipientControllers[0].text = profile.email ?? '';
      }
    } catch (e) {
      if (!mounted) return;
      setState(() {
        _loading = false;
        _error = 'Bilgiler yüklenemedi.';
      });
    }
  }

  String get _effectivePlan {
    if (_profile == null) return 'FREE';
    final isExpired = _profile!.subscriptionEndsAt != null &&
        _profile!.subscriptionEndsAt!.isBefore(DateTime.now());
    if (isExpired) return 'FREE';
    return (_profile!.subscriptionPlanCode ?? 'FREE').toUpperCase();
  }

  int get _maxRecipients => _profile?.maxRecipientsPerMessage ?? 1;
  int get _maxPhotos => _profile?.maxPhotosPerMessage ?? 0;
  int get _maxPhotoSizeBytes => _profile?.maxPhotoSizeBytes ?? 0;
  int get _maxFiles => _profile?.maxFilesPerMessage ?? 0;
  int get _maxFileSizeBytes => _profile?.maxFileSizeBytes ?? 0;
  bool get _canPhoto => _maxPhotos > 0;
  bool get _canFile => _maxFiles > 0;
  bool get _canVoice => _profile?.allowVoice == true && (_profile?.maxAudioPerMessage ?? 0) > 0;
  int get _maxAudioSizeBytes => _profile?.maxAudioSizeBytes ?? 0;
  int get _remaining => _quota?.remaining ?? 0;
  bool get _canAddMore => _remaining > 0;

  // ─── Recipients ───

  void _addRecipient() {
    if (_recipientControllers.length >= _maxRecipients) return;
    setState(() => _recipientControllers.add(TextEditingController()));
  }

  void _removeRecipient(int index) {
    if (_recipientControllers.length <= 1) return;
    setState(() {
      _recipientControllers[index].dispose();
      _recipientControllers.removeAt(index);
    });
  }

  // ─── Date Picker ───

  Future<void> _pickDateTime() async {
    final now = DateTime.now();
    final date = await showDatePicker(
      context: context,
      initialDate: _scheduledAt ?? now.add(const Duration(days: 1)),
      firstDate: now,
      lastDate: DateTime(2100),
      builder: (ctx, child) => Theme(
        data: ThemeData.dark().copyWith(
          colorScheme: const ColorScheme.dark(
            primary: LoginColors.primaryEnd,
            surface: LoginColors.surface,
          ),
        ),
        child: child!,
      ),
    );
    if (date == null || !mounted) return;
    final time = await showTimePicker(
      context: context,
      initialTime: TimeOfDay.fromDateTime(_scheduledAt ?? now),
      builder: (ctx, child) => Theme(
        data: ThemeData.dark().copyWith(
          colorScheme: const ColorScheme.dark(
            primary: LoginColors.primaryEnd,
            surface: LoginColors.surface,
          ),
        ),
        child: child!,
      ),
    );
    if (time == null || !mounted) return;
    setState(() {
      _scheduledAt = DateTime(date.year, date.month, date.day, time.hour, time.minute);
    });
  }

  // ─── İzin iste (reddedilirse false) ───

  Future<bool> _requestPhotoPermission() async {
    if (await Permission.photos.isGranted) return true;
    if (await Permission.storage.isGranted) return true;
    if (await Permission.photos.isPermanentlyDenied && await Permission.storage.isPermanentlyDenied) {
      if (mounted) _showSnack('Fotoğraf erişimi reddedildi. Ayarlardan izin verin.');
      return false;
    }
    PermissionStatus status = await Permission.photos.request();
    if (!status.isGranted && !status.isPermanentlyDenied) {
      status = await Permission.storage.request();
    }
    if (status.isGranted) return true;
    if (status.isPermanentlyDenied && mounted) {
      _showSnack('Fotoğraf izni için ayarlara gidin.');
      await openAppSettings();
    } else if (mounted) {
      _showSnack('Fotoğraf erişimi için izin gerekli.');
    }
    return false;
  }

  Future<bool> _requestStoragePermission() async {
    final perm = await Permission.storage.status;
    if (perm.isGranted) return true;
    if (perm.isPermanentlyDenied) {
      if (mounted) _showSnack('Dosya erişimi reddedildi. Ayarlardan izin verin.');
      return false;
    }
    final status = await Permission.storage.request();
    if (status.isGranted) return true;
    if (status.isPermanentlyDenied && mounted) {
      _showSnack('Dosya izni için ayarlara gidin.');
      await openAppSettings();
    } else if (mounted) {
      _showSnack('Dosya erişimi için izin gerekli.');
    }
    return false;
  }

  Future<bool> _requestMicrophonePermission() async {
    if (await Permission.microphone.isGranted) return true;
    if (await Permission.microphone.isPermanentlyDenied) {
      if (mounted) _showSnack('Mikrofon erişimi reddedildi. Ayarlardan izin verin.');
      return false;
    }
    final status = await Permission.microphone.request();
    if (status.isGranted) return true;
    if (status.isPermanentlyDenied && mounted) {
      _showSnack('Mikrofon izni için ayarlara gidin.');
      await openAppSettings();
    } else if (mounted) {
      _showSnack('Ses kaydı için mikrofon izni gerekli.');
    }
    return false;
  }

  // ─── Photo Upload ───

  Future<void> _pickAndUploadPhoto() async {
    if (_photos.length >= _maxPhotos || _messageService == null) return;
    if (!await _requestPhotoPermission()) return;
    final picker = ImagePicker();
    final xFile = await picker.pickImage(source: ImageSource.gallery, maxWidth: 1920, imageQuality: 85);
    if (xFile == null) return;
    final bytes = await xFile.readAsBytes();
    if (_maxPhotoSizeBytes > 0 && bytes.length > _maxPhotoSizeBytes) {
      if (mounted) _showSnack('Fotoğraf en fazla ${_formatSize(_maxPhotoSizeBytes)} olabilir.');
      return;
    }
    setState(() => _uploadingPhoto = true);
    try {
      final result = await _messageService!.uploadAttachment(
        bytes: bytes,
        fileName: xFile.name.isEmpty ? 'photo.jpg' : xFile.name,
        type: 'IMAGE',
        mimeType: xFile.mimeType ?? 'image/jpeg',
      );
      if (!mounted) return;
      if (result == null) {
        _showSnack('Fotoğraf yüklenemedi.');
      } else {
        setState(() => _photos.add(_UploadedFile(url: result.url, fileName: result.fileName, fileSize: result.fileSize)));
      }
    } catch (e) {
      if (mounted) _showSnack('Fotoğraf yüklenemedi.');
    }
    if (mounted) setState(() => _uploadingPhoto = false);
  }

  // ─── File Upload ───

  Future<void> _pickAndUploadFile() async {
    if (_files.length >= _maxFiles || _messageService == null) return;
    if (!await _requestStoragePermission()) return;
    final result = await FilePicker.platform.pickFiles(
      type: FileType.any,
      allowMultiple: false,
    );
    if (result == null || result.files.isEmpty) return;
    final picked = result.files.first;
    if (picked.path == null) return;
    final file = File(picked.path!);
    final bytes = await file.readAsBytes();
    if (_maxFileSizeBytes > 0 && bytes.length > _maxFileSizeBytes) {
      if (mounted) _showSnack('Dosya en fazla ${_formatSize(_maxFileSizeBytes)} olabilir.');
      return;
    }
    setState(() => _uploadingFile = true);
    try {
      final uploadResult = await _messageService!.uploadAttachment(
        bytes: bytes,
        fileName: picked.name,
        type: 'FILE',
        mimeType: _guessMimeType(picked.name),
      );
      if (!mounted) return;
      if (uploadResult == null) {
        _showSnack('Dosya yüklenemedi.');
      } else {
        setState(() => _files.add(_UploadedFile(url: uploadResult.url, fileName: uploadResult.fileName, fileSize: uploadResult.fileSize)));
      }
    } catch (e) {
      if (mounted) _showSnack('Dosya yüklenemedi.');
    }
    if (mounted) setState(() => _uploadingFile = false);
  }

  String _guessMimeType(String name) {
    final ext = name.contains('.') ? name.split('.').last.toLowerCase() : '';
    switch (ext) {
      case 'pdf':
        return 'application/pdf';
      case 'zip':
        return 'application/zip';
      case 'rar':
        return 'application/x-rar-compressed';
      case 'doc':
        return 'application/msword';
      case 'docx':
        return 'application/vnd.openxmlformats-officedocument.wordprocessingml.document';
      case 'xls':
        return 'application/vnd.ms-excel';
      case 'xlsx':
        return 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet';
      case 'png':
        return 'image/png';
      case 'jpg':
      case 'jpeg':
        return 'image/jpeg';
      case 'mp3':
        return 'audio/mpeg';
      case 'mp4':
        return 'video/mp4';
      default:
        return 'application/octet-stream';
    }
  }

  // ─── Voice Recording ───

  Future<void> _startRecording() async {
    if (_voiceRecording != null || _messageService == null || _isRecording) return;
    if (!await _requestMicrophonePermission()) return;
    try {
      final hasPermission = await _recorder.hasPermission();
      if (!hasPermission) {
        if (mounted) _showSnack('Mikrofon izni gerekli. Ayarlardan izin verin.');
        return;
      }
      final dir = await getTemporaryDirectory();
      final path = '${dir.path}/voice_${DateTime.now().millisecondsSinceEpoch}.m4a';
      await _recorder.start(
        const RecordConfig(encoder: AudioEncoder.aacLc),
        path: path,
      );
      if (!mounted) return;
      setState(() {
        _isRecording = true;
        _recordingSeconds = 0;
      });
      _recordingTimer = Timer.periodic(const Duration(seconds: 1), (_) {
        if (mounted) setState(() => _recordingSeconds++);
      });
    } on MissingPluginException catch (_) {
      if (mounted) _showSnack('Ses kaydı eklentisi yüklenmedi. Uygulamayı kapatıp yeniden başlatın.');
    } catch (e) {
      if (mounted) _showSnack('Ses kaydı başlatılamadı: ${e.toString().split('\n').first}');
    }
  }

  Future<void> _stopRecordingAndUpload() async {
    if (!_isRecording) return;
    _recordingTimer?.cancel();
    try {
      final path = await _recorder.stop();
      if (!mounted) return;
      setState(() => _isRecording = false);
      if (path == null || _messageService == null) return;
      final file = File(path);
      if (!await file.exists()) return;
      final bytes = await file.readAsBytes();
      if (_maxAudioSizeBytes > 0 && bytes.length > _maxAudioSizeBytes) {
        if (mounted) _showSnack('Ses kaydı en fazla ${_formatSize(_maxAudioSizeBytes)} olabilir.');
        return;
      }
      setState(() => _uploadingVoice = true);
      try {
        final result = await _messageService!.uploadAttachment(
          bytes: bytes,
          fileName: 'recording.m4a',
          type: 'AUDIO',
          mimeType: 'audio/mp4',
        );
        if (!mounted) return;
        if (result == null) {
          _showSnack('Ses yüklenemedi.');
        } else {
          setState(() => _voiceRecording = _UploadedFile(url: result.url, fileName: result.fileName, fileSize: result.fileSize));
        }
      } catch (e) {
        if (mounted) _showSnack('Ses yüklenemedi.');
      }
    } on MissingPluginException catch (_) {
      if (mounted) {
        setState(() => _isRecording = false);
        _showSnack('Ses kaydı eklentisi yüklenmedi. Uygulamayı kapatıp yeniden başlatın.');
      }
    } catch (e) {
      if (mounted) {
        setState(() => _isRecording = false);
        _showSnack('Kayıt durdurulamadı.');
      }
    }
    if (mounted) setState(() => _uploadingVoice = false);
  }

  Future<void> _cancelRecording() async {
    _recordingTimer?.cancel();
    try {
      await _recorder.stop();
    } catch (_) {}
    setState(() {
      _isRecording = false;
      _recordingSeconds = 0;
    });
  }

  String _formatRecordingTime(int seconds) {
    final m = (seconds ~/ 60).toString().padLeft(2, '0');
    final s = (seconds % 60).toString().padLeft(2, '0');
    return '$m:$s';
  }

  // ─── Submit ───

  Future<void> _submit() async {
    if (_messageService == null) return;
    final content = _contentController.text.trim();
    if (content.isEmpty) {
      _showSnack('Mesaj metni boş olamaz.');
      return;
    }
    if (_scheduledAt == null) {
      _showSnack('Lütfen bir iletim tarihi seçin.');
      return;
    }
    if (_scheduledAt!.isBefore(DateTime.now())) {
      _showSnack('İletim tarihi gelecekte olmalıdır.');
      return;
    }
    if (!_canAddMore) {
      _showSnack('Mesaj hakkınız doldu. Planınızı yükseltin.');
      return;
    }

    setState(() => _submitting = true);
    try {
      final scheduledAtISO = _scheduledAt!.toUtc().toIso8601String();
      bool ok;

      if (_effectivePlan == 'FREE') {
        ok = await _messageService!.createSimpleMessage(
          content: content,
          scheduledAt: scheduledAtISO,
          isPublic: _isPublic,
        );
      } else {
        final emails = _recipientControllers
            .map((c) => c.text.trim())
            .where((e) => e.isNotEmpty)
            .toList();
        if (emails.isEmpty) {
          _showSnack('En az bir alıcı e-posta adresi girin.');
          setState(() => _submitting = false);
          return;
        }
        final contents = <Map<String, dynamic>>[
          {'type': 'TEXT', 'text': content},
          ..._photos.map((p) => {
                'type': 'IMAGE',
                'fileUrl': p.url,
                'fileName': p.fileName,
                if (p.fileSize != null) 'fileSize': p.fileSize,
              }),
          ..._files.map((f) => {
                'type': 'FILE',
                'fileUrl': f.url,
                'fileName': f.fileName,
                if (f.fileSize != null) 'fileSize': f.fileSize,
              }),
          if (_voiceRecording != null)
            {
              'type': 'AUDIO',
              'fileUrl': _voiceRecording!.url,
              'fileName': _voiceRecording!.fileName,
              if (_voiceRecording!.fileSize != null) 'fileSize': _voiceRecording!.fileSize,
            },
        ];
        ok = await _messageService!.scheduleMessage(
          recipientEmails: emails,
          scheduledAt: scheduledAtISO,
          contents: contents,
          isPublic: _isPublic,
        );
      }
      if (!mounted) return;
      if (ok) {
        _showSnack('Mesajınız zamanlandı!');
        _resetForm();
        await _loadData();
      } else {
        _showSnack('Mesaj kaydedilemedi.');
      }
    } catch (e) {
      if (mounted) _showSnack('Mesaj kaydedilemedi.');
    }
    if (mounted) setState(() => _submitting = false);
  }

  void _resetForm() {
    _contentController.clear();
    for (final c in _recipientControllers) {
      c.dispose();
    }
    _recipientControllers.clear();
    _recipientControllers.add(TextEditingController(text: _effectivePlan != 'FREE' ? _profile?.email ?? '' : ''));
    _photos.clear();
    _files.clear();
    _voiceRecording = null;
    _scheduledAt = null;
    _isPublic = false;
  }

  void _showSnack(String msg) {
    ScaffoldMessenger.of(context).showSnackBar(
      SnackBar(
        content: Text(msg),
        backgroundColor: LoginColors.surface,
        behavior: SnackBarBehavior.floating,
      ),
    );
  }

  String _formatSize(int bytes) {
    if (bytes < 1024) return '$bytes B';
    if (bytes < 1024 * 1024) return '${(bytes / 1024).toStringAsFixed(0)} KB';
    return '${(bytes / (1024 * 1024)).toStringAsFixed(0)} MB';
  }

  String _formatDateTime(DateTime d) {
    return '${d.day.toString().padLeft(2, '0')}.${d.month.toString().padLeft(2, '0')}.${d.year}  ${d.hour.toString().padLeft(2, '0')}:${d.minute.toString().padLeft(2, '0')}';
  }

  // ─── BUILD ───

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: LoginColors.background,
      body: _loading
          ? const Center(child: CircularProgressIndicator(color: LoginColors.primaryEnd))
          : _error != null
              ? _buildError()
              : _buildForm(),
    );
  }

  Widget _buildError() {
    return Center(
      child: Padding(
        padding: const EdgeInsets.all(24),
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            const Icon(Icons.error_outline_rounded, size: 56, color: LoginColors.textMuted),
            const SizedBox(height: 16),
            Text(_error!, style: const TextStyle(color: LoginColors.textLightGray), textAlign: TextAlign.center),
            const SizedBox(height: 16),
            TextButton(onPressed: _loadData, child: const Text('Tekrar dene', style: TextStyle(color: LoginColors.primaryEnd))),
          ],
        ),
      ),
    );
  }

  Widget _buildForm() {
    return CustomScrollView(
      slivers: [
        SliverToBoxAdapter(
          child: Padding(
            padding: const EdgeInsets.fromLTRB(20, 16, 20, 0),
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                _buildHeader(),
                const SizedBox(height: 20),
                _buildQuotaCard(),
                const SizedBox(height: 20),
                _buildTextField(),
                const SizedBox(height: 16),
                _buildRecipientsSection(),
                const SizedBox(height: 16),
                _buildDatePicker(),
                const SizedBox(height: 16),
                _buildPublicToggle(),
                if (_canPhoto) ...[
                  const SizedBox(height: 16),
                  _buildPhotoSection(),
                ],
                if (_canFile) ...[
                  const SizedBox(height: 16),
                  _buildFileSection(),
                ],
                if (_canVoice) ...[
                  const SizedBox(height: 16),
                  _buildVoiceSection(),
                ],
                const SizedBox(height: 24),
                _buildSubmitButton(),
                const SizedBox(height: 40),
              ],
            ),
          ),
        ),
      ],
    );
  }

  Widget _buildHeader() {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        const Text(
          'Geleceğe Mesaj Yaz',
          style: TextStyle(fontSize: 24, fontWeight: FontWeight.w700, color: LoginColors.textWhite),
        ),
        const SizedBox(height: 6),
        Text(
          'Kendinize veya sevdiklerinize zamanlanmış mesaj bırakın.',
          style: TextStyle(fontSize: 14, color: LoginColors.textMuted),
        ),
      ],
    );
  }

  Widget _buildQuotaCard() {
    final planName = _quota?.planName ?? _effectivePlan;
    final used = _quota?.used ?? 0;
    final limit = _quota?.limit ?? _profile?.maxMessagesPerPlan ?? 3;
    final remaining = _quota?.remaining ?? 0;

    return Container(
      width: double.infinity,
      padding: const EdgeInsets.all(16),
      decoration: BoxDecoration(
        gradient: LinearGradient(
          begin: Alignment.topLeft,
          end: Alignment.bottomRight,
          colors: [
            LoginColors.surface,
            LoginColors.primaryEnd.withValues(alpha: 0.08),
          ],
        ),
        borderRadius: BorderRadius.circular(14),
        border: Border.all(color: LoginColors.border),
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Row(
            children: [
              Container(
                padding: const EdgeInsets.symmetric(horizontal: 10, vertical: 4),
                decoration: BoxDecoration(
                  color: LoginColors.primaryEnd.withValues(alpha: 0.2),
                  borderRadius: BorderRadius.circular(8),
                ),
                child: Text(planName, style: const TextStyle(fontSize: 12, fontWeight: FontWeight.w700, color: LoginColors.primaryEnd)),
              ),
              const SizedBox(width: 12),
              Text('$used / $limit mesaj', style: const TextStyle(fontSize: 13, color: LoginColors.textLightGray)),
            ],
          ),
          const SizedBox(height: 10),
          ClipRRect(
            borderRadius: BorderRadius.circular(4),
            child: LinearProgressIndicator(
              value: limit > 0 ? used / limit : 0,
              minHeight: 6,
              backgroundColor: LoginColors.border,
              valueColor: AlwaysStoppedAnimation<Color>(
                remaining > 0 ? LoginColors.primaryEnd : Colors.red.shade400,
              ),
            ),
          ),
          const SizedBox(height: 8),
          Text(
            remaining > 0 ? 'Kalan hak: $remaining' : 'Mesaj hakkınız doldu. Planınızı yükseltin.',
            style: TextStyle(fontSize: 12, color: remaining > 0 ? LoginColors.textMuted : Colors.red.shade300),
          ),
          if (_effectivePlan != 'FREE') ...[
            const SizedBox(height: 8),
            Wrap(
              spacing: 8,
              runSpacing: 4,
              children: [
                _featureChip('Metin', true),
                _featureChip('Fotoğraf ($_maxPhotos)', _canPhoto),
                _featureChip('Dosya ($_maxFiles)', _canFile),
                _featureChip('Ses kaydı', _canVoice),
                _featureChip('Alıcı ($_maxRecipients)', _maxRecipients > 1),
              ],
            ),
          ],
        ],
      ),
    );
  }

  Widget _featureChip(String label, bool enabled) {
    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 3),
      decoration: BoxDecoration(
        color: enabled ? LoginColors.primaryEnd.withValues(alpha: 0.12) : LoginColors.border.withValues(alpha: 0.3),
        borderRadius: BorderRadius.circular(6),
      ),
      child: Row(
        mainAxisSize: MainAxisSize.min,
        children: [
          Icon(enabled ? Icons.check_rounded : Icons.close_rounded, size: 14, color: enabled ? LoginColors.primaryEnd : LoginColors.textMuted),
          const SizedBox(width: 4),
          Text(label, style: TextStyle(fontSize: 11, color: enabled ? LoginColors.primaryEnd : LoginColors.textMuted)),
        ],
      ),
    );
  }

  Widget _buildTextField() {
    return _sectionCard(
      icon: Icons.edit_note_rounded,
      title: 'Mesaj metni',
      child: TextField(
        controller: _contentController,
        maxLines: 6,
        minLines: 4,
        style: const TextStyle(fontSize: 15, color: LoginColors.textWhite, height: 1.5),
        decoration: InputDecoration(
          hintText: 'Merhaba gelecekteki ben...',
          hintStyle: TextStyle(color: LoginColors.textMuted.withValues(alpha: 0.5)),
          filled: true,
          fillColor: LoginColors.background,
          border: OutlineInputBorder(borderRadius: BorderRadius.circular(12), borderSide: BorderSide(color: LoginColors.border)),
          enabledBorder: OutlineInputBorder(borderRadius: BorderRadius.circular(12), borderSide: BorderSide(color: LoginColors.border)),
          focusedBorder: OutlineInputBorder(borderRadius: BorderRadius.circular(12), borderSide: const BorderSide(color: LoginColors.primaryEnd)),
          contentPadding: const EdgeInsets.all(14),
        ),
      ),
    );
  }

  Widget _buildRecipientsSection() {
    return _sectionCard(
      icon: Icons.person_outline_rounded,
      title: _effectivePlan == 'FREE' ? 'Alıcı' : 'Alıcılar (en fazla $_maxRecipients)',
      child: Column(
        children: [
          if (_effectivePlan == 'FREE') ...[
            Container(
              width: double.infinity,
              padding: const EdgeInsets.all(12),
              decoration: BoxDecoration(
                color: LoginColors.background,
                borderRadius: BorderRadius.circular(12),
                border: Border.all(color: LoginColors.border),
              ),
              child: Row(
                children: [
                  const Icon(Icons.email_outlined, size: 18, color: LoginColors.textMuted),
                  const SizedBox(width: 10),
                  Expanded(
                    child: Text(
                      _profile?.email ?? '—',
                      style: const TextStyle(fontSize: 14, color: LoginColors.textLightGray),
                    ),
                  ),
                  Container(
                    padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 2),
                    decoration: BoxDecoration(
                      color: LoginColors.primaryEnd.withValues(alpha: 0.15),
                      borderRadius: BorderRadius.circular(6),
                    ),
                    child: const Text('Kendiniz', style: TextStyle(fontSize: 10, color: LoginColors.primaryEnd, fontWeight: FontWeight.w600)),
                  ),
                ],
              ),
            ),
            const SizedBox(height: 6),
            Text(
              'Ücretsiz planda mesaj sadece size iletilir.',
              style: TextStyle(fontSize: 12, color: LoginColors.textMuted),
            ),
          ] else ...[
            ...List.generate(_recipientControllers.length, (i) {
              return Padding(
                padding: const EdgeInsets.only(bottom: 8),
                child: Row(
                  children: [
                    Expanded(
                      child: TextField(
                        controller: _recipientControllers[i],
                        keyboardType: TextInputType.emailAddress,
                        style: const TextStyle(fontSize: 14, color: LoginColors.textWhite),
                        decoration: InputDecoration(
                          hintText: 'ornek@email.com',
                          hintStyle: TextStyle(color: LoginColors.textMuted.withValues(alpha: 0.5)),
                          filled: true,
                          fillColor: LoginColors.background,
                          border: OutlineInputBorder(borderRadius: BorderRadius.circular(12), borderSide: BorderSide(color: LoginColors.border)),
                          enabledBorder: OutlineInputBorder(borderRadius: BorderRadius.circular(12), borderSide: BorderSide(color: LoginColors.border)),
                          focusedBorder: OutlineInputBorder(borderRadius: BorderRadius.circular(12), borderSide: const BorderSide(color: LoginColors.primaryEnd)),
                          contentPadding: const EdgeInsets.symmetric(horizontal: 14, vertical: 12),
                          prefixIcon: const Icon(Icons.email_outlined, size: 18, color: LoginColors.textMuted),
                        ),
                      ),
                    ),
                    if (_recipientControllers.length > 1) ...[
                      const SizedBox(width: 8),
                      IconButton(
                        onPressed: () => _removeRecipient(i),
                        icon: const Icon(Icons.remove_circle_outline_rounded, color: Colors.red, size: 22),
                        visualDensity: VisualDensity.compact,
                      ),
                    ],
                  ],
                ),
              );
            }),
            if (_recipientControllers.length < _maxRecipients)
              Align(
                alignment: Alignment.centerLeft,
                child: TextButton.icon(
                  onPressed: _addRecipient,
                  icon: const Icon(Icons.add_rounded, size: 18),
                  label: const Text('Alıcı ekle'),
                  style: TextButton.styleFrom(foregroundColor: LoginColors.primaryEnd),
                ),
              ),
          ],
        ],
      ),
    );
  }

  Widget _buildDatePicker() {
    return _sectionCard(
      icon: Icons.schedule_rounded,
      title: 'İletim tarihi ve saati',
      child: Material(
        color: LoginColors.background,
        borderRadius: BorderRadius.circular(12),
        child: InkWell(
          onTap: _pickDateTime,
          borderRadius: BorderRadius.circular(12),
          child: Container(
            width: double.infinity,
            padding: const EdgeInsets.all(14),
            decoration: BoxDecoration(
              borderRadius: BorderRadius.circular(12),
              border: Border.all(color: LoginColors.border),
            ),
            child: Row(
              children: [
                Icon(Icons.calendar_today_rounded, size: 20, color: _scheduledAt != null ? LoginColors.primaryEnd : LoginColors.textMuted),
                const SizedBox(width: 12),
                Expanded(
                  child: Text(
                    _scheduledAt != null ? _formatDateTime(_scheduledAt!) : 'Tarih seçmek için dokunun',
                    style: TextStyle(
                      fontSize: 14,
                      color: _scheduledAt != null ? LoginColors.textWhite : LoginColors.textMuted,
                    ),
                  ),
                ),
                const Icon(Icons.arrow_forward_ios_rounded, size: 16, color: LoginColors.textMuted),
              ],
            ),
          ),
        ),
      ),
    );
  }

  Widget _buildPublicToggle() {
    return Container(
      padding: const EdgeInsets.all(14),
      decoration: BoxDecoration(
        color: LoginColors.surface,
        borderRadius: BorderRadius.circular(14),
        border: Border.all(color: LoginColors.border),
      ),
      child: Row(
        children: [
          Expanded(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                const Text('Herkese açık', style: TextStyle(fontSize: 14, fontWeight: FontWeight.w600, color: LoginColors.textWhite)),
                const SizedBox(height: 2),
                Text('Mesaj iletildikten sonra herkese açık sayfada listelensin.', style: TextStyle(fontSize: 12, color: LoginColors.textMuted)),
              ],
            ),
          ),
          Switch(
            value: _isPublic,
            onChanged: (v) => setState(() => _isPublic = v),
            activeColor: LoginColors.primaryEnd,
          ),
        ],
      ),
    );
  }

  Widget _buildPhotoSection() {
    return _sectionCard(
      icon: Icons.photo_library_rounded,
      title: 'Fotoğraf (en fazla $_maxPhotos, maks ${_formatSize(_maxPhotoSizeBytes)})',
      child: Column(
        children: [
          if (_photos.isNotEmpty)
            SizedBox(
              height: 100,
              child: ListView.separated(
                scrollDirection: Axis.horizontal,
                itemCount: _photos.length,
                separatorBuilder: (_, __) => const SizedBox(width: 10),
                itemBuilder: (_, i) {
                  return Stack(
                    children: [
                      ClipRRect(
                        borderRadius: BorderRadius.circular(10),
                        child: Image.network(_photos[i].url, width: 100, height: 100, fit: BoxFit.cover),
                      ),
                      Positioned(
                        top: 4,
                        right: 4,
                        child: GestureDetector(
                          onTap: () => setState(() => _photos.removeAt(i)),
                          child: Container(
                            padding: const EdgeInsets.all(4),
                            decoration: BoxDecoration(color: Colors.black54, shape: BoxShape.circle),
                            child: const Icon(Icons.close_rounded, size: 16, color: Colors.white),
                          ),
                        ),
                      ),
                    ],
                  );
                },
              ),
            ),
          if (_photos.length < _maxPhotos) ...[
            if (_photos.isNotEmpty) const SizedBox(height: 10),
            _uploadingPhoto
                ? const Padding(
                    padding: EdgeInsets.all(12),
                    child: CircularProgressIndicator(strokeWidth: 2, color: LoginColors.primaryEnd),
                  )
                : _uploadZone(
                    icon: Icons.add_photo_alternate_outlined,
                    label: 'Fotoğraf ekle',
                    onTap: _pickAndUploadPhoto,
                  ),
          ],
        ],
      ),
    );
  }

  Widget _buildFileSection() {
    return _sectionCard(
      icon: Icons.attach_file_rounded,
      title: 'Dosya (en fazla $_maxFiles, maks ${_formatSize(_maxFileSizeBytes)})',
      child: Column(
        children: [
          ..._files.asMap().entries.map((entry) {
            final i = entry.key;
            final f = entry.value;
            return Padding(
              padding: const EdgeInsets.only(bottom: 8),
              child: Container(
                padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 10),
                decoration: BoxDecoration(
                  color: LoginColors.background,
                  borderRadius: BorderRadius.circular(10),
                  border: Border.all(color: LoginColors.border),
                ),
                child: Row(
                  children: [
                    const Icon(Icons.insert_drive_file_rounded, size: 20, color: LoginColors.primaryEnd),
                    const SizedBox(width: 10),
                    Expanded(
                      child: Text(f.fileName, style: const TextStyle(fontSize: 13, color: LoginColors.textWhite), overflow: TextOverflow.ellipsis),
                    ),
                    GestureDetector(
                      onTap: () => setState(() => _files.removeAt(i)),
                      child: const Icon(Icons.close_rounded, size: 20, color: LoginColors.textMuted),
                    ),
                  ],
                ),
              ),
            );
          }),
          if (_files.length < _maxFiles) ...[
            _uploadingFile
                ? const Padding(
                    padding: EdgeInsets.all(12),
                    child: CircularProgressIndicator(strokeWidth: 2, color: LoginColors.primaryEnd),
                  )
                : _uploadZone(
                    icon: Icons.upload_file_rounded,
                    label: 'Dosya ekle',
                    onTap: _pickAndUploadFile,
                  ),
          ],
        ],
      ),
    );
  }

  Widget _uploadZone({required IconData icon, required String label, required VoidCallback onTap}) {
    return Material(
      color: LoginColors.background,
      borderRadius: BorderRadius.circular(12),
      child: InkWell(
        onTap: onTap,
        borderRadius: BorderRadius.circular(12),
        child: Container(
          width: double.infinity,
          padding: const EdgeInsets.symmetric(vertical: 20),
          decoration: BoxDecoration(
            borderRadius: BorderRadius.circular(12),
            border: Border.all(color: LoginColors.border, style: BorderStyle.solid),
          ),
          child: Column(
            children: [
              Icon(icon, size: 32, color: LoginColors.primaryEnd.withValues(alpha: 0.6)),
              const SizedBox(height: 6),
              Text(label, style: const TextStyle(fontSize: 13, color: LoginColors.textMuted)),
            ],
          ),
        ),
      ),
    );
  }

  Widget _buildVoiceSection() {
    return _sectionCard(
      icon: Icons.mic_rounded,
      title: 'Ses kaydı (maks ${_formatSize(_maxAudioSizeBytes)})',
      child: Column(
        children: [
          if (_voiceRecording != null)
            Container(
              padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 10),
              decoration: BoxDecoration(
                color: LoginColors.background,
                borderRadius: BorderRadius.circular(10),
                border: Border.all(color: LoginColors.border),
              ),
              child: Row(
                children: [
                  Container(
                    width: 36,
                    height: 36,
                    decoration: BoxDecoration(
                      color: const Color(0xFFFF9800).withValues(alpha: 0.15),
                      borderRadius: BorderRadius.circular(8),
                    ),
                    alignment: Alignment.center,
                    child: const Icon(Icons.audiotrack_rounded, size: 20, color: Color(0xFFFF9800)),
                  ),
                  const SizedBox(width: 10),
                  Expanded(
                    child: Text(_voiceRecording!.fileName, style: const TextStyle(fontSize: 13, color: LoginColors.textWhite), overflow: TextOverflow.ellipsis),
                  ),
                  GestureDetector(
                    onTap: () => setState(() => _voiceRecording = null),
                    child: const Icon(Icons.close_rounded, size: 20, color: LoginColors.textMuted),
                  ),
                ],
              ),
            )
          else if (_isRecording)
            Container(
              padding: const EdgeInsets.all(16),
              decoration: BoxDecoration(
                color: LoginColors.background,
                borderRadius: BorderRadius.circular(12),
                border: Border.all(color: Colors.red.withValues(alpha: 0.4)),
              ),
              child: Column(
                children: [
                  Row(
                    mainAxisAlignment: MainAxisAlignment.center,
                    children: [
                      Container(
                        width: 12,
                        height: 12,
                        decoration: const BoxDecoration(color: Colors.red, shape: BoxShape.circle),
                      ),
                      const SizedBox(width: 10),
                      Text(
                        'Kaydediliyor  ${_formatRecordingTime(_recordingSeconds)}',
                        style: const TextStyle(fontSize: 15, fontWeight: FontWeight.w600, color: LoginColors.textWhite),
                      ),
                    ],
                  ),
                  const SizedBox(height: 16),
                  Row(
                    mainAxisAlignment: MainAxisAlignment.center,
                    children: [
                      OutlinedButton.icon(
                        onPressed: _cancelRecording,
                        icon: const Icon(Icons.close_rounded, size: 18),
                        label: const Text('İptal'),
                        style: OutlinedButton.styleFrom(
                          foregroundColor: LoginColors.textMuted,
                          side: const BorderSide(color: LoginColors.border),
                        ),
                      ),
                      const SizedBox(width: 12),
                      FilledButton.icon(
                        onPressed: _stopRecordingAndUpload,
                        icon: const Icon(Icons.stop_rounded, size: 18),
                        label: const Text('Durdur ve kaydet'),
                        style: FilledButton.styleFrom(
                          backgroundColor: const Color(0xFFFF9800),
                          foregroundColor: Colors.white,
                        ),
                      ),
                    ],
                  ),
                ],
              ),
            )
          else if (_uploadingVoice)
            const Padding(
              padding: EdgeInsets.all(16),
              child: Column(
                children: [
                  CircularProgressIndicator(strokeWidth: 2, color: Color(0xFFFF9800)),
                  SizedBox(height: 8),
                  Text('Ses yükleniyor...', style: TextStyle(fontSize: 12, color: LoginColors.textMuted)),
                ],
              ),
            )
          else
            Material(
              color: LoginColors.background,
              borderRadius: BorderRadius.circular(12),
              child: InkWell(
                onTap: _startRecording,
                borderRadius: BorderRadius.circular(12),
                child: Container(
                  width: double.infinity,
                  padding: const EdgeInsets.symmetric(vertical: 20),
                  decoration: BoxDecoration(
                    borderRadius: BorderRadius.circular(12),
                    border: Border.all(color: LoginColors.border),
                  ),
                  child: Column(
                    children: [
                      Icon(Icons.mic_none_rounded, size: 36, color: const Color(0xFFFF9800).withValues(alpha: 0.7)),
                      const SizedBox(height: 6),
                      const Text('Ses kaydet', style: TextStyle(fontSize: 13, color: LoginColors.textMuted)),
                      const SizedBox(height: 2),
                      const Text('Mikrofona dokunarak kaydı başlatın', style: TextStyle(fontSize: 11, color: LoginColors.textMuted)),
                    ],
                  ),
                ),
              ),
            ),
        ],
      ),
    );
  }

  Widget _buildSubmitButton() {
    return SizedBox(
      width: double.infinity,
      height: 52,
      child: Material(
        borderRadius: BorderRadius.circular(14),
        child: InkWell(
          onTap: (_submitting || !_canAddMore) ? null : _submit,
          borderRadius: BorderRadius.circular(14),
          child: Container(
            decoration: BoxDecoration(
              gradient: (_submitting || !_canAddMore)
                  ? null
                  : const LinearGradient(colors: [LoginColors.primaryStart, LoginColors.primaryEnd]),
              color: (_submitting || !_canAddMore) ? LoginColors.border : null,
              borderRadius: BorderRadius.circular(14),
              boxShadow: (_submitting || !_canAddMore)
                  ? null
                  : [
                      BoxShadow(
                        color: LoginColors.primaryStart.withValues(alpha: 0.3),
                        blurRadius: 12,
                        offset: const Offset(0, 4),
                      ),
                    ],
            ),
            alignment: Alignment.center,
            child: _submitting
                ? const SizedBox(width: 24, height: 24, child: CircularProgressIndicator(strokeWidth: 2, color: Colors.white))
                : const Row(
                    mainAxisAlignment: MainAxisAlignment.center,
                    children: [
                      Icon(Icons.send_rounded, size: 20, color: Colors.white),
                      SizedBox(width: 10),
                      Text('Mesajı Zamanla', style: TextStyle(fontSize: 16, fontWeight: FontWeight.w600, color: Colors.white)),
                    ],
                  ),
          ),
        ),
      ),
    );
  }

  Widget _sectionCard({required IconData icon, required String title, required Widget child}) {
    return Container(
      width: double.infinity,
      padding: const EdgeInsets.all(16),
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
              Icon(icon, size: 18, color: LoginColors.primaryEnd),
              const SizedBox(width: 8),
              Text(title, style: const TextStyle(fontSize: 13, fontWeight: FontWeight.w600, color: LoginColors.textLightGray)),
            ],
          ),
          const SizedBox(height: 12),
          child,
        ],
      ),
    );
  }
}

class _UploadedFile {
  _UploadedFile({required this.url, required this.fileName, this.fileSize});
  final String url;
  final String fileName;
  final int? fileSize;
}
