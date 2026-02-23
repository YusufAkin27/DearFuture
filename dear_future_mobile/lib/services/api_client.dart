import 'dart:convert';
import 'dart:io';

import 'package:http/http.dart' as http;
import 'package:http_parser/http_parser.dart';

import '../config/api_config.dart';

/// Backend REST API istemcisi. Base URL: https://api.dearfuture.info
class ApiClient {
  ApiClient({String? token, this.onUnauthorized}) : _token = token;

  String? _token;
  /// 401 alındığında (oturum geçersiz) çağrılır; login sayfasına yönlendirmek için kullanılır.
  final void Function()? onUnauthorized;

  String get baseUrl => ApiConfig.baseUrl;

  /// Upload istekleri gibi harici kullanım için auth header'ı döner.
  Map<String, String>? get authHeaders {
    if (_token == null || _token!.isEmpty) return null;
    return {'Authorization': 'Bearer $_token', 'Accept': 'application/json'};
  }

  void setToken(String? token) {
    _token = token;
  }

  Map<String, String> get _headers {
    final headers = <String, String>{
      'Content-Type': 'application/json',
      'Accept': 'application/json',
    };
    if (_token != null && _token!.isNotEmpty) {
      headers['Authorization'] = 'Bearer $_token';
    }
    return headers;
  }

  Future<http.Response> get(String path) async {
    final res = await http.get(
      Uri.parse('$baseUrl$path'),
      headers: _headers,
    );
    if (res.statusCode == 401 && _token != null) onUnauthorized?.call();
    return res;
  }

  Future<http.Response> post(String path, {Map<String, dynamic>? body}) async {
    final res = await http.post(
      Uri.parse('$baseUrl$path'),
      headers: _headers,
      body: body != null ? jsonEncode(body) : null,
    );
    if (res.statusCode == 401 && _token != null) onUnauthorized?.call();
    return res;
  }

  Future<http.Response> put(String path, {Map<String, dynamic>? body}) async {
    final res = await http.put(
      Uri.parse('$baseUrl$path'),
      headers: _headers,
      body: body != null ? jsonEncode(body) : null,
    );
    if (res.statusCode == 401 && _token != null) onUnauthorized?.call();
    return res;
  }

  Future<http.Response> delete(String path) async {
    final res = await http.delete(
      Uri.parse('$baseUrl$path'),
      headers: _headers,
    );
    if (res.statusCode == 401 && _token != null) onUnauthorized?.call();
    return res;
  }

  Future<http.Response> patch(String path, {Map<String, dynamic>? body}) async {
    final res = await http.patch(
      Uri.parse('$baseUrl$path'),
      headers: _headers,
      body: body != null ? jsonEncode(body) : null,
    );
    if (res.statusCode == 401 && _token != null) onUnauthorized?.call();
    return res;
  }

  /// Multipart POST for profile photo upload. [file] is the local image file.
  Future<http.StreamedResponse> postMultipartFile(String path, File file, {String fieldName = 'photo'}) async {
    final uri = Uri.parse('$baseUrl$path');
    final request = http.MultipartRequest('POST', uri);
    if (_token != null && _token!.isNotEmpty) {
      request.headers['Authorization'] = 'Bearer $_token';
    }
    request.headers['Accept'] = 'application/json';
    request.files.add(await http.MultipartFile.fromPath(fieldName, file.path));
    final res = await request.send();
    if (res.statusCode == 401 && _token != null) onUnauthorized?.call();
    return res;
  }

  /// Multipart POST with bytes (Android content URI vb. için güvenli).
  /// [contentType] örn. 'image/jpeg' — backend "Sadece görsel" hatası vermemesi için zorunlu.
  Future<http.StreamedResponse> postMultipartBytes(
    String path, {
    required String fieldName,
    required List<int> bytes,
    required String filename,
    String? contentType,
  }) async {
    final uri = Uri.parse('$baseUrl$path');
    final request = http.MultipartRequest('POST', uri);
    if (_token != null && _token!.isNotEmpty) {
      request.headers['Authorization'] = 'Bearer $_token';
    }
    request.headers['Accept'] = 'application/json';
    final type = contentType != null && contentType.startsWith('image/')
        ? MediaType.parse(contentType)
        : MediaType('image', 'jpeg');
    request.files.add(http.MultipartFile.fromBytes(
      fieldName,
      bytes,
      filename: filename,
      contentType: type,
    ));
    final res = await request.send();
    if (res.statusCode == 401 && _token != null) onUnauthorized?.call();
    return res;
  }
}
