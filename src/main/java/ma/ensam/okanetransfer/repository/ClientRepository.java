package ma.ensam.okanetransfer.repository;

import java.util.Optional;
import ma.ensam.okanetransfer.domain.user.Client;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClientRepository extends JpaRepository<Client, Long> {
    Optional<Client> findByEmailIgnoreCase(String email);

    Optional<Client> findByPhoneNumber(String phoneNumber);
}
