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
import example.DearFuture.user.dto.response.UserResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface AdminService {

    // ── Dashboard ──
    DashboardStatsResponse getDashboardStats();

    // ── Kullanıcı Yönetimi ──
    List<UserResponse> getAllUsers();
    UserResponse getUser(Long userId);
    void deleteUser(Long userId);
    void toggleUserEnabled(Long userId, boolean enabled);
    void changeUserPlan(Long userId, String planCode);

    // ── Plan Yönetimi ──
    List<PlanDetailResponse> getAllPlans();
    PlanDetailResponse getPlan(Long planId);
    PlanDetailResponse updatePlan(Long planId, UpdatePlanRequest request);

    // ── Mesaj Yönetimi ──
    List<AdminMessageResponse> getAllMessages();
    List<AdminMessageResponse> getMessagesByUser(Long userId);
    void deleteMessage(Long messageId);

    // ── Sözleşme Yönetimi ──
    List<Contract> getAllContracts();
    Contract getContract(Long id);
    Contract createContract(ContractCreateRequest request);
    Contract updateContract(Long id, ContractUpdateRequest request);
    void deleteContract(Long id);
    Page<ContractAcceptanceItemResponse> getContractAcceptances(Long contractId, Pageable pageable);

    // ── Çerez Tercihleri (kim onaylamış) ──
    Page<CookiePreferenceItemResponse> getCookiePreferences(Pageable pageable);
}
