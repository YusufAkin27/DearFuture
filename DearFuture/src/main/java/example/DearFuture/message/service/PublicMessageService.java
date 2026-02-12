package example.DearFuture.message.service;

import example.DearFuture.message.dto.response.PublicMessageItemResponse;

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
}
