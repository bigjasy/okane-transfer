package ma.ensam.okanetransfer.domain.user;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import ma.ensam.okanetransfer.enums.Role;

@Entity
@Table(name = "managers")
@DiscriminatorValue("MANAGER")
public class Manager extends User {
    @Column(name = "agency_id")
    private Long agencyId;

    @Column(name = "approval_limit", precision = 19, scale = 2)
    private BigDecimal approvalLimit;

    public Manager() {
        setRole(Role.ROLE_MANAGER);
    }

    public Long getAgencyId() {
        return agencyId;
    }

    public void setAgencyId(Long agencyId) {
        this.agencyId = agencyId;
    }

    public BigDecimal getApprovalLimit() {
        return approvalLimit;
    }

    public void setApprovalLimit(BigDecimal approvalLimit) {
        this.approvalLimit = approvalLimit;
    }
}
