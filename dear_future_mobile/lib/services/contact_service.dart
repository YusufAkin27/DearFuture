import 'dart:convert';

import '../config/api_config.dart';
import 'api_client.dart';

/// İletişim formu API. Giriş gerekmez.
class ContactService {
  ContactService() : _client = ApiClient();

  final ApiClient _client;

  /// Mesaj gönderir; e-posta doğrulama kodu gönderilir.
  Future<ContactSendResult> sendMessage({
    required String name,
    required String email,
    required String subject,
    required String message,
    String? phone,
  }) async {
    final res = await _client.post(ApiConfig.contactSend, body: {
      'name': name.trim(),
      'email': email.trim(),
      'subject': subject.trim(),
      'message': message.trim(),
      if (phone != null && phone.trim().isNotEmpty) 'phone': phone.trim(),
    });
    final data = res.body.isNotEmpty ? jsonDecode(res.body) as Map<String, dynamic>? : null;
    final success = data?['success'] == true;
    final msg = data?['message'] as String? ?? _defaultMessage(res.statusCode);
    return ContactSendResult(success: success, message: msg);
  }

  /// E-posta doğrulama kodu ile mesajı onaylar.
  Future<ContactVerifyResult> verifyEmail(String code) async {
    final res = await _client.post(ApiConfig.contactVerifyEmail, body: {'code': code.trim()});
    final data = res.body.isNotEmpty ? jsonDecode(res.body) as Map<String, dynamic>? : null;
    final success = data?['success'] == true;
    final msg = data?['message'] as String? ?? _defaultMessage(res.statusCode);
    return ContactVerifyResult(success: success, message: msg);
  }

  String _defaultMessage(int statusCode) {
    if (statusCode == 400) return 'Geçersiz istek.';
    if (statusCode == 429) return 'Çok fazla deneme. Lütfen bekleyin.';
    return 'Bir hata oluştu. Lütfen tekrar deneyin.';
  }
}

class ContactSendResult {
  ContactSendResult({required this.success, required this.message});
  final bool success;
  final String message;
}

class ContactVerifyResult {
  ContactVerifyResult({required this.success, required this.message});
  final bool success;
  final String message;
}
