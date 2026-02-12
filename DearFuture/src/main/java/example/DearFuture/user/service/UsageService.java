package example.DearFuture.user.service;

import example.DearFuture.user.dto.response.UsageResponse;

public interface UsageService {

    /**
     * Kullanıcının abonelik durumuna göre bu dönem kalan mesaj kullanım hakkı.
     */
    UsageResponse getUsage(Long userId);
}
