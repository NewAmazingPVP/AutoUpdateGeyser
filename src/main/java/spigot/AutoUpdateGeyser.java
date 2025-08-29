package spigot;

import common.BuildYml;
import common.Floodgate;
import common.Geyser;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

public final class AutoUpdateGeyser extends JavaPlugin {

    private Geyser m_geyser;
    private Floodgate m_floodgate;
    private FileConfiguration config;
    private Plugin ifGeyser;
    private Plugin ifFloodgate;
    private boolean configGeyser;
    private boolean configFloodgate;
    private BuildYml buildYml;

    @Override
    public void onEnable() {
        new Metrics(this, 18445);
        buildYml = new BuildYml(this.getLogger());
        m_geyser = new Geyser(buildYml);
        m_floodgate = new Floodgate(buildYml);
        loadConfiguration();
        buildYml.createYamlFile(getDataFolder().getAbsolutePath());
        updateChecker();
        if (getCommand("updategeyser") != null) {
            getCommand("updategeyser").setExecutor(new UpdateCommand());
        } else {
            getLogger().warning("Command 'updategeyser' not registered in plugin.yml");
        }
    }

    public void updateChecker() {
        ifGeyser = Bukkit.getPluginManager().getPlugin("Geyser-Spigot");
        ifFloodgate = Bukkit.getPluginManager().getPlugin("floodgate");
        int interval = config.getInt("updates.interval");
        long bootDelay = config.getInt("updates.bootTime");
        configGeyser = config.getBoolean("updates.geyser");
        configFloodgate = config.getBoolean("updates.floodgate");

        SchedulerCompat.scheduleUpdateRepeating(this, () -> {
            updatePlugin("Geyser", ifGeyser, configGeyser);
            updatePlugin("Floodgate", ifFloodgate, configFloodgate);
        }, bootDelay, interval);
    }

    private void updatePlugin(String pluginName, Object pluginInstance, boolean configCheck) {
        if (pluginInstance == null && configCheck) {
            buildYml.updateBuildNumber(pluginName, -1);
            if (updatePluginInstallation(pluginName)) {
                getLogger().info(ChatColor.GREEN + pluginName + " has been installed for the first time." + ChatColor.YELLOW + " Please restart the server again to let it take effect.");
                scheduleRestartIfAutoRestart();
            }
        } else if (configCheck) {
            if (updatePluginInstallation(pluginName)) {
                getLogger().info(ChatColor.GREEN + "New update of " + pluginName + " was downloaded." + ChatColor.YELLOW + " Please restart to let it take effect.");
                scheduleRestartIfAutoRestart();
            }
        }
    }

    private boolean updatePluginInstallation(String pluginName) {
        switch (pluginName) {
            case "Geyser":
                return m_geyser.updateGeyser("spigot");
            case "Floodgate":
                return m_floodgate.updateFloodgate("spigot");
            default:
                return false;
        }
    }

    private void scheduleRestartIfAutoRestart() {
        if (config.getBoolean("updates.autoRestart")) {
            getServer().broadcastMessage(ChatColor.translateAlternateColorCodes('&', config.getString("updates.restartMessage")));
            long delaySeconds = config.getInt("updates.restartDelay");
            SchedulerCompat.scheduleConsoleRestart(this, () ->
                            getServer().dispatchCommand(getServer().getConsoleSender(), "restart"),
                    delaySeconds
            );
        }
    }

    public void loadConfiguration() {
        saveDefaultConfig();
        config = getConfig();
        config.addDefault("updates.geyser", true);
        config.addDefault("updates.floodgate", true);
        config.addDefault("updates.interval", 60);
        config.addDefault("updates.bootTime", 5);
        config.addDefault("updates.autoRestart", false);
        config.addDefault("updates.restartDelay", 60);
        config.addDefault("updates.restartMessage", "Server is restarting shortly!");
        config.options().copyDefaults(true);
        saveConfig();
    }

    public class UpdateCommand implements CommandExecutor {
        @Override
        public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
            updatePlugin("Geyser", ifGeyser, configGeyser);
            updatePlugin("Floodgate", ifFloodgate, configFloodgate);
            sender.sendMessage(ChatColor.AQUA + "Update checker for Geyser and Floodgate successful!");
            return true;
        }
    }
}
