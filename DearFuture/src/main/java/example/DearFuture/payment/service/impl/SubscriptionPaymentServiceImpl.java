package example.DearFuture.payment.service.impl;

import com.iyzipay.model.*;
import com.iyzipay.request.CreateCheckoutFormInitializeRequest;
import com.iyzipay.request.CreatePaymentRequest;
import com.iyzipay.request.RetrieveCheckoutFormRequest;
import example.DearFuture.mail.*;
import example.DearFuture.payment.dto.request.InitializeCheckoutRequest;
import example.DearFuture.payment.dto.response.CheckoutInitializeResponse;
import example.DearFuture.payment.entity.SubscriptionPayment;
import example.DearFuture.payment.repository.SubscriptionPaymentRepository;
import example.DearFuture.payment.service.SubscriptionPaymentService;
import example.DearFuture.exception.security.UserNotFoundException;
import example.DearFuture.user.entity.SubscriptionPlan;
import example.DearFuture.user.entity.User;
import example.DearFuture.user.repository.SubscriptionPlanRepository;
import example.DearFuture.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class SubscriptionPaymentServiceImpl implements SubscriptionPaymentService {

    private final com.iyzipay.Options iyzicoOptions;
    private final SubscriptionPaymentRepository subscriptionPaymentRepository;
    private final UserRepository userRepository;
    private final SubscriptionPlanRepository planRepository;
    private final MailService mailService;

    @Value("${app.base-url:http://localhost:8080}")
    private String baseUrl;

    @Value("${app.frontend-url:http://localhost:5173}")
    private String frontendUrl;

    @Override
    @Transactional
    public CheckoutInitializeResponse initializeCheckout(User user, InitializeCheckoutRequest request) {
        SubscriptionPlan plan = planRepository.findByCode(request.getPlanCode().toUpperCase())
                .orElseThrow(() -> new IllegalArgumentException("Plan bulunamadı: " + request.getPlanCode()));

        if (plan.isFree()) {
            throw new IllegalArgumentException("Ücretli bir plan seçiniz.");
        }
        if (!plan.isActive()) {
            throw new IllegalArgumentException("Bu plan şu anda aktif değil.");
        }

        BigDecimal price = plan.getMonthlyPrice();

        String conversationId = "df-" + user.getId() + "-" + plan.getCode() + "-" + System.currentTimeMillis();

        SubscriptionPayment payment = SubscriptionPayment.builder()
                .user(user)
                .planCode(plan.getCode())
                .amount(price)
                .conversationId(conversationId)
                .status(SubscriptionPayment.PaymentStatus.PENDING)
                .renewal(false)
                .build();
        subscriptionPaymentRepository.save(payment);

        CreateCheckoutFormInitializeRequest req = new CreateCheckoutFormInitializeRequest();
        req.setLocale(Locale.TR.getValue());
        req.setConversationId(conversationId);
        req.setPrice(price);
        req.setPaidPrice(price);
        req.setCurrency(Currency.TRY.name());
        req.setBasketId("B" + payment.getId());
        req.setPaymentGroup(PaymentGroup.PRODUCT.name());
        req.setCallbackUrl(baseUrl.trim().replaceAll("/$", "") + "/api/subscription/callback");

        // Kullanıcının daha önce saklanan kartı varsa, checkout formunda göster
        if (user.getCardUserKey() != null && !user.getCardUserKey().isBlank()) {
            req.setCardUserKey(user.getCardUserKey());
        }

        DateTimeFormatter iyzicoDate = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        LocalDateTime now = LocalDateTime.now();
        String dateStr = now.format(iyzicoDate);
        String regDateStr = user.getCreatedAt() != null ? user.getCreatedAt().format(iyzicoDate) : dateStr;

        String firstName = (user.getFirstName() != null && !user.getFirstName().isBlank()) ? user.getFirstName().trim() : "Kullanıcı";
        String lastName = (user.getLastName() != null && !user.getLastName().isBlank()) ? user.getLastName().trim() : "DearFuture";

        Buyer buyer = new Buyer();
        buyer.setId("BY" + user.getId());
        buyer.setName(firstName);
        buyer.setSurname(lastName);
        buyer.setEmail(user.getEmail());
        buyer.setIdentityNumber("11111111111");
        buyer.setGsmNumber("+905350000000");
        buyer.setRegistrationAddress("Türkiye");
        buyer.setIp("85.34.78.112");
        buyer.setCity("Istanbul");
        buyer.setCountry("Turkey");
        buyer.setZipCode("34000");
        buyer.setRegistrationDate(regDateStr);
        buyer.setLastLoginDate(dateStr);
        req.setBuyer(buyer);

        Address address = new Address();
        address.setContactName(firstName + " " + lastName);
        address.setCity("Istanbul");
        address.setCountry("Turkey");
        address.setAddress("Türkiye");
        address.setZipCode("34000");
        req.setShippingAddress(address);
        req.setBillingAddress(address);

        List<BasketItem> basketItems = new ArrayList<>();
        BasketItem item = new BasketItem();
        item.setId("BI" + payment.getId());
        item.setName("Dear Future " + plan.getName() + " - Aylık Üyelik");
        item.setCategory1("Abonelik");
        item.setCategory2("Üyelik");
        item.setItemType(BasketItemType.VIRTUAL.name());
        item.setPrice(price);
        basketItems.add(item);
        req.setBasketItems(basketItems);

        com.iyzipay.model.CheckoutFormInitialize checkoutFormInitialize =
                com.iyzipay.model.CheckoutFormInitialize.create(req, iyzicoOptions);

        if (checkoutFormInitialize.getCheckoutFormContent() == null && checkoutFormInitialize.getPaymentPageUrl() == null) {
            log.warn("iyzico checkout initialize failed: {}", checkoutFormInitialize.getErrorMessage());
            throw new IllegalArgumentException("Ödeme sayfası oluşturulamadı: " + checkoutFormInitialize.getErrorMessage());
        }

        payment.setCheckoutToken(checkoutFormInitialize.getToken());
        subscriptionPaymentRepository.save(payment);

        return CheckoutInitializeResponse.builder()
                .paymentPageUrl(checkoutFormInitialize.getPaymentPageUrl())
                .checkoutFormContent(checkoutFormInitialize.getCheckoutFormContent())
                .token(checkoutFormInitialize.getToken())
                .build();
    }

    @Override
    @Transactional
    public String handleCallback(String token) {
        if (token == null || token.isBlank()) {
            return buildRedirectUrl(false, "Geçersiz ödeme.");
        }

        SubscriptionPayment payment = subscriptionPaymentRepository.findByCheckoutToken(token).orElse(null);
        if (payment == null) {
            log.warn("Payment not found for token: {}", token);
            return buildRedirectUrl(false, "Ödeme kaydı bulunamadı.");
        }

        RetrieveCheckoutFormRequest req = new RetrieveCheckoutFormRequest();
        req.setLocale(Locale.TR.getValue());
        req.setToken(token);
        req.setConversationId(payment.getConversationId());

        com.iyzipay.model.CheckoutForm result = com.iyzipay.model.CheckoutForm.retrieve(req, iyzicoOptions);

        if (!result.verifySignature(iyzicoOptions.getSecretKey())) {
            log.warn("Callback signature verification failed for token: {}", token);
            return buildRedirectUrl(false, "Güvenlik doğrulaması başarısız.");
        }

        if (payment.getStatus() == SubscriptionPayment.PaymentStatus.SUCCESS) {
            return buildRedirectUrl(true, null);
        }

        if (!"success".equalsIgnoreCase(result.getPaymentStatus())) {
            payment.setStatus(SubscriptionPayment.PaymentStatus.FAILED);
            subscriptionPaymentRepository.save(payment);
            return buildRedirectUrl(false, result.getErrorMessage() != null ? result.getErrorMessage() : "Ödeme başarısız.");
        }

        payment.setStatus(SubscriptionPayment.PaymentStatus.SUCCESS);
        payment.setIyzicoPaymentId(result.getPaymentId());
        payment.setPaidAt(LocalDateTime.now());
        payment.setPeriodStart(LocalDateTime.now());
        payment.setPeriodEnd(LocalDateTime.now().plusMonths(1));
        subscriptionPaymentRepository.save(payment);

        // Plan entity'sini bul ve kullanıcıya ata
        SubscriptionPlan plan = planRepository.findByCode(payment.getPlanCode()).orElse(null);
        User user = payment.getUser();
        if (plan != null) {
            user.setSubscriptionPlan(plan);
        }
        user.setSubscriptionEndsAt(payment.getPeriodEnd());

        // Kart bilgilerini sakla (tekrarlayan ödemeler için)
        if (result.getCardUserKey() != null && !result.getCardUserKey().isBlank()) {
            user.setCardUserKey(result.getCardUserKey());
        }
        if (result.getCardToken() != null && !result.getCardToken().isBlank()) {
            user.setCardToken(result.getCardToken());
        }

        // Başarısız ödeme sayacını sıfırla
        user.setConsecutivePaymentFailures(0);
        user.setPaymentFailedSince(null);

        userRepository.save(user);

        // Başarılı satın alım e-postası gönder
        String planName = plan != null ? plan.getName() : payment.getPlanCode();
        sendSubscriptionSuccessEmail(user, planName, payment.getAmount(), payment.getPeriodEnd(), false);

        log.info("Subscription activated: userId={}, plan={}, periodEnd={}", user.getId(), payment.getPlanCode(), payment.getPeriodEnd());
        return buildRedirectUrl(true, null);
    }

    @Override
    @Transactional
    public void cancelSubscription(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found: " + userId));
        SubscriptionPlan freePlan = planRepository.findByCode("FREE").orElse(null);
        user.setSubscriptionPlan(freePlan);
        user.setSubscriptionEndsAt(null);
        userRepository.save(user);
        log.info("Subscription cancelled: userId={}", userId);
    }

    @Override
    @Transactional
    public boolean processRecurringPayment(User user) {
        SubscriptionPlan plan = user.getSubscriptionPlan();
        if (plan == null || plan.isFree()) {
            log.warn("processRecurringPayment called for FREE user: userId={}", user.getId());
            return false;
        }

        if (user.getCardUserKey() == null || user.getCardUserKey().isBlank()
                || user.getCardToken() == null || user.getCardToken().isBlank()) {
            log.warn("No stored card for user: userId={}", user.getId());
            handlePaymentFailure(user, "Kayıtlı kart bulunamadı");
            return false;
        }

        BigDecimal price = plan.getMonthlyPrice();

        String conversationId = "df-renew-" + user.getId() + "-" + plan.getCode() + "-" + System.currentTimeMillis();

        // SubscriptionPayment kaydı oluştur (PENDING)
        SubscriptionPayment payment = SubscriptionPayment.builder()
                .user(user)
                .planCode(plan.getCode())
                .amount(price)
                .conversationId(conversationId)
                .status(SubscriptionPayment.PaymentStatus.PENDING)
                .renewal(true)
                .build();
        subscriptionPaymentRepository.save(payment);

        try {
            // iyzico CreatePaymentRequest ile saklanan kart üzerinden ödeme
            CreatePaymentRequest req = new CreatePaymentRequest();
            req.setLocale(Locale.TR.getValue());
            req.setConversationId(conversationId);
            req.setPrice(price);
            req.setPaidPrice(price);
            req.setCurrency(Currency.TRY.name());
            req.setInstallment(1);
            req.setBasketId("B" + payment.getId());
            req.setPaymentGroup(PaymentGroup.PRODUCT.name());
            req.setPaymentChannel(PaymentChannel.WEB.name());

            // Saklanan kart bilgileri
            PaymentCard paymentCard = new PaymentCard();
            paymentCard.setCardUserKey(user.getCardUserKey());
            paymentCard.setCardToken(user.getCardToken());
            req.setPaymentCard(paymentCard);

            // Buyer bilgileri
            DateTimeFormatter iyzicoDate = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            LocalDateTime now = LocalDateTime.now();
            String dateStr = now.format(iyzicoDate);
            String regDateStr = user.getCreatedAt() != null ? user.getCreatedAt().format(iyzicoDate) : dateStr;

            String firstName = (user.getFirstName() != null && !user.getFirstName().isBlank()) ? user.getFirstName().trim() : "Kullanıcı";
            String lastName = (user.getLastName() != null && !user.getLastName().isBlank()) ? user.getLastName().trim() : "DearFuture";

            Buyer buyer = new Buyer();
            buyer.setId("BY" + user.getId());
            buyer.setName(firstName);
            buyer.setSurname(lastName);
            buyer.setEmail(user.getEmail());
            buyer.setIdentityNumber("11111111111");
            buyer.setGsmNumber("+905350000000");
            buyer.setRegistrationAddress("Türkiye");
            buyer.setIp("85.34.78.112");
            buyer.setCity("Istanbul");
            buyer.setCountry("Turkey");
            buyer.setZipCode("34000");
            buyer.setRegistrationDate(regDateStr);
            buyer.setLastLoginDate(dateStr);
            req.setBuyer(buyer);

            Address address = new Address();
            address.setContactName(firstName + " " + lastName);
            address.setCity("Istanbul");
            address.setCountry("Turkey");
            address.setAddress("Türkiye");
            address.setZipCode("34000");
            req.setShippingAddress(address);
            req.setBillingAddress(address);

            List<BasketItem> basketItems = new ArrayList<>();
            BasketItem item = new BasketItem();
            item.setId("BI" + payment.getId());
            item.setName("Dear Future " + plan.getName() + " - Aylık Üyelik Yenileme");
            item.setCategory1("Abonelik");
            item.setCategory2("Üyelik");
            item.setItemType(BasketItemType.VIRTUAL.name());
            item.setPrice(price);
            basketItems.add(item);
            req.setBasketItems(basketItems);

            // Ödeme isteği gönder
            Payment result = Payment.create(req, iyzicoOptions);

            if ("success".equalsIgnoreCase(result.getStatus())) {
                // Ödeme başarılı
                payment.setStatus(SubscriptionPayment.PaymentStatus.SUCCESS);
                payment.setIyzicoPaymentId(result.getPaymentId());
                payment.setPaidAt(LocalDateTime.now());
                payment.setPeriodStart(LocalDateTime.now());
                payment.setPeriodEnd(LocalDateTime.now().plusMonths(1));
                subscriptionPaymentRepository.save(payment);

                user.setSubscriptionEndsAt(payment.getPeriodEnd());
                user.setConsecutivePaymentFailures(0);
                user.setPaymentFailedSince(null);
                userRepository.save(user);

                // Yenileme başarı e-postası gönder
                sendSubscriptionSuccessEmail(user, plan.getName(), price, payment.getPeriodEnd(), true);

                log.info("Recurring payment successful: userId={}, plan={}, periodEnd={}",
                        user.getId(), plan.getCode(), payment.getPeriodEnd());
                return true;
            } else {
                // Ödeme başarısız
                payment.setStatus(SubscriptionPayment.PaymentStatus.FAILED);
                subscriptionPaymentRepository.save(payment);

                String errorMsg = result.getErrorMessage() != null ? result.getErrorMessage() : "Ödeme başarısız";
                handlePaymentFailure(user, errorMsg);

                log.warn("Recurring payment failed: userId={}, error={}", user.getId(), errorMsg);
                return false;
            }
        } catch (Exception e) {
            payment.setStatus(SubscriptionPayment.PaymentStatus.FAILED);
            subscriptionPaymentRepository.save(payment);

            handlePaymentFailure(user, "Ödeme işlemi sırasında hata oluştu");

            log.error("Recurring payment exception: userId={}", user.getId(), e);
            return false;
        }
    }

    /**
     * Başarısız ödeme durumunu günceller ve e-posta gönderir.
     */
    private void handlePaymentFailure(User user, String reason) {
        user.setConsecutivePaymentFailures(user.getConsecutivePaymentFailures() + 1);
        if (user.getPaymentFailedSince() == null) {
            user.setPaymentFailedSince(LocalDateTime.now());
        }
        userRepository.save(user);

        int attemptCount = user.getConsecutivePaymentFailures();
        int daysLeft = Math.max(0, 7 - attemptCount);
        String planName = user.getSubscriptionPlan() != null ? user.getSubscriptionPlan().getName() : "Bilinmeyen";

        String htmlBody = PaymentFailedEmailTemplate.build(planName, reason, daysLeft, attemptCount);
        EmailMessage emailMessage = EmailMessage.builder()
                .toEmail(user.getEmail())
                .subject("Dear Future - Ödeme Başarısız")
                .body(htmlBody)
                .isHtml(true)
                .build();
        mailService.enqueueEmail(emailMessage);

        log.info("Payment failure email sent: userId={}, attempt={}/7", user.getId(), attemptCount);
    }

    /**
     * Abonelik başarılı/yenileme e-postası gönderir.
     */
    private void sendSubscriptionSuccessEmail(User user, String planName, BigDecimal amount,
                                               LocalDateTime nextPayDate, boolean isRenewal) {
        String actionLabel = isRenewal ? "yenilendi" : "aktif edildi";
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMMM yyyy",
                new java.util.Locale("tr", "TR"));
        String nextPayDateStr = nextPayDate.format(formatter);

        String htmlBody = SubscriptionSuccessEmailTemplate.build(planName, actionLabel,
                amount.stripTrailingZeros().toPlainString(), nextPayDateStr);
        EmailMessage emailMessage = EmailMessage.builder()
                .toEmail(user.getEmail())
                .subject("Dear Future - Abonelik " + (isRenewal ? "Yenilendi" : "Aktif Edildi"))
                .body(htmlBody)
                .isHtml(true)
                .build();
        mailService.enqueueEmail(emailMessage);

        log.info("Subscription success email sent: userId={}, plan={}, renewal={}",
                user.getId(), planName, isRenewal);
    }

    /**
     * Abonelik iptal e-postası gönderir.
     */
    void sendSubscriptionCancelledEmail(User user, String planName) {
        String htmlBody = SubscriptionCancelledEmailTemplate.build(planName);
        EmailMessage emailMessage = EmailMessage.builder()
                .toEmail(user.getEmail())
                .subject("Dear Future - Abonelik İptal Edildi")
                .body(htmlBody)
                .isHtml(true)
                .build();
        mailService.enqueueEmail(emailMessage);

        log.info("Subscription cancelled email sent: userId={}, plan={}", user.getId(), planName);
    }

    private String buildRedirectUrl(boolean success, String message) {
        String base = frontendUrl != null && !frontendUrl.isBlank() ? frontendUrl.trim().replaceAll("/$", "") : baseUrl;
        String path = success ? "/change-subscription?success=1" : "/change-subscription?success=0&message=" + (message != null ? java.net.URLEncoder.encode(message, java.nio.charset.StandardCharsets.UTF_8) : "");
        return base + path;
    }
}
