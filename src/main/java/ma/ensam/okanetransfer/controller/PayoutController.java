package ma.ensam.okanetransfer.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import ma.ensam.okanetransfer.dto.transfer.PayoutConfirmRequest;
import ma.ensam.okanetransfer.dto.transfer.PayoutResponse;
import ma.ensam.okanetransfer.service.PayoutService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/payouts")
public class PayoutController {

    private final PayoutService payoutService;

    public PayoutController(PayoutService payoutService) {
        this.payoutService = payoutService;
    }

    @PostMapping("/confirm")
    public ResponseEntity<PayoutResponse> confirmPayout(
            @Valid @RequestBody PayoutConfirmRequest request,
            @AuthenticationPrincipal UserDetails currentUser) {
        
        String agentEmail = currentUser.getUsername();
        PayoutResponse response = payoutService.confirmPayout(request, agentEmail);
        return ResponseEntity.ok(response);
    }
}