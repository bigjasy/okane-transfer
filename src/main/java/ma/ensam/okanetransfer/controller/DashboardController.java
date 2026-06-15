package ma.ensam.okanetransfer.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ma.ensam.okanetransfer.dto.dashboard.DashboardSummaryResponse;
import ma.ensam.okanetransfer.service.DashboardService;

@RestController
@RequestMapping("/api/v1/dashboards")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<DashboardSummaryResponse> getAdminDashboard() {
        return ResponseEntity.ok(dashboardService.getAdminDashboard());
    }

    @GetMapping("/manager")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<DashboardSummaryResponse> getManagerDashboard(
            @AuthenticationPrincipal UserDetails currentUser) {
        return ResponseEntity.ok(dashboardService.getManagerDashboard(currentUser.getUsername()));
    }

    @GetMapping("/agent")
    @PreAuthorize("hasRole('AGENT')")
    public ResponseEntity<DashboardSummaryResponse> getAgentDashboard(
            @AuthenticationPrincipal UserDetails currentUser) {
        return ResponseEntity.ok(dashboardService.getAgentDashboard(currentUser.getUsername()));
    }

    @GetMapping("/client")
    @PreAuthorize("hasRole('CLIENT')")
    public ResponseEntity<DashboardSummaryResponse> getClientDashboard(
            @AuthenticationPrincipal UserDetails currentUser) {
        return ResponseEntity.ok(dashboardService.getClientDashboard(currentUser.getUsername()));
    }
}
