package example.DearFuture.user.dto.request;

import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class UpdateSettingsRequest {

    /** Dil kodu: tr, en */
    @Pattern(regexp = "tr|en", message = "Geçerli dil seçiniz (tr, en)")
    private String locale;

    private Boolean emailNotifications;

    private Boolean marketingEmails;
}
