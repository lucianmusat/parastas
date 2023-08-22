package online.lucianmusat.Parastas.utils;

import java.io.PrintStream;
import org.springframework.core.env.Environment;
import org.springframework.boot.Banner;
import org.springframework.boot.SpringBootVersion;

public class PBanner implements Banner {

    @Override
    public void printBanner(Environment environment, Class<?> sourceClass, PrintStream out) {
        out.println("__________                               __                 \n" +
                    "\\______   \\_____ ____________    _______/  |______    ______\n" +
                    "|     ___/\\__  \\\\_  __ \\__  \\  /  ___/\\   __\\__  \\  /  ___/\n" +
                    "|    |     / __ \\|  | \\// __ \\_\\___ \\  |  |  / __ \\_\\___ \\ \n" +
                    "|____|    (____  /__|  (____  /____  > |__| (____  /____  >\n" +
                    "               \\/           \\/     \\/            \\/     \\/ \n" +
                    getApplicationInfo(environment));
    }

    private String getApplicationInfo(Environment environment) {
        return String.format("Powered by Spring Boot %s", SpringBootVersion.getVersion());
    }
}
