import 'dart:convert';
import 'dart:io';

import 'package:http/http.dart' as http;
import 'package:http_parser/http_parser.dart';

import '../config/api_config.dart';
import 'api_client.dart';

class UploadResult {
  UploadResult({required this.url, required this.fileName, this.fileSize});

  final String url;
  final String fileName;
  final int? fileSize;

  static UploadResult? fromJson(Map<String, dynamic>? json) {
    if (json == null) return null;
    return UploadResult(
      url: json['url'] as String? ?? '',
      fileName: json['fileName'] as String? ?? '',
      fileSize: (json['fileSize'] as num?)?.toInt(),
    );
  }
}

class MessageService {
  MessageService(this._client);

  final ApiClient _client;

  /// FREE plan: POST /api/messages  {content, scheduledAt, isPublic}
  Future<bool> createSimpleMessage({
    required String content,
    required String scheduledAt,
    bool isPublic = false,
  }) async {
    final res = await _client.post(ApiConfig.messagesCreate, body: {
      'content': content,
      'scheduledAt': scheduledAt,
      'isPublic': isPublic,
    });
    return res.statusCode == 200 || res.statusCode == 201;
  }

  /// PLUS/PREMIUM plan: POST /api/messages/schedule
  Future<bool> scheduleMessage({
    required List<String> recipientEmails,
    required String scheduledAt,
    required List<Map<String, dynamic>> contents,
    bool isPublic = false,
  }) async {
    final res = await _client.post(ApiConfig.messagesSchedule, body: {
      'recipientEmails': recipientEmails,
      'scheduledAt': scheduledAt,
      'contents': contents,
      'isPublic': isPublic,
    });
    return res.statusCode == 200 || res.statusCode == 201;
  }

  /// POST /api/messages/upload  (multipart: file + type)
  Future<UploadResult?> uploadAttachment({
    required List<int> bytes,
    required String fileName,
    required String type,
    String? mimeType,
  }) async {
    final uri = Uri.parse('${_client.baseUrl}${ApiConfig.messagesUpload}');
    final request = http.MultipartRequest('POST', uri);
    final headers = _client.authHeaders;
    if (headers != null) {
      request.headers.addAll(headers);
    }
    request.fields['type'] = type;
    final mt = mimeType != null
        ? MediaType.parse(mimeType)
        : MediaType('application', 'octet-stream');
    request.files.add(http.MultipartFile.fromBytes(
      'file',
      bytes,
      filename: fileName,
      contentType: mt,
    ));
    final streamed = await request.send();
    if (streamed.statusCode != 200 && streamed.statusCode != 201) return null;
    final body = await streamed.stream.bytesToString();
    final json = jsonDecode(body) as Map<String, dynamic>?;
    return UploadResult.fromJson(json);
  }
}
