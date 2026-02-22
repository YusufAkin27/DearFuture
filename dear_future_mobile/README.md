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

Bu projede kullanılan değerler (`lib/config/google_auth_config.dart`):

- **Client ID:** `75778679149-temd0gqflf9plbgtb507q0ou04nk2mnf.apps.googleusercontent.com`
- **SHA-1:** `BB:A7:8C:22:38:AF:51:EA:9C:BE:BA:21:0D:74:C6:89:87:EC:F1:6C`

**Önemli:** `{"installed":{"client_id":"75778679149-...", "project_id":"dearfuture-487110", ...}}` şeklinde indirdiğiniz JSON **Desktop/Diğer** tipi client’a aittir. Android’de girişin çalışması için Console’da **Android** tipinde ayrı bir OAuth client oluşturup package name ve SHA-1 eklemeniz gerekir (Android için JSON indirilmez).

### Android – OAuth client kurulumu

1. [Google Cloud Console](https://console.cloud.google.com/) → **APIs & Services** → **Credentials**.
2. **+ CREATE CREDENTIALS** → **OAuth client ID**.
3. Application type: **Android**.
4. Name: örn. "Dear Future Android".
5. Package name: `info.dearfuture.dear_future_mobile`.
6. **SHA-1 certificate fingerprint:** Yukarıdaki SHA-1 değerini yapıştırın:  
   `BB:A7:8C:22:38:AF:51:EA:9C:BE:BA:21:0D:74:C6:89:87:EC:F1:6C`
7. **Create** ile kaydedin. Oluşan Client ID’yi kullanıyorsanız (`75778679149-temd0gqflf9plbgtb507q0ou04nk2mnf...`) uygulama bu değerle çalışır.

Farklı bir keystore (release) kullanıyorsanız o keystore’un SHA-1’ini de aynı Android OAuth client’a ekleyin.

### iOS

- OAuth 2.0 Client ID (iOS) oluşturup bundle ID’yi ekleyin.

### Genel

- Backend’deki `POST /api/auth/google` endpoint’i, Google’dan gelen ID token’ı doğrular ve JWT döndürür.

## Backend auth endpoint’leri

- `POST /api/auth/send-code` – `{ "email": "..." }`
- `POST /api/auth/verify` – `{ "email": "...", "code": "..." }` → `{ "token": "..." }`
- `POST /api/auth/google` – `{ "idToken": "..." }` → `{ "token": "..." }`
