package bungeecord;

import common.Floodgate;
import common.Geyser;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;
import net.md_5.bungee.api.ChatColor;

import java.io.*;
import java.nio.file.Files;
import java.util.concurrent.TimeUnit;

import static common.BuildYml.createYamlFile;
import static common.BuildYml.updateBuildNumber;

public final class AutoUpdateGeyser extends Plugin {

    private Geyser m_geyser;
    private Floodgate m_floodgate;
    private Configuration config;
    private Plugin ifGeyser;
    private Plugin ifFloodgate;
    private boolean configGeyser;
    private boolean configFloodgate;

    @Override
    public void onEnable() {
        new Metrics(this, 18449);
        m_geyser = new Geyser();
        m_floodgate = new Floodgate();
        saveDefaultConfig();
        loadConfiguration();
        createYamlFile(getDataFolder().getAbsolutePath());
        updateChecker();
        ProxyServer.getInstance().getPluginManager().registerCommand(this, new UpdateCommand());
    }

    public void updateChecker() {
        ifGeyser = getProxy().getPluginManager().getPlugin("Geyser-BungeeCord");
        ifFloodgate = getProxy().getPluginManager().getPlugin("floodgate");
        int interval = config.getInt("updates.interval");
        long updateInterval = interval * 60L;
        long bootDelay = config.getInt("updates.bootTime");
        configGeyser = config.getBoolean("updates.geyser");
        configFloodgate = config.getBoolean("updates.floodgate");

        getProxy().getScheduler().schedule(this, () -> {
            getProxy().getScheduler().runAsync(this, () -> {
                updatePlugin("Geyser", ifGeyser, configGeyser);
                updatePlugin("Floodgate", ifFloodgate, configFloodgate);
            });
        }, bootDelay, updateInterval, TimeUnit.SECONDS);
    }

    private void updatePlugin(String pluginName, Object pluginInstance, boolean configCheck) {
        if (pluginInstance == null && configCheck) {
            updateBuildNumber(pluginName, -1);
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
        return switch (pluginName) {
            case "Geyser" -> m_geyser.updateGeyser("bungeecord");
            case "Floodgate" -> m_floodgate.updateFloodgate("bungeecord");
            default -> false;
        };
    }

    private void scheduleRestartIfAutoRestart() {
        if (config.getBoolean("updates.autoRestart")) {
            getLogger().info(ChatColor.RED + "Restarting in 10 seconds");
            ProxyServer.getInstance().getScheduler().schedule(this, () -> {
                ProxyServer.getInstance().getPluginManager().dispatchCommand(ProxyServer.getInstance().getConsole(), "end");
            }, 10, TimeUnit.SECONDS);
        }
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

    public class UpdateCommand extends Command {

        public UpdateCommand() {
            super("updategeyser", "autoupdategeyser.admin");
        }

        @Override
        public void execute(CommandSender sender, String[] args) {
            updatePlugin("Geyser", ifGeyser, configGeyser);
            updatePlugin("Floodgate", ifFloodgate, configFloodgate);
            sender.sendMessage(ChatColor.AQUA + "Update checker for Geyser and Floodgate successful!");
        }
    }
}
