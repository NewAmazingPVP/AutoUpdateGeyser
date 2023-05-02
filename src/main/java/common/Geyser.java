package common;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

public class Geyser {

    public void updateGeyser(String platform) {
        String latestVersionUrl;
        latestVersionUrl = "https://download.geysermc.org/v2/projects/geyser/versions/latest/builds/latest/downloads/" + platform;
        String outputFilePath = "plugins/Geyser-" + platform + ".jar";

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
}

