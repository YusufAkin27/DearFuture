# Dear Future

Geleceğe mesaj bırakma uygulaması. Kendinize veya sevdiklerinize zamanlanmış metin, fotoğraf, dosya ve ses kaydı gönderebilir; mesajlar iletildikten sonra herkese açık sayfada paylaşılabilir.

## Özellikler

- **Kimlik doğrulama:** E-posta kodu veya Google ile giriş
- **Zamanlanmış mesajlar:** Belirlediğiniz tarih ve saatte iletilen mesajlar
- **İçerik türleri:** Metin, fotoğraf, dosya; Premium ile ses kaydı
- **Abonelik planları:** Ücretsiz (3 mesaj, tek alıcı), Plus (20 mesaj, 5 alıcı, fotoğraf/dosya), Premium (100 mesaj, 20 alıcı, ses kaydı)
- **Herkese açık mesajlar:** İletilen mesajları herkese açık sayfada listeleme ve yıldızlama
- **Profil, ayarlar, abonelik yönetimi:** iyzico ile ödeme, e-posta bildirimleri

## Teknoloji

| Katman      | Teknoloji |
|------------|-----------|
| Backend    | Java 21, Spring Boot 4, Spring Security, JWT, OAuth2 |
| Veritabanı | PostgreSQL |
| Önbellek   | Redis (rate limit, isteğe bağlı) |
| Frontend   | React 19, Vite, React Router, Axios |
| Dosya     | Cloudinary |
| E-posta   | Brevo (SMTP) |
| Ödeme     | iyzico |

## Proje yapısı

```
dearFuture/
├── DearFuture/           # Backend (Spring Boot)
│   ├── src/
│   ├── pom.xml
│   └── Dockerfile
├── dear-future-web/      # Frontend (React + Vite)
│   ├── src/
│   ├── package.json
│   ├── nginx.conf
│   └── Dockerfile
├── docker-compose.yml    # Tüm servisler
├── DOCKER.md             # Docker detayları
└── README.md
```

## Gereksinimler

- **Yerel çalıştırma:** Java 21, Maven, Node.js 22+, PostgreSQL 16, (isteğe bağlı) Redis 7
- **Docker ile:** Docker ve Docker Compose

---

## Docker ile çalıştırma (önerilen)

Tüm ortamlarda aynı şekilde çalışır:

```bash
docker compose up -d
```

- **Uygulama:** http://localhost  
- **API:** http://localhost/api/… (nginx üzerinden backend’e yönlenir)

Detaylar için [DOCKER.md](./DOCKER.md) dosyasına bakın.

---

## Yerel geliştirme

### Backend

1. PostgreSQL’de `dearfuture` veritabanı ve kullanıcı oluşturun.
2. `DearFuture/src/main/resources/application.properties` içinde veritabanı, Redis, mail ve diğer ayarları düzenleyin.
3. Backend’i başlatın:

```bash
cd DearFuture
mvn spring-boot:run
```

Backend varsayılan olarak https://api.dearfuture.info üzerinde çalışır.

### Frontend

1. Bağımlılıkları yükleyin ve geliştirme sunucusunu başlatın:

```bash
cd dear-future-web
npm install
npm run dev
```

2. Tarayıcıda https://dearfuture.com.tr açılır. Vite, `/api` isteklerini backend’e (localhost:8080) proxy eder.

### Build

- **Backend:** `cd DearFuture && mvn package`
- **Frontend:** `cd dear-future-web && npm run build` (çıktı: `dist/`)

---

## Canlı ortam adresleri

| Ortam   | URL |
|---------|-----|
| Frontend | https://dearfuture.com.tr |
| Backend  | https://api.dearfuture.info |

Google ile giriş: Kullanıcı dearfuture.com.tr üzerinde "Google ile Giriş"e tıklar → tarayıcı api.dearfuture.info/oauth2/authorization/google adresine gider → Google girişi sonrası backend kullanıcıyı https://dearfuture.com.tr/auth/callback?token=... ile yönlendirir.

**Önemli:** Ziyaretçiler her zaman **dearfuture.com.tr** adresinden girmeli; ilk açılışta Welcome (hoş geldin) sayfası görünür, giriş zorunlu değildir. **api.dearfuture.info** yalnızca API ve OAuth için kullanılır; bu adresi tarayıcıda açanlara artık login sayfası yerine kısa bir metin gösterilir. Sunucuda dearfuture.com.tr için React build (SPA) servis edilmeli, api.dearfuture.info’ya yönlendirme yapılmamalıdır.

### Frontend’i dearfuture.com.tr’de yayınlama

1. `cd dear-future-web && npm run build`
2. Oluşan **dist/** klasörünü dearfuture.com.tr sunucusuna atın.
3. Sunucuda dearfuture.com.tr için root olarak bu dist gösterilsin; tüm path’ler index.html’e yönlendirilsin (SPA fallback: `try_files $uri $uri/ /index.html`).
4. api.dearfuture.info ayrı çalışsın; dearfuture.com.tr’yi api’ye yönlendirmeyin.

---

## Ortam değişkenleri (backend)

Docker veya yerel ortamda kullanılabilecek örnekler:

| Değişken | Açıklama |
|----------|----------|
| `SPRING_DATASOURCE_URL` | PostgreSQL JDBC URL |
| `SPRING_DATASOURCE_USERNAME` / `PASSWORD` | Veritabanı kullanıcı bilgileri |
| `SPRING_DATA_REDIS_HOST` / `PORT` | Redis adresi (rate limit) |
| `APP_BASE_URL` | Backend’in dış erişim adresi (callback vb.) |
| `APP_FRONTEND_URL` | Frontend adresi (https://dearfuture.com.tr, e-posta ve OAuth sonrası yönlendirme) |

Geliştirme için hassas bilgiler (JWT secret, Cloudinary, Brevo, iyzico) `application.properties` veya ortam değişkenleri ile ayarlanır; bu dosyaları versiyon kontrolüne **commit etmeyin**.

---

## Google ile giriş (OAuth2) – redirect_uri_mismatch hatası

**Erişim engellendi: 400 redirect_uri_mismatch** alıyorsanız, Google Cloud Console’da yönlendirme adresini eklemeniz gerekir:

1. [Google Cloud Console](https://console.cloud.google.com/) → **APIs & Services** → **Credentials**
2. OAuth 2.0 **Client ID**’nizi seçin (Web uygulaması)
3. **Authorized redirect URIs** bölümüne şu adresi **birebir** ekleyin (sonunda `/` olmamalı):
   - **Canlı (production):** `https://api.dearfuture.info/login/oauth2/code/google`
   - **Yerel test:** `http://localhost:8080/login/oauth2/code/google`
4. **Save** ile kaydedin.

Backend, `app.base-url` değerine göre redirect URI üretir (`application.properties` içinde `spring.security.oauth2.client.registration.google.redirect-uri=${app.base-url}/login/oauth2/code/google`). Sunucunuz farklı bir domain’deyse (örn. `https://api.dearfuture.com.tr`) `app.base-url` değerini buna göre güncelleyin ve Google Console’daki redirect URI’yi de aynı adresle eşleştirin.

---

## Lisans

Bu proje özel kullanım içindir.
