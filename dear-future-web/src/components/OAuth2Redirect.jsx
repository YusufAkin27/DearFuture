import { useEffect } from 'react';
import { useLocation } from 'react-router-dom';

/**
 * Frontend (dearfuture.com.tr) üzerinde /oauth2/... açılırsa session backend'de tutulduğu için
 * cookie farklı domain'de kalır → authorization_request_not_found.
 * Bu bileşen kullanıcıyı doğrudan api.dearfuture.info/oauth2/... adresine yönlendirir.
 */
const OAUTH_BACKEND = 'https://api.dearfuture.info';

const OAuth2Redirect = () => {
    const location = useLocation();

    useEffect(() => {
        const path = location.pathname + location.search;
        if (path.startsWith('/oauth2/') || path.startsWith('/login/oauth2')) {
            window.location.replace(OAUTH_BACKEND + path);
        }
    }, [location.pathname, location.search]);

    return (
        <div style={{ padding: '2rem', textAlign: 'center' }}>
            Giriş sayfasına yönlendiriliyorsunuz…
        </div>
    );
};

export default OAuth2Redirect;
