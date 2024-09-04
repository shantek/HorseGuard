package io.shantek;

import io.shantek.functions.HelperFunctions;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Horse;
import org.bukkit.entity.Player;

import java.util.UUID;

public class HorseCommand implements CommandExecutor {

    private final HorseGuard plugin; // Store the main plugin instance
    private final HelperFunctions helperFunctions;

    public HorseCommand(HorseGuard horseGuard) {
        this.plugin = horseGuard; // Assign the main plugin instance
        this.helperFunctions = new HelperFunctions(horseGuard);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(plugin.getMessagePrefix() + "Only players can use this command.");
            return true;
        }

        if (args.length == 0) {
            player.sendMessage(plugin.getMessagePrefix() + "Usage: /horse <trust|untrust|trustlist|transfer> [playername]");
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
            case "transfer":
                handleTransfer(player, args);
                break;
            case "reload":
                pluginReload(player);
            default:
                player.sendMessage(plugin.getMessagePrefix() + "Unknown subcommand. Usage: /horse <trust|untrust|trustlist|transfer> [playername]");
                break;
        }

        return true;
    }

    private void pluginReload(Player player) {
        if (player.hasPermission("shantek.horseguard.reload")) {
            plugin.reloadHorseGuardConfig();
            player.sendMessage(plugin.getMessagePrefix() + "Configuration reloaded.");
        } else {
            player.sendMessage(plugin.getMessagePrefix() + "You do not have permission to reload the configuration.");
        }
    }

    private void handleTrust(Player player, String[] args) {
        if (args.length != 2) {
            player.sendMessage(plugin.getMessagePrefix() + "Usage: /horse trust <playername>");
            return;
        }

        String targetName = args[1];
        OfflinePlayer target = Bukkit.getOfflinePlayer(targetName);

        if (!target.hasPlayedBefore() && !target.isOnline()) {
            player.sendMessage(plugin.getMessagePrefix() + "Player not found.");
            return;
        }

        Horse horse = helperFunctions.getHorsePlayerOwns(player);
        if (horse == null) return;

        UUID targetUUID = target.getUniqueId();

        // Prevent the owner from trusting themselves
        if (player.getUniqueId().equals(targetUUID)) {
            player.sendMessage(plugin.getMessagePrefix() + "You cannot trust yourself on a horse you own.");
            return;
        }

        if (helperFunctions.isPlayerTrusted(horse.getUniqueId(), targetUUID)) {
            player.sendMessage(plugin.getMessagePrefix() + target.getName() + " is already trusted with this horse.");
            return;
        }

        helperFunctions.addTrustedPlayer(horse.getUniqueId(), targetUUID);
        player.sendMessage(plugin.getMessagePrefix() + target.getName() + " can now ride and lead your horse.");
    }

    private void handleUntrust(Player player, String[] args) {
        if (args.length != 2) {
            player.sendMessage(plugin.getMessagePrefix() + "Usage: /horse untrust <playername>");
            return;
        }

        String targetName = args[1];
        OfflinePlayer target = Bukkit.getOfflinePlayer(targetName);

        if (!target.hasPlayedBefore() && !target.isOnline()) {
            player.sendMessage(plugin.getMessagePrefix() + "Player not found.");
            return;
        }

        Horse horse = helperFunctions.getHorsePlayerOwns(player);
        if (horse == null) return;

        UUID targetUUID = target.getUniqueId();

        if (!helperFunctions.isPlayerTrusted(horse.getUniqueId(), targetUUID)) {
            player.sendMessage(plugin.getMessagePrefix() + target.getName() + " is not trusted with your horse.");
            return;
        }

        helperFunctions.removeTrustedPlayer(horse.getUniqueId(), targetUUID);
        player.sendMessage(plugin.getMessagePrefix() + target.getName() + " can no longer ride or lead your horse.");
    }

    private void handleTrustList(Player player) {
        Horse horse = helperFunctions.getHorsePlayerOwns(player);
        if (horse == null) return;

        var trustedPlayers = helperFunctions.getTrustedPlayerNames(horse);

        if (trustedPlayers.isEmpty()) {
            player.sendMessage(plugin.getMessagePrefix() + "No players are trusted with this horse.");
        } else {
            String trustedList = String.join(", ", trustedPlayers);
            player.sendMessage(plugin.getMessagePrefix() + "Trusted players: " + trustedList);
        }
    }

    private void handleTransfer(Player player, String[] args) {
        if (args.length != 2) {
            player.sendMessage(plugin.getMessagePrefix() + "Usage: /horse transfer <playername>");
            return;
        }

        String targetName = args[1];
        OfflinePlayer target = Bukkit.getOfflinePlayer(targetName);

        if (!target.hasPlayedBefore() && !target.isOnline()) {
            player.sendMessage(plugin.getMessagePrefix() + "Player not found.");
            return;
        }

        Horse horse = helperFunctions.getHorsePlayerOwns(player);
        if (horse == null) return;

        UUID targetUUID = target.getUniqueId();

        // Transfer ownership
        helperFunctions.setHorseOwner(horse.getUniqueId(), targetUUID);
        player.sendMessage(plugin.getMessagePrefix() + "This horse has been transferred to " + target.getName() + ".");

        // Eject the current player from the horse
        horse.eject();
    }
}
