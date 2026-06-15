package ma.ensam.okanetransfer.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import ma.ensam.okanetransfer.domain.transfer.Beneficiary;

import java.util.List;
import java.util.Optional;

@Repository
public interface BeneficiaryRepository extends JpaRepository<Beneficiary, Long> {
    List<Beneficiary> findByClientId(Long clientId);
    Page<Beneficiary> findByClientId(Long clientId, Pageable pageable);
    Optional<Beneficiary> findByPhoneNumber(String phoneNumber);
}