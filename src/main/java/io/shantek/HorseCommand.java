package io.shantek;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Horse;
import org.bukkit.entity.Player;

import java.util.UUID;
import java.util.List;

public class HorseCommand implements CommandExecutor {

    private final HelperFunctions helperFunctions;

    public HorseCommand(HorseGuard horseGuard) {
        this.helperFunctions = new HelperFunctions(horseGuard);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Only players can use this command.");
            return true;
        }

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
                player.sendMessage("Unknown subcommand. Usage: /horse <trust|untrust|trustlist> [playername]");
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

        Horse horse = helperFunctions.getHorsePlayerOwns(player);
        if (horse == null) return;

        UUID targetUUID = target.getUniqueId();

        // Prevent the owner from trusting themselves
        if (player.getUniqueId().equals(targetUUID)) {
            player.sendMessage("You cannot trust yourself on a horse you own.");
            return;
        }

        if (helperFunctions.isPlayerTrusted(horse.getUniqueId(), targetUUID)) {
            player.sendMessage(target.getName() + " is already trusted with your horse.");
            return;
        }

        helperFunctions.addTrustedPlayer(horse.getUniqueId(), targetUUID);
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

        Horse horse = helperFunctions.getHorsePlayerOwns(player);
        if (horse == null) return;

        UUID targetUUID = target.getUniqueId();

        if (!helperFunctions.isPlayerTrusted(horse.getUniqueId(), targetUUID)) {
            player.sendMessage(target.getName() + " is not trusted with your horse.");
            return;
        }

        helperFunctions.removeTrustedPlayer(horse.getUniqueId(), targetUUID);
        player.sendMessage(target.getName() + " can no longer ride or leash your horse.");
    }

    private void handleTrustList(Player player) {
        Horse horse = helperFunctions.getHorsePlayerOwns(player);
        if (horse == null) return;

        List<String> trustedPlayers = helperFunctions.getTrustedPlayerNames(horse);

        if (trustedPlayers.isEmpty()) {
            player.sendMessage("No players are trusted with this horse.");
        } else {
            String trustedList = String.join(", ", trustedPlayers);
            player.sendMessage("Trusted players: " + trustedList);
        }
    }
}
