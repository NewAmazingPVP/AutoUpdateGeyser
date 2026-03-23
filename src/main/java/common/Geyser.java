package common;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.net.URL;
import java.util.Locale;

public class Geyser {
    private final BuildYml buildYml;

    public Geyser(BuildYml buildYml) {
        this.buildYml = buildYml;
    }

    public boolean simpleUpdateGeyser(String platform) {
        return simpleUpdateGeyser(platform, new File("plugins"));
    }

    public boolean simpleUpdateGeyser(String platform, File outputDirectory) {
        String latestVersionUrl;
        latestVersionUrl = "https://download.geysermc.org/v2/projects/geyser/versions/latest/builds/latest/downloads/" + platform;
        String outputFileName = resolveOutputFileName(platform);

        return JarDownloadUtil.downloadJar(buildYml, latestVersionUrl, outputDirectory, outputFileName, "Geyser");
    }

    public boolean updateGeyser(String platform) {
        return updateGeyser(platform, new File("plugins"));
    }

    public boolean updateGeyser(String platform, File outputDirectory) {
        String apiUrl = "https://download.geysermc.org/v2/projects/geyser/versions/latest";

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode jsonNode = objectMapper.readTree(new URL(apiUrl));

            JsonNode buildsNode = jsonNode.get("builds");

            if (buildYml.getDownloadedBuild("Geyser") == -1) {
                if (simpleUpdateGeyser(platform, outputDirectory)) {
                    buildYml.updateBuildNumber("Geyser", buildYml.getMaxBuildNumber(buildsNode));
                    return true;
                }
            } else if (buildYml.getDownloadedBuild("Geyser") != buildYml.getMaxBuildNumber(buildsNode)) {
                if (simpleUpdateGeyser(platform, outputDirectory)) {
                    buildYml.updateBuildNumber("Geyser", buildYml.getMaxBuildNumber(buildsNode));
                    return true;
                }
            }
        } catch (Exception e) {
            buildYml.getLogger().info("Failed to update Geyser: " + e.getMessage());
        }
        return false;
    }

    private String resolveOutputFileName(String platform) {
        String normalized = platform == null ? "" : platform.toLowerCase(Locale.ROOT);
        switch (normalized) {
            case "spigot":
                return "Geyser-Spigot.jar";
            case "bungeecord":
                return "Geyser-BungeeCord.jar";
            case "velocity":
                return "Geyser-Velocity.jar";
            case "standalone":
                return "Geyser-Standalone.jar";
            default:
                return "Geyser-" + (platform == null ? "" : platform) + ".jar";
        }
    }
}
