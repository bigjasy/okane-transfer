package ma.ensam.okanetransfer.domain.user;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import ma.ensam.okanetransfer.enums.Role;

@Entity
@Table(name = "admins")
@DiscriminatorValue("ADMIN")
public class Admin extends User {
    @Column(length = 120)
    private String department;

    @Column(name = "super_admin", nullable = false)
    private boolean superAdmin;

    public Admin() {
        setRole(Role.ROLE_ADMIN);
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public boolean isSuperAdmin() {
        return superAdmin;
    }

    public void setSuperAdmin(boolean superAdmin) {
        this.superAdmin = superAdmin;
    }
}
