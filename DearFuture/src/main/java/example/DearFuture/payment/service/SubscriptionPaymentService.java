package example.DearFuture.payment.service;

import example.DearFuture.payment.dto.request.InitializeCheckoutRequest;
import example.DearFuture.payment.dto.response.CheckoutInitializeResponse;
import example.DearFuture.user.entity.User;

public interface SubscriptionPaymentService {

    /**
     * Üyelik ödemesi için iyzico checkout başlatır. Sadece giriş yapmış kullanıcı çağırabilir.
     */
    CheckoutInitializeResponse initializeCheckout(User user, InitializeCheckoutRequest request);

    /**
     * iyzico callback: token ile ödeme sonucunu alır, başarılıysa aboneliği aktif eder.
     * Redirect URL döner (başarı/hata sayfası).
     */
    String handleCallback(String token);

    /**
     * Aboneliği iptal eder: plan FREE yapılır, bitiş tarihi temizlenir.
     */
    void cancelSubscription(Long userId);

    /**
     * Saklanan kart ile otomatik tekrarlayan ödeme alır.
     * @return true ise ödeme başarılı, false ise başarısız
     */
    boolean processRecurringPayment(User user);
}
