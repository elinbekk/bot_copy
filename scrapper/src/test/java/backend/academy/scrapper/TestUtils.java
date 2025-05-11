package backend.academy.scrapper;

import java.nio.file.Files;
import java.nio.file.Paths;

public class TestUtils {
    public static String loadFixture(String path) throws Exception {
        return Files.readString(
                Paths.get(TestUtils.class.getClassLoader().getResource(path).toURI()));
    }
}
