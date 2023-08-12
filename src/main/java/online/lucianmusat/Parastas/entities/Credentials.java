package online.lucianmusat.Parastas.entities;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class Credentials {

    @Value("${PARASTAS_USERNAME}")
    private String username;

    @Value("${PARASTAS_PASSWORD}")
    private String password;

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }
}

