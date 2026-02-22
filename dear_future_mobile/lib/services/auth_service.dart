import 'dart:convert';

import 'package:google_sign_in/google_sign_in.dart';
import 'package:shared_preferences/shared_preferences.dart';

import '../config/api_config.dart';
import 'api_client.dart';

class AuthService {
  AuthService() : _client = ApiClient();

  final ApiClient _client;
  static const _tokenKey = 'dear_future_jwt_token';

  final GoogleSignIn _googleSignIn = GoogleSignIn(
    scopes: ['email', 'profile'],
  );

  String? _token;
  String? get token => _token;

  Future<void> loadStoredToken() async {
    final prefs = await SharedPreferences.getInstance();
    _token = prefs.getString(_tokenKey);
    if (_token != null) {
      _client.setToken(_token);
    }
  }

  Future<bool> get isLoggedIn async {
    await loadStoredToken();
    return _token != null && _token!.isNotEmpty;
  }

  Future<void> _saveToken(String token) async {
    _token = token;
    _client.setToken(token);
    final prefs = await SharedPreferences.getInstance();
    await prefs.setString(_tokenKey, token);
  }

  Future<void> logout() async {
    _token = null;
    _client.setToken(null);
    final prefs = await SharedPreferences.getInstance();
    await prefs.remove(_tokenKey);
    await _googleSignIn.signOut();
  }

  /// E-posta ile giriş kodu gönderir.
  Future<void> sendCode(String email) async {
    final res = await _client.post(ApiConfig.authSendCode, body: {'email': email});
    if (res.statusCode != 200) {
      final msg = _errorMessage(res);
      throw Exception(msg);
    }
  }

  /// Kodu doğrular ve JWT alır.
  Future<String> verifyCode(String email, String code) async {
    final res = await _client.post(ApiConfig.authVerify, body: {
      'email': email,
      'code': code,
    });
    if (res.statusCode != 200) {
      final msg = _errorMessage(res);
      throw Exception(msg);
    }
    final data = jsonDecode(res.body) as Map<String, dynamic>;
    final token = data['token'] as String?;
    if (token == null || token.isEmpty) {
      throw Exception('Token alınamadı.');
    }
    await _saveToken(token);
    return token;
  }

  /// Google ile giriş: ID token'ı backend'e gönderir, JWT alır.
  Future<String> signInWithGoogle() async {
    final account = await _googleSignIn.signIn();
    if (account == null) {
      throw Exception('Google giriş iptal edildi.');
    }
    final auth = await account.authentication;
    final idToken = auth.idToken;
    if (idToken == null || idToken.isEmpty) {
      throw Exception('Google kimlik bilgisi alınamadı.');
    }
    final res = await _client.post(ApiConfig.authGoogle, body: {'idToken': idToken});
    if (res.statusCode != 200) {
      final msg = _errorMessage(res);
      throw Exception(msg);
    }
    final data = jsonDecode(res.body) as Map<String, dynamic>;
    final token = data['token'] as String?;
    if (token == null || token.isEmpty) {
      throw Exception('Token alınamadı.');
    }
    await _saveToken(token);
    return token;
  }

  String _errorMessage(dynamic res) {
    try {
      final body = jsonDecode(res.body);
      if (body is Map && body['message'] != null) {
        return body['message'] as String;
      }
    } catch (_) {}
    if (res.statusCode == 400) return 'Geçersiz istek.';
    if (res.statusCode == 401) return 'Yetkisiz.';
    if (res.statusCode == 429) return 'Çok fazla deneme. Lütfen bekleyin.';
    return 'Bir hata oluştu. Lütfen tekrar deneyin.';
  }
}
