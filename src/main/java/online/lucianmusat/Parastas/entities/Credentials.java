package online.lucianmusat.Parastas.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity
public class Credentials {

    @Id
    private Long id = 1L;
    private String username;
    private String password;

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public void setUsername(String newUsername) {
        username = newUsername;
    }

    public void setPassword(String newPassword) {
        password = newPassword;
    }
}

