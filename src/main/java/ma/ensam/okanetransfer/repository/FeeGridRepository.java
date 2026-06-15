package ma.ensam.okanetransfer.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import ma.ensam.okanetransfer.domain.agency.FeeGrid;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface FeeGridRepository extends JpaRepository<FeeGrid, Long> {
    
    @Query("SELECT f FROM FeeGrid f WHERE f.corridor.id = :corridorId AND f.active = true " +
           "AND :amount >= f.minAmount AND :amount <= f.maxAmount " +
           "AND :date >= f.validFrom AND (f.validTo IS NULL OR :date <= f.validTo)")
    Optional<FeeGrid> findActiveGrid(@Param("corridorId") Long corridorId, 
                                     @Param("amount") BigDecimal amount, 
                                     @Param("date") LocalDate date);

    Page<FeeGrid> findByCorridorId(Long corridorId, Pageable pageable);

    @Query("SELECT COUNT(f) > 0 FROM FeeGrid f WHERE f.corridor.id = :corridorId " +
           "AND f.active = true " +
           "AND f.id != :excludeId " + 
           "AND (f.minAmount <= :max AND f.maxAmount >= :min)")
    boolean hasOverlappingActiveGrids(
            @Param("corridorId") Long corridorId,
            @Param("min") BigDecimal min,
            @Param("max") BigDecimal max,
            @Param("excludeId") Long excludeId
    );
}
