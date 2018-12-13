import java.io.IOException;
import java.nio.file.*;

/**
 * @autor slonikmak on 13.12.2018.
 */
public class TestFileWriting {
    public static void main(String[] args) {
        Path file = Paths.get("log.txt");
        for (int i = 0; i < 5; i++) {
            try {
                Files.write(file, ("str"+i+"\n").getBytes(), StandardOpenOption.APPEND);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
