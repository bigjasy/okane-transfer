package ma.ensam.okanetransfer.dto.compliance;

import java.time.LocalDateTime;
import ma.ensam.okanetransfer.enums.KycDocumentType;
import ma.ensam.okanetransfer.enums.KycStatus;

public class KycDocumentResponse {
    private Long id;
    private KycDocumentType documentType;
    private KycStatus status;
    private LocalDateTime uploadedAt;
    private String rejectionReason;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public KycDocumentType getDocumentType() {
        return documentType;
    }

    public void setDocumentType(KycDocumentType documentType) {
        this.documentType = documentType;
    }

    public KycStatus getStatus() {
        return status;
    }

    public void setStatus(KycStatus status) {
        this.status = status;
    }

    public LocalDateTime getUploadedAt() {
        return uploadedAt;
    }

    public void setUploadedAt(LocalDateTime uploadedAt) {
        this.uploadedAt = uploadedAt;
    }

    public String getRejectionReason() {
        return rejectionReason;
    }

    public void setRejectionReason(String rejectionReason) {
        this.rejectionReason = rejectionReason;
    }
}
