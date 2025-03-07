package io.shantek;

import io.shantek.functions.HelperFunctions;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.AbstractHorse;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.UUID;

public class HorseCommand implements CommandExecutor {

    private final HorseGuard plugin; // Store the main plugin instance
    private final HelperFunctions helperFunctions;

    public HorseCommand(HorseGuard horseGuard) {
        this.plugin = horseGuard;
        this.helperFunctions = new HelperFunctions(horseGuard);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // Command handling logic
        // Simulated implementation:
        if (!(sender instanceof Player)) {
            sender.sendMessage("Only players can use this command.");
            return true;
        }

        Player player = (Player) sender;

        if (args.length == 0) {
            player.sendMessage("Usage: /horse <command>");
            return true;
        }

        String subCommand = args[0].toLowerCase();

        switch (subCommand) {
            case "reload":
                pluginReload(player);
                break;
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
            default:
                player.sendMessage("Unknown sub-command.");
                break;
        }

        return true;
    }

    private void pluginReload(Player player) {
        if (!player.hasPermission("shantek.horseguard.reload")) {
            player.sendMessage(plugin.getMessagePrefix() + "You do not have permission to reload the plugin.");
            return;
        }

        plugin.reloadConfig();
        plugin.loadMessagePrefix();
        plugin.loadDisabledWorlds();
        player.sendMessage(plugin.getMessagePrefix() + "Plugin configuration reloaded.");
    }

    private void handleTrust(Player player, String[] args) {
        if (args.length < 1) {
            player.sendMessage("Usage: /horse trust <player>");
            return;
        }

        AbstractHorse entity = getRiddenHorse(player);
        if (entity == null) return;

        String targetName = args[1];

        OfflinePlayer target = Bukkit.getOfflinePlayer(targetName);

        // Check if the target player has played on the server before (exists)
        if (target == null || !target.hasPlayedBefore()) {
            player.sendMessage(plugin.getMessagePrefix() + "Invalid player name provided.");
            return;
        }

        UUID horseUUID = entity.getUniqueId();

        helperFunctions.addTrustedPlayer(horseUUID, target.getUniqueId());
        player.sendMessage(plugin.getMessagePrefix() + targetName + " has been trusted with your " + helperFunctions.formatEntityType(entity) + ".");
    }

    private void handleUntrust(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage("Usage: /horse untrust <player>");
            return;
        }

        AbstractHorse entity = getRiddenHorse(player);
        if (entity == null) return;

        String targetName = args[1];
        OfflinePlayer target = Bukkit.getOfflinePlayer(targetName);

        // Check if the target player has played on the server before (exists)
        if (target == null || !target.hasPlayedBefore()) {
            player.sendMessage(plugin.getMessagePrefix() + "Invalid player name provided.");
            return;
        }

        UUID horseUUID = entity.getUniqueId();

        helperFunctions.removeTrustedPlayer(horseUUID, target.getUniqueId());
        player.sendMessage(plugin.getMessagePrefix() + targetName + " has been untrusted with your " + helperFunctions.formatEntityType(entity));
    }

    private void handleTrustList(Player player) {
        AbstractHorse entity = getRiddenHorse(player);
        if (entity == null) return;
        UUID horseUUID = entity.getUniqueId();
        StringBuilder trustList = new StringBuilder(plugin.getMessagePrefix() + "Trusted players for your " + helperFunctions.formatEntityType(entity) + ": ");

        List<String> trustedPlayers = helperFunctions.getTrustedPlayerNames(entity);
        if (trustedPlayers.isEmpty()) {
            trustList.append("No trusted players found.");
        } else {
            trustedPlayers.forEach(name -> trustList.append(name).append(", "));
            if (trustList.lastIndexOf(", ") != -1) {
                trustList.setLength(trustList.length() - 2); // Remove last comma and space
            }
        }

        player.sendMessage(trustList.toString());
    }

    private void handleTransfer(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage("Usage: /horse transfer <player>");
            return;
        }

        AbstractHorse horse = getRiddenHorse(player);
        if (horse == null) return;

        String targetName = args[1];
        OfflinePlayer target = Bukkit.getOfflinePlayer(targetName);

        // Check if the target player has played on the server before (exists)
        if (target == null || !target.hasPlayedBefore()) {
            player.sendMessage(plugin.getMessagePrefix() + "Invalid player name provided.");
            return;
        }

        UUID horseUUID = horse.getUniqueId();
        helperFunctions.clearTrustedPlayers(horseUUID);
        helperFunctions.setHorseOwner(horseUUID, target.getUniqueId());
        String entityType = horse.getType().name().toLowerCase().replace('_', ' ');

        player.sendMessage(plugin.getMessagePrefix() + "Ownership of your " + entityType + " has been transferred to " + targetName + ".");
        horse.eject();
    }

    private AbstractHorse getRiddenHorse(Player player) {
        if (player.getVehicle() instanceof AbstractHorse horse) {
            UUID horseUUID = horse.getUniqueId();
            UUID ownerUUID = helperFunctions.getHorseOwner(horseUUID);

            if (ownerUUID != null && ownerUUID.equals(player.getUniqueId())) {
                return horse;
            } else {
                player.sendMessage(plugin.getMessagePrefix() + "You must be riding a " +
                        horse.getType().name().toLowerCase().replace('_', ' ') + " that you own to use this command.");
                return null;
            }
        } else {
            player.sendMessage(plugin.getMessagePrefix() + "You must be riding a horse to use this command.");
            return null;
        }
    }
}