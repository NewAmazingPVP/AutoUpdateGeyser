package velocity;

import com.google.inject.Inject;
import com.moandjiezana.toml.Toml;
import com.velocitypowered.api.command.CommandManager;
import com.velocitypowered.api.command.CommandMeta;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.PluginContainer;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import common.BuildYml;
import common.Floodgate;
import common.Geyser;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.logging.Logger;


@Plugin(id = "autoupdategeyser", name = "AutoUpdateGeyser", version = "7.0.0", url = "https://www.spigotmc.org/resources/autoupdategeyser.109632/", authors = "NewAmazingPVP")
public final class AutoUpdateGeyser {

    private Geyser m_geyser;
    private Floodgate m_floodgate;
    private final Toml config;
    private final ProxyServer proxy;
    private final Metrics.Factory metricsFactory;
    private final Path dataDirectory;
    private PluginContainer ifGeyser;
    private PluginContainer ifFloodgate;
    private boolean configGeyser;
    private boolean configFloodgate;
    private BuildYml buildYml;

    @Inject
    public AutoUpdateGeyser(ProxyServer proxy, @DataDirectory Path dataDirectory, Metrics.Factory metricsFactory) {
        this.proxy = proxy;
        config = loadConfig(dataDirectory);
        this.metricsFactory = metricsFactory;
        this.dataDirectory = dataDirectory;
    }

    @Subscribe
    public void onProxyInitialize(ProxyInitializeEvent event) {
        metricsFactory.make(this, 18448);
        buildYml = new BuildYml(Logger.getLogger("AutoUpdateGeyser"));
        m_geyser = new Geyser(buildYml);
        m_floodgate = new Floodgate(buildYml);
        buildYml.createYamlFile(dataDirectory.toAbsolutePath().toString());
        updateChecker();
        CommandManager commandManager = proxy.getCommandManager();
        CommandMeta commandMeta = commandManager.metaBuilder("updategeyser")
                .plugin(this)
                .build();

        SimpleCommand simpleCommand = new UpdateCommand();
        commandManager.register(commandMeta, simpleCommand);
    }

    public void updateChecker() {
        ifGeyser = proxy.getPluginManager().getPlugin("geyser").orElse(null);
        ifFloodgate = proxy.getPluginManager().getPlugin("floodgate").orElse(null);
        long interval = config.getLong("updates.interval");
        long bootDelay = config.getLong("updates.bootTime");
        configGeyser = config.getBoolean("updates.geyser");
        configFloodgate = config.getBoolean("updates.floodgate");

        proxy.getScheduler().buildTask(this, () -> {
            updatePlugin("Geyser", ifGeyser, configGeyser);
            updatePlugin("Floodgate", ifFloodgate, configFloodgate);

        }).delay(Duration.ofSeconds(bootDelay)).repeat(Duration.ofMinutes(interval)).schedule();
    }

    private void updatePlugin(String pluginName, Object pluginInstance, boolean configCheck) {
        if (pluginInstance == null && configCheck) {
            buildYml.updateBuildNumber(pluginName, -1);
            if (updatePluginInstallation(pluginName)) {
                proxy.getConsoleCommandSource().sendMessage(Component.text(pluginName + " has been installed for the first time. Please restart the server again to let it take effect.", NamedTextColor.GREEN));
                scheduleRestartIfAutoRestart();
            }
        } else if (configCheck) {
            if (updatePluginInstallation(pluginName)) {
                proxy.getConsoleCommandSource().sendMessage(Component.text("New update of " + pluginName + " was downloaded. Please restart to let it take effect.", NamedTextColor.GREEN));
                scheduleRestartIfAutoRestart();
            }
        }
    }

    private boolean updatePluginInstallation(String pluginName) {
        switch (pluginName) {
            case "Geyser":
                return m_geyser.updateGeyser("velocity");
            case "Floodgate":
                return m_floodgate.updateFloodgate("velocity");
            default:
                return false;
        }
    }

    private void scheduleRestartIfAutoRestart() {
        if (config.getBoolean("updates.autoRestart")) {
            proxy.sendMessage(Component.text(config.getString("updates.restartMessage")));
            proxy.getScheduler().buildTask(this, () -> {
                proxy.getCommandManager().executeAsync(proxy.getConsoleCommandSource(), "shutdown");
            }).delay(Duration.ofSeconds(config.getLong("updates.restartDelay"))).schedule();
        }
    }


    private Toml loadConfig(Path path) {
        File folder = path.toFile();
        File file = new File(folder, "config.toml");

        if (!file.exists()) {
            if (!file.getParentFile().exists()) {
                file.getParentFile().mkdirs();
            }
            try (InputStream input = getClass().getResourceAsStream("/" + file.getName())) {
                if (input != null) {
                    Files.copy(input, file.toPath());
                } else {
                    file.createNewFile();
                }
            } catch (IOException exception) {
                Logger.getLogger("AutoUpdateGeyser").warning("Failed to create config file: " + exception.getMessage());
                return new Toml();
            }
        }
        return new Toml().read(file);
    }

    public class UpdateCommand implements SimpleCommand {
        @Override
        public boolean hasPermission(final Invocation invocation) {
            return invocation.source().hasPermission("autoupdategeyser.admin");
        }

        @Override
        public void execute(Invocation invocation) {
            CommandSource source = invocation.source();
            updatePlugin("Geyser", ifGeyser, configGeyser);
            updatePlugin("Floodgate", ifFloodgate, configFloodgate);
            source.sendMessage(Component.text("Update checker for Geyser and Floodgate successful!").color(NamedTextColor.AQUA));
        }
    }

}
