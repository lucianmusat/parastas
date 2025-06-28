package online.lucianmusat.Parastas.domain.repositories;

import online.lucianmusat.Parastas.domain.entities.StateSettings;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface StateSettingsRepository  extends JpaRepository<StateSettings, Long>{

}
