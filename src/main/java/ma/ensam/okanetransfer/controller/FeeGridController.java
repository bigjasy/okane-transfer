package ma.ensam.okanetransfer.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import ma.ensam.okanetransfer.dto.agency.FeeGridRequest;
import ma.ensam.okanetransfer.dto.agency.FeeGridResponse;
import ma.ensam.okanetransfer.dto.agency.FeeSimulationRequest;
import ma.ensam.okanetransfer.dto.agency.FeeSimulationResponse;
import ma.ensam.okanetransfer.service.FeeCalculationService;
import ma.ensam.okanetransfer.service.FeeGridService;

import jakarta.validation.Valid;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

@RestController
@RequestMapping("/api/v1/fee-grids")
public class FeeGridController {

    private final FeeGridService feeGridService;
    private final FeeCalculationService feeCalculationService;

    public FeeGridController(FeeGridService feeGridService, FeeCalculationService feeCalculationService) {
        this.feeGridService = feeGridService;
        this.feeCalculationService = feeCalculationService;
    }

    @GetMapping
    public ResponseEntity<Page<FeeGridResponse>> getFeeGrids(
            @RequestParam(name = "corridorId", required = false) Long corridorId,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "20") int size) {
        
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(feeGridService.getFeeGrids(corridorId, pageable));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<FeeGridResponse> createFeeGrid(@Valid @RequestBody FeeGridRequest request) {
        FeeGridResponse response = feeGridService.createFeeGrid(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<FeeGridResponse> updateFeeGrid(
            @PathVariable("id") Long id, 
            @Valid @RequestBody FeeGridRequest request) {
        
        return ResponseEntity.ok(feeGridService.updateFeeGrid(id, request));
    }

    @PatchMapping("/{id}/activation")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<FeeGridResponse> toggleActivation(
            @PathVariable("id") Long id, 
            @RequestBody Map<String, Boolean> payload) {
        
        boolean active = payload.getOrDefault("active", false);
        return ResponseEntity.ok(feeGridService.toggleActivation(id, active));
    }

    @PostMapping("/simulate")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','AGENT','CLIENT')")
    public ResponseEntity<FeeSimulationResponse> simulateFees(
            @Valid @RequestBody FeeSimulationRequest request) {
        return ResponseEntity.ok(feeCalculationService.simulateFees(request));
    }

    @GetMapping("/export")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<byte[]> exportFeeGrids(
            @RequestParam(name = "format", defaultValue = "CSV") String format,
            @RequestParam(name = "corridorId", required = false) Long corridorId) {
        if (!"CSV".equalsIgnoreCase(format)) {
            throw new ma.ensam.okanetransfer.exception.BusinessException("Seul l'export CSV est supporté pour le moment.");
        }
        byte[] content = feeGridService.exportCsv(corridorId);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=fee-grids.csv")
                .contentType(new MediaType("text", "csv", StandardCharsets.UTF_8))
                .body(content);
    }
}
