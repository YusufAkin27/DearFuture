import { useRef, useCallback } from 'react';

/**
 * Debounced callback: art arda tıklamalarda sadece belirli süre (delay ms) geçtikten sonra
 * yeni tetiklemeye izin verir. API isteği atan butonlarda çift tıklamayı önlemek için kullanın.
 * @param {Function} fn - Çağrılacak fonksiyon (async olabilir)
 * @param {number} delay - Minimum ms (örn. 500) — bu süre dolmadan tekrar çağrı yapılmaz
 * @returns Debounced fonksiyon
 */
export function useDebouncedCallback(fn, delay = 500) {
    const lastCallRef = useRef(0);
    const fnRef = useRef(fn);
    fnRef.current = fn;

    return useCallback(
        (...args) => {
            const now = Date.now();
            if (now - lastCallRef.current < delay) return;
            lastCallRef.current = now;
            return fnRef.current(...args);
        },
        [delay]
    );
}
