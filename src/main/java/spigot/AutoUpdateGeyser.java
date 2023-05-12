package spigot;

import common.Floodgate;
import common.Geyser;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.Bukkit;
import spigot.Metrics;

public final class AutoUpdateGeyser extends JavaPlugin {

    private Geyser m_geyser;
    private Floodgate m_floodgate;
    private FileConfiguration config;

    @Override
    public void onEnable() {
        new Metrics(this, 18445);
        m_geyser = new Geyser();
        m_floodgate = new Floodgate();
        loadConfiguration();
        updateChecker();
    }

    public void updateChecker() {
        Plugin ifGeyser = Bukkit.getPluginManager().getPlugin("Geyser-spigot");
        Plugin ifFloodgate = Bukkit.getPluginManager().getPlugin("floodgate");
        int interval = config.getInt("updates.interval");
        long bootDelay = config.getInt("updates.bootTime");
        boolean configGeyser = config.getBoolean("updates.geyser");
        boolean configFloodgate = config.getBoolean("updates.floodgate");

        Bukkit.getScheduler().runTaskTimer(this, new Runnable() {
            @Override
            public void run() {
                if (ifGeyser == null && configGeyser) {
                    m_geyser.updateGeyser("spigot");
                    getLogger().info(ChatColor.GREEN + "Geyser has been installed for the first time." + ChatColor.YELLOW + " Please restart the server again to let it take in effect.");
                } else if (configGeyser) {
                    m_geyser.updateGeyser("spigot");
                }

                if (ifFloodgate == null && configFloodgate) {
                    m_floodgate.updateFloodgate("spigot");
                    getLogger().info(ChatColor.GREEN + "Floodgate has been installed for the first time." + ChatColor.YELLOW + " Please restart the server again to let it take in effect.");
                } else if (configFloodgate) {
                    m_floodgate.updateFloodgate("spigot");
                }

                if (configGeyser || configFloodgate) {
                    getLogger().info(ChatColor.AQUA + "Periodic Updating Done.");
                }
            }
        }, bootDelay, 20L * 60L * interval);
    }

    public void loadConfiguration(){
        saveDefaultConfig();

        config = getConfig();

        config.addDefault("updates.geyser", true);
        config.addDefault("updates.floodgate", false);
        config.addDefault("updates.interval", 60);

        config.options().copyDefaults(true);
        saveConfig();
    }
}
