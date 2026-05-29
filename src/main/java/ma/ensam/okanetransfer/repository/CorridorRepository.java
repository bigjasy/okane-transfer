package ma.ensam.okanetransfer.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ma.ensam.okanetransfer.domain.agency.Corridor;
import java.util.List;
import java.util.Optional;

@Repository
public interface CorridorRepository extends JpaRepository<Corridor, Long> {
    Optional<Corridor> findBySourceCountryIdAndDestinationCountryId(Long sourceCountryId, Long destinationCountryId);
    List<Corridor> findByActiveTrue();
}