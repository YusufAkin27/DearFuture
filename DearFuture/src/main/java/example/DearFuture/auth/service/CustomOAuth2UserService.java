package example.DearFuture.auth.service;

import example.DearFuture.user.entity.Role;
import example.DearFuture.user.entity.SubscriptionPlan;
import example.DearFuture.user.entity.User;
import example.DearFuture.user.repository.SubscriptionPlanRepository;
import example.DearFuture.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Set;

/**
 * Google OAuth2 ile gelen kullanıcıyı işler.
 * - İlk giriş: DB'de yoksa kaydeder (profil bilgileri + FREE plan).
 * - Var olan kullanıcı: profil bilgilerini günceller, giriş yaptırır.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;
    private final SubscriptionPlanRepository planRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oauth2User = super.loadUser(userRequest);
        Map<String, Object> attributes = oauth2User.getAttributes();

        String email = (String) attributes.get("email");
        if (email == null || email.isBlank()) {
            log.warn("Google OAuth2: email missing in attributes");
            throw new OAuth2AuthenticationException("email_missing");
        }
        email = email.trim().toLowerCase();

        String givenName = (String) attributes.get("given_name");
        String familyName = (String) attributes.get("family_name");
        String fullName = (String) attributes.get("name");
        String picture = (String) attributes.get("picture");

        User user = userRepository.findByEmail(email).orElse(null);

        if (user != null) {
            updateProfile(user, givenName, familyName, fullName, picture);
            userRepository.save(user);
            log.info("Google OAuth2: existing user logged in, profile updated: {}", email);
        } else {
            user = createUser(email, givenName, familyName, fullName, picture);
            userRepository.save(user);
            log.info("Google OAuth2: new user created: {}", email);
        }

        return oauth2User;
    }

    private void updateProfile(User user, String givenName, String familyName, String fullName, String picture) {
        setNames(user, givenName, familyName, fullName);
        if (picture != null && !picture.isBlank()) {
            user.setProfilePictureUrl(picture.trim());
        }
        user.setEmailVerified(true);
    }

    private User createUser(String email, String givenName, String familyName, String fullName, String picture) {
        User user = new User();
        user.setEmail(email);
        user.setRoles(Set.of(Role.USER));
        user.setEnabled(true);
        user.setEmailVerified(true);
        setNames(user, givenName, familyName, fullName);
        if (picture != null && !picture.isBlank()) {
            user.setProfilePictureUrl(picture.trim());
        }
        SubscriptionPlan freePlan = planRepository.findByCode("FREE").orElse(null);
        user.setSubscriptionPlan(freePlan);
        return user;
    }

    private void setNames(User user, String givenName, String familyName, String fullName) {
        if (givenName != null && !givenName.isBlank()) {
            user.setFirstName(givenName.trim());
        }
        if (familyName != null && !familyName.isBlank()) {
            user.setLastName(familyName.trim());
        }
        if (user.getFirstName() == null && user.getLastName() == null && fullName != null && !fullName.isBlank()) {
            String name = fullName.trim();
            int space = name.indexOf(' ');
            if (space > 0) {
                user.setFirstName(name.substring(0, space));
                user.setLastName(name.substring(space + 1).trim());
            } else {
                user.setFirstName(name);
            }
        }
    }
}
