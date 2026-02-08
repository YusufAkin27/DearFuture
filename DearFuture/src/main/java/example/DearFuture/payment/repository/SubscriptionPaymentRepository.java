package example.DearFuture.payment.repository;

import example.DearFuture.payment.entity.SubscriptionPayment;
import example.DearFuture.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SubscriptionPaymentRepository extends JpaRepository<SubscriptionPayment, Long> {

    List<SubscriptionPayment> findByUser(User user);

    Optional<SubscriptionPayment> findByConversationId(String conversationId);

    Optional<SubscriptionPayment> findByCheckoutToken(String checkoutToken);

    boolean existsByConversationId(String conversationId);
}
