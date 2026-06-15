package ma.ensam.okanetransfer.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import ma.ensam.okanetransfer.dto.transfer.BeneficiaryRequest;
import ma.ensam.okanetransfer.dto.transfer.BeneficiaryResponse;
import ma.ensam.okanetransfer.service.BeneficiaryService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/beneficiaries")
public class BeneficiaryController {

    private final BeneficiaryService beneficiaryService;

    public BeneficiaryController(BeneficiaryService beneficiaryService) {
        this.beneficiaryService = beneficiaryService;
    }

    @GetMapping
    public ResponseEntity<Page<BeneficiaryResponse>> getAllBeneficiaries(
            @RequestParam(name = "clientId", required = false) Long clientId,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "20") int size,
            @AuthenticationPrincipal UserDetails currentUser) {
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<BeneficiaryResponse> response = beneficiaryService.getBeneficiaries(clientId, currentUser.getUsername(), pageable);
        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<BeneficiaryResponse> createBeneficiary(
            @Valid @RequestBody BeneficiaryRequest request,
            @AuthenticationPrincipal UserDetails currentUser) {
        
        BeneficiaryResponse response = beneficiaryService.createBeneficiary(request, currentUser.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<BeneficiaryResponse> getBeneficiaryById(
            @PathVariable("id") Long id,
            @AuthenticationPrincipal UserDetails currentUser) {
        
        BeneficiaryResponse response = beneficiaryService.getBeneficiaryById(id, currentUser.getUsername());
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<BeneficiaryResponse> updateBeneficiary(
            @PathVariable("id") Long id,
            @Valid @RequestBody BeneficiaryRequest request,
            @AuthenticationPrincipal UserDetails currentUser) {
        
        BeneficiaryResponse response = beneficiaryService.updateBeneficiary(id, request, currentUser.getUsername());
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBeneficiary(
            @PathVariable("id") Long id,
            @AuthenticationPrincipal UserDetails currentUser) {
        
        beneficiaryService.deleteBeneficiary(id, currentUser.getUsername());
        return ResponseEntity.noContent().build();
    }
}
