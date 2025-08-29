package common;

import com.fasterxml.jackson.databind.JsonNode;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.logging.Logger;

public class BuildYml {

    private Path filePath;
    private final Logger logger;

    public BuildYml(Logger logger) {
        this.logger = logger;
    }

    public void createYamlFile(String folder) {
        this.filePath = Paths.get(folder, "builds.yml");
        Path oldFilePath = Paths.get(folder, "doNotTouch.yml");
        if (Files.exists(oldFilePath)) {
            try {
                Files.delete(oldFilePath);
                logger.info("AutoUpdateGeyser old doNotTouch.yml file detected. Deleting it and regenerating builds.yml file...");
            } catch (IOException e) {
                logger.info("AutoUpdateGeyser failed to delete old doNotTouch.yml file...." + e.getMessage());
            }
        }
        if (!Files.exists(this.filePath)) {
            DumperOptions options = new DumperOptions();
            options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
            Yaml yaml = new Yaml(options);
            java.util.HashMap<String, Integer> initialData = new java.util.HashMap<String, Integer>();
            initialData.put("Geyser", -1);
            initialData.put("Floodgate", -1);
            try (FileWriter writer = new FileWriter(this.filePath.toFile())) {
                yaml.dump(initialData, writer);
            } catch (IOException e) {
                logger.info("AutoUpdateGeyser failed to create builds.yml file...." + e.getMessage());
            }
        }
    }

    public void updateBuildNumber(String key, int newBuildNumber) {
        try {
            ensureFilePath();
            Map<String, Integer> data = readYamlFile(this.filePath);
            if (data.containsKey(key)) {
                data.put(key, newBuildNumber);
                writeYamlFile(this.filePath, data);
                if (newBuildNumber != -1) {
                    logger.info(key + " build number updated to " + newBuildNumber);
                }
            } else {
                logger.info(key + " not found in the YAML file. Did you touch the builds.yml file? Regenerate it");
            }
        } catch (IOException e) {
            logger.info("Error reading or writing YAML file. Did you touch the builds.yml file? Regenerate it" + e.getMessage());
        }
    }

    public int getDownloadedBuild(String key) {
        try {
            ensureFilePath();
            Map<String, Integer> data = readYamlFile(this.filePath);
            if (data.containsKey(key)) {
                return data.get(key);
            } else {
                logger.info(key + " not found in the YAML file. Did you touch the builds.yml file? Regenerate it");
                return -1;
            }
        } catch (IOException e) {
            logger.info("Error reading YAML file. Did you touch the builds.yml file? Regenerate it" + e.getMessage());
            return -1;
        }
    }

    private Map<String, Integer> readYamlFile(Path filePath) throws IOException {
        Yaml yaml = new Yaml();
        try {
            Object obj = yaml.load(Files.newBufferedReader(filePath));
            if (obj instanceof Map) {
                Map<String, Integer> result = (Map<String, Integer>) obj;
                return result;
            } else {
                throw new RuntimeException("Invalid YAML file format. Expected a Map. Did you touch the builds.yml file? Regenerate it");
            }
        } catch (IOException e) {
            throw new IOException("Error reading YAML file. Did you touch the builds.yml file? Regenerate it", e);
        }
    }

    private void writeYamlFile(Path filePath, Map<String, Integer> data) throws IOException {
        DumperOptions options = new DumperOptions();
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        Yaml yaml = new Yaml(options);
        try (FileWriter writer = new FileWriter(filePath.toFile())) {
            yaml.dump(data, writer);
        }
    }

    public int getMaxBuildNumber(JsonNode buildsNode) {
        int maxBuildNumber = Integer.MIN_VALUE;
        for (JsonNode buildNode : buildsNode) {
            int buildNumber = buildNode.asInt();
            maxBuildNumber = Math.max(maxBuildNumber, buildNumber);
        }
        return maxBuildNumber;
    }

    public Logger getLogger() {
        return logger;
    }

    private void ensureFilePath() throws IOException {
        if (this.filePath == null) {
            throw new IOException("builds.yml not initialized. Call createYamlFile() first.");
        }
    }
}
