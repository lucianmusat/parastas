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
}
