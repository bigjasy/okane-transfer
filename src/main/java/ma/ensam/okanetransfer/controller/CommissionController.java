package ma.ensam.okanetransfer.controller;

import java.util.List;
import ma.ensam.okanetransfer.dto.common.PageResponse;
import ma.ensam.okanetransfer.dto.finance.CommissionResponse;
import ma.ensam.okanetransfer.service.CommissionService;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/commissions")
public class CommissionController {

    private final CommissionService commissionService;

    public CommissionController(CommissionService commissionService) {
        this.commissionService = commissionService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ResponseEntity<PageResponse<CommissionResponse>> listCommissions(
            @RequestParam(name = "agencyId", required = false) Long agencyId,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "20") int size,
            @AuthenticationPrincipal UserDetails currentUser) {
        return ResponseEntity.ok(commissionService.listCommissions(
                currentUser.getUsername(),
                agencyId,
                PageRequest.of(Math.max(page, 0), Math.max(1, Math.min(size, 100)))));
    }

    @GetMapping("/transfer/{reference}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ResponseEntity<List<CommissionResponse>> getByTransfer(
            @PathVariable("reference") String reference,
            @AuthenticationPrincipal UserDetails currentUser) {
        return ResponseEntity.ok(commissionService.getCommissionsByTransferReference(
                currentUser.getUsername(), reference));
    }
}
