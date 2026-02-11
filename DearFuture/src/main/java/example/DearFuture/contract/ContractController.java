package example.DearFuture.contract;

import example.DearFuture.user.entity.User;
import example.DearFuture.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Public sözleşme endpoint'leri.
 * Kullanıcılar sözleşmeleri görüntüleyebilir ve onaylayabilir.
 */
@RestController
@RequestMapping("/api/contracts")
@RequiredArgsConstructor
@Slf4j
public class ContractController {

    private final ContractService contractService;
    private final ContractRepository contractRepository;
    private final UserRepository userRepository;

    @GetMapping
    public ResponseEntity<List<Contract>> getAllActiveContracts() {
        List<Contract> contracts = contractService.getAllActiveContracts();
        return ResponseEntity.ok(contracts);
    }

    @GetMapping("/type/{type}")
    public ResponseEntity<Contract> getContractByType(@PathVariable ContractType type) {
        try {
            Contract contract = contractService.getLatestActiveContractByType(type);
            return ResponseEntity.ok(contract);
        } catch (Exception e) {
            log.error("Sözleşme getirilirken hata: {}", e.getMessage(), e);
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Contract> getContractById(@PathVariable Long id) {
        return contractRepository.findById(id)
                .filter(Contract::getActive)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}/status")
    public ResponseEntity<Map<String, Object>> getContractStatus(
            @PathVariable Long id,
            @AuthenticationPrincipal Object principal,
            @RequestParam(value = "guestUserId", required = false) String guestUserId) {
        try {
            Long userId = principal != null ? (Long) principal : null;
            boolean isAccepted = contractService.isContractAccepted(id, userId, guestUserId);
            Map<String, Object> status = new HashMap<>();
            status.put("accepted", isAccepted);
            status.put("contractId", id);
            return ResponseEntity.ok(status);
        } catch (Exception e) {
            log.error("Onay durumu kontrol edilirken hata: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(Map.of("error", "Onay durumu kontrol edilemedi: " + e.getMessage()));
        }
    }

    @GetMapping("/type/{type}/status")
    public ResponseEntity<Map<String, Object>> getContractTypeStatus(
            @PathVariable ContractType type,
            @AuthenticationPrincipal Object principal,
            @RequestParam(value = "guestUserId", required = false) String guestUserId) {
        try {
            Long userId = principal != null ? (Long) principal : null;
            boolean isAccepted = contractService.isContractTypeAccepted(type, userId, guestUserId);
            Map<String, Object> status = new HashMap<>();
            status.put("accepted", isAccepted);
            status.put("contractType", type.name());
            return ResponseEntity.ok(Map.of("message", "Onay durumu getirildi", "data", status));
        } catch (Exception e) {
            log.error("Onay durumu kontrol edilirken hata: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(Map.of("error", "Onay durumu kontrol edilemedi: " + e.getMessage()));
        }
    }

    @PostMapping("/{id}/accept")
    public ResponseEntity<?> acceptContract(
            @PathVariable Long id,
            @AuthenticationPrincipal Object principal,
            @RequestParam(value = "guestUserId", required = false) String guestUserId,
            HttpServletRequest request) {
        Long userId = principal != null ? (Long) principal : null;
        if (userId == null && (guestUserId == null || guestUserId.isBlank())) {
            return ResponseEntity.badRequest().body(Map.of("error", "Kullanıcı bilgisi bulunamadı"));
        }
        User user = userId != null ? userRepository.findById(userId).orElse(null) : null;
        try {
            ContractAcceptance acceptance = contractService.acceptContract(id, user, guestUserId, request);
            return ResponseEntity.ok(Map.of("message", "Sözleşme başarıyla onaylandı", "data", acceptance));
        } catch (Exception e) {
            log.error("Sözleşme onaylanırken hata: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(Map.of("error", "Sözleşme onaylanamadı: " + e.getMessage()));
        }
    }

    @PostMapping("/type/{type}/accept")
    public ResponseEntity<?> acceptContractByType(
            @PathVariable ContractType type,
            @AuthenticationPrincipal Object principal,
            @RequestParam(value = "guestUserId", required = false) String guestUserId,
            HttpServletRequest request) {
        Long userId = principal != null ? (Long) principal : null;
        if (userId == null && (guestUserId == null || guestUserId.isBlank())) {
            return ResponseEntity.badRequest().body(Map.of("error", "Kullanıcı bilgisi bulunamadı"));
        }
        User user = userId != null ? userRepository.findById(userId).orElse(null) : null;
        try {
            Contract contract = contractService.getLatestActiveContractByType(type);
            ContractAcceptance acceptance = contractService.acceptContract(contract.getId(), user, guestUserId, request);
            return ResponseEntity.ok(Map.of("message", "Sözleşme başarıyla onaylandı", "data", acceptance));
        } catch (Exception e) {
            log.error("Sözleşme onaylanırken hata: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(Map.of("error", "Sözleşme onaylanamadı: " + e.getMessage()));
        }
    }

    @PostMapping("/{id}/reject")
    public ResponseEntity<?> rejectContract(
            @PathVariable Long id,
            @AuthenticationPrincipal Object principal,
            @RequestParam(value = "guestUserId", required = false) String guestUserId,
            HttpServletRequest request) {
        Long userId = principal != null ? (Long) principal : null;
        if (userId == null && (guestUserId == null || guestUserId.isBlank())) {
            return ResponseEntity.badRequest().body(Map.of("error", "Kullanıcı bilgisi bulunamadı"));
        }
        User user = userId != null ? userRepository.findById(userId).orElse(null) : null;
        try {
            ContractAcceptance rejection = contractService.rejectContract(id, user, guestUserId, request);
            return ResponseEntity.ok(Map.of("message", "Sözleşme reddedildi", "data", rejection));
        } catch (Exception e) {
            log.error("Sözleşme reddedilirken hata: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(Map.of("error", "Sözleşme reddedilemedi: " + e.getMessage()));
        }
    }

    @PostMapping("/type/{type}/reject")
    public ResponseEntity<?> rejectContractByType(
            @PathVariable ContractType type,
            @AuthenticationPrincipal Object principal,
            @RequestParam(value = "guestUserId", required = false) String guestUserId,
            HttpServletRequest request) {
        Long userId = principal != null ? (Long) principal : null;
        if (userId == null && (guestUserId == null || guestUserId.isBlank())) {
            return ResponseEntity.badRequest().body(Map.of("error", "Kullanıcı bilgisi bulunamadı"));
        }
        User user = userId != null ? userRepository.findById(userId).orElse(null) : null;
        try {
            Contract contract = contractService.getLatestActiveContractByType(type);
            ContractAcceptance rejection = contractService.rejectContract(contract.getId(), user, guestUserId, request);
            return ResponseEntity.ok(Map.of("message", "Sözleşme reddedildi", "data", rejection));
        } catch (Exception e) {
            log.error("Sözleşme reddedilirken hata: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(Map.of("error", "Sözleşme reddedilemedi: " + e.getMessage()));
        }
    }

    @GetMapping("/my-history")
    public ResponseEntity<?> getMyContractHistory(
            @AuthenticationPrincipal Object principal,
            @RequestParam(value = "guestUserId", required = false) String guestUserId) {
        Long userId = principal != null ? (Long) principal : null;
        if (userId == null && (guestUserId == null || guestUserId.isBlank())) {
            return ResponseEntity.badRequest().body(Map.of("error", "Kullanıcı bilgisi bulunamadı"));
        }
        try {
            List<ContractAcceptance> history = contractService.getUserAcceptances(userId, guestUserId);
            return ResponseEntity.ok(Map.of("message", "Sözleşme geçmişi getirildi", "data", history));
        } catch (Exception e) {
            log.error("Sözleşme geçmişi getirilirken hata: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(Map.of("error", "Sözleşme geçmişi getirilemedi: " + e.getMessage()));
        }
    }
}
