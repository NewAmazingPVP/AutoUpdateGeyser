package velocity;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import com.moandjiezana.toml.Toml;
import com.velocitypowered.api.command.CommandManager;
import com.velocitypowered.api.command.CommandMeta;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.plugin.PluginContainer;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.messages.ChannelIdentifier;
import com.velocitypowered.api.proxy.messages.MinecraftChannelIdentifier;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import common.Floodgate;
import common.Geyser;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.proxy.ProxyServer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;

import com.google.inject.Inject;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static common.BuildYml.createYamlFile;
import static common.BuildYml.updateBuildNumber;

@Plugin(id = "autoupdategeyser",name = "AutoUpdateGeyser",version = "6.0", url = "https://www.spigotmc.org/resources/autoupdategeyser.109632/",authors = "NewAmazingPVP")
public final class AutoUpdateGeyser {

    private Geyser m_geyser;
    private Floodgate m_floodgate;
    private Toml config;
    private ProxyServer proxy;
    private final Metrics.Factory metricsFactory;
    private Path dataDirectory;
    private PluginContainer ifGeyser;
    private PluginContainer ifFloodgate;
    private boolean configGeyser;
    private boolean configFloodgate;

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
        m_geyser = new Geyser();
        m_floodgate = new Floodgate();
        createYamlFile(dataDirectory.toAbsolutePath().toString());
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
            updateBuildNumber(pluginName, -1);
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
        return switch (pluginName) {
            case "Geyser" -> m_geyser.updateGeyser("velocity");
            case "Floodgate" -> m_floodgate.updateFloodgate("velocity");
            default -> false;
        };
    }

    public boolean sendPluginMessageToBackend(RegisteredServer server, ChannelIdentifier identifier, byte[] data) {
        // On success, returns true
        return server.sendPluginMessage(identifier, data);
    }

    private void scheduleRestartIfAutoRestart() {
        if (config.getBoolean("updates.autoRestart")) {
            proxy.sendMessage(Component.text(config.getString("updates.restartMessage")));
            proxy.getScheduler().buildTask(this, () -> {
                MinecraftChannelIdentifier IDENTIFIER = MinecraftChannelIdentifier.from("nappixel:lifesteal");
                ByteArrayDataOutput out = ByteStreams.newDataOutput();
                out.writeUTF("forceRestart");


                ByteArrayOutputStream msgbytes = new ByteArrayOutputStream();
                DataOutputStream msgout = new DataOutputStream(msgbytes);
                try {
                    msgout.writeUTF("forceRestartLOL");
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                try {
                    msgout.writeShort(42);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

                out.writeShort(msgbytes.toByteArray().length);
                out.write(msgbytes.toByteArray());

                Optional<RegisteredServer> optionalServer = proxy.getServer("smp");
                if( proxy.getServer("smp").isPresent()){
                    RegisteredServer server = optionalServer.get();
                    sendPluginMessageToBackend(server, IDENTIFIER, out.toByteArray());
                }
            }).delay(Duration.ofSeconds(config.getLong("updates.restartDelay")-1)).schedule();
            for (Player player : proxy.getAllPlayers()) {
                player.sendMessage(Component.text("WARNING")
                        .color(TextColor.color(0xFF0000))
                        .decorate(TextDecoration.BOLD)
                        .append(Component.text(" Proxy will be restarting in 1 minute for a bedrock version update!")
                                .color(TextColor.color(0xFFFF00))));
                System.out.println("Proxy will be restarting in 1 minute for a bedrock version update!");

                player.showTitle(Title.title((Component.text("Proxy Restart!")
                        .color(TextColor.color(0xFF0000))
                        .decorate(TextDecoration.BOLD)), Component.text("In 60 seconds!")
                        .color(TextColor.color(0xFF0000))));

                proxy.getScheduler().buildTask(this, () -> player.sendMessage(Component.text("WARNING")
                        .color(TextColor.color(0xFF0000))
                        .decorate(TextDecoration.BOLD)
                        .append(Component.text(" Proxy will be restarting in 30 seconds for a bedrock version update!")
                                .color(TextColor.color(0xFFFF00))))
                ).delay(30, TimeUnit.SECONDS).schedule();

                proxy.getScheduler().buildTask(this, () -> player.sendMessage(Component.text("WARNING")
                        .color(TextColor.color(0xFF0000))
                        .decorate(TextDecoration.BOLD)
                        .append(Component.text(" Proxy will be restarting in 10 seconds for a bedrock version update!")
                                .color(TextColor.color(0xFFFF00))))
                ).delay(50, TimeUnit.SECONDS).schedule();

                for (int i = 9; i > 0; i--) {
                    final int timeLeft = i;
                    proxy.getScheduler().buildTask(this, () -> player.sendMessage(Component.text("WARNING")
                            .color(TextColor.color(0xFF0000))
                            .decorate(TextDecoration.BOLD)
                            .append(Component.text(" Proxy will be restarting in " + timeLeft + " seconds for a bedrock version update!")
                                    .color(TextColor.color(0xFFFF00))))
                    ).delay(50 + (10 - timeLeft), TimeUnit.SECONDS).schedule();
                }
            }

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
                exception.printStackTrace();
                return null;
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
