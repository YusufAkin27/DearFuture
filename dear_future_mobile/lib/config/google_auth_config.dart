/// Google Sign-In için kullanılan yapılandırma.
///
/// Android OAuth client oluştururken Google Cloud Console'da
/// bu SHA-1 parmak izini ekleyin. Client ID uygulama içinde kullanılır.
class GoogleAuthConfig {
  GoogleAuthConfig._();

  /// Android OAuth 2.0 Client ID (Google Cloud Console).
  static const String androidClientId =
      '75778679149-temd0gqflf9plbgtb507q0ou04nk2mnf.apps.googleusercontent.com';

  /// Bu projede kullanılan SHA-1 parmak izi (Google Cloud Console'da
  /// Android OAuth client'a eklenmeli).
  static const String sha1Fingerprint =
      'BB:A7:8C:22:38:AF:51:EA:9C:BE:BA:21:0D:74:C6:89:87:EC:F1:6C';
}
