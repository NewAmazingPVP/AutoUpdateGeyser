package spigot;

import common.Floodgate;
import common.Geyser;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.Bukkit;

public final class AutoUpdateGeyser extends JavaPlugin {

    private Geyser m_geyser;
    private Floodgate m_floodgate;
    private FileConfiguration config;

    @Override
    public void onEnable() {
        m_geyser = new Geyser();
        m_floodgate = new Floodgate();
        loadConfiguration();
        updateChecker();
    }

    public void updateChecker() {
        Plugin ifGeyser = Bukkit.getPluginManager().getPlugin("Geyser-spigot");
        Plugin ifFloodgate = Bukkit.getPluginManager().getPlugin("Floodgate");
        if(ifGeyser == null || ifFloodgate == null)
        {
            if(ifGeyser == null){
                m_geyser.updateGeyser("spigot");
                getLogger().info(ChatColor.GREEN + "Geyser has been installed for the first time." + ChatColor.YELLOW + " Please restart the serve again to let it take in effect.");
            } else {
                m_floodgate.updateFloodgate("spigot");
                getLogger().info(ChatColor.GREEN + "Floodgate has been installed for the first time." + ChatColor.YELLOW + " Please restart the serve again to let it take in effect.");
            }
        }

        int interval = config.getInt("updates.interval");
        Bukkit.getScheduler().runTaskTimer(this, new Runnable() {
            @Override
            public void run() {
                m_geyser.updateGeyser("spigot");
                m_floodgate.updateFloodgate("spigot");
                getLogger().info(ChatColor.AQUA + "Periodic Updating Done.");
            }
        }, 300L, 20L * 60L * interval);
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
