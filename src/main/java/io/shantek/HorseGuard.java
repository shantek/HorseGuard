package io.shantek;

import io.shantek.functions.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.HashSet;
import java.util.UUID;

public class HorseGuard extends JavaPlugin {

    public HashMap<UUID, UUID> horseOwners = new HashMap<>();
    public HashMap<UUID, HashSet<UUID>> trustedPlayers = new HashMap<>();
    private Configuration configuration;
    public Metrics metrics;

    @Override
    public void onEnable() {
        this.configuration = new Configuration(this);

        getCommand("horse").setExecutor(new HorseCommand(this));
        getCommand("horse").setTabCompleter(new HorseTabCompleter(this));

        Bukkit.getPluginManager().registerEvents(new Listeners(this), this);

        configuration.loadHorseData();

        int pluginId = 23218;
        Metrics metrics = new Metrics(this, pluginId);
    }

    @Override
    public void onDisable() {
        configuration.saveHorseData();
    }

    public Configuration getConfiguration() {
        return configuration;
    }
}
