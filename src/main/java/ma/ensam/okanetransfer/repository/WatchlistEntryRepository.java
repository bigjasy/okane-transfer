package ma.ensam.okanetransfer.repository;

import java.util.List;
import ma.ensam.okanetransfer.domain.compliance.WatchlistEntry;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WatchlistEntryRepository extends JpaRepository<WatchlistEntry, Long> {
    List<WatchlistEntry> findByLastNameIgnoreCaseAndActiveTrue(String lastName);

    Page<WatchlistEntry> findByActiveTrue(Pageable pageable);
}
