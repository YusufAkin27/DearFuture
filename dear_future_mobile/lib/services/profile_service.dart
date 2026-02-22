import 'dart:convert';
import 'dart:io';

import '../config/api_config.dart';
import '../models/profile_models.dart';
import 'api_client.dart';

class ProfileService {
  ProfileService(this._client);

  final ApiClient _client;

  /// GET /api/user/profile → UserResponse
  Future<ProfileData?> getProfile() async {
    final res = await _client.get(ApiConfig.userProfile);
    if (res.statusCode != 200) return null;
    final map = jsonDecode(res.body) as Map<String, dynamic>?;
    return ProfileData.fromJson(map);
  }

  /// PUT /api/user/profile
  Future<bool> updateProfile({String? firstName, String? lastName}) async {
    final body = <String, dynamic>{};
    if (firstName != null) body['firstName'] = firstName;
    if (lastName != null) body['lastName'] = lastName;
    final res = await _client.put(ApiConfig.userProfile, body: body.isNotEmpty ? body : null);
    return res.statusCode == 200;
  }

  /// GET /api/user/message-quota → MessageQuotaResponse
  Future<MessageQuotaData?> getMessageQuota() async {
    final res = await _client.get(ApiConfig.userMessageQuota);
    if (res.statusCode != 200) return null;
    final map = jsonDecode(res.body) as Map<String, dynamic>?;
    return MessageQuotaData.fromJson(map);
  }

  /// POST /api/user/profile/photo (multipart). Başarı: null, hata: mesaj metni.
  Future<String?> uploadPhoto(File file) async {
    try {
      final streamed = await _client.postMultipartFile(ApiConfig.userProfilePhoto, file);
      if (streamed.statusCode >= 200 && streamed.statusCode < 300) return null;
      final body = await streamed.stream.bytesToString();
      return _errorFromBody(body);
    } catch (e) {
      return e.toString();
    }
  }

  /// Byte listesi ile yükleme (Android content URI vb. için).
  /// [mimeType] örn. 'image/jpeg' — backend sadece image kabul ettiği için gönderilir.
  Future<String?> uploadPhotoFromBytes(List<int> bytes, String filename, {String? mimeType}) async {
    try {
      final streamed = await _client.postMultipartBytes(
        ApiConfig.userProfilePhoto,
        fieldName: 'photo',
        bytes: bytes,
        filename: filename.isEmpty ? 'photo.jpg' : filename,
        contentType: mimeType,
      );
      if (streamed.statusCode >= 200 && streamed.statusCode < 300) return null;
      final body = await streamed.stream.bytesToString();
      return _errorFromBody(body);
    } catch (e) {
      return e.toString();
    }
  }

  static String _errorFromBody(String body) {
    try {
      final map = jsonDecode(body) as Map<String, dynamic>?;
      if (map != null && map['message'] != null) return map['message'] as String;
    } catch (_) {}
    return body.isNotEmpty ? body : 'Fotoğraf yüklenemedi.';
  }

  /// DELETE /api/user/profile/photo
  Future<bool> deletePhoto() async {
    final res = await _client.delete(ApiConfig.userProfilePhoto);
    return res.statusCode == 200 || res.statusCode == 204;
  }

  /// PUT /api/user/settings
  Future<bool> updateSettings({
    String? locale,
    bool? emailNotifications,
    bool? marketingEmails,
  }) async {
    final body = <String, dynamic>{};
    if (locale != null) body['locale'] = locale;
    if (emailNotifications != null) body['emailNotifications'] = emailNotifications;
    if (marketingEmails != null) body['marketingEmails'] = marketingEmails;
    final res = await _client.put(ApiConfig.userSettings, body: body.isNotEmpty ? body : null);
    return res.statusCode == 200;
  }

  /// PATCH /api/user/account/deactivate
  Future<bool> deactivateAccount() async {
    final res = await _client.patch(ApiConfig.userAccountDeactivate);
    return res.statusCode == 200 || res.statusCode == 204;
  }

  /// DELETE /api/user/account
  Future<bool> deleteAccount() async {
    final res = await _client.delete(ApiConfig.userAccount);
    return res.statusCode == 200 || res.statusCode == 204;
  }

  /// GET /api/messages → iletilen mesajlar listesi
  Future<List<DeliveredMessageItem>> getDeliveredMessages() async {
    final res = await _client.get(ApiConfig.messagesDelivered);
    if (res.statusCode != 200) return [];
    final list = jsonDecode(res.body);
    if (list is! List) return [];
    return list
        .map((e) => DeliveredMessageItem.fromJson(e as Map<String, dynamic>?))
        .whereType<DeliveredMessageItem>()
        .toList();
  }
}
