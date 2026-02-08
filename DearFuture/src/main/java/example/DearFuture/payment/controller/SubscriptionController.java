package example.DearFuture.payment.controller;

import example.DearFuture.payment.dto.request.InitializeCheckoutRequest;
import example.DearFuture.payment.dto.response.CheckoutInitializeResponse;
import example.DearFuture.payment.dto.response.PlanResponse;
import example.DearFuture.payment.service.SubscriptionPaymentService;
import example.DearFuture.user.entity.SubscriptionPlan;
import example.DearFuture.user.entity.User;
import example.DearFuture.user.repository.UserRepository;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/subscription")
@RequiredArgsConstructor
public class SubscriptionController {

    private final SubscriptionPaymentService subscriptionPaymentService;
    private final UserRepository userRepository;

    @Value("${app.subscription.price.plus:100}")
    private int pricePlus;

    @Value("${app.subscription.price.premium:150}")
    private int pricePremium;

    /**
     * Üyelik ödemesi başlat (PLUS 100 TL/ay, PREMIUM 150 TL/ay).
     * Sadece giriş yapmış kullanıcılar çağırabilir.
     */
    @PostMapping("/checkout/initialize")
    public ResponseEntity<CheckoutInitializeResponse> initializeCheckout(
            @Valid @RequestBody InitializeCheckoutRequest request,
            Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Kullanıcı bulunamadı"));
        CheckoutInitializeResponse response = subscriptionPaymentService.initializeCheckout(user, request);
        return ResponseEntity.ok(response);
    }

    /**
     * iyzico ödeme sonrası callback. GET (redirect) veya POST ile token gelir.
     * Giriş gerekmez.
     */
    @GetMapping(value = "/callback")
    public void paymentCallbackGet(
            @RequestParam(value = "token", required = false) String token,
            HttpServletResponse response) throws IOException {
        String redirectUrl = subscriptionPaymentService.handleCallback(token);
        response.sendRedirect(redirectUrl);
    }

    @PostMapping(value = "/callback")
    public void paymentCallbackPost(
            @RequestParam(value = "token", required = false) String token,
            HttpServletResponse response) throws IOException {
        String redirectUrl = subscriptionPaymentService.handleCallback(token);
        response.sendRedirect(redirectUrl);
    }

    /**
     * Fiyatlandırma sayfası için plan listesi (Türkçe). Giriş gerekmez.
     */
    @GetMapping("/plans")
    public ResponseEntity<List<PlanResponse>> getPlans() {
        List<PlanResponse> plans = new ArrayList<>();

        plans.add(PlanResponse.builder()
                .id(SubscriptionPlan.FREE.name())
                .name("Ücretsiz")
                .price(0)
                .priceLabel("₺/ay")
                .features(List.of(
                        SubscriptionPlan.FREE.getMaxMessages() + " zamanlanmış mesaj",
                        "Sadece metin",
                        "1 alıcı / mesaj"
                ))
                .recommended(false)
                .build());

        plans.add(PlanResponse.builder()
                .id(SubscriptionPlan.PLUS.name())
                .name("Plus")
                .price(pricePlus)
                .priceLabel("₺/ay")
                .features(List.of(
                        SubscriptionPlan.PLUS.getMaxMessages() + " zamanlanmış mesaj",
                        "Fotoğraf & dosya",
                        SubscriptionPlan.PLUS.getMaxRecipientsPerMessage() + " alıcı / mesaj",
                        "Öncelikli özellikler"
                ))
                .recommended(true)
                .build());

        plans.add(PlanResponse.builder()
                .id(SubscriptionPlan.PREMIUM.name())
                .name("Premium")
                .price(pricePremium)
                .priceLabel("₺/ay")
                .features(List.of(
                        SubscriptionPlan.PREMIUM.getMaxMessages() + " zamanlanmış mesaj",
                        "Fotoğraf, dosya & ses kaydı",
                        SubscriptionPlan.PREMIUM.getMaxRecipientsPerMessage() + " alıcı / mesaj",
                        "Tüm özellikler"
                ))
                .recommended(false)
                .build());

        return ResponseEntity.ok(plans);
    }

    /**
     * Aboneliği iptal et (plan FREE, bitiş tarihi temizlenir).
     */
    @PostMapping("/cancel")
    public ResponseEntity<Void> cancelSubscription(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        subscriptionPaymentService.cancelSubscription(userId);
        return ResponseEntity.ok().build();
    }
}
