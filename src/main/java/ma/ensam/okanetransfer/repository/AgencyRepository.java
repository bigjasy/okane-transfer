package ma.ensam.okanetransfer.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ma.ensam.okanetransfer.domain.agency.Agency;
import ma.ensam.okanetransfer.enums.AgencyStatus;
import java.util.List;
import java.util.Optional;

@Repository
public interface AgencyRepository extends JpaRepository<Agency, Long> {
    Optional<Agency> findByCode(String code);
    List<Agency> findByCountryId(Long countryId);
    List<Agency> findByStatus(AgencyStatus status);
    Page<Agency> findByCountryId(Long countryId, Pageable pageable);
    Page<Agency> findByStatus(AgencyStatus status, Pageable pageable);
    Page<Agency> findByCountryIdAndStatus(Long countryId, AgencyStatus status, Pageable pageable);
}