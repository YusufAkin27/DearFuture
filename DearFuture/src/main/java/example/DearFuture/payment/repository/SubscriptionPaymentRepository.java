package example.DearFuture.payment.repository;

import example.DearFuture.payment.entity.SubscriptionPayment;
import example.DearFuture.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SubscriptionPaymentRepository extends JpaRepository<SubscriptionPayment, Long> {

    List<SubscriptionPayment> findByUser(User user);

    /** Kullanıcının son başarılı ödemesi (dönem bilgisi için). */
    Optional<SubscriptionPayment> findTopByUserIdAndStatusOrderByPaidAtDesc(Long userId, SubscriptionPayment.PaymentStatus status);

    Page<SubscriptionPayment> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    Optional<SubscriptionPayment> findByConversationId(String conversationId);

    Optional<SubscriptionPayment> findByCheckoutToken(String checkoutToken);

    boolean existsByConversationId(String conversationId);
}
