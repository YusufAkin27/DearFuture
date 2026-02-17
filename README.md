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

Backend varsayılan olarak http://localhost:8080 üzerinde çalışır.

### Frontend

1. Bağımlılıkları yükleyin ve geliştirme sunucusunu başlatın:

```bash
cd dear-future-web
npm install
npm run dev
```

2. Tarayıcıda http://localhost:5173 açılır. Vite, `/api` isteklerini backend’e (localhost:8080) proxy eder.

### Build

- **Backend:** `cd DearFuture && mvn package`
- **Frontend:** `cd dear-future-web && npm run build` (çıktı: `dist/`)

---

## Ortam değişkenleri (backend)

Docker veya yerel ortamda kullanılabilecek örnekler:

| Değişken | Açıklama |
|----------|----------|
| `SPRING_DATASOURCE_URL` | PostgreSQL JDBC URL |
| `SPRING_DATASOURCE_USERNAME` / `PASSWORD` | Veritabanı kullanıcı bilgileri |
| `SPRING_DATA_REDIS_HOST` / `PORT` | Redis adresi (rate limit) |
| `APP_BASE_URL` | Backend’in dış erişim adresi (callback vb.) |
| `APP_FRONTEND_URL` | Frontend adresi (e-posta linkleri) |

Geliştirme için hassas bilgiler (JWT secret, Cloudinary, Brevo, iyzico) `application.properties` veya ortam değişkenleri ile ayarlanır; bu dosyaları versiyon kontrolüne **commit etmeyin**.

---

## Lisans

Bu proje özel kullanım içindir.
