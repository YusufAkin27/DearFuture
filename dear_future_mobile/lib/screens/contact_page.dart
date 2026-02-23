import 'package:flutter/material.dart';
import 'package:flutter/services.dart';

import '../theme/login_theme.dart';
import '../services/contact_service.dart';

const int _messageMin = 10;
const int _messageMax = 5000;
const String _contactEmail = 'merhaba@dearfuture.info';

class ContactPage extends StatefulWidget {
  const ContactPage({super.key});

  @override
  State<ContactPage> createState() => _ContactPageState();
}

class _ContactPageState extends State<ContactPage> {
  final ContactService _contactService = ContactService();
  final _nameController = TextEditingController();
  final _emailController = TextEditingController();
  final _subjectController = TextEditingController();
  final _messageController = TextEditingController();
  final _phoneController = TextEditingController();
  final _codeController = TextEditingController();

  String _step = 'form'; // form | verify | done
  bool _loading = false;
  String? _error;
  String? _successMessage;

  @override
  void dispose() {
    _nameController.dispose();
    _emailController.dispose();
    _subjectController.dispose();
    _messageController.dispose();
    _phoneController.dispose();
    _codeController.dispose();
    super.dispose();
  }

  Future<void> _sendMessage() async {
    final name = _nameController.text.trim();
    final email = _emailController.text.trim();
    final subject = _subjectController.text.trim();
    final message = _messageController.text.trim();
    final phone = _phoneController.text.trim();

    if (name.length < 2 || name.length > 200) {
      setState(() => _error = 'Ad Soyad 2–200 karakter olmalıdır.');
      return;
    }
    if (email.isEmpty) {
      setState(() => _error = 'E-posta adresi zorunludur.');
      return;
    }
    if (!RegExp(r'^[^\s@]+@[^\s@]+\.[^\s@]+$').hasMatch(email)) {
      setState(() => _error = 'Geçerli bir e-posta adresi giriniz.');
      return;
    }
    if (subject.length < 3 || subject.length > 500) {
      setState(() => _error = 'Konu 3–500 karakter olmalıdır.');
      return;
    }
    if (message.length < _messageMin || message.length > _messageMax) {
      setState(() => _error = 'Mesaj $_messageMin–$_messageMax karakter olmalıdır.');
      return;
    }
    setState(() {
      _error = null;
      _loading = true;
    });
    try {
      final result = await _contactService.sendMessage(
        name: name,
        email: email,
        subject: subject,
        message: message,
        phone: phone.isEmpty ? null : phone,
      );
      if (!mounted) return;
      if (result.success) {
        setState(() {
          _step = 'verify';
          _loading = false;
          _codeController.clear();
        });
      } else {
        setState(() {
          _error = result.message;
          _loading = false;
        });
      }
    } catch (e) {
      if (mounted) {
        setState(() {
          _error = e.toString().replaceFirst('Exception: ', '');
          _loading = false;
        });
      }
    }
  }

  Future<void> _verifyCode() async {
    final code = _codeController.text.replaceAll(RegExp(r'\D'), '');
    if (code.length != 6) {
      setState(() => _error = 'Doğrulama kodu 6 haneli olmalıdır.');
      return;
    }
    setState(() {
      _error = null;
      _loading = true;
    });
    try {
      final result = await _contactService.verifyEmail(code);
      if (!mounted) return;
      if (result.success) {
        setState(() {
          _step = 'done';
          _loading = false;
          _successMessage = result.message;
        });
      } else {
        setState(() {
          _error = result.message;
          _loading = false;
        });
      }
    } catch (e) {
      if (mounted) {
        setState(() {
          _error = e.toString().replaceFirst('Exception: ', '');
          _loading = false;
        });
      }
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: LoginColors.background,
      appBar: AppBar(
        backgroundColor: LoginColors.surface,
        foregroundColor: LoginColors.textWhite,
        title: const Text('İletişim', style: TextStyle(color: LoginColors.textWhite)),
        elevation: 0,
      ),
      body: SingleChildScrollView(
        padding: const EdgeInsets.fromLTRB(20, 24, 20, 32),
        child: _step == 'done' ? _buildDone() : _step == 'verify' ? _buildVerify() : _buildForm(),
      ),
    );
  }

