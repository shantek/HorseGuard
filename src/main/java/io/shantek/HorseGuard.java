package io.shantek;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Horse;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityTameEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class HorseGuard extends JavaPlugin implements Listener {

    private HashMap<UUID, UUID> horseOwners = new HashMap<>();
    private HashMap<UUID, HashSet<UUID>> trustedPlayers = new HashMap<>();
    private File dataFile;
    private FileConfiguration dataConfig;

    @Override
    public void onEnable() {
        Bukkit.getPluginManager().registerEvents(this, this);
        getCommand("horse").setExecutor(new HorseCommand(this));
        getCommand("horse").setTabCompleter(new HorseTabCompleter(this)); // Registering the TabCompleter

        dataFile = new File(getDataFolder(), "horse_data.yml");

        if (!dataFile.exists()) {
            dataFile.getParentFile().mkdirs();
            saveResource("horse_data.yml", false);
        }
        dataConfig = YamlConfiguration.loadConfiguration(dataFile);
        loadHorseData();
    }

    @Override
    public void onDisable() {
        saveHorseData();
    }

    public UUID getHorseOwner(UUID horseUUID) {
        return horseOwners.get(horseUUID);
    }

    public void setHorseOwner(UUID horseUUID, UUID playerUUID) {
        horseOwners.put(horseUUID, playerUUID);
        saveHorseData();
    }

    public boolean isPlayerTrusted(UUID horseUUID, UUID playerUUID) {
        return trustedPlayers.containsKey(horseUUID) && trustedPlayers.get(horseUUID).contains(playerUUID);
    }

    public void addTrustedPlayer(UUID horseUUID, UUID playerUUID) {
        trustedPlayers.computeIfAbsent(horseUUID, k -> new HashSet<>()).add(playerUUID);
        saveHorseData();
    }

    public void removeTrustedPlayer(UUID horseUUID, UUID playerUUID) {
        if (trustedPlayers.containsKey(horseUUID)) {
            trustedPlayers.get(horseUUID).remove(playerUUID);
            saveHorseData();
        }
    }

    public HashSet<UUID> getTrustedPlayers(UUID horseUUID) {
        return trustedPlayers.getOrDefault(horseUUID, new HashSet<>());
    }

    private void loadHorseData() {
        if (dataConfig.contains("horseOwners")) {
            for (String horseUUIDString : dataConfig.getConfigurationSection("horseOwners").getKeys(false)) {
                UUID horseUUID = UUID.fromString(horseUUIDString);
                UUID ownerUUID = UUID.fromString(dataConfig.getString("horseOwners." + horseUUIDString));
                horseOwners.put(horseUUID, ownerUUID);
            }
        }
        if (dataConfig.contains("trustedPlayers")) {
            for (String horseUUIDString : dataConfig.getConfigurationSection("trustedPlayers").getKeys(false)) {
                UUID horseUUID = UUID.fromString(horseUUIDString);
                List<String> trustedUUIDStrings = dataConfig.getStringList("trustedPlayers." + horseUUIDString);
                HashSet<UUID> trustedSet = new HashSet<>();
                for (String uuidString : trustedUUIDStrings) {
                    trustedSet.add(UUID.fromString(uuidString));
                }
                trustedPlayers.put(horseUUID, trustedSet);
            }
        }
    }

    private void saveHorseData() {
        dataConfig.set("horseOwners", null); // Clear existing data
        dataConfig.set("trustedPlayers", null);

        // Save horse owners as strings
        for (UUID horseUUID : horseOwners.keySet()) {
            dataConfig.set("horseOwners." + horseUUID.toString(), horseOwners.get(horseUUID).toString());
        }

        // Save trusted players as strings
        for (UUID horseUUID : trustedPlayers.keySet()) {
            HashSet<UUID> trustedSet = trustedPlayers.get(horseUUID);
            List<String> trustedUUIDStrings = new ArrayList<>();
            for (UUID uuid : trustedSet) {
                trustedUUIDStrings.add(uuid.toString());
            }
            dataConfig.set("trustedPlayers." + horseUUID.toString(), trustedUUIDStrings);
        }

        try {
            dataConfig.save(dataFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @EventHandler
    public void onEntityTame(EntityTameEvent event) {
        if (event.getEntity() instanceof Horse horse) {
            Player player = (Player) event.getOwner();
            UUID horseUUID = horse.getUniqueId();
            UUID playerUUID = player.getUniqueId();

            horseOwners.put(horseUUID, playerUUID);
            player.sendMessage("You are now the owner of this horse.");

            saveHorseData();
        }
    }

    @EventHandler
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        if (event.getRightClicked() instanceof Horse horse) {
            Player player = event.getPlayer();
            UUID horseUUID = horse.getUniqueId();
            UUID playerUUID = player.getUniqueId();

            // Check if the horse's UUID is already in the plugin's config
            if (!horseOwners.containsKey(horseUUID)) {
                // If not, check if the horse is tamed and has an owner according to Minecraft
                if (horse.isTamed() && horse.getOwner() instanceof Player horseOwner) {
                    UUID ownerUUID = horseOwner.getUniqueId();

                    // Add the horse to the plugin's config along with the owner's UUID
                    setHorseOwner(horseUUID, ownerUUID);
                    player.sendMessage("This horse is owned by " + horseOwner.getName() + " and has now been registered in the system.");

                    // Check if the interacting player is the owner or not
                    if (!ownerUUID.equals(playerUUID)) {
                        event.setCancelled(true);
                        player.sendMessage("This horse is owned by " + horseOwner.getName() + ". You cannot interact with it.");
                        return;
                    }
                } else {
                    // If the horse isn't tamed or doesn't have an owner, allow interaction (or deny as needed)
                    player.sendMessage("This horse is not tamed or has no registered owner.");
                    return;
                }
            } else {
                // If the horse is already in the config, do the usual checks
                UUID ownerUUID = horseOwners.get(horseUUID);

                if (!ownerUUID.equals(playerUUID) && !isPlayerTrusted(horseUUID, playerUUID)) {
                    event.setCancelled(true);
                    Player owner = Bukkit.getPlayer(ownerUUID);
                    String ownerName = (owner != null) ? owner.getName() : "an unknown player";
                    player.sendMessage("This horse is owned by " + ownerName + ". You cannot interact with it.");
                }
            }
        }
    }

    @EventHandler
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if (event.getEntity() instanceof Horse horse) {
            UUID horseUUID = horse.getUniqueId();
            if (horseOwners.containsKey(horseUUID)) {
                UUID ownerUUID = horseOwners.get(horseUUID);
                if (event.getDamager() instanceof Player damager) {
                    if (!ownerUUID.equals(damager.getUniqueId())) {
                        event.setCancelled(true);
                        damager.sendMessage("This horse is owned by " + Bukkit.getPlayer(ownerUUID).getName());
                    }
                } else {
                    event.setCancelled(true);
                }
            }
        }
    }
}
