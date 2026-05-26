package ma.ensam.okanetransfer.repository;

import java.util.List;
import java.util.Optional;
import ma.ensam.okanetransfer.domain.user.Agent;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AgentRepository extends JpaRepository<Agent, Long> {
    List<Agent> findByAgencyId(Long agencyId);

    Optional<Agent> findByEmployeeCode(String employeeCode);
}
