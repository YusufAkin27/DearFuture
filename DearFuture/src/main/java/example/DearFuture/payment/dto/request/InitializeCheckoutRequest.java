package example.DearFuture.payment.dto.request;

import example.DearFuture.user.entity.SubscriptionPlan;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class InitializeCheckoutRequest {

    @NotNull(message = "Plan seçiniz (PLUS veya PREMIUM)")
    private SubscriptionPlan plan;
}
