package ma.ensam.okanetransfer.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import ma.ensam.okanetransfer.dto.common.PageResponse;
import ma.ensam.okanetransfer.dto.compliance.ComplianceSummaryResponse;
import ma.ensam.okanetransfer.dto.compliance.KycDocumentResponse;
import ma.ensam.okanetransfer.dto.compliance.KycReviewRequest;
import ma.ensam.okanetransfer.enums.KycDocumentType;
import ma.ensam.okanetransfer.service.KycService;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
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
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/kyc")
@Tag(name = "KYC", description = "Know Your Customer document workflow (M3)")
@SecurityRequirement(name = "BearerAuth")
public class KycController {

    private final KycService kycService;

    public KycController(KycService kycService) {
        this.kycService = kycService;
    }

    @PostMapping(value = "/documents", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('CLIENT','AGENT')")
    @Operation(summary = "Upload KYC document", description = "Stores an identity document for client verification.")
    public ResponseEntity<KycDocumentResponse> uploadDocument(
            Authentication authentication,
            HttpServletRequest request,
            @RequestPart("file") MultipartFile file,
            @RequestParam("documentType") KycDocumentType documentType,
            @RequestParam("documentNumber") String documentNumber,
            @RequestParam(name = "userId", required = false) Long userId
    ) {
        KycDocumentResponse response = kycService.uploadDocument(
                file,
                documentType,
                documentNumber,
                authentication.getName(),
                userId,
                request.getRemoteAddr(),
                request.getHeader("User-Agent")
        );
        return ResponseEntity.created(URI.create("/api/v1/kyc/documents/" + response.getId())).body(response);
    }

    @GetMapping("/documents/me")
    @PreAuthorize("hasRole('CLIENT')")
    @Operation(summary = "My KYC documents")
    public ResponseEntity<List<KycDocumentResponse>> myDocuments(Authentication authentication) {
        return ResponseEntity.ok(kycService.getMyDocuments(authentication.getName()));
    }

    @GetMapping("/users/{userId}/documents")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','AGENT')")
    @Operation(summary = "KYC documents for a user")
    public ResponseEntity<List<KycDocumentResponse>> userDocuments(@PathVariable("userId") Long userId) {
        return ResponseEntity.ok(kycService.getUserDocuments(userId));
    }

    @PatchMapping("/documents/{id}/review")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    @Operation(summary = "Review KYC document", description = "Approve or reject a pending document.")
    public ResponseEntity<KycDocumentResponse> reviewDocument(
            Authentication authentication,
            HttpServletRequest request,
            @PathVariable("id") Long id,
            @Valid @RequestBody KycReviewRequest body
    ) {
        return ResponseEntity.ok(kycService.reviewDocument(
                id,
                body,
                authentication.getName(),
                request.getRemoteAddr(),
                request.getHeader("User-Agent")
        ));
    }

    @GetMapping("/pending")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    @Operation(summary = "Pending KYC documents", description = "Paginated queue for compliance officers.")
    public ResponseEntity<PageResponse<KycDocumentResponse>> pendingDocuments(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "20") int size
    ) {
        return ResponseEntity.ok(kycService.getPendingDocuments(PageRequest.of(page, size)));
    }
}
