import 'dart:convert';

import 'package:http/http.dart' as http;

import '../config/api_config.dart';

/// Backend REST API istemcisi. Base URL: https://api.dearfuture.info
class ApiClient {
  ApiClient({String? token}) : _token = token;

  String? _token;
  String get baseUrl => ApiConfig.baseUrl;

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
    return http.get(
      Uri.parse('$baseUrl$path'),
      headers: _headers,
    );
  }

  Future<http.Response> post(String path, {Map<String, dynamic>? body}) async {
    return http.post(
      Uri.parse('$baseUrl$path'),
      headers: _headers,
      body: body != null ? jsonEncode(body) : null,
    );
  }

  Future<http.Response> put(String path, {Map<String, dynamic>? body}) async {
    return http.put(
      Uri.parse('$baseUrl$path'),
      headers: _headers,
      body: body != null ? jsonEncode(body) : null,
    );
  }

  Future<http.Response> delete(String path) async {
    return http.delete(
      Uri.parse('$baseUrl$path'),
      headers: _headers,
    );
  }
}
