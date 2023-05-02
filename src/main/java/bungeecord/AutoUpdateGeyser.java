package bungeecord;

import common.Floodgate;
import common.Geyser;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;

import java.io.*;
import java.nio.file.Files;
import java.util.concurrent.TimeUnit;

public final class AutoUpdateGeyser extends Plugin {

    private Geyser m_geyser;
    private Floodgate m_floodgate;
    private Configuration config;

    @Override
    public void onEnable() {
        m_geyser = new Geyser();
        m_floodgate = new Floodgate();

        saveDefaultConfig();
        loadConfiguration();
        updateChecker();
    }

    public void updateChecker() {
        getProxy().getScheduler().schedule(this, () -> {
            m_geyser.updateGeyser("bungeecord");
            m_floodgate.updateFloodgate("bungee");
        }, 20L, 60L, TimeUnit.SECONDS);
    }

    private void saveDefaultConfig() {
        File file = new File(getDataFolder(), "config.yml");
        if (!file.exists()) {
            file.getParentFile().mkdirs();
            try (InputStream in = getClass().getResourceAsStream("/config.yml")) {
                Files.copy(in, file.toPath());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    private void loadConfiguration() {
        File file = new File(getDataFolder(), "config.yml");
        try {
            config = ConfigurationProvider.getProvider(YamlConfiguration.class).load(file);
            if (!config.contains("updates.geyser")) {
                config.set("updates.geyser", true);
                saveConfiguration(file);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void saveConfiguration(File file) {
        try {
            ConfigurationProvider.getProvider(YamlConfiguration.class).save(config, file);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
