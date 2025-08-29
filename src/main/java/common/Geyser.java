package common;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

public class Geyser {
    private final BuildYml buildYml;

    public Geyser(BuildYml buildYml) {
        this.buildYml = buildYml;
    }

    public boolean simpleUpdateGeyser(String platform) {
        String latestVersionUrl;
        latestVersionUrl = "https://download.geysermc.org/v2/projects/geyser/versions/latest/builds/latest/downloads/" + platform;
        String outputFilePath = "plugins/Geyser-" + platform + ".jar";

        try (InputStream in = new URL(latestVersionUrl).openStream()) {
            java.io.File outFile = new java.io.File(outputFilePath);
            if (outFile.getParentFile() != null) {
                outFile.getParentFile().mkdirs();
            }
            try (FileOutputStream out = new FileOutputStream(outFile)) {
                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = in.read(buffer)) != -1) {
                    out.write(buffer, 0, bytesRead);
                }
            }
            return true;
        } catch (IOException e) {
            buildYml.getLogger().info("Failed to download Geyser jar: " + e.getMessage());
            return false;
        }
    }

    public boolean updateGeyser(String platform) {
        String apiUrl = "https://download.geysermc.org/v2/projects/geyser/versions/latest";

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode jsonNode = objectMapper.readTree(new URL(apiUrl));

            JsonNode buildsNode = jsonNode.get("builds");

            if (buildYml.getDownloadedBuild("Geyser") == -1) {
                if (simpleUpdateGeyser(platform)) {
                    buildYml.updateBuildNumber("Geyser", buildYml.getMaxBuildNumber(buildsNode));
                    return true;
                }
            } else if (buildYml.getDownloadedBuild("Geyser") != buildYml.getMaxBuildNumber(buildsNode)) {
                if (simpleUpdateGeyser(platform)) {
                    buildYml.updateBuildNumber("Geyser", buildYml.getMaxBuildNumber(buildsNode));
                    return true;
                }
            }
        } catch (Exception e) {
            buildYml.getLogger().info("Failed to update Geyser: " + e.getMessage());
        }
        return false;
    }
}
