package example.DearFuture.admin.dto.request;

import example.DearFuture.contract.ContractType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ContractCreateRequest {
    @NotNull(message = "Sözleşme türü gerekli")
    private ContractType type;
    @NotBlank(message = "Başlık gerekli")
    private String title;
    @NotBlank(message = "İçerik gerekli")
    private String content;
    private Boolean requiredApproval = true;
}
