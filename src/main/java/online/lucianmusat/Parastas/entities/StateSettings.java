package online.lucianmusat.Parastas.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;


@Entity
public class StateSettings {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private int refreshPeriodSeconds = 30;

    public void setRefreshPeriodSeconds(int refreshPeriod) {
        this.refreshPeriodSeconds = refreshPeriod;
    }

    public int getRefreshPeriodSeconds() {
        return refreshPeriodSeconds;
    }
}
