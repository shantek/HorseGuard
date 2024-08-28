package io.shantek;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class Configuration {

    private final HorseGuard horseGuard;
    private File dataFile;
    private FileConfiguration dataConfig;

    public Configuration(HorseGuard horseGuard) {
        this.horseGuard = horseGuard;
        this.dataFile = new File(horseGuard.getDataFolder(), "horse_data.yml");

        if (!dataFile.exists()) {
            dataFile.getParentFile().mkdirs();
            horseGuard.saveResource("horse_data.yml", false);
        }
        this.dataConfig = YamlConfiguration.loadConfiguration(dataFile);
    }

    public void loadHorseData() {
        if (dataConfig.contains("horseOwners")) {
            for (String horseUUIDString : dataConfig.getConfigurationSection("horseOwners").getKeys(false)) {
                UUID horseUUID = UUID.fromString(horseUUIDString);
                UUID ownerUUID = UUID.fromString(dataConfig.getString("horseOwners." + horseUUIDString));
                horseGuard.horseOwners.put(horseUUID, ownerUUID);
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
                horseGuard.trustedPlayers.put(horseUUID, trustedSet);
            }
        }
    }

    public void saveHorseData() {
        dataConfig.set("horseOwners", null); // Clear existing data
        dataConfig.set("trustedPlayers", null);

        // Save horse owners as strings
        for (UUID horseUUID : horseGuard.horseOwners.keySet()) {
            dataConfig.set("horseOwners." + horseUUID.toString(), horseGuard.horseOwners.get(horseUUID).toString());
        }

        // Save trusted players as strings
        for (UUID horseUUID : horseGuard.trustedPlayers.keySet()) {
            HashSet<UUID> trustedSet = horseGuard.trustedPlayers.get(horseUUID);
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
}
