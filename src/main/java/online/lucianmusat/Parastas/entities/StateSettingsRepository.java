package online.lucianmusat.Parastas.entities;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface StateSettingsRepository  extends JpaRepository<StateSettings, Long>{

}
