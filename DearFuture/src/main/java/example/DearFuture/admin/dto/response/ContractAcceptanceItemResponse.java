package example.DearFuture.admin.dto.response;

import example.DearFuture.contract.ContractAcceptance;
import example.DearFuture.contract.ContractAcceptanceStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContractAcceptanceItemResponse {
    private Long id;
    private Long contractId;
    private String contractTitle;
    private Long userId;
    private String userEmail;
    private String userName;
    private String guestUserId;
    private ContractAcceptanceStatus status;
    private Integer acceptedVersion;
    private LocalDateTime acceptedAt;
    private String ipAddress;

    public static ContractAcceptanceItemResponse fromEntity(ContractAcceptance ca) {
        String email = ca.getUser() != null ? ca.getUser().getEmail() : null;
        String name = null;
        if (ca.getUser() != null) {
            String f = ca.getUser().getFirstName();
            String l = ca.getUser().getLastName();
            if (f != null && !f.isBlank()) {
                name = f + (l != null && !l.isBlank() ? " " + l : "");
            }
        }
        return ContractAcceptanceItemResponse.builder()
                .id(ca.getId())
                .contractId(ca.getContract() != null ? ca.getContract().getId() : null)
                .contractTitle(ca.getContract() != null ? ca.getContract().getTitle() : null)
                .userId(ca.getUser() != null ? ca.getUser().getId() : null)
                .userEmail(email)
                .userName(name)
                .guestUserId(ca.getGuestUserId())
                .status(ca.getStatus())
                .acceptedVersion(ca.getAcceptedVersion())
                .acceptedAt(ca.getAcceptedAt())
                .ipAddress(ca.getIpAddress())
                .build();
    }
}
