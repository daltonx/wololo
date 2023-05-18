package Office;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.SecureRandom;

public class Util {
    private static final String tmpdir = System.getProperty("java.io.tmpdir");
    private static final SecureRandom random = new SecureRandom();

    public static Path newTempPath (String prefix, String suffix) {
        long n = random.nextLong();
        String nus = Long.toUnsignedString(n);
        String name = prefix + nus + suffix;
        return Path.of(tmpdir, name);
    }
    public static File writeTempFile (String prefix, String suffix, byte[] bytes) throws IOException {
        Path path = newTempPath(prefix, suffix);
        return Files.write(path, bytes).toFile();
    }
}
