package online.lucianmusat.Parastas.infrastructure;

import org.springframework.lang.NonNull;

public record DockerContainer(String id, String name) {

    public String shortID() {
        return this.id.substring(0, 12);
    }

    @Override
    @NonNull
    public String toString() {
        return this.name + " (" + this.shortID() + ")";
    }
}
