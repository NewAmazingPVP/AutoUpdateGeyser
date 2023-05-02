package spigot;

import common.Floodgate;
import common.Geyser;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.Bukkit;
import java.io.File;
import java.io.IOException;
import org.bukkit.configuration.file.YamlConfiguration;

public final class AutoUpdateGeyser extends JavaPlugin {

    private Geyser m_geyser;
    private Floodgate m_floodgate;
    private FileConfiguration config;

    @Override
    public void onEnable() {
        m_geyser = new Geyser();
        m_floodgate = new Floodgate();
        config = getConfig();

        if (!getDataFolder().exists()) {
            getDataFolder().mkdir();
        }
        File file = new File(getDataFolder(), "config.yml");
        if (!file.exists()) {
            try {
                file.createNewFile();

                FileConfiguration configuration = YamlConfiguration.loadConfiguration(file);

                configuration.set("Geyser.enabled", true);
                configuration.set("Geyser.dev", false);
                configuration.set("Floodgate.enabled", true);
                configuration.set("Floodgate.dev", false);
                configuration.set("Check-Interval", 30);

                configuration.save(file);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        updateChecker();
    }

    public void updateChecker() {
        Bukkit.getScheduler().runTaskTimer(this, new Runnable() {
            @Override
            public void run() {
                m_geyser.updateGeyser("spigot");
                m_floodgate.updateFloodgate("spigot");}
        }, 300L, 20L * 60L * 2);
    }

    public FileConfiguration getConfig() {
        try {
            File configFile = new File(getDataFolder(), "config.yml");
            FileConfiguration configuration = YamlConfiguration.loadConfiguration(configFile);
            return configuration;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
