package online.lucianmusat.Parastas.entities;

import org.springframework.stereotype.Component;

@Component
public class Credentials {

    private String username = "admin";
    private String password = "admin";

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

