# SEO Rehberi – Dear Future Web

## Google Search Console doğrulama (HTML dosyası)

Google size `googleXXXXXXXX.html` adında bir dosya verdi. Bu dosyayı **site kökünde** yayına almanız gerekir.

### Ne yaptık?

- Doğrulama dosyası **`public/google1194c4a4a6b1bad8.html`** içine kondu.
- `npm run build` ile build aldığınızda bu dosya `dist/` içine kopyalanır ve canlıda **`https://siteniz.com/google1194c4a4a6b1bad8.html`** adresinden erişilebilir olur.

### Siz ne yapacaksınız?

1. Projeyi build alıp sunucuya deploy edin (`npm run build` → `dist/` içeriğini yayınlayın).
2. Tarayıcıda şu adresi açın: **`https://dearfuture.com.tr/google1194c4a4a6b1bad8.html`**
3. Sayfada sadece şu metin görünmeli:  
   `google-site-verification: google1194c4a4a6b1bad8.html`
4. [Google Search Console](https://search.google.com/search-console) → Doğrulama sayfasına dönüp **“Doğrula”** butonuna tıklayın.

**Not:** Eğer dosyayı proje kökünde (dear-future-web klasöründe) bıraktıysanız, oradan silebilirsiniz; artık geçerli konum `public/google1194c4a4a6b1bad8.html`.

---

## 1. Title, Meta, Canonical

- **Title:** Her sayfa için `App.jsx` içindeki `PAGE_TITLES` ile sayfa başlığı ayarlanıyor; format: `Sayfa | Dear Future – Geleceğe Mesaj Yaz`.
- **Meta description:** Sayfa bazlı açıklamalar `PAGE_DESCRIPTIONS` ile tanımlı; `DocumentHead` her route değişiminde `<meta name="description">` ve `og:description` değerlerini güncelliyor.
- **Canonical:** Her sayfa için `link rel="canonical"` otomatik set ediliyor (kök adres `VITE_APP_URL` veya `window.location.origin`).

---

## 2. H1 ve başlık hiyerarşisi

- Her sayfada tek bir anlamlı **H1** var (Welcome, Features, Pricing, About, SSS, Contact, Blog, Download, vb.).
- Alt başlıklar H2/H3 ile veriliyor. Yeni sayfa eklerken sayfa başına tek H1 kullanın.

---

## 3. URL’ler

- Temiz, anlamlı path’ler kullanılıyor: `/pricing`, `/about`, `/blog`, `/sss`, `/contact`, `/download`, `/public-messages` vb.
- Gereksiz query parametreleri yok; OAuth/callback gibi zorunlu parametreler sadece ilgili sayfalarda.

---

## 4. Alt etiketleri (Alt tag)

- Logo ve önemli görsellerde `alt` metni var (örn. "Dear Future").
- Blog kartlarındaki görsellerde `alt={post.title}` kullanılıyor.
- NewMessagePage dosya önizlemelerinde `alt` açıklayıcı metinle dolduruldu.
- Yeni görsel eklerken her zaman anlamlı bir `alt` yazın.

---

## 5. Schema (JSON-LD)

- **`index.html`** içinde:
  - **Organization:** site adı, URL, logo, kısa açıklama.
  - **WebSite:** site adı, URL, dil (tr), isteğe bağlı SearchAction (public-messages araması).
- Sosyal medya linkleriniz varsa `Organization.sameAs` dizisine ekleyebilirsiniz.

---

## 6. Site hızı

- **Preconnect:** Google Fonts için `preconnect` kullanılıyor.
- **Font:** `display=swap` ile font bloğu azaltıldı.
- **Preload:** Ana logo (`/logo.png`) için `rel="preload" as="image"` eklendi.
- **Lazy load:** Blog ve liste görsellerinde `loading="lazy"` var.
- **Build:** Vite build’de minify ve chunk’lama (vendor-react, vendor-router) ile paket boyutu optimize edildi.

---

## 7. Sitemap ve robots.txt

- **Sitemap:** `public/sitemap.xml` (build sırasında `scripts/generate-seo.js` ile base URL güncellenir).
- **robots.txt:** `public/robots.txt`; tarama kuralları ve Sitemap URL’i burada.
- **Google Search Console:** Site doğrulandıktan sonra “Site haritaları” bölümüne `sitemap.xml` ekleyin.

---

## Özet kontrol listesi

| Öğe | Durum |
|-----|--------|
| Google doğrulama dosyası | `public/google1194c4a4a6b1bad8.html` |
| Title (sayfa bazlı) | `PAGE_TITLES` + DocumentHead |
| Meta description | `PAGE_DESCRIPTIONS` + DocumentHead |
| Canonical URL | DocumentHead ile otomatik |
| H1 (sayfa başına tek) | Mevcut sayfalarda uyumlu |
| Alt tag | Logo, blog, önizleme görsellerinde dolu |
| Schema (JSON-LD) | Organization + WebSite |
| Site hızı | Preconnect, preload, lazy, chunking |
| Sitemap / robots | public/sitemap.xml, public/robots.txt |
