package io.shantek;

import io.shantek.functions.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Objects;
import java.util.UUID;

public class HorseGuard extends JavaPlugin {

    public HashMap<UUID, UUID> horseOwners = new HashMap<>();
    public HashMap<UUID, HashSet<UUID>> trustedPlayers = new HashMap<>();
    private Configuration configuration;
    public Metrics metrics;

    private String messagePrefix;

    @Override
    public void onEnable() {
        this.configuration = new Configuration(this);
        reloadHorseGuardConfig();

        Objects.requireNonNull(getCommand("horse")).setExecutor(new HorseCommand(this));
        Objects.requireNonNull(getCommand("horse")).setTabCompleter(new HorseTabCompleter(this));

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

    // Load the message prefix from the config
    public void loadMessagePrefix() {
        String prefixFromConfig = getConfig().getString("message-prefix", "&7[&bHorse&6Guard&7] ");
        this.messagePrefix = ChatColor.translateAlternateColorCodes('&', prefixFromConfig);
    }

    // Get the message prefix with ChatColor.RESET appended
    public String getMessagePrefix() {
        return messagePrefix + ChatColor.RESET;
    }

    // Reload the config and reload the prefix
    public void reloadHorseGuardConfig() {
        reloadConfig();
        loadMessagePrefix();
    }
}
