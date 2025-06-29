package online.lucianmusat.Parastas.domain.repositories;

import online.lucianmusat.Parastas.domain.entities.SmtpSettings;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SmtpSettingsRepository extends JpaRepository<SmtpSettings, Long> {
    Optional<SmtpSettings> findTopByOrderByIdAsc();
}
