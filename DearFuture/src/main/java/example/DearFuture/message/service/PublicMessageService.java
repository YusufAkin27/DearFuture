package example.DearFuture.message.service;

import example.DearFuture.message.dto.response.PublicMessageItemResponse;
import example.DearFuture.message.dto.response.PublicPhotoItemResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * Herkese açık (public) mesajlar ve yıldız işlemleri.
 */
public interface PublicMessageService {

    /**
     * Açılmış ve public olan mesajları listeler. Giriş yapılmışsa starredByMe dolu olur.
     * @param currentUserId giriş yapan kullanıcı id (null ise misafir)
     */
    List<PublicMessageItemResponse> getPublicMessages(Long currentUserId);

    /**
     * Açılmış ve public olan mesajları sayfalı listeler.
     */
    Page<PublicMessageItemResponse> getPublicMessages(Long currentUserId, Pageable pageable);

    /**
     * Giriş yapan kullanıcının yıldızladığı mesajları listeler.
     */
    List<PublicMessageItemResponse> getMyStarredMessages();

    /**
     * Mesajı yıldızla (giriş gerekli).
     */
    void starMessage(Long messageId);

    /**
     * Yıldızı kaldır (giriş gerekli).
     */
    void unstarMessage(Long messageId);

    /**
     * Açılmış ve herkese açık mesajlardaki sadece fotoğrafları sayfalı listeler. Giriş gerekmez.
     */
    Page<PublicPhotoItemResponse> getPublicPhotos(Pageable pageable);
}
