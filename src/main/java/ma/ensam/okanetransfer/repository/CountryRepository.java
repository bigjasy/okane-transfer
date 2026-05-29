package ma.ensam.okanetransfer.repository;

import java.util.List;
import java.util.Optional;
import ma.ensam.okanetransfer.domain.referential.Country;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CountryRepository extends JpaRepository<Country, Long> {
    Optional<Country> findByIsoCode(String isoCode);

    List<Country> findByActiveTrue();
}
