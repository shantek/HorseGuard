package io.shantek;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Horse;
import org.bukkit.entity.Player;

import java.util.UUID;
import java.util.HashSet;

public class HorseCommand implements CommandExecutor {

    private final HorseGuard horseGuard;

    public HorseCommand(HorseGuard horseGuard) {
        this.horseGuard = horseGuard;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Only players can use this command.");
            return true;
        }

        Player player = (Player) sender;

        if (args.length == 0) {
            player.sendMessage("Usage: /horse <trust|untrust|trustlist> [playername]");
            return true;
        }

        String subCommand = args[0].toLowerCase();

        switch (subCommand) {
            case "trust":
                handleTrust(player, args);
                break;
            case "untrust":
                handleUntrust(player, args);
                break;
            case "trustlist":
                handleTrustList(player);
                break;
            default:
                player.sendMessage("Unknown subcommand. Usage: /horse <trust | untrust | trustlist> [playername]");
                break;
        }

        return true;
    }

    private void handleTrust(Player player, String[] args) {
        if (args.length != 2) {
            player.sendMessage("Usage: /horse trust <playername>");
            return;
        }

        String targetName = args[1];
        OfflinePlayer target = Bukkit.getOfflinePlayer(targetName);

        if (!target.hasPlayedBefore() && !target.isOnline()) {
            player.sendMessage("Player not found.");
            return;
        }

        if (!(player.getVehicle() instanceof Horse)) {
            player.sendMessage("You must be riding a horse to use this command.");
            return;
        }

        Horse horse = (Horse) player.getVehicle();
        UUID horseUUID = horse.getUniqueId();
        UUID ownerUUID = horseGuard.getHorseOwner(horseUUID);

        if (ownerUUID == null || !ownerUUID.equals(player.getUniqueId())) {
            player.sendMessage("You are not the owner of this horse.");
            return;
        }

        UUID targetUUID = target.getUniqueId();

        if (horseGuard.isPlayerTrusted(horseUUID, targetUUID)) {
            player.sendMessage(target.getName() + " is already trusted with your horse.");
            return;
        }

        horseGuard.addTrustedPlayer(horseUUID, targetUUID);
        player.sendMessage(target.getName() + " can now ride and leash your horse.");
    }

    private void handleUntrust(Player player, String[] args) {
        if (args.length != 2) {
            player.sendMessage("Usage: /horse untrust <playername>");
            return;
        }

        String targetName = args[1];
        OfflinePlayer target = Bukkit.getOfflinePlayer(targetName);

        if (!target.hasPlayedBefore() && !target.isOnline()) {
            player.sendMessage("Player not found.");
            return;
        }

        if (!(player.getVehicle() instanceof Horse)) {
            player.sendMessage("You must be riding a horse to use this command.");
            return;
        }

        Horse horse = (Horse) player.getVehicle();
        UUID horseUUID = horse.getUniqueId();
        UUID ownerUUID = horseGuard.getHorseOwner(horseUUID);

        if (ownerUUID == null || !ownerUUID.equals(player.getUniqueId())) {
            player.sendMessage("You are not the owner of this horse.");
            return;
        }

        UUID targetUUID = target.getUniqueId();

        if (!horseGuard.isPlayerTrusted(horseUUID, targetUUID)) {
            player.sendMessage(target.getName() + " is not trusted with your horse.");
            return;
        }

        horseGuard.removeTrustedPlayer(horseUUID, targetUUID);
        player.sendMessage(target.getName() + " can no longer ride or leash your horse.");
    }

    private void handleTrustList(Player player) {
        if (!(player.getVehicle() instanceof Horse)) {
            player.sendMessage("You must be riding a horse to use this command.");
            return;
        }

        Horse horse = (Horse) player.getVehicle();
        UUID horseUUID = horse.getUniqueId();
        UUID ownerUUID = horseGuard.getHorseOwner(horseUUID);

        if (ownerUUID == null || !ownerUUID.equals(player.getUniqueId())) {
            player.sendMessage("You are not the owner of this horse.");
            return;
        }

        HashSet<UUID> trustedPlayers = horseGuard.getTrustedPlayers(horseUUID);

        if (trustedPlayers.isEmpty()) {
            player.sendMessage("No players are trusted with this horse.");
        } else {
            StringBuilder trustedList = new StringBuilder("Trusted players: ");
            for (UUID trustedUUID : trustedPlayers) {
                OfflinePlayer trustedPlayer = Bukkit.getOfflinePlayer(trustedUUID);
                String name = trustedPlayer.getName();
                if (name == null) {
                    name = "Unknown";
                }
                trustedList.append(name).append(", ");
            }
            player.sendMessage(trustedList.substring(0, trustedList.length() - 2)); // Remove trailing comma
        }
    }
}
