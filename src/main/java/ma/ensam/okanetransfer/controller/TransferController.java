package ma.ensam.okanetransfer.controller;

import jakarta.validation.Valid;
import ma.ensam.okanetransfer.dto.agency.FeeSimulationRequest;
import ma.ensam.okanetransfer.dto.agency.FeeSimulationResponse;
import ma.ensam.okanetransfer.dto.common.PageResponse;
import ma.ensam.okanetransfer.dto.transfer.TransferCreateRequest;
import ma.ensam.okanetransfer.dto.transfer.TransferReceiptResponse;
import ma.ensam.okanetransfer.dto.transfer.TransferResponse;
import ma.ensam.okanetransfer.dto.transfer.TransferTrackingResponse;
import ma.ensam.okanetransfer.service.DocumentExportService;
import ma.ensam.okanetransfer.service.FeeCalculationService;
import ma.ensam.okanetransfer.service.TransferService;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/transfers")
public class TransferController {

    private final TransferService transferService;
    private final FeeCalculationService feeCalculationService;
    private final DocumentExportService documentExportService;

    public TransferController(
            TransferService transferService,
            FeeCalculationService feeCalculationService,
            DocumentExportService documentExportService
    ) {
        this.transferService = transferService;
        this.feeCalculationService = feeCalculationService;
        this.documentExportService = documentExportService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','AGENT','CLIENT')")
    public ResponseEntity<PageResponse<TransferResponse>> listTransfers(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "20") int size,
            @AuthenticationPrincipal UserDetails currentUser) {

        Pageable pageable = PageRequest.of(Math.max(page, 0), Math.max(1, Math.min(size, 100)));
        return ResponseEntity.ok(transferService.listVisibleTransfers(currentUser.getUsername(), pageable));
    }

    @PostMapping
    @PreAuthorize("hasRole('AGENT')")
    public ResponseEntity<TransferResponse> createTransfer(
            @Valid @RequestBody TransferCreateRequest request,
            @AuthenticationPrincipal UserDetails currentUser) {
        TransferResponse response = transferService.createTransfer(request, currentUser.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/{reference}/confirm-payment")
    @PreAuthorize("hasRole('AGENT')")
    public ResponseEntity<String> confirmPaymentAtSending(
            @Valid @PathVariable("reference") String reference,
            @AuthenticationPrincipal UserDetails currentUser) {
        String withdrawalCode = transferService.confirmPaymentAtSending(reference, currentUser.getUsername());
        return ResponseEntity.ok(withdrawalCode);
    }

    @PostMapping("/simulate")
    @PreAuthorize("hasAnyRole('AGENT','CLIENT')")
    public ResponseEntity<FeeSimulationResponse> simulateTransfer(
            @Valid @RequestBody FeeSimulationRequest request) {
        return ResponseEntity.ok(feeCalculationService.simulateFees(request));
    }

    @GetMapping("/track/{reference}")
    @PreAuthorize("hasAnyRole('AGENT','CLIENT')")
    public ResponseEntity<TransferTrackingResponse> trackTransfer(@PathVariable("reference") String reference) {
        return ResponseEntity.ok(transferService.trackTransfer(reference));
    }

    @GetMapping("/{reference}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','AGENT','CLIENT')")
    public ResponseEntity<TransferResponse> getTransfer(
            @PathVariable("reference") String reference,
            @AuthenticationPrincipal UserDetails currentUser) {
        return ResponseEntity.ok(transferService.getVisibleTransfer(reference, currentUser.getUsername()));
    }

    @GetMapping("/{reference}/receipt")
    @PreAuthorize("hasAnyRole('AGENT','CLIENT')")
    public ResponseEntity<?> getSendReceipt(
            @PathVariable("reference") String reference,
            @RequestParam(name = "format", defaultValue = "JSON") String format,
            @RequestParam(name = "withdrawalCode", required = false) String withdrawalCode,
            @AuthenticationPrincipal UserDetails currentUser) {
        TransferReceiptResponse receipt = transferService.getSendReceipt(
                reference, currentUser.getUsername(), withdrawalCode);
        if ("PDF".equalsIgnoreCase(format)) {
            byte[] pdf = documentExportService.sendReceiptPdf(receipt);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=send-receipt-" + reference + ".pdf")
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(pdf);
        }
        return ResponseEntity.ok(receipt);
    }

    @PatchMapping("/{reference}/cancel")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','AGENT')")
    public ResponseEntity<TransferResponse> cancelTransfer(
            @PathVariable("reference") String reference,
            @AuthenticationPrincipal UserDetails currentUser) {
        return ResponseEntity.ok(transferService.cancelTransfer(reference, currentUser.getUsername()));
    }
}
