package online.lucianmusat.Parastas.domain.repositories;

import online.lucianmusat.Parastas.domain.entities.Credentials;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CredentialsRepository  extends JpaRepository<Credentials, Long> {
    Optional<Credentials> findTopByOrderByIdAsc();
}
