package example.DearFuture.payment.controller;

import example.DearFuture.exception.security.UserNotFoundException;
import example.DearFuture.payment.dto.request.InitializeCheckoutRequest;
import example.DearFuture.payment.dto.response.CheckoutInitializeResponse;
import example.DearFuture.payment.dto.response.PlanResponse;
import example.DearFuture.payment.service.SubscriptionPaymentService;
import example.DearFuture.user.entity.SubscriptionPlan;
import example.DearFuture.user.entity.User;
import example.DearFuture.user.repository.SubscriptionPlanRepository;
import example.DearFuture.user.repository.UserRepository;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/subscription")
@RequiredArgsConstructor
public class SubscriptionController {

    private final SubscriptionPaymentService subscriptionPaymentService;
    private final UserRepository userRepository;
    private final SubscriptionPlanRepository planRepository;

    /**
     * Üyelik ödemesi başlat. Plan kodu (PLUS, PREMIUM vb.) gönderilir.
     * Fiyat bilgisi veritabanındaki plan entity'sinden alınır.
     */
    @PostMapping("/checkout/initialize")
    public ResponseEntity<CheckoutInitializeResponse> initializeCheckout(
            @Valid @RequestBody InitializeCheckoutRequest request,
            Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("Kullanıcı bulunamadı"));
        CheckoutInitializeResponse response = subscriptionPaymentService.initializeCheckout(user, request);
        return ResponseEntity.ok(response);
    }

    /**
     * iyzico ödeme sonrası callback. GET (redirect) veya POST ile token gelir.
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
     * Fiyatlandırma sayfası için aktif plan listesi. Giriş gerekmez.
     * Planlar veritabanından yüklenir (admin panelinden yönetilir).
     */
    @GetMapping("/plans")
    public ResponseEntity<List<PlanResponse>> getPlans() {
        List<SubscriptionPlan> activePlans = planRepository.findByActiveTrueOrderByDisplayOrderAsc();
        List<PlanResponse> plans = activePlans.stream()
                .map(p -> PlanResponse.builder()
                        .id(p.getCode())
                        .name(p.getName())
                        .description(p.getDescription())
                        .price(p.getMonthlyPrice().intValue())
                        .priceLabel(p.getPriceLabel() != null ? p.getPriceLabel() : "₺/ay")
                        .features(p.getFeatures())
                        .recommended(p.isRecommended())
                        .build())
                .collect(Collectors.toList());
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
