package common;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

final class JarDownloadUtil {

    private JarDownloadUtil() {
    }

    static boolean downloadJar(BuildYml buildYml, String latestVersionUrl, File outputDirectory, String outputFileName, String pluginName) {
        File outFile = new File(outputDirectory, outputFileName);
        File parent = outFile.getParentFile();

        try (InputStream in = new URL(latestVersionUrl).openStream()) {
            if (parent != null) {
                parent.mkdirs();
            }

            clearCaseInsensitiveDuplicates(outFile);

            Path tempFile = Files.createTempFile(parent == null ? new File(".").toPath() : parent.toPath(), "download-", ".part");
            try {
                try (FileOutputStream out = new FileOutputStream(tempFile.toFile())) {
                    byte[] buffer = new byte[8192];
                    int bytesRead;
                    while ((bytesRead = in.read(buffer)) != -1) {
                        out.write(buffer, 0, bytesRead);
                    }
                }

                moveIntoPlace(tempFile, outFile.toPath());
            } finally {
                Files.deleteIfExists(tempFile);
            }

            return true;
        } catch (IOException e) {
            buildYml.getLogger().info("Failed to download " + pluginName + " jar: " + e.getMessage());
            return false;
        }
    }

    private static void moveIntoPlace(Path source, Path target) throws IOException {
        try {
            Files.move(source, target, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (AtomicMoveNotSupportedException ignored) {
            Files.move(source, target, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    private static void clearCaseInsensitiveDuplicates(File target) throws IOException {
        File parent = target.getParentFile();
        if (parent == null || !parent.exists()) {
            return;
        }

        File[] siblings = parent.listFiles();
        if (siblings == null) {
            return;
        }

        for (File sibling : siblings) {
            if (sibling.getName().equals(target.getName())) {
                continue;
            }
            if (sibling.getName().equalsIgnoreCase(target.getName())) {
                Files.deleteIfExists(sibling.toPath());
            }
        }
    }
}
