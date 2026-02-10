package example.DearFuture.payment.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class InitializeCheckoutRequest {

    @NotBlank(message = "Plan kodu seçiniz (PLUS, PREMIUM vb.)")
    private String planCode;
}