  Widget _buildDone() {
    return Center(
      child: Padding(
        padding: const EdgeInsets.only(top: 48),
        child: Column(
          children: [
            Container(
              padding: const EdgeInsets.symmetric(horizontal: 14, vertical: 6),
              decoration: BoxDecoration(
                color: Colors.transparent,
                borderRadius: BorderRadius.circular(20),
                border: Border.all(color: LoginColors.primaryEnd),
              ),
              child: Text('TEŞEKKÜRLER', style: TextStyle(fontSize: 12, fontWeight: FontWeight.w600, color: LoginColors.primaryEnd)),
            ),
            const SizedBox(height: 20),
            Text(
              'Mesajınız alındı',
              style: TextStyle(fontSize: 24, fontWeight: FontWeight.w700, color: LoginColors.textWhite),
              textAlign: TextAlign.center,
            ),
            const SizedBox(height: 12),
            Text(
              _successMessage ?? 'En kısa sürede size dönüş yapacağız.',
              style: TextStyle(fontSize: 15, color: LoginColors.textLightGray, height: 1.4),
              textAlign: TextAlign.center,
            ),
          ],
        ),
      ),
    );
  }

  Widget _buildVerify() {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.stretch,
      children: [
        Container(
          padding: const EdgeInsets.symmetric(horizontal: 14, vertical: 6),
          decoration: BoxDecoration(
            borderRadius: BorderRadius.circular(20),
            border: Border.all(color: LoginColors.primaryEnd),
          ),
          child: Text('DOĞRULAMA', style: TextStyle(fontSize: 12, fontWeight: FontWeight.w600, color: LoginColors.primaryEnd)),
        ),
        const SizedBox(height: 16),
        Text(
          'E-postanıza gönderilen 6 haneli kodu girin',
          style: TextStyle(fontSize: 18, fontWeight: FontWeight.w600, color: LoginColors.textWhite),
        ),
        const SizedBox(height: 8),
        Text(
          'Kod 15 dakika geçerlidir. Görmediyseniz spam klasörünü kontrol edin.',
          style: TextStyle(fontSize: 14, color: LoginColors.textLightGray),
        ),
        const SizedBox(height: 24),
        TextField(
          controller: _codeController,
          keyboardType: TextInputType.number,
          maxLength: 6,
          inputFormatters: [FilteringTextInputFormatter.digitsOnly],
          style: const TextStyle(color: LoginColors.textWhite, fontSize: 24, letterSpacing: 8),
          textAlign: TextAlign.center,
          decoration: InputDecoration(
            hintText: '000000',
            hintStyle: TextStyle(color: LoginColors.textMuted.withValues(alpha: 0.5)),
            counterText: '',
            filled: true,
            fillColor: LoginColors.surface,
            border: OutlineInputBorder(borderRadius: BorderRadius.circular(12), borderSide: BorderSide(color: LoginColors.border)),
            enabledBorder: OutlineInputBorder(borderRadius: BorderRadius.circular(12), borderSide: BorderSide(color: LoginColors.border)),
            focusedBorder: OutlineInputBorder(borderRadius: BorderRadius.circular(12), borderSide: const BorderSide(color: LoginColors.primaryEnd)),
          ),
          onChanged: (_) => setState(() => _error = null),
        ),
        if (_error != null) ...[
          const SizedBox(height: 12),
          Text(_error!, style: const TextStyle(color: Colors.red, fontSize: 13)),
        ],
        const SizedBox(height: 24),
        FilledButton(
          onPressed: _loading ? null : _verifyCode,
          style: FilledButton.styleFrom(backgroundColor: LoginColors.primaryEnd, foregroundColor: Colors.white, padding: const EdgeInsets.symmetric(vertical: 16)),
          child: _loading ? const SizedBox(height: 22, width: 22, child: CircularProgressIndicator(strokeWidth: 2, color: Colors.white)) : const Text('Doğrula'),
        ),
        const SizedBox(height: 12),
        TextButton(
          onPressed: () => setState(() => _step = 'form'),
          child: Text('Geri', style: TextStyle(color: LoginColors.primaryEnd)),
        ),
      ],
    );
  }

  Widget _buildForm() {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.stretch,
      children: [
        Center(
          child: Container(
            padding: const EdgeInsets.symmetric(horizontal: 14, vertical: 6),
            decoration: BoxDecoration(
              borderRadius: BorderRadius.circular(20),
              border: Border.all(color: LoginColors.primaryEnd),
            ),
            child: Text('İLETİŞİM', style: TextStyle(fontSize: 12, fontWeight: FontWeight.w600, color: LoginColors.primaryEnd)),
          ),
        ),
        const SizedBox(height: 16),
        Text(
          'Bizimle iletişime geçin',
          style: TextStyle(fontSize: 22, fontWeight: FontWeight.w700, color: LoginColors.textWhite),
        ),
        const SizedBox(height: 8),
        Text(
          'Sorularınız veya önerileriniz için formu doldurun. E-postanıza gönderilen doğrulama kodu ile mesajınızı onaylayın.',
          style: TextStyle(fontSize: 14, color: LoginColors.textLightGray, height: 1.45),
        ),
        const SizedBox(height: 20),
        Row(
          children: [
            Icon(Icons.email_outlined, size: 22, color: LoginColors.primaryEnd),
            const SizedBox(width: 10),
            Text('E-posta', style: TextStyle(fontSize: 13, color: LoginColors.textWhite)),
            const SizedBox(width: 8),
            Expanded(child: Text(_contactEmail, style: TextStyle(fontSize: 13, color: LoginColors.primaryEnd))),
          ],
        ),
        const SizedBox(height: 24),
        Container(
          padding: const EdgeInsets.all(20),
          decoration: BoxDecoration(
            color: LoginColors.surface,
            borderRadius: BorderRadius.circular(16),
            border: Border.all(color: LoginColors.border),
          ),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              _field('Ad Soyad *', _nameController, 'Adınız Soyadınız', false),
              const SizedBox(height: 16),
              _field('E-posta *', _emailController, 'ornek@email.com', false),
              const SizedBox(height: 16),
              _field('Konu *', _subjectController, 'Mesajınızın konusu', false),
              const SizedBox(height: 16),
              _messageField(),
              const SizedBox(height: 16),
              _field('Telefon (isteğe bağlı)', _phoneController, '+90 5XX XXX XX XX', false),
              if (_error != null) ...[
                const SizedBox(height: 12),
                Text(_error!, style: const TextStyle(color: Colors.red, fontSize: 13)),
              ],
              const SizedBox(height: 20),
              SizedBox(
                width: double.infinity,
                child: FilledButton(
                  onPressed: _loading ? null : _sendMessage,
                  style: FilledButton.styleFrom(backgroundColor: LoginColors.primaryEnd, foregroundColor: Colors.white, padding: const EdgeInsets.symmetric(vertical: 16)),
                  child: _loading ? const SizedBox(height: 22, width: 22, child: CircularProgressIndicator(strokeWidth: 2, color: Colors.white)) : const Text('Mesaj Gönder'),
                ),
              ),
            ],
          ),
        ),
      ],
    );
  }

  Widget _field(String label, TextEditingController controller, String hint, bool multiline) {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Text(label, style: TextStyle(fontSize: 13, fontWeight: FontWeight.w600, color: LoginColors.textWhite)),
        const SizedBox(height: 8),
        TextField(
          controller: controller,
          maxLines: multiline ? 5 : 1,
          style: const TextStyle(color: LoginColors.textWhite, fontSize: 15),
          decoration: InputDecoration(
            hintText: hint,
            hintStyle: TextStyle(color: LoginColors.textMuted.withValues(alpha: 0.6)),
            filled: true,
            fillColor: LoginColors.background,
            border: OutlineInputBorder(borderRadius: BorderRadius.circular(12), borderSide: BorderSide(color: LoginColors.border)),
            enabledBorder: OutlineInputBorder(borderRadius: BorderRadius.circular(12), borderSide: BorderSide(color: LoginColors.border)),
            focusedBorder: OutlineInputBorder(borderRadius: BorderRadius.circular(12), borderSide: const BorderSide(color: LoginColors.primaryEnd)),
          ),
          onChanged: (_) => setState(() => _error = null),
        ),
      ],
    );
  }

  Widget _messageField() {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Text('Mesajınız *', style: TextStyle(fontSize: 13, fontWeight: FontWeight.w600, color: LoginColors.textWhite)),
        const SizedBox(height: 8),
        TextField(
          controller: _messageController,
          maxLines: 5,
          maxLength: _messageMax,
          style: const TextStyle(color: LoginColors.textWhite, fontSize: 15),
          decoration: InputDecoration(
            hintText: 'En az $_messageMin karakter...',
            hintStyle: TextStyle(color: LoginColors.textMuted.withValues(alpha: 0.6)),
            filled: true,
            fillColor: LoginColors.background,
            border: OutlineInputBorder(borderRadius: BorderRadius.circular(12), borderSide: BorderSide(color: LoginColors.border)),
            enabledBorder: OutlineInputBorder(borderRadius: BorderRadius.circular(12), borderSide: BorderSide(color: LoginColors.border)),
            focusedBorder: OutlineInputBorder(borderRadius: BorderRadius.circular(12), borderSide: const BorderSide(color: LoginColors.primaryEnd)),
            counterText: '',
          ),
          onChanged: (_) => setState(() {}),
        ),
        const SizedBox(height: 4),
        Align(
          alignment: Alignment.centerRight,
          child: Text('${_messageController.text.length}/$_messageMax', style: TextStyle(fontSize: 12, color: LoginColors.textMuted)),
        ),
      ],
    );
  }
}
