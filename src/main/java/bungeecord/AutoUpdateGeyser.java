package bungeecord;

import common.Floodgate;
import common.Geyser;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;
import net.md_5.bungee.api.ChatColor;

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
        getLogger().info(".");
    }

    public void updateChecker() {
        Plugin ifGeyser = getProxy().getPluginManager().getPlugin("Geyser-BungeeCord");
        Plugin ifFloodgate = getProxy().getPluginManager().getPlugin("floodgate");
        int interval = config.getInt("updates.interval");
        long updateInterval = interval * 60L;
        long bootDelay = config.getInt("updates.bootTime");

        boolean configGeyser = config.getBoolean("updates.geyser");
        boolean configFloodgate = config.getBoolean("updates.floodgate");

        getProxy().getScheduler().schedule(this, () -> {
            if (ifGeyser == null && configGeyser) {
                m_geyser.updateGeyser("bungeecord");
                getLogger().info(ChatColor.GREEN + "Geyser has been installed for the first time." + ChatColor.YELLOW + " Please restart the server again to let it take in effect.");
            } else if (configGeyser) {
                m_geyser.updateGeyser("bungeecord");
            }

            if (ifFloodgate == null && configFloodgate) {
                m_floodgate.updateFloodgate("bungee");
                getLogger().info(ChatColor.GREEN + "Floodgate has been installed for the first time." + ChatColor.YELLOW + " Please restart the server again to let it take in effect.");
            } else if (configFloodgate) {
                m_floodgate.updateFloodgate("bungee");
            }

            if (configGeyser || configFloodgate) {
                getLogger().info(ChatColor.AQUA + "Periodic Updating Done.");
            }
        }, bootDelay, updateInterval, TimeUnit.SECONDS);
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
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
