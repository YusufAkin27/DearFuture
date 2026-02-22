/**
 * Bellekleme (cache): GET isteklerinde yanıtları belirli süre saklar.
 * Tekrar aynı istek yapıldığında ağa gitmeden önbellekten döner.
 */

const store = new Map();

/** TTL süreleri (ms) */
export const CACHE_TTL = {
    /** Planlar, sözleşmeler: nadiren değişir */
    STATIC: 10 * 60 * 1000,      // 10 dk
    /** Profil, kota: kısa süre */
    USER: 1 * 60 * 1000,         // 1 dk
    /** Herkese açık mesaj listesi */
    PUBLIC_LIST: 2 * 60 * 1000,  // 2 dk
    /** Token ile mesaj görüntüleme (public) */
    VIEW_MESSAGE: 5 * 60 * 1000, // 5 dk
    /** Kurucu ortak GitHub profili (projeler + README) */
    TEAM_GITHUB: 10 * 60 * 1000, // 10 dk
};

/**
 * Cache'lenecek path'leri ve TTL'lerini döner.
 * @param {string} path - istek path'i (örn. /subscription/plans)
 * @returns {{ ttl: number } | null} cache kullanılacaksa { ttl }, değilse null
 */
export function getCacheConfig(path) {
    const p = (path || '').replace(/^\//, '/');
    if (p.startsWith('/subscription/plans')) return { ttl: CACHE_TTL.STATIC };
    if (p.startsWith('/contracts')) return { ttl: CACHE_TTL.STATIC };
    if (p.startsWith('/user/profile') || p.startsWith('/user/message-quota')) return { ttl: CACHE_TTL.USER };
    if (p.startsWith('/messages/public') && !p.includes('/star')) return { ttl: CACHE_TTL.PUBLIC_LIST };
    if (p.startsWith('/messages/view/')) return { ttl: CACHE_TTL.VIEW_MESSAGE };
    if (p.startsWith('/public/team/github/')) return { ttl: CACHE_TTL.TEAM_GITHUB };
    return null;
}

/**
 * Cache anahtarı: method + url + kullanıcı (token varsa kısa hash).
 */
export function cacheKey(config) {
    const token = config.headers?.Authorization;
    const userPart = token ? String(token).slice(0, 32) : 'anon';
    const url = config.url || '';
    const params = config.params ? '?' + new URLSearchParams(config.params).toString() : '';
    return `${config.method || 'get'}:${url}${params}:${userPart}`;
}

export function get(key) {
    const entry = store.get(key);
    if (!entry) return null;
    if (Date.now() > entry.expiresAt) {
        store.delete(key);
        return null;
    }
    return entry.data;
}

export function set(key, data, ttlMs) {
    store.set(key, {
        data,
        expiresAt: Date.now() + ttlMs,
    });
}

/** Tüm önbelleği temizler (çıkış yapıldığında kullanılır). */
export function clear() {
    store.clear();
}

/** Anahtarı verilen prefix ile başlayan kayıtları siler (örn. profil güncellemesi sonrası). */
export function invalidatePrefix(prefix) {
    if (!prefix) return;
    for (const key of store.keys()) {
        if (key.startsWith(prefix)) store.delete(key);
    }
}
