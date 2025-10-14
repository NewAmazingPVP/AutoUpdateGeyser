package common;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.util.Locale;

public class Floodgate {

    private final BuildYml buildYml;

    public Floodgate(BuildYml buildYml) {
        this.buildYml = buildYml;
    }

    public boolean simpleUpdateFloodgate(String platform) {
        String downloadPlatform = resolveDownloadPlatform(platform);
        String latestVersionUrl = "https://download.geysermc.org/v2/projects/floodgate/versions/latest/builds/latest/downloads/" + downloadPlatform;
        String outputFileName = resolveOutputFileName(platform);

        try (InputStream in = new URL(latestVersionUrl).openStream()) {
            File outFile = new File("plugins", outputFileName);
            if (outFile.getParentFile() != null) {
                outFile.getParentFile().mkdirs();
            }
            clearCaseInsensitiveDuplicates(outFile);
            try (FileOutputStream out = new FileOutputStream(outFile)) {
                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = in.read(buffer)) != -1) {
                    out.write(buffer, 0, bytesRead);
                }
            }
            return true;
        } catch (IOException e) {
            buildYml.getLogger().info("Failed to download Floodgate jar: " + e.getMessage());
            return false;
        }
    }

    public boolean updateFloodgate(String platform) {
        String apiUrl = "https://download.geysermc.org/v2/projects/floodgate/versions/latest";

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode jsonNode = objectMapper.readTree(new URL(apiUrl));

            JsonNode buildsNode = jsonNode.get("builds");

            if (buildYml.getDownloadedBuild("Floodgate") == -1) {
                if (simpleUpdateFloodgate(platform)) {
                    buildYml.updateBuildNumber("Floodgate", buildYml.getMaxBuildNumber(buildsNode));
                    return true;
                }
            } else if (buildYml.getDownloadedBuild("Floodgate") != buildYml.getMaxBuildNumber(buildsNode)) {
                if (simpleUpdateFloodgate(platform)) {
                    buildYml.updateBuildNumber("Floodgate", buildYml.getMaxBuildNumber(buildsNode));
                    return true;
                }
            }
        } catch (Exception e) {
            buildYml.getLogger().info("Failed to update Floodgate: " + e.getMessage());
        }
        return false;
    }

    private String resolveDownloadPlatform(String platform) {
        if (platform == null) {
            return "";
        }
        return "bungeecord".equalsIgnoreCase(platform) ? "bungee" : platform.toLowerCase(Locale.ROOT);
    }

    private String resolveOutputFileName(String platform) {
        String normalized = platform == null ? "" : platform.toLowerCase(Locale.ROOT);
        switch (normalized) {
            case "spigot":
                return "floodgate-spigot.jar";
            case "bungeecord":
            case "bungee":
                return "floodgate-bungee.jar";
            case "velocity":
                return "floodgate-velocity.jar";
            default:
                return "floodgate-" + (normalized.isEmpty() ? "" : normalized) + ".jar";
        }
    }

    private void clearCaseInsensitiveDuplicates(File target) throws IOException {
        Files.deleteIfExists(target.toPath());

        File parent = target.getParentFile();
        if (parent == null || !parent.exists()) {
            return;
        }

        File[] siblings = parent.listFiles();
        if (siblings == null) {
            return;
        }

        for (File sibling : siblings) {
            if (sibling.getName().equalsIgnoreCase(target.getName())) {
                Files.deleteIfExists(sibling.toPath());
            }
        }
    }
}

