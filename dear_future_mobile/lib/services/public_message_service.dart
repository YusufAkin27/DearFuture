import 'dart:convert';

import '../config/api_config.dart';
import '../models/public_message_models.dart';
import 'api_client.dart';

class PublicMessageService {
  PublicMessageService([ApiClient? client]) : _client = client ?? ApiClient();

  final ApiClient _client;

  /// GET /api/messages/public?page=&size= — sayfalı public mesaj listesi.
  Future<PublicMessagePage?> getPublicMessages({int page = 0, int size = 12}) async {
    final path = '${ApiConfig.messagesPublic}?page=$page&size=$size';
    final res = await _client.get(path);
    if (res.statusCode != 200) return null;
    final map = jsonDecode(res.body) as Map<String, dynamic>?;
    return PublicMessagePage.fromJson(map);
  }

  /// GET /api/messages/view/{token} — mesaj detayı (giriş gerekmez).
  Future<MessageViewDetail?> getMessageView(String viewToken) async {
    if (viewToken.isEmpty) return null;
    final res = await _client.get(ApiConfig.messageViewByToken(viewToken));
    if (res.statusCode != 200) return null;
    final map = jsonDecode(res.body) as Map<String, dynamic>?;
    return MessageViewDetail.fromJson(map);
  }

  /// GET /api/messages/public/starred — yıldızlı mesajlarım (giriş gerekir).
  Future<List<PublicMessageItem>> getStarredMessages() async {
    final res = await _client.get(ApiConfig.messagesPublicStarred);
    if (res.statusCode != 200) return [];
    final list = jsonDecode(res.body);
    if (list is! List) return [];
    return list
        .map((e) => PublicMessageItem.fromJson(e as Map<String, dynamic>?))
        .whereType<PublicMessageItem>()
        .toList();
  }

  /// POST /api/messages/public/{id}/star — mesajı yıldızla (giriş gerekir).
  Future<bool> starMessage(int messageId) async {
    final res = await _client.post(ApiConfig.publicMessageStar(messageId), body: {});
    return res.statusCode == 200 || res.statusCode == 204;
  }

  /// DELETE /api/messages/public/{id}/star — yıldızı kaldır (giriş gerekir).
  Future<bool> unstarMessage(int messageId) async {
    final res = await _client.delete(ApiConfig.publicMessageStar(messageId));
    return res.statusCode == 200 || res.statusCode == 204;
  }
}
