# DearFuture — Backend API

Dear Future platformunun REST API sunucusu. Spring Boot 3.2 ile geliştirilmiştir.

## Gereksinimler

- Java 17+
- Maven 3.6+
- PostgreSQL (veya `application.properties` içinde yapılandırdığınız veritabanı)

## Kurulum ve Çalıştırma

```bash
# Bağımlılıkları indir ve uygulamayı çalıştır
./mvnw spring-boot:run
```

Windows’ta:

```bash
mvnw.cmd spring-boot:run
```

## Yapılandırma

`src/main/resources/application.properties` dosyasında veritabanı bağlantısı, port ve diğer ayarları düzenleyebilirsiniz.

## API

Uygulama çalıştığında varsayılan olarak `http://localhost:8080` üzerinden erişilebilir. Swagger/OpenAPI dokümantasyonu yapılandırıldıysa ilgili endpoint üzerinden inceleyebilirsiniz.

## Proje Yapısı

- **controller** — REST endpoint’leri
- **service** — İş mantığı
- **repository** — Veritabanı erişimi
- **dto** — Veri transfer nesneleri
- **entity** — JPA varlıkları

---

Canlı uygulama: [dearfuture.com.tr](https://dearfuture.com.tr)
