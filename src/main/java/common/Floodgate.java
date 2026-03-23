package common;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.net.URL;
import java.util.Locale;

public class Floodgate {

    private final BuildYml buildYml;

    public Floodgate(BuildYml buildYml) {
        this.buildYml = buildYml;
    }

    public boolean simpleUpdateFloodgate(String platform) {
        return simpleUpdateFloodgate(platform, new File("plugins"));
    }

    public boolean simpleUpdateFloodgate(String platform, File outputDirectory) {
        String downloadPlatform = resolveDownloadPlatform(platform);
        String latestVersionUrl = "https://download.geysermc.org/v2/projects/floodgate/versions/latest/builds/latest/downloads/" + downloadPlatform;
        String outputFileName = resolveOutputFileName(platform);

        return JarDownloadUtil.downloadJar(buildYml, latestVersionUrl, outputDirectory, outputFileName, "Floodgate");
    }

    public boolean updateFloodgate(String platform) {
        return updateFloodgate(platform, new File("plugins"));
    }

    public boolean updateFloodgate(String platform, File outputDirectory) {
        String apiUrl = "https://download.geysermc.org/v2/projects/floodgate/versions/latest";

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode jsonNode = objectMapper.readTree(new URL(apiUrl));

            JsonNode buildsNode = jsonNode.get("builds");

            if (buildYml.getDownloadedBuild("Floodgate") == -1) {
                if (simpleUpdateFloodgate(platform, outputDirectory)) {
                    buildYml.updateBuildNumber("Floodgate", buildYml.getMaxBuildNumber(buildsNode));
                    return true;
                }
            } else if (buildYml.getDownloadedBuild("Floodgate") != buildYml.getMaxBuildNumber(buildsNode)) {
                if (simpleUpdateFloodgate(platform, outputDirectory)) {
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
}

