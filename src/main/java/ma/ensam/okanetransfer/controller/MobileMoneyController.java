package ma.ensam.okanetransfer.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.net.URI;
import ma.ensam.okanetransfer.dto.common.PageResponse;
import ma.ensam.okanetransfer.dto.transfer.MobileMoneyCallbackRequest;
import ma.ensam.okanetransfer.dto.transfer.MobileMoneyReconciliationRequest;
import ma.ensam.okanetransfer.dto.transfer.MobileMoneyReconciliationResponse;
import ma.ensam.okanetransfer.dto.transfer.MobileMoneyRequest;
import ma.ensam.okanetransfer.dto.transfer.MobileMoneyResponse;
import ma.ensam.okanetransfer.service.MobileMoneyService;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/mobile-money")
@Tag(name = "Mobile Money", description = "Simulated mobile wallet payouts (Orange Money, Wave, M-Pesa)")
@SecurityRequirement(name = "BearerAuth")
public class MobileMoneyController {

    private final MobileMoneyService mobileMoneyService;

    public MobileMoneyController(MobileMoneyService mobileMoneyService) {
        this.mobileMoneyService = mobileMoneyService;
    }

    @PostMapping("/transfers")
    @PreAuthorize("hasRole('AGENT')")
    @Operation(summary = "Send transfer to mobile wallet", description = "Simulates dispatch to the selected mobile money operator.")
    public ResponseEntity<MobileMoneyResponse> createTransfer(
            @Valid @RequestBody MobileMoneyRequest request,
            Authentication authentication,
            HttpServletRequest httpRequest
    ) {
        MobileMoneyResponse response = mobileMoneyService.createTransfer(
                request,
                authentication.getName(),
                httpRequest.getRemoteAddr(),
                httpRequest.getHeader("User-Agent")
        );
        return ResponseEntity.created(URI.create("/api/v1/mobile-money/transfers/" + response.getId())).body(response);
    }

    @GetMapping("/transfers")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','AGENT')")
    @Operation(summary = "List mobile money transfers")
    public ResponseEntity<PageResponse<MobileMoneyResponse>> listTransfers(
            Authentication authentication,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ResponseEntity.ok(mobileMoneyService.listTransfers(
                authentication.getName(),
                PageRequest.of(page, size)
        ));
    }

    @GetMapping("/transfers/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','AGENT')")
    @Operation(summary = "Get mobile money transfer details")
    public ResponseEntity<MobileMoneyResponse> getTransfer(
            @PathVariable("id") Long id,
            Authentication authentication
    ) {
        return ResponseEntity.ok(mobileMoneyService.getById(id, authentication.getName()));
    }

    @PatchMapping("/transfers/{id}/simulate-callback")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Simulate operator callback", description = "Admin-only simulation of operator confirmation webhook.")
    public ResponseEntity<MobileMoneyResponse> simulateCallback(
            @PathVariable("id") Long id,
            @RequestBody(required = false) MobileMoneyCallbackRequest request,
            Authentication authentication,
            HttpServletRequest httpRequest
    ) {
        return ResponseEntity.ok(mobileMoneyService.simulateCallback(
                id,
                request,
                authentication.getName(),
                httpRequest.getRemoteAddr(),
                httpRequest.getHeader("User-Agent")
        ));
    }

    @PostMapping("/reconciliation")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    @Operation(summary = "Reconcile operator statement", description = "Simulated end-of-day reconciliation with operator records.")
    public ResponseEntity<MobileMoneyReconciliationResponse> reconcile(
            @Valid @RequestBody MobileMoneyReconciliationRequest request,
            Authentication authentication,
            HttpServletRequest httpRequest
    ) {
        return ResponseEntity.ok(mobileMoneyService.reconcile(
                request,
                authentication.getName(),
                httpRequest.getRemoteAddr(),
                httpRequest.getHeader("User-Agent")
        ));
    }
}
