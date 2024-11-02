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

public class BuildYml {

    public static String file;

    public static void createYamlFile(String folder) {
        file = folder + "/builds.yml";
        Path filePath = Paths.get(file);

        // for those who updated
        if (Files.exists(Paths.get(folder + "/doNotTouch.yml"))) {
            try {
                Files.delete(filePath);
                System.out.println("AutoUpdateGeyser old doNotTouch.yml file detected. Deleting it and regenerating builds.yml file...");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if (!Files.exists(filePath)) {
            DumperOptions options = new DumperOptions();
            options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);

            Yaml yaml = new Yaml(options);

            Map<String, Integer> initialData = Map.ofEntries(
                        Map.entry("Geyser", -1),
                        Map.entry("Floodgate", -1)
                );

            try (FileWriter writer = new FileWriter(filePath.toFile())) {
                yaml.dump(initialData, writer);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }


    }

    public static void updateBuildNumber(String key, int newBuildNumber) {
        try {
            Path filePath = Paths.get(file);
            Map<String, Integer> data = readYamlFile(filePath);

            if (data.containsKey(key)) {
                data.put(key, newBuildNumber);
                writeYamlFile(filePath, data);
                if(newBuildNumber != -1) {
                    System.out.println(key + " build number updated to " + newBuildNumber);
                }
            } else {
                System.out.println(key + " not found in the YAML file. Did you touch the builds.yml file? Regenerate it");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static int getDownloadedBuild(String key) {
        try {
            Path filePath = Paths.get(file);
            Map<String, Integer> data = readYamlFile(filePath);

            if (data.containsKey(key)) {
                return data.get(key);
            } else {
                System.out.println(key + " not found in the YAML file. Did you touch the builds.yml file? Regenerate it");
                return -1;
            }
        } catch (IOException e) {
            e.printStackTrace();
            return -1;
        }
    }

    private static Map<String, Integer> readYamlFile(Path filePath) throws IOException {
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

    private static void writeYamlFile(Path filePath, Map<String, Integer> data) throws IOException {
        DumperOptions options = new DumperOptions();
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);

        Yaml yaml = new Yaml(options);

        try (FileWriter writer = new FileWriter(filePath.toFile())) {
            yaml.dump(data, writer);
        }
    }

    public static int getMaxBuildNumber(JsonNode buildsNode) {
        int maxBuildNumber = Integer.MIN_VALUE;

        for (JsonNode buildNode : buildsNode) {
            int buildNumber = buildNode.asInt();
            maxBuildNumber = Math.max(maxBuildNumber, buildNumber);
        }

        return maxBuildNumber;
    }
}
