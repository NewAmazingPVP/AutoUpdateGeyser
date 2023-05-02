package spigot;

import common.Floodgate;
import common.Geyser;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.Bukkit;

public final class AutoUpdateGeyser extends JavaPlugin {

    private Geyser m_geyser;
    private Floodgate m_floodgate;

    @Override
    public void onEnable() {
        m_geyser = new Geyser();
        m_floodgate = new Floodgate();

        saveDefaultConfig();

        FileConfiguration config = getConfig();

        config.addDefault("updates.geyser", true);
        config.addDefault("updates.floodgate", false);
        config.addDefault("updates.interval", 60);


        config.options().copyDefaults(true);
        saveConfig();

        updateChecker();
    }

    public void updateChecker() {
        Plugin ifGeyser = Bukkit.getPluginManager().getPlugin("Geyser-spigot");
        Plugin ifFloodgate = Bukkit.getPluginManager().getPlugin("Floodgate");
        if(ifGeyser == null || ifFloodgate == null)
        {
            if(ifGeyser == null){
                getLogger().info("Geyser was not installed therefore installing it now");
            } else if(ifFloodgate == null)
            {
                getLogger().info("Floodgate was not installed therefore installing it now");
            }

        }
        else {
            getLogger().info("Geyser was found!");
        }

        Bukkit.getScheduler().runTaskTimer(this, new Runnable() {
            @Override
            public void run() {
                m_geyser.updateGeyser("spigot");
                m_floodgate.updateFloodgate("spigot");}
        }, 300L, 20L * 60L * 2);
    }

}
