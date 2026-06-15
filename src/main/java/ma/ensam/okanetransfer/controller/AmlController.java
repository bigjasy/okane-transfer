package ma.ensam.okanetransfer.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.net.URI;
import ma.ensam.okanetransfer.dto.common.PageResponse;
import ma.ensam.okanetransfer.dto.compliance.AmlAlertResponse;
import ma.ensam.okanetransfer.dto.compliance.AmlCheckTransferRequest;
import ma.ensam.okanetransfer.dto.compliance.AmlCheckTransferResponse;
import ma.ensam.okanetransfer.dto.compliance.AmlReviewRequest;
import ma.ensam.okanetransfer.dto.compliance.ComplianceSummaryResponse;
import ma.ensam.okanetransfer.dto.compliance.WatchlistEntryRequest;
import ma.ensam.okanetransfer.dto.compliance.WatchlistEntryResponse;
import ma.ensam.okanetransfer.enums.AmlAlertStatus;
import ma.ensam.okanetransfer.enums.RiskLevel;
import ma.ensam.okanetransfer.service.AmlService;
import ma.ensam.okanetransfer.service.WatchlistService;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/aml")
@Tag(name = "AML & Compliance", description = "Anti-money laundering alerts, watchlist and compliance dashboard (M3)")
@SecurityRequirement(name = "BearerAuth")
public class AmlController {

    private final AmlService amlService;
    private final WatchlistService watchlistService;

    public AmlController(AmlService amlService, WatchlistService watchlistService) {
        this.amlService = amlService;
        this.watchlistService = watchlistService;
    }

    @GetMapping("/compliance-summary")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    @Operation(summary = "Compliance dashboard summary", description = "Aggregated KPIs for the admin compliance dashboard.")
    public ResponseEntity<ComplianceSummaryResponse> complianceSummary() {
        return ResponseEntity.ok(amlService.getComplianceSummary());
    }

    @GetMapping("/alerts")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    @Operation(summary = "List AML alerts")
    public ResponseEntity<PageResponse<AmlAlertResponse>> listAlerts(
            @RequestParam(name = "status", required = false) AmlAlertStatus status,
            @RequestParam(name = "riskLevel", required = false) RiskLevel riskLevel,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "20") int size
    ) {
        return ResponseEntity.ok(amlService.listAlerts(status, riskLevel, PageRequest.of(page, size)));
    }

    @GetMapping("/alerts/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    @Operation(summary = "Get AML alert")
    public ResponseEntity<AmlAlertResponse> getAlert(@PathVariable("id") Long id) {
        return ResponseEntity.ok(amlService.getAlert(id));
    }

    @PatchMapping("/alerts/{id}/review")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    @Operation(summary = "Review AML alert", description = "Resolve, escalate or mark false positive.")
    public ResponseEntity<AmlAlertResponse> reviewAlert(
            Authentication authentication,
            HttpServletRequest request,
            @PathVariable("id") Long id,
            @Valid @RequestBody AmlReviewRequest body
    ) {
        return ResponseEntity.ok(amlService.reviewAlert(
                id,
                body,
                authentication.getName(),
                request.getRemoteAddr(),
                request.getHeader("User-Agent")
        ));
    }

    @PostMapping("/check-transfer")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','AGENT')")
    @Operation(summary = "Run AML check on transfer", description = "Threshold and OFAC watchlist screening.")
    public ResponseEntity<AmlCheckTransferResponse> checkTransfer(@Valid @RequestBody AmlCheckTransferRequest body) {
        return ResponseEntity.ok(amlService.checkTransfer(body.getTransferReference()));
    }

    @GetMapping("/watchlist")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "List watchlist entries")
    public ResponseEntity<PageResponse<WatchlistEntryResponse>> listWatchlist(
            @RequestParam(name = "active", required = false) Boolean active,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "20") int size
    ) {
        return ResponseEntity.ok(watchlistService.listEntries(active, PageRequest.of(page, size)));
    }

    @PostMapping("/watchlist")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create watchlist entry")
    public ResponseEntity<WatchlistEntryResponse> createWatchlistEntry(@Valid @RequestBody WatchlistEntryRequest body) {
        WatchlistEntryResponse created = watchlistService.createEntry(body);
        return ResponseEntity.created(URI.create("/api/v1/aml/watchlist/" + created.getId())).body(created);
    }

    @PutMapping("/watchlist/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update watchlist entry")
    public ResponseEntity<WatchlistEntryResponse> updateWatchlistEntry(
            @PathVariable("id") Long id,
            @Valid @RequestBody WatchlistEntryRequest body
    ) {
        return ResponseEntity.ok(watchlistService.updateEntry(id, body));
    }

    @DeleteMapping("/watchlist/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Deactivate watchlist entry")
    public ResponseEntity<Void> deactivateWatchlistEntry(@PathVariable("id") Long id) {
        watchlistService.deactivateEntry(id);
        return ResponseEntity.noContent().build();
    }
}
