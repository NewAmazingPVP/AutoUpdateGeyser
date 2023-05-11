package velocity;

import com.moandjiezana.toml.Toml;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.plugin.PluginContainer;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import common.Floodgate;
import common.Geyser;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.scheduler.ScheduledTask;
import com.velocitypowered.api.proxy.ProxyServer;
import net.kyori.adventure.text.Component;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import net.kyori.adventure.text.format.NamedTextColor;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;

@Plugin(id = "autoupdategeyser", name = "AutoUpdateGeyser", version = "1.0.0")
public final class AutoUpdateGeyser {

    private Geyser m_geyser;
    private Floodgate m_floodgate;
    private ConfigurationNode config;
    private ProxyServer proxy;
    private ScheduledTask task;
    private final Path dataDirectory;

    @Inject
    public AutoUpdateGeyser(ProxyServer proxy, @DataDirectory Path dataDirectory) {
        this.proxy = proxy;
        this.dataDirectory = dataDirectory;
        loadConfig(dataDirectory);
    }


    @Subscribe
    public void onProxyInitialize(ProxyInitializeEvent event) {
        m_geyser = new Geyser();
        m_floodgate = new Floodgate();
        updateChecker();
    }

    public void updateChecker() {
        PluginContainer ifGeyser = proxy.getPluginManager().getPlugin("Geyser-Velocity").orElse(null);
        PluginContainer ifFloodgate = proxy.getPluginManager().getPlugin("floodgate").orElse(null);
        int interval = config.getNode("updates", "interval").getInt();
        long updateInterval = interval * 60L;
        long bootDelay = config.getNode("updates", "bootTime").getInt();

        boolean configGeyser = config.getNode("updates", "geyser").getBoolean();
        boolean configFloodgate = config.getNode("updates", "floodgate").getBoolean();

        task = proxy.getScheduler().buildTask(this, () -> {
            if (ifGeyser == null && configGeyser) {
                m_geyser.updateGeyser("velocity");
                proxy.getConsoleCommandSource().sendMessage(Component.text("Geyser has been installed for the first time. Please restart the server again to let it take in effect.", NamedTextColor.GREEN));
            } else if (configGeyser) {
                m_geyser.updateGeyser("velocity");
            }

            if (ifFloodgate == null && configFloodgate) {
                m_floodgate.updateFloodgate("velocity");
                proxy.getConsoleCommandSource().sendMessage(Component.text("Floodgate has been installed for the first time. Please restart the server again to let it take in effect.", NamedTextColor.GREEN));
            } else if (configFloodgate) {
                m_floodgate.updateFloodgate("velocity");
            }

            if (configGeyser || configFloodgate) {
                proxy.getConsoleCommandSource().sendMessage(Component.text("Periodic Updating Done.", NamedTextColor.AQUA));
            }
        }).delay(Duration.ofSeconds(bootDelay)).repeat(Duration.ofSeconds(updateInterval)).schedule();
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
                exception.printStackTrace();
                return null;
            }
        }
        return new Toml().read(file);
    }


}
