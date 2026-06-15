package ma.ensam.okanetransfer.domain.user;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;

import ma.ensam.okanetransfer.domain.agency.Agency;
import ma.ensam.okanetransfer.enums.Role;

@Entity
@Table(name = "managers")
@DiscriminatorValue("MANAGER")
public class Manager extends User {
    
    @ManyToOne
    @JoinColumn(name = "agency_id")
    private Agency agency;

    @Column(name = "approval_limit", precision = 19, scale = 2)
    private BigDecimal approvalLimit;

    public Manager() {
        setRole(Role.ROLE_MANAGER);
    }

    public Agency getAgency() {
        return agency;
    }

    public void setAgency(Agency agency) {
        this.agency = agency;
    }

    public BigDecimal getApprovalLimit() {
        return approvalLimit;
    }

    public void setApprovalLimit(BigDecimal approvalLimit) {
        this.approvalLimit = approvalLimit;
    }
}