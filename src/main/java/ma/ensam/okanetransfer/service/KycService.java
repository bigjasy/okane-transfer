package ma.ensam.okanetransfer.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import ma.ensam.okanetransfer.domain.compliance.KycDocument;
import ma.ensam.okanetransfer.domain.user.Client;
import ma.ensam.okanetransfer.domain.user.User;
import ma.ensam.okanetransfer.dto.compliance.KycDocumentResponse;
import ma.ensam.okanetransfer.dto.compliance.KycReviewRequest;
import ma.ensam.okanetransfer.dto.common.PageResponse;
import ma.ensam.okanetransfer.enums.AuditAction;
import ma.ensam.okanetransfer.enums.KycDocumentType;
import ma.ensam.okanetransfer.enums.KycStatus;
import ma.ensam.okanetransfer.exception.BusinessException;
import ma.ensam.okanetransfer.exception.ForbiddenOperationException;
import ma.ensam.okanetransfer.exception.ResourceNotFoundException;
import ma.ensam.okanetransfer.repository.KycDocumentRepository;
import ma.ensam.okanetransfer.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@Transactional
public class KycService {

    private static final Set<String> ALLOWED_EXTENSIONS = Set.of("pdf", "jpg", "jpeg", "png");
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024;

    private final KycDocumentRepository kycDocumentRepository;
    private final UserRepository userRepository;
    private final AuditService auditService;
    private final NotificationService notificationService;
    private final String uploadDir;

    public KycService(
            KycDocumentRepository kycDocumentRepository,
            UserRepository userRepository,
            AuditService auditService,
            NotificationService notificationService,
            @Value("${kyc.upload-dir}") String uploadDir
    ) {
        this.kycDocumentRepository = kycDocumentRepository;
        this.userRepository = userRepository;
        this.auditService = auditService;
        this.notificationService = notificationService;
        this.uploadDir = uploadDir;
    }

    public KycDocumentResponse uploadDocument(
            MultipartFile file,
            KycDocumentType documentType,
            String documentNumber,
            String actorEmail,
            Long targetUserId,
            String ipAddress,
            String userAgent
    ) {
        validateFile(file);
        User actor = userRepository.findByEmailIgnoreCase(actorEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User", actorEmail));

        User documentOwner = targetUserId == null ? actor : userRepository.findById(targetUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User", targetUserId));

        String storedPath = storeFile(file);

        KycDocument document = new KycDocument();
        document.setUser(documentOwner);
        document.setDocumentType(documentType);
        document.setDocumentNumberEncrypted(documentNumber);
        document.setFilePath(storedPath);
        document.setStatus(KycStatus.PENDING);

        if (documentOwner instanceof Client client) {
            client.setKycStatus(KycStatus.PENDING);
            userRepository.save(client);
        }

        KycDocument saved = kycDocumentRepository.save(document);
        auditService.record(
                AuditAction.KYC_REVIEW,
                actor,
                "KycDocument",
                String.valueOf(saved.getId()),
                ipAddress,
                userAgent,
                "{\"action\":\"UPLOAD\",\"status\":\"PENDING\"}"
        );
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<KycDocumentResponse> getMyDocuments(String email) {
        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", email));
        return kycDocumentRepository.findByUserId(user.getId()).stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<KycDocumentResponse> getUserDocuments(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User", userId);
        }
        return kycDocumentRepository.findByUserId(userId).stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public PageResponse<KycDocumentResponse> getPendingDocuments(Pageable pageable) {
        Page<KycDocument> page = kycDocumentRepository.findByStatus(KycStatus.PENDING, pageable);
        return PageResponse.from(page.map(this::toResponse));
    }

    public KycDocumentResponse reviewDocument(
            Long documentId,
            KycReviewRequest request,
            String reviewerEmail,
            String ipAddress,
            String userAgent
    ) {
        if (request.getStatus() != KycStatus.APPROVED && request.getStatus() != KycStatus.REJECTED) {
            throw new BusinessException("Review status must be APPROVED or REJECTED");
        }
        if (request.getStatus() == KycStatus.REJECTED
                && (request.getRejectionReason() == null || request.getRejectionReason().isBlank())) {
            throw new BusinessException("Rejection reason is required when rejecting a document");
        }

        KycDocument document = kycDocumentRepository.findById(documentId)
                .orElseThrow(() -> new ResourceNotFoundException("KycDocument", documentId));
        User reviewer = userRepository.findByEmailIgnoreCase(reviewerEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User", reviewerEmail));

        document.setStatus(request.getStatus());
        document.setRejectionReason(request.getRejectionReason());
        document.setReviewedBy(reviewer);
        document.setReviewedAt(LocalDateTime.now());

        User owner = document.getUser();
        if (owner instanceof Client client) {
            client.setKycStatus(request.getStatus());
            userRepository.save(client);
        }

        KycDocument saved = kycDocumentRepository.save(document);
        auditService.record(
                AuditAction.KYC_REVIEW,
                reviewer,
                "KycDocument",
                String.valueOf(saved.getId()),
                ipAddress,
                userAgent,
                "{\"status\":\"" + request.getStatus() + "\"}"
        );

        notificationService.notifyKycReview(owner, request.getStatus());
        return toResponse(saved);
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException("KYC file is required");
        }
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new BusinessException("FILE_TOO_LARGE", "KYC file exceeds 5 MB limit", HttpStatus.PAYLOAD_TOO_LARGE);
        }
        String originalName = file.getOriginalFilename();
        if (originalName == null || !originalName.contains(".")) {
            throw new BusinessException("Invalid file extension");
        }
        String extension = originalName.substring(originalName.lastIndexOf('.') + 1).toLowerCase();
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new ForbiddenOperationException("Unsupported file type: " + extension);
        }
    }

    private String storeFile(MultipartFile file) {
        try {
            String dir = uploadDir != null && !uploadDir.isBlank()
                    ? uploadDir
                    : System.getProperty("java.io.tmpdir") + "/okane-kyc";
            Path directory = Paths.get(dir);
            Files.createDirectories(directory);
            String filename = UUID.randomUUID() + "-" + file.getOriginalFilename();
            Path target = directory.resolve(filename);
            file.transferTo(target);
            return target.toString();
        } catch (IOException exception) {
            throw new BusinessException("Unable to store KYC document");
        }
    }

    private KycDocumentResponse toResponse(KycDocument document) {
        KycDocumentResponse response = new KycDocumentResponse();
        response.setId(document.getId());
        response.setDocumentType(document.getDocumentType());
        response.setStatus(document.getStatus());
        response.setUploadedAt(document.getUploadedAt());
        response.setRejectionReason(document.getRejectionReason());
        return response;
    }
}
