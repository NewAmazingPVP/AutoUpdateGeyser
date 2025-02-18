package common;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.logging.Logger;

import static common.BuildYml.*;

public class Floodgate {

    private final BuildYml buildYml;

    public Floodgate(BuildYml buildYml) {
        this.buildYml = buildYml;
    }

    public void simpleUpdateFloodgate(String platform) {
        String latestVersionUrl;
        if(platform.equals("bungeecord")){
            platform = "bungee";
        }
        latestVersionUrl = "https://download.geysermc.org/v2/projects/floodgate/versions/latest/builds/latest/downloads/" + platform;
        String outputFilePath = "plugins/Floodgate-" + platform + ".jar";

        try (InputStream in = new URL(latestVersionUrl).openStream();
             FileOutputStream out = new FileOutputStream(outputFilePath)) {
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = in.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
            }
        } catch (IOException ignored) {
        }
    }

    public boolean updateFloodgate(String platform){
        String apiUrl = "https://download.geysermc.org/v2/projects/floodgate/versions/latest";

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode jsonNode = objectMapper.readTree(new URL(apiUrl));

            JsonNode buildsNode = jsonNode.get("builds");

            if (buildYml.getDownloadedBuild("Floodgate") == -1) {
                simpleUpdateFloodgate(platform);
                buildYml.updateBuildNumber("Floodgate", buildYml.getMaxBuildNumber(buildsNode));
                return true;
            } else if(buildYml.getDownloadedBuild("Floodgate") != buildYml.getMaxBuildNumber(buildsNode)){
                simpleUpdateFloodgate(platform);
                buildYml.updateBuildNumber("Floodgate", buildYml.getMaxBuildNumber(buildsNode));
                return true;
            }
        } catch (Exception e) {
            buildYml.getLogger().info("Failed to update Floodgate: " + e.getMessage());
        }
        return false;
    }


}

