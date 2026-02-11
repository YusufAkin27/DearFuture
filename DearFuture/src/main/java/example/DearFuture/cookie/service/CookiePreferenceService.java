package example.DearFuture.cookie.service;

import example.DearFuture.cookie.dto.CookiePreferenceRequest;
import example.DearFuture.cookie.entity.CookiePreference;
import example.DearFuture.user.entity.User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

import java.util.Optional;

/**
 * Cookie Preference service interface
 */
public interface CookiePreferenceService {

    CookiePreference saveCookiePreference(
            @Valid CookiePreferenceRequest request,
            User user,
            HttpServletRequest httpRequest);

    Optional<CookiePreference> getCookiePreference(
            User user,
            String sessionId,
            HttpServletRequest httpRequest);

    CookiePreference revokeConsent(
            User user,
            String sessionId,
            String reason,
            HttpServletRequest httpRequest);

    CookiePreference transferGuestPreferencesToUser(
            User user,
            String sessionId,
            HttpServletRequest httpRequest);

    boolean isConsentVersionCurrent(User user, String sessionId, HttpServletRequest httpRequest);
}

