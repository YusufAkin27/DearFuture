package example.DearFuture.cloudinary.service;

import example.DearFuture.cloudinary.dto.OptimizedImageResult;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;

/**
 * Media upload service interface
 */
public interface MediaUploadService {

    /**
     * Medya dosyasını otomatik olarak algılayıp yükler (resim veya video)
     */
    String uploadAndOptimizeMedia(MultipartFile file) throws IOException;

    /**
     * Perde ürün görsellerini optimize edilmiş şekilde yükler
     * WebP formatına dönüştürür, CDN ve caching kullanır
     * 
     * @deprecated Yeni optimize edilmiş yükleme için uploadAndOptimizeProductImage kullanın
     */
    @Deprecated
    String uploadAndOptimizeImage(MultipartFile photo) throws IOException;
    
    /**
     * CDN URL'i oluşturur (caching ve optimizasyon ile)
     * WebP formatına dönüştürür, kaliteyi koruyarak sıkıştırır
     * Cache kullanır - aynı parametreler için tekrar istek atmaz
     * 
     * @param publicId Cloudinary public ID veya full URL
     * @param width Genişlik (opsiyonel)
     * @param height Yükseklik (opsiyonel)
     * @return Optimize edilmiş CDN URL'i (WebP, cached)
     */
    String generateCdnUrl(String publicId, Integer width, Integer height);
    
    /**
     * Görsel URL'ini optimize edilmiş hale getirir (görüntüleme için)
     * WebP formatına dönüştürür, kaliteyi koruyarak sıkıştırır
     * Cache kullanır - sürekli istek atmaz
     * 
     * @param imageUrl Orijinal görsel URL'i (Cloudinary URL'i veya public ID)
     * @param width Genişlik (opsiyonel, null ise orijinal boyut)
     * @param height Yükseklik (opsiyonel, null ise orijinal boyut)
     * @return Optimize edilmiş görsel URL'i (WebP, cached)
     */
    String getOptimizedImageUrl(String imageUrl, Integer width, Integer height);

    /**
     * Responsive image URLs oluşturur
     */
    OptimizedImageResult.ImageVariants getResponsiveImageUrls(String imageUrl);
    
    /**
     * Görseli CDN'den siler (cache temizleme için)
     * 
     * @param publicId Cloudinary public ID
     */
    void invalidateCdnCache(String publicId);

    /**
     * Görseli optimize edilmiş şekilde yükler (byte array ile)
     * - Orijinal dosyayı arşiv klasörüne kaydeder
     * - WebP formatına dönüştürür (kaliteyi koruyarak)
     * - CDN ve caching kullanır
     * - Farklı boyutlarda responsive versiyonlar oluşturur
     * 
     * @param imageBytes Görsel byte array'i
     * @param originalFilename Orijinal dosya adı
     * @param originalSize Orijinal dosya boyutu
     * @return Optimize edilmiş görsel sonucu
     */
    OptimizedImageResult uploadAndOptimizeProductImage(byte[] imageBytes, String originalFilename, long originalSize) throws IOException;

    /**
     * Görseli optimize edilmiş şekilde yükler (MultipartFile ile - uyumluluk için)
     * - Orijinal dosyayı arşiv klasörüne kaydeder
     * - WebP formatına dönüştürür (kaliteyi koruyarak)
     * - CDN ve caching kullanır
     * - Farklı boyutlarda responsive versiyonlar oluşturur
     * 
     * @param photo Yüklenecek görsel
     * @return Optimize edilmiş görsel sonucu
     */
    OptimizedImageResult uploadAndOptimizeProductImage(MultipartFile photo) throws IOException;

    /**
     * Görseli kalite kaybı olmadan yükler (orijinal boyut ve kalite korunur)
     * Admin panel ürün fotoğrafları için kullanılır
     * Optimize edilmiş: Eager transformation ile hızlı yükleme
     * Senkron versiyon - hızlı geri dönüş için
     * 
     * @deprecated Yeni optimize edilmiş yükleme için uploadAndOptimizeProductImage kullanın
     */
    @Deprecated
    String uploadImageWithoutQualityLoss(MultipartFile photo) throws IOException;

    /**
     * Görseli asenkron olarak optimize eder ve yükler (arka planda)
     * Admin panel ürün fotoğrafları için kullanılır
     * Hızlı geri dönüş sağlar, yükleme arka planda devam eder
     * 
     * NOT: MultipartFile'ı byte array'e çevirir (geçici dosya silinmeden önce)
     * 
     * @param photo Yüklenecek görsel
     * @return Optimize edilmiş görsel URL'i
     */
    CompletableFuture<String> uploadImageWithoutQualityLossAsync(MultipartFile photo);

    /**
     * Görseli asenkron olarak optimize eder ve yükler (detaylı sonuç ile)
     * 
     * NOT: MultipartFile'ı byte array'e çevirir (geçici dosya silinmeden önce)
     * 
     * @param photo Yüklenecek görsel
     * @return Optimize edilmiş görsel sonucu
     */
    CompletableFuture<OptimizedImageResult> uploadAndOptimizeProductImageAsync(MultipartFile photo);
    
    /**
     * Cache'den görsel sonucunu getir
     * 
     * @param publicId Cloudinary public ID
     * @return Cache'deki görsel sonucu (yoksa null)
     */
    OptimizedImageResult getCachedResult(String publicId);
    
    /**
     * Cache'i temizle
     * 
     * @param publicId Cloudinary public ID (null ise tüm cache temizlenir)
     */
    void clearCache(String publicId);
    
    /**
     * Cache istatistiklerini döndürür
     * 
     * @return Cache istatistikleri
     */
    String getCacheStats();

    /**
     * Perde tanıtım videolarını yükler
     * Full HD kalite korunur
     */
    String uploadAndOptimizeVideo(MultipartFile video) throws IOException;

    /**
     * Thumbnail (küçük önizleme) oluşturur
     * Ürün listelerinde kullanmak için
     */
    String uploadThumbnail(MultipartFile photo) throws IOException;

    /**
     * Video thumbnail oluşturur
     */
    String generateVideoThumbnail(String videoPublicId) throws IOException;
}

