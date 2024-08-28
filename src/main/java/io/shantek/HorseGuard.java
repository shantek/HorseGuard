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
        if (event.getEntity() instanceof Horse) {
            Horse horse = (Horse) event.getEntity();
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
        if (event.getRightClicked() instanceof Horse) {
            Horse horse = (Horse) event.getRightClicked();
            Player player = event.getPlayer();
            UUID horseUUID = horse.getUniqueId();
            UUID playerUUID = player.getUniqueId();

            if (horseOwners.containsKey(horseUUID)) {
                UUID ownerUUID = horseOwners.get(horseUUID);
                if (!ownerUUID.equals(playerUUID) && !isPlayerTrusted(horseUUID, playerUUID)) {
                    event.setCancelled(true);
                    player.sendMessage("This horse is owned by " + Bukkit.getPlayer(ownerUUID).getName());
                }
            }
        }
    }

    @EventHandler
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if (event.getEntity() instanceof Horse) {
            Horse horse = (Horse) event.getEntity();
            UUID horseUUID = horse.getUniqueId();
            if (horseOwners.containsKey(horseUUID)) {
                UUID ownerUUID = horseOwners.get(horseUUID);
                if (event.getDamager() instanceof Player) {
                    Player damager = (Player) event.getDamager();
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
