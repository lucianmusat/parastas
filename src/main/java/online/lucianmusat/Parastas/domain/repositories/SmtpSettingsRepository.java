package online.lucianmusat.Parastas.domain.repositories;

import online.lucianmusat.Parastas.domain.SmtpSettings;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SmtpSettingsRepository extends JpaRepository<SmtpSettings, Long> {

}
