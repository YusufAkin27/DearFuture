package example.DearFuture.payment.service.impl;

import com.iyzipay.model.*;
import com.iyzipay.request.CreateCheckoutFormInitializeRequest;
import com.iyzipay.request.RetrieveCheckoutFormRequest;
import example.DearFuture.payment.dto.request.InitializeCheckoutRequest;
import example.DearFuture.payment.dto.response.CheckoutInitializeResponse;
import example.DearFuture.payment.entity.SubscriptionPayment;
import example.DearFuture.payment.repository.SubscriptionPaymentRepository;
import example.DearFuture.payment.service.SubscriptionPaymentService;
import example.DearFuture.exception.security.UserNotFoundException;
import example.DearFuture.user.entity.SubscriptionPlan;
import example.DearFuture.user.entity.User;
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

    @Value("${app.base-url:http://localhost:8080}")
    private String baseUrl;

    @Value("${app.frontend-url:http://localhost:5173}")
    private String frontendUrl;

    @Value("${app.subscription.price.plus:100}")
    private int pricePlus;

    @Value("${app.subscription.price.premium:150}")
    private int pricePremium;

    @Override
    @Transactional
    public CheckoutInitializeResponse initializeCheckout(User user, InitializeCheckoutRequest request) {
        SubscriptionPlan plan = request.getPlan();
        if (plan == null || plan == SubscriptionPlan.FREE) {
            throw new IllegalArgumentException("PLUS veya PREMIUM planı seçiniz.");
        }

        BigDecimal price = plan == SubscriptionPlan.PLUS
                ? BigDecimal.valueOf(pricePlus)
                : BigDecimal.valueOf(pricePremium);

        String conversationId = "df-" + user.getId() + "-" + plan.name() + "-" + System.currentTimeMillis();

        SubscriptionPayment payment = SubscriptionPayment.builder()
                .user(user)
                .plan(plan)
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
        item.setName("Dear Future " + plan.name() + " - Aylık Üyelik");
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
            throw new RuntimeException("Ödeme sayfası oluşturulamadı: " + checkoutFormInitialize.getErrorMessage());
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

        User user = payment.getUser();
        user.setSubscriptionPlan(payment.getPlan());
        user.setSubscriptionEndsAt(payment.getPeriodEnd());
        userRepository.save(user);

        log.info("Subscription activated: userId={}, plan={}, periodEnd={}", user.getId(), payment.getPlan(), payment.getPeriodEnd());
        return buildRedirectUrl(true, null);
    }

    @Override
    @Transactional
    public void cancelSubscription(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found: " + userId));
        user.setSubscriptionPlan(SubscriptionPlan.FREE);
        user.setSubscriptionEndsAt(null);
        userRepository.save(user);
        log.info("Subscription cancelled: userId={}", userId);
    }

    private String buildRedirectUrl(boolean success, String message) {
        String base = frontendUrl != null && !frontendUrl.isBlank() ? frontendUrl.trim().replaceAll("/$", "") : baseUrl;
        String path = success ? "/change-subscription?success=1" : "/change-subscription?success=0&message=" + (message != null ? java.net.URLEncoder.encode(message, java.nio.charset.StandardCharsets.UTF_8) : "");
        return base + path;
    }
}
