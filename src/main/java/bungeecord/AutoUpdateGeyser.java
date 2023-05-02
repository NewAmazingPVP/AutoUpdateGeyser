package bungeecord;

import common.Floodgate;
import common.Geyser;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.concurrent.TimeUnit;

public final class AutoUpdateGeyser extends Plugin {

    private Geyser m_geyser;
    private Floodgate m_floodgate;

    @Override
    public void onEnable() {
        m_geyser = new Geyser();
        m_floodgate = new Floodgate();

        File configFile = new File(getDataFolder(), "config.yml");
        if (!configFile.exists()) {
            configFile.getParentFile().mkdirs();
            try (InputStream in = getClass().getResourceAsStream("/config.yml")) {
                Files.copy(in, configFile.toPath());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        Configuration config = null;
        try {
            config = ConfigurationProvider.getProvider(YamlConfiguration.class).load(configFile);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        if (!config.contains("updates.geyser")) {
            config.set("updates.geyser", true);
        }
        if (!config.contains("updates.floodgate")) {
            config.set("updates.floodgate", false);
        }
        if (!config.contains("updates.interval")) {
            config.set("updates.interval", 60);
        }

        try {
            ConfigurationProvider.getProvider(YamlConfiguration.class).save(config, configFile);
        } catch (IOException ignored) {
        }
        updateChecker();
    }

    public void updateChecker() {
        getProxy().getScheduler().schedule(this, () -> {
            m_geyser.updateGeyser("bungeecord");
            m_floodgate.updateFloodgate("bungee");
        }, 20L, 60L, TimeUnit.SECONDS);
    }


}
