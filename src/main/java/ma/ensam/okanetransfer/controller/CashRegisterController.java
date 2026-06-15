package ma.ensam.okanetransfer.controller;

import java.util.List;
import ma.ensam.okanetransfer.dto.finance.CashClosingRequest;
import ma.ensam.okanetransfer.dto.finance.CashMovementRequest;
import ma.ensam.okanetransfer.dto.finance.CashMovementResponse;
import ma.ensam.okanetransfer.dto.finance.CashRegisterOpenRequest;
import ma.ensam.okanetransfer.dto.finance.CashRegisterResponse;
import ma.ensam.okanetransfer.service.CashRegisterService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/cash-registers")
public class CashRegisterController {

    private final CashRegisterService cashRegisterService;

    public CashRegisterController(CashRegisterService cashRegisterService) {
        this.cashRegisterService = cashRegisterService;
    }

    @PostMapping("/open")
    @PreAuthorize("hasAnyRole('AGENT','MANAGER')")
    public ResponseEntity<CashRegisterResponse> openRegister(@Valid @RequestBody CashRegisterOpenRequest request) {
        CashRegisterResponse response = cashRegisterService.openRegister(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/{id}/close")
    @PreAuthorize("hasAnyRole('AGENT','MANAGER')")
    public ResponseEntity<CashRegisterResponse> closeRegister(
            @PathVariable("id") Long id,
            @Valid @RequestBody CashClosingRequest request,
            @AuthenticationPrincipal UserDetails currentUser) {
        CashRegisterResponse response = cashRegisterService.closeRegister(id, request, currentUser.getUsername());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/current")
    @PreAuthorize("hasAnyRole('AGENT','MANAGER')")
    public ResponseEntity<CashRegisterResponse> getCurrentOpenRegister(
            @RequestParam(name = "currencyCode", required = false) String currencyCode,
            @AuthenticationPrincipal UserDetails currentUser) {
        CashRegisterResponse response = cashRegisterService.getCurrentOpenRegister(
                currentUser.getUsername(), currencyCode);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/movements")
    @PreAuthorize("hasAnyRole('AGENT','MANAGER')")
    public ResponseEntity<CashMovementResponse> addCashMovement(
            @PathVariable("id") Long id,
            @Valid @RequestBody CashMovementRequest request,
            @AuthenticationPrincipal UserDetails currentUser) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(cashRegisterService.addManualMovement(id, request, currentUser.getUsername()));
    }

    @GetMapping("/{id}/movements")
    @PreAuthorize("hasAnyRole('AGENT','MANAGER','ADMIN')")
    public ResponseEntity<List<CashMovementResponse>> getCashMovements(
            @PathVariable("id") Long id,
            @AuthenticationPrincipal UserDetails currentUser) {
        return ResponseEntity.ok(cashRegisterService.getMovements(id, currentUser.getUsername()));
    }

    @GetMapping("/agency/{agencyId}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ResponseEntity<List<CashRegisterResponse>> listAgencyRegisters(
            @PathVariable("agencyId") Long agencyId,
            @AuthenticationPrincipal UserDetails currentUser) {
        return ResponseEntity.ok(cashRegisterService.listAgencyRegisters(agencyId, currentUser.getUsername()));
    }
}
