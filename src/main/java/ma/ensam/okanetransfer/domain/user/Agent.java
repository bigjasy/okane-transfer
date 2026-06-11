package ma.ensam.okanetransfer.domain.user;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import ma.ensam.okanetransfer.enums.Role;

@Entity
@Table(name = "agents")
@DiscriminatorValue("AGENT")
public class Agent extends User {
    @Column(name = "agency_id")
    private Long agencyId;

    @Column(name = "employee_code", unique = true, length = 50)
    private String employeeCode;

    public Agent() {
        setRole(Role.ROLE_AGENT);
    }

    public Long getAgencyId() {
        return agencyId;
    }

    public void setAgencyId(Long agencyId) {
        this.agencyId = agencyId;
    }

    public String getEmployeeCode() {
        return employeeCode;
    }

    public void setEmployeeCode(String employeeCode) {
        this.employeeCode = employeeCode;
    }
}
