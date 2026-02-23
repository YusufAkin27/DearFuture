/// Google Sign-In için kullanılan yapılandırma.
///
/// - Android OAuth client: Google Cloud Console'da paket adı + SHA-1 ekleyin.
/// - serverClientId olarak Web client ID kullanılır (idToken backend'e gönderilir).
class GoogleAuthConfig {
  GoogleAuthConfig._();

  /// Android OAuth 2.0 Client ID (Google Cloud Console - Android uygulaması).
  /// Uygulama imzası için SHA-1 aynı konsolda bu client'a eklenmeli.
  static const String androidClientId =
      '75778679149-qlolur9k39llge3bk2lq3ku01u671pbd.apps.googleusercontent.com';

  /// Web client ID (backend ile aynı). serverClientId olarak bu kullanılır ki
  /// idToken alınabilsin ve backend doğrulayabilsin.
  static const String webClientId =
      '75778679149-lslf8k3tfk1uriq5crtiliiahf7lpo0h.apps.googleusercontent.com';

  /// Bu projede kullanılan SHA-1 (debug: keytool -list -v -keystore ~/.android/debug.keystore).
  /// Google Cloud Console'da Android OAuth client'a eklenmeli.
  static const String sha1Fingerprint =
      'BB:A7:8C:22:38:AF:51:EA:9C:BE:BA:21:0D:74:C6:89:87:EC:F1:6C';
}
