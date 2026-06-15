package ma.ensam.okanetransfer.domain.user;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import ma.ensam.okanetransfer.domain.agency.Agency;
import ma.ensam.okanetransfer.enums.Role;

@Entity
@Table(name = "agents")
@DiscriminatorValue("AGENT")
public class Agent extends User {
    
    @ManyToOne
    @JoinColumn(name = "agency_id")
    private Agency agency;

    @Column(name = "employee_code", unique = true, length = 50)
    private String employeeCode;

    public Agent() {
        setRole(Role.ROLE_AGENT);
    }

    public Agency getAgency() {
        return agency;
    }

    public void setAgency(Agency agency) {
        this.agency = agency;
    }

    public String getEmployeeCode() {
        return employeeCode;
    }

    public void setEmployeeCode(String employeeCode) {
        this.employeeCode = employeeCode;
    }
}
