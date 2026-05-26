package ma.ensam.okanetransfer.domain.user;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import java.time.LocalDate;
import ma.ensam.okanetransfer.enums.IdentityType;
import ma.ensam.okanetransfer.enums.KycStatus;
import ma.ensam.okanetransfer.enums.Role;
import ma.ensam.okanetransfer.util.AesEncryptionConverter;

@Entity
@Table(name = "clients")
@DiscriminatorValue("CLIENT")
public class Client extends User {
    @Enumerated(EnumType.STRING)
    @Column(name = "identity_type", length = 30)
    private IdentityType identityType;

    @Convert(converter = AesEncryptionConverter.class)
    @Column(name = "identity_number_encrypted")
    private String identityNumberEncrypted;

    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    @Column(name = "country_id")
    private Long countryId;

    @Enumerated(EnumType.STRING)
    @Column(name = "kyc_status", nullable = false, length = 30)
    private KycStatus kycStatus = KycStatus.NOT_SUBMITTED;

    public Client() {
        setRole(Role.ROLE_CLIENT);
    }

    public IdentityType getIdentityType() {
        return identityType;
    }

    public void setIdentityType(IdentityType identityType) {
        this.identityType = identityType;
    }

    public String getIdentityNumberEncrypted() {
        return identityNumberEncrypted;
    }

    public void setIdentityNumberEncrypted(String identityNumberEncrypted) {
        this.identityNumberEncrypted = identityNumberEncrypted;
    }

    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(LocalDate dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public Long getCountryId() {
        return countryId;
    }

    public void setCountryId(Long countryId) {
        this.countryId = countryId;
    }

    public KycStatus getKycStatus() {
        return kycStatus;
    }

    public void setKycStatus(KycStatus kycStatus) {
        this.kycStatus = kycStatus;
    }
}
