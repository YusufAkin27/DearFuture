# Dear Future – Mobil Uygulama

Flutter ile yazılmış Dear Future mobil uygulaması. Backend: **https://api.dearfuture.info**

## Özellikler

- **Hoşgeldin ekranı** – Başla ile giriş sayfasına geçiş
- **Giriş sayfası**
  - E-posta ile doğrulama kodu (kod e-posta ile gönderilir, doğrula ile JWT alınır)
  - Google ile giriş (Google ID token backend’e gönderilir, JWT döner)
- JWT token `SharedPreferences` ile saklanır; uygulama açıldığında oturum kontrol edilir

## Çalıştırma

```bash
flutter pub get
flutter run
```

## API adresi

Tüm istekler `https://api.dearfuture.info` base URL’i üzerinden yapılır. Adres `lib/config/api_config.dart` içinde tanımlıdır.

## Google ile giriş (Android / iOS)

1. [Google Cloud Console](https://console.cloud.google.com/) → APIs & Services → Credentials.
2. **Android**: OAuth 2.0 Client ID (Android) oluştur; package name: `info.dearfuture.dear_future_mobile`, debug/release SHA-1 ekleyin.
3. **iOS**: OAuth 2.0 Client ID (iOS) oluştur; bundle ID’yi ekleyin.
4. Backend’deki `POST /api/auth/google` endpoint’i, Google’dan gelen ID token’ı doğrular ve JWT döndürür; ek backend yapılandırması gerekmez (token doğrulama Google tokeninfo ile yapılır).

## Backend auth endpoint’leri

- `POST /api/auth/send-code` – `{ "email": "..." }`
- `POST /api/auth/verify` – `{ "email": "...", "code": "..." }` → `{ "token": "..." }`
- `POST /api/auth/google` – `{ "idToken": "..." }` → `{ "token": "..." }`
