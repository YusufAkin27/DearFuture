/// Dear Future API yapılandırması.
/// Tüm REST istekleri bu base URL üzerinden yapılır.
class ApiConfig {
  ApiConfig._();

  static const String baseUrl = 'https://api.dearfuture.info';

  static const String authSendCode = '/api/auth/send-code';
  static const String authVerify = '/api/auth/verify';
  static const String authResendCode = '/api/auth/resend-code';
  static const String authGoogle = '/api/auth/google';
}
