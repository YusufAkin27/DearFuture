package example.DearFuture.payment.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlanResponse {

    /** Plan kodu: FREE, PLUS, PREMIUM */
    private String id;

    /** Türkçe plan adı */
    private String name;

    /** Aylık fiyat (TL); FREE için 0 */
    private int price;

    /** Fiyat etiketi, örn. "₺/ay" */
    private String priceLabel;

    /** Özellik listesi (Türkçe) */
    private List<String> features;

    /** Öne çıkarılan plan mı */
    private boolean recommended;
}
