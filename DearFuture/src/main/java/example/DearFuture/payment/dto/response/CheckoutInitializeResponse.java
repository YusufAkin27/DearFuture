package example.DearFuture.payment.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CheckoutInitializeResponse {

    /** iyzico ödeme sayfasına yönlendirme URL'i */
    private String paymentPageUrl;
    /** Alternatif: checkout form HTML içeriği (sayfada göstermek için) */
    private String checkoutFormContent;
    private String token;
}
