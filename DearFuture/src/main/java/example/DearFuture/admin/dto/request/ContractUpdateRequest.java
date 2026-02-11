package example.DearFuture.admin.dto.request;

import lombok.Data;

@Data
public class ContractUpdateRequest {
    private String title;
    private String content;
    private Boolean active;
    private Boolean requiredApproval;
}
