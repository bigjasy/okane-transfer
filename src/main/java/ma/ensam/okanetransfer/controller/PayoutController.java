package ma.ensam.okanetransfer.controller;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import ma.ensam.okanetransfer.dto.transfer.PayoutConfirmRequest;
import ma.ensam.okanetransfer.dto.transfer.PayoutReceiptResponse;
import ma.ensam.okanetransfer.dto.transfer.PayoutResponse;
import ma.ensam.okanetransfer.dto.transfer.PayoutSearchRequest;
import ma.ensam.okanetransfer.dto.transfer.PayoutSearchResponse;
import ma.ensam.okanetransfer.dto.transfer.PayoutValidateRequest;
import ma.ensam.okanetransfer.dto.transfer.PayoutValidateResponse;
import ma.ensam.okanetransfer.service.DocumentExportService;
import ma.ensam.okanetransfer.service.PayoutService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/payouts")
public class PayoutController {

    private final PayoutService payoutService;
    private final DocumentExportService documentExportService;

    public PayoutController(PayoutService payoutService, DocumentExportService documentExportService) {
        this.payoutService = payoutService;
        this.documentExportService = documentExportService;
    }

    @PostMapping("/search")
    @PreAuthorize("hasRole('AGENT')")
    public ResponseEntity<PayoutSearchResponse> searchTransfer(
            @Valid @RequestBody PayoutSearchRequest request) {
        PayoutSearchResponse response = payoutService.searchTransfer(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/validate")
    @PreAuthorize("hasRole('AGENT')")
    public ResponseEntity<PayoutValidateResponse> validateBeneficiary(
            @Valid @RequestBody PayoutValidateRequest request) {
        return ResponseEntity.ok(payoutService.validateBeneficiary(request));
    }

    @PostMapping("/confirm")
    @PreAuthorize("hasRole('AGENT')")
    public ResponseEntity<PayoutResponse> confirmPayout(
            @Valid @RequestBody PayoutConfirmRequest request,
            @AuthenticationPrincipal UserDetails currentUser) {
        
        String agentEmail = currentUser.getUsername();
        PayoutResponse response = payoutService.confirmPayout(request, agentEmail);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{transferReference}/receipt")
    @PreAuthorize("hasRole('AGENT')")
    public ResponseEntity<?> getReceipt(
            @PathVariable("transferReference") String transferReference,
            @RequestParam(name = "format", defaultValue = "JSON") String format) {
        var receipt = payoutService.getReceipt(transferReference);
        if ("PDF".equalsIgnoreCase(format)) {
            byte[] pdf = documentExportService.payoutReceiptPdf(receipt);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=payout-receipt-" + transferReference + ".pdf")
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(pdf);
        }
        return ResponseEntity.ok(receipt);
    }
}
