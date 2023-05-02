package newamazingpvp.autoupdategeyser;

import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

public final class AutoUpdateGeyser extends Plugin {

    private Geyser m_geyser;
    private Configuration config;

    @Override
    public void onEnable() {
        m_geyser = new Geyser();
        config = getConfig();

        if (!getDataFolder().exists()) {
            getDataFolder().mkdir();
        }
        File file = new File(getDataFolder(), "config.yml");
        if (!file.exists()) {
            try {
                file.createNewFile();

                Configuration configuration = ConfigurationProvider.getProvider(YamlConfiguration.class).load(file);

                configuration.set("Geyser.enabled", true);
                configuration.set("Geyser.dev", false);
                configuration.set("Floodgate.enabled", true);
                configuration.set("Floodgate.dev", false);
                configuration.set("Check-Interval", 30);

                ConfigurationProvider.getProvider(YamlConfiguration.class).save(configuration, file);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        updateChecker();
    }

    public void updateChecker() {
        getProxy().getScheduler().schedule(this, new Runnable() {
            @Override
            public void run() {
                m_geyser.updateGeyser();
            }
        }, 1L, 1L, TimeUnit.MINUTES);
    }

    public Configuration getConfig() {
        try {
            File configFile = new File(getDataFolder(), "config.yml");
            return ConfigurationProvider.getProvider(YamlConfiguration.class).load(configFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
