package io.shantek;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Horse;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class HorseTabCompleter implements TabCompleter {

    private final HelperFunctions helperFunctions;

    public HorseTabCompleter(HorseGuard horseGuard) {
        this.helperFunctions = new HelperFunctions(horseGuard);
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!(sender instanceof Player player)) {
            return null; // Only players should use this
        }

        if (args.length == 1) {
            // Suggest "trust", "untrust", "trustlist" for the first argument
            List<String> subcommands = new ArrayList<>();
            subcommands.add("trust");
            subcommands.add("untrust");
            subcommands.add("trustlist");
            return subcommands;
        } else if (args.length == 2) {
            String subCommand = args[0].toLowerCase();

            if (subCommand.equals("trust")) {
                // Populate with online players for "/horse trust <playername>"
                List<String> playerNames = new ArrayList<>();
                for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                    playerNames.add(onlinePlayer.getName());
                }
                return playerNames;
            } else if (subCommand.equals("untrust")) {
                // Populate with currently trusted players for "/horse untrust <playername>"
                if (!(player.getVehicle() instanceof Horse horse)) {
                    return null; // Player is not riding a horse
                }

                UUID horseUUID = horse.getUniqueId();
                UUID ownerUUID = helperFunctions.getHorseOwner(horseUUID);

                if (ownerUUID == null || !ownerUUID.equals(player.getUniqueId())) {
                    return null; // The player is not the owner
                }

                List<String> trustedPlayerNames = new ArrayList<>();
                for (UUID trustedUUID : helperFunctions.getTrustedPlayers(horseUUID)) {
                    OfflinePlayer trustedPlayer = Bukkit.getOfflinePlayer(trustedUUID);
                    String name = trustedPlayer.getName();
                    if (name != null) {
                        trustedPlayerNames.add(name);
                    }
                }

                return trustedPlayerNames.isEmpty() ? null : trustedPlayerNames;
            }
        }

        return null; // No suggestions available
    }
}
