package example.DearFuture.admin.service;

import example.DearFuture.admin.dto.request.ContractCreateRequest;
import example.DearFuture.admin.dto.request.ContractUpdateRequest;
import example.DearFuture.admin.dto.request.UpdatePlanRequest;
import example.DearFuture.admin.dto.response.AdminMessageResponse;
import example.DearFuture.admin.dto.response.ContractAcceptanceItemResponse;
import example.DearFuture.admin.dto.response.CookiePreferenceItemResponse;
import example.DearFuture.admin.dto.response.DashboardStatsResponse;
import example.DearFuture.admin.dto.response.PlanDetailResponse;
import example.DearFuture.contract.Contract;
import example.DearFuture.contract.ContractAcceptanceRepository;
import example.DearFuture.contract.ContractRepository;
import example.DearFuture.contract.ContractService;
import example.DearFuture.cookie.repository.CookiePreferenceRepository;
import example.DearFuture.exception.security.UserNotFoundException;
import example.DearFuture.message.entity.FutureMessage;
import example.DearFuture.message.entity.MessageStatus;
import example.DearFuture.message.repository.FutureMessageRepository;
import example.DearFuture.payment.repository.SubscriptionPaymentRepository;
import example.DearFuture.user.dto.response.UserResponse;
import example.DearFuture.user.entity.SubscriptionPlan;
import example.DearFuture.user.entity.User;
import example.DearFuture.user.repository.SubscriptionPlanRepository;
import example.DearFuture.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminServiceImpl implements AdminService {

    private final UserRepository userRepository;
    private final SubscriptionPlanRepository planRepository;
    private final FutureMessageRepository messageRepository;
    private final SubscriptionPaymentRepository paymentRepository;
    private final ContractRepository contractRepository;
    private final ContractAcceptanceRepository contractAcceptanceRepository;
    private final ContractService contractService;
    private final CookiePreferenceRepository cookiePreferenceRepository;

    // ════════════════════════════════════════
    //  Dashboard
    // ════════════════════════════════════════

    @Override
    @Transactional(readOnly = true)
    public DashboardStatsResponse getDashboardStats() {
        long totalUsers = userRepository.count();
        long totalMessages = messageRepository.count();
        long totalPayments = paymentRepository.count();

        // Plan bazında kullanıcı sayıları
        Map<String, Long> usersPerPlan = new LinkedHashMap<>();
        List<SubscriptionPlan> allPlans = planRepository.findAllByOrderByDisplayOrderAsc();
        for (SubscriptionPlan plan : allPlans) {
            long count = userRepository.countBySubscriptionPlan(plan);
            usersPerPlan.put(plan.getCode(), count);
        }
        // Plan atanmamış kullanıcılar
        long unassigned = totalUsers - usersPerPlan.values().stream().mapToLong(Long::longValue).sum();
        if (unassigned > 0) {
            usersPerPlan.put("UNASSIGNED", unassigned);
        }

        // Mesaj durumlarına göre sayılar
        Map<String, Long> messagesPerStatus = new LinkedHashMap<>();
        for (MessageStatus status : MessageStatus.values()) {
            // Use a simple count approach
            messagesPerStatus.put(status.name(), 0L);
        }
        List<FutureMessage> allMessages = messageRepository.findAll();
        for (FutureMessage msg : allMessages) {
            messagesPerStatus.merge(msg.getStatus().name(), 1L, Long::sum);
        }

        return DashboardStatsResponse.builder()
                .totalUsers(totalUsers)
                .totalMessages(totalMessages)
                .totalPayments(totalPayments)
                .usersPerPlan(usersPerPlan)
                .messagesPerStatus(messagesPerStatus)
                .build();
    }

    // ════════════════════════════════════════
    //  Kullanıcı Yönetimi
    // ════════════════════════════════════════

    @Override
    @Transactional(readOnly = true)
    public List<UserResponse> getAllUsers() {
        return userRepository.findAll().stream()
                .map(UserResponse::fromUser)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("Kullanıcı bulunamadı: " + userId));
        return UserResponse.fromUser(user);
    }

    @Override
    @Transactional
    public void deleteUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("Kullanıcı bulunamadı: " + userId));
        userRepository.delete(user);
        log.info("Admin deleted user: userId={}, email={}", userId, user.getEmail());
    }

    @Override
    @Transactional
    public void toggleUserEnabled(Long userId, boolean enabled) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("Kullanıcı bulunamadı: " + userId));
        user.setEnabled(enabled);
        userRepository.save(user);
        log.info("Admin toggled user enabled: userId={}, enabled={}", userId, enabled);
    }

    @Override
    @Transactional
    public void changeUserPlan(Long userId, String planCode) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("Kullanıcı bulunamadı: " + userId));
        SubscriptionPlan plan = planRepository.findByCode(planCode.toUpperCase())
                .orElseThrow(() -> new IllegalArgumentException("Plan bulunamadı: " + planCode));
        user.setSubscriptionPlan(plan);
        if (plan.isFree()) {
            user.setSubscriptionEndsAt(null);
        }
        userRepository.save(user);
        log.info("Admin changed user plan: userId={}, newPlan={}", userId, planCode);
    }

    // ════════════════════════════════════════
    //  Plan Yönetimi
    // ════════════════════════════════════════

    @Override
    @Transactional(readOnly = true)
    public List<PlanDetailResponse> getAllPlans() {
        List<SubscriptionPlan> plans = planRepository.findAllByOrderByDisplayOrderAsc();
        return plans.stream().map(plan -> {
            long userCount = userRepository.countBySubscriptionPlan(plan);
            return PlanDetailResponse.fromEntity(plan, userCount);
        }).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public PlanDetailResponse getPlan(Long planId) {
        SubscriptionPlan plan = planRepository.findById(planId)
                .orElseThrow(() -> new IllegalArgumentException("Plan bulunamadı: " + planId));
        long userCount = userRepository.countBySubscriptionPlan(plan);
        return PlanDetailResponse.fromEntity(plan, userCount);
    }

    @Override
    @Transactional
    public PlanDetailResponse updatePlan(Long planId, UpdatePlanRequest request) {
        SubscriptionPlan plan = planRepository.findById(planId)
                .orElseThrow(() -> new IllegalArgumentException("Plan bulunamadı: " + planId));

        if (request.getName() != null) plan.setName(request.getName());
        if (request.getDescription() != null) plan.setDescription(request.getDescription());
        if (request.getMonthlyPrice() != null) plan.setMonthlyPrice(request.getMonthlyPrice());
        if (request.getPriceLabel() != null) plan.setPriceLabel(request.getPriceLabel());
        if (request.getMaxMessages() != null) plan.setMaxMessages(request.getMaxMessages());
        if (request.getMaxRecipientsPerMessage() != null) plan.setMaxRecipientsPerMessage(request.getMaxRecipientsPerMessage());
        if (request.getAllowPhoto() != null) plan.setAllowPhoto(request.getAllowPhoto());
        if (request.getAllowFile() != null) plan.setAllowFile(request.getAllowFile());
        if (request.getAllowVoice() != null) plan.setAllowVoice(request.getAllowVoice());
        if (request.getMaxPhotosPerMessage() != null) plan.setMaxPhotosPerMessage(request.getMaxPhotosPerMessage());
        if (request.getMaxPhotoSizeBytes() != null) plan.setMaxPhotoSizeBytes(request.getMaxPhotoSizeBytes());
        if (request.getMaxFilesPerMessage() != null) plan.setMaxFilesPerMessage(request.getMaxFilesPerMessage());
        if (request.getMaxFileSizeBytes() != null) plan.setMaxFileSizeBytes(request.getMaxFileSizeBytes());
        if (request.getFeatures() != null) plan.setFeatures(request.getFeatures());
        if (request.getRecommended() != null) plan.setRecommended(request.getRecommended());
        if (request.getActive() != null) plan.setActive(request.getActive());
        if (request.getDisplayOrder() != null) plan.setDisplayOrder(request.getDisplayOrder());

        planRepository.save(plan);
        long userCount = userRepository.countBySubscriptionPlan(plan);

        log.info("Admin updated plan: planId={}, code={}", planId, plan.getCode());
        return PlanDetailResponse.fromEntity(plan, userCount);
    }

    // ════════════════════════════════════════
    //  Mesaj Yönetimi
    // ════════════════════════════════════════

    @Override
    @Transactional(readOnly = true)
    public List<AdminMessageResponse> getAllMessages() {
        return messageRepository.findAll().stream()
                .map(AdminMessageResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<AdminMessageResponse> getMessagesByUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("Kullanıcı bulunamadı: " + userId));
        return messageRepository.findAllByUser(user).stream()
                .map(AdminMessageResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deleteMessage(Long messageId) {
        FutureMessage message = messageRepository.findById(messageId)
                .orElseThrow(() -> new IllegalArgumentException("Mesaj bulunamadı: " + messageId));
        messageRepository.delete(message);
        log.info("Admin deleted message: messageId={}", messageId);
    }

    // ════════════════════════════════════════
    //  Sözleşme Yönetimi
    // ════════════════════════════════════════

    @Override
    @Transactional(readOnly = true)
    public List<Contract> getAllContracts() {
        return contractRepository.findAll().stream()
                .sorted(Comparator.comparing(Contract::getType).thenComparing(Contract::getVersion, Comparator.reverseOrder()))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Contract getContract(Long id) {
        return contractRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Sözleşme bulunamadı: " + id));
    }

    @Override
    @Transactional
    public Contract createContract(ContractCreateRequest request) {
        return contractService.createContract(
                request.getType(),
                request.getTitle(),
                request.getContent(),
                request.getRequiredApproval());
    }

    @Override
    @Transactional
    public Contract updateContract(Long id, ContractUpdateRequest request) {
        return contractService.updateContract(
                id,
                request.getTitle(),
                request.getContent(),
                request.getActive(),
                request.getRequiredApproval());
    }

    @Override
    @Transactional
    public void deleteContract(Long id) {
        contractService.deleteContract(id);
        log.info("Admin deleted contract: id={}", id);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ContractAcceptanceItemResponse> getContractAcceptances(Long contractId, Pageable pageable) {
        return contractAcceptanceRepository.findByContractIdOrderByAcceptedAtDesc(contractId, pageable)
                .map(ContractAcceptanceItemResponse::fromEntity);
    }

    // ════════════════════════════════════════
    //  Çerez Tercihleri (kim onaylamış)
    // ════════════════════════════════════════

    @Override
    @Transactional(readOnly = true)
    public Page<CookiePreferenceItemResponse> getCookiePreferences(Pageable pageable) {
        return cookiePreferenceRepository.findAll(pageable)
                .map(CookiePreferenceItemResponse::fromEntity);
    }
}
