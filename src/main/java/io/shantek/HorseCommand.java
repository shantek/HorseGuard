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
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Only players can use this command.");
            return true;
        }

        // ✅ No arguments = Open the Horse Management GUI
        if (args.length == 0) {
            AbstractHorse horse = getRiddenHorse(player);
            if (horse == null) return true;

            helperFunctions.openHorseManagement(player, horse);
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
        if (args.length < 2) {
            player.sendMessage("Usage: /horse trust <player>");
            return;
        }

        AbstractHorse horse = getRiddenHorse(player);
        if (horse == null) return;

        String targetName = args[1];
        OfflinePlayer target = Bukkit.getOfflinePlayer(targetName);

        if (target == null || !target.hasPlayedBefore()) {
            player.sendMessage(plugin.getMessagePrefix() + "Invalid player name provided.");
            return;
        }

        UUID horseUUID = horse.getUniqueId();
        helperFunctions.addTrustedPlayer(horseUUID, target.getUniqueId());

        player.sendMessage(plugin.getMessagePrefix() + targetName + " has been trusted with your " + helperFunctions.formatEntityType(horse) + ".");
    }

    private void handleUntrust(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage("Usage: /horse untrust <player>");
            return;
        }

        AbstractHorse horse = getRiddenHorse(player);
        if (horse == null) return;

        String targetName = args[1];
        OfflinePlayer target = Bukkit.getOfflinePlayer(targetName);

        if (target == null || !target.hasPlayedBefore()) {
            player.sendMessage(plugin.getMessagePrefix() + "Invalid player name provided.");
            return;
        }

        UUID horseUUID = horse.getUniqueId();
        helperFunctions.removeTrustedPlayer(horseUUID, target.getUniqueId());

        player.sendMessage(plugin.getMessagePrefix() + targetName + " has been untrusted with your " + helperFunctions.formatEntityType(horse));
    }

    private void handleTrustList(Player player) {
        AbstractHorse horse = getRiddenHorse(player);
        if (horse == null) return;

        UUID horseUUID = horse.getUniqueId();
        StringBuilder trustList = new StringBuilder(plugin.getMessagePrefix() + "Trusted players for your " + helperFunctions.formatEntityType(horse) + ": ");

        List<String> trustedPlayers = helperFunctions.getTrustedPlayerNames(horse);
        if (trustedPlayers.isEmpty()) {
            trustList.append("No trusted players found.");
        } else {
            trustList.append(String.join(", ", trustedPlayers));
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

        if (target == null || !target.hasPlayedBefore()) {
            player.sendMessage(plugin.getMessagePrefix() + "Invalid player name provided.");
            return;
        }

        UUID horseUUID = horse.getUniqueId();
        helperFunctions.clearTrustedPlayers(horseUUID);
        helperFunctions.setHorseOwner(horseUUID, target.getUniqueId());

        String entityType = helperFunctions.formatEntityType(horse);
        player.sendMessage(plugin.getMessagePrefix() + "Ownership of your " + entityType + " has been transferred to " + targetName + ".");
        horse.eject();
    }

    private AbstractHorse getRiddenHorse(Player player) {
        if (!(player.getVehicle() instanceof AbstractHorse horse)) {
            player.sendMessage(plugin.getMessagePrefix() + "You must be riding a horse to use this command.");
            return null;
        }

        UUID horseUUID = horse.getUniqueId();
        UUID ownerUUID = helperFunctions.getHorseOwner(horseUUID);

        if (ownerUUID == null) {
            player.sendMessage(plugin.getMessagePrefix() + "This horse is not registered in the system.");
            return null;
        }

        if (!ownerUUID.equals(player.getUniqueId())) {
            OfflinePlayer owner = Bukkit.getOfflinePlayer(ownerUUID);
            player.sendMessage(plugin.getMessagePrefix() + "This horse belongs to " + (owner.getName() != null ? owner.getName() : "Unknown Owner") + ".");
            return null;
        }

        return horse;
    }
}
