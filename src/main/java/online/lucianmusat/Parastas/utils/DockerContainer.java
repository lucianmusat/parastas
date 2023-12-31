package online.lucianmusat.Parastas.utils;

public class DockerContainer {
    private final String id;
    private final String name;

    public DockerContainer(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public String id() {
        return this.id;
    }

    public String name() {
        return this.name;
    }

    public String shortID() {
        return this.id.substring(0, 12);
    }

    public String toString() {
        return this.name + " (" + this.shortID() + ")";
    }
}
