package ma.ensam.okanetransfer.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import ma.ensam.okanetransfer.dto.transfer.TransferCreateRequest;
import ma.ensam.okanetransfer.dto.transfer.TransferResponse;
import ma.ensam.okanetransfer.service.TransferService;

@RestController
@RequestMapping("/api/v1/transfers")
public class TransferController {

    private final TransferService transferService;

    public TransferController(TransferService transferService) {
        this.transferService = transferService;
    }

    @PostMapping
    public ResponseEntity<TransferResponse> createTransfer(
            @RequestBody TransferCreateRequest request,
            @AuthenticationPrincipal UserDetails currentUser) {
        
        // On récupère l'email de l'agent connecté de manière standard
        String agentEmail = currentUser.getUsername();
        
        TransferResponse response = transferService.createTransfer(request, agentEmail);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/{reference}/confirm-payment")
    public ResponseEntity<String> confirmPaymentAtSending(
            @PathVariable String reference,
            @AuthenticationPrincipal UserDetails currentUser) {
        
        String agentEmail = currentUser.getUsername();
        
        String withdrawalCode = transferService.confirmPaymentAtSending(reference, agentEmail);
        return ResponseEntity.ok(withdrawalCode);
    }
}