Backend yalnızca API sunar; web arayüzü ayrı deploy edilir (dearfuture.com.tr).
Bu klasör isteğe bağlı statik dosyalar için kullanılabilir.

Frontend deploy: dear-future-web içinde "npm run build" çalıştırıp
oluşan dist/ klasörünü dearfuture.com.tr sunucusunda yayınlayın (nginx, Netlify, vb.).
