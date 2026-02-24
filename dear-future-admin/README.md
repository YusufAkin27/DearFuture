# Dear Future – Admin Panel

Admin web paneli. Backend: **https://api.dearfuture.info**

## Giriş

- Admin hesabı: **ysufakn63@gmail.com** (backend ilk açılışta bu e-posta ile admin oluşturur).
- Giriş: E-posta + doğrulama kodu (ana sitedeki gibi: kodu e-posta ile alıp girin).

## Geliştirme

```bash
npm install
npm run dev
```

Tarayıcıda `http://localhost:5173` açılır. Giriş sonrası dashboard görünür.

## Production build

API adresini vererek build alın:

```bash
VITE_API_URL=https://api.dearfuture.info npm run build
```

Çıktı: `dist/`. Bu klasörü `admin.dearfuture.info` veya `dearfuture.com.tr/admin` gibi bir adreste statik olarak sunun. API istekleri `VITE_API_URL` ile yapılır.

## Backend (AdminInitializer)

Backend ilk çalıştığında `AdminInitializer`:

- E-posta **ysufakn63@gmail.com** ile kullanıcı yoksa oluşturur (ADMIN + USER rolü).
- Varsa bu kullanıcıya ADMIN rolü ekler.

Farklı admin e-posta kullanmak için:

- Ortam değişkeni: `APP_ADMIN_EMAIL=admin@example.com`
- veya JVM: `-Dapp.admin-email=admin@example.com`
