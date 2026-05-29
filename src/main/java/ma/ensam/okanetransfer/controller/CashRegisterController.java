package ma.ensam.okanetransfer.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import ma.ensam.okanetransfer.dto.finance.CashClosingRequest;
import ma.ensam.okanetransfer.dto.finance.CashRegisterOpenRequest;
import ma.ensam.okanetransfer.dto.finance.CashRegisterResponse;
import ma.ensam.okanetransfer.service.CashRegisterService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/cash-registers")
public class CashRegisterController {

    private final CashRegisterService cashRegisterService;

    public CashRegisterController(CashRegisterService cashRegisterService) {
        this.cashRegisterService = cashRegisterService;
    }

    @PostMapping("/open")
    public ResponseEntity<CashRegisterResponse> openRegister(@Valid @RequestBody CashRegisterOpenRequest request) {
        CashRegisterResponse response = cashRegisterService.openRegister(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/{id}/close")
    public ResponseEntity<CashRegisterResponse> closeRegister(
            @PathVariable Long id, 
            @Valid @RequestBody CashClosingRequest request,
            @AuthenticationPrincipal UserDetails currentUser) {
        
        String userEmail = currentUser.getUsername();
        CashRegisterResponse response = cashRegisterService.closeRegister(id, request, userEmail);
        return ResponseEntity.ok(response);
    }
}