package io.shantek;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Horse;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class HelperFunctions {

    private final HorseGuard horseGuard;

    public HelperFunctions(HorseGuard horseGuard) {
        this.horseGuard = horseGuard;
    }

    public Horse getHorsePlayerOwns(Player player) {
        if (!(player.getVehicle() instanceof Horse horse)) {
            player.sendMessage("You must be riding a horse to use this command.");
            return null;
        }

        UUID horseUUID = horse.getUniqueId();
        UUID ownerUUID = getHorseOwner(horseUUID);

        if (ownerUUID == null || !ownerUUID.equals(player.getUniqueId())) {
            player.sendMessage("You are not the owner of this horse.");
            return null;
        }

        return horse;
    }

    public boolean isOwner(Player player, Horse horse) {
        UUID horseUUID = horse.getUniqueId();
        UUID ownerUUID = getHorseOwner(horseUUID);
        return ownerUUID != null && ownerUUID.equals(player.getUniqueId());
    }

    public List<String> getTrustedPlayerNames(Horse horse) {
        UUID horseUUID = horse.getUniqueId();
        HashSet<UUID> trustedPlayers = getTrustedPlayers(horseUUID);

        return trustedPlayers.stream()
                .map(Bukkit::getOfflinePlayer)
                .map(OfflinePlayer::getName)
                .filter(name -> name != null)
                .collect(Collectors.toList());
    }

    public UUID getHorseOwner(UUID horseUUID) {
        return horseGuard.horseOwners.get(horseUUID);
    }

    public boolean isPlayerTrusted(UUID horseUUID, UUID playerUUID) {
        return horseGuard.trustedPlayers.containsKey(horseUUID) && horseGuard.trustedPlayers.get(horseUUID).contains(playerUUID);
    }

    public void addTrustedPlayer(UUID horseUUID, UUID playerUUID) {
        horseGuard.trustedPlayers.computeIfAbsent(horseUUID, k -> new HashSet<>()).add(playerUUID);
        horseGuard.getConfiguration().saveHorseData(); // Save data after modifying
    }

    public void removeTrustedPlayer(UUID horseUUID, UUID playerUUID) {
        if (horseGuard.trustedPlayers.containsKey(horseUUID)) {
            horseGuard.trustedPlayers.get(horseUUID).remove(playerUUID);
            horseGuard.getConfiguration().saveHorseData(); // Save data after modifying
        }
    }

    public HashSet<UUID> getTrustedPlayers(UUID horseUUID) {
        return horseGuard.trustedPlayers.getOrDefault(horseUUID, new HashSet<>());
    }
}
