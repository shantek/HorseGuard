package io.shantek;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.HashSet;
import java.util.UUID;

public class HorseGuard extends JavaPlugin {

    HashMap<UUID, UUID> horseOwners = new HashMap<>();
    HashMap<UUID, HashSet<UUID>> trustedPlayers = new HashMap<>();
    private Configuration configuration;

    @Override
    public void onEnable() {
        this.configuration = new Configuration(this);

        getCommand("horse").setExecutor(new HorseCommand(this));
        getCommand("horse").setTabCompleter(new HorseTabCompleter(this));

        Bukkit.getPluginManager().registerEvents(new Listeners(this), this);

        configuration.loadHorseData();
    }

    @Override
    public void onDisable() {
        configuration.saveHorseData();
    }

    public Configuration getConfiguration() {
        return configuration;
    }
}
