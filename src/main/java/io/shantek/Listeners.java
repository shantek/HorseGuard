package io.shantek;

import io.shantek.functions.HelperFunctions;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Horse;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityTameEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;

import java.util.UUID;

public class Listeners implements Listener {

    private final HelperFunctions helperFunctions;

    public Listeners(HorseGuard horseGuard) {
        this.helperFunctions = new HelperFunctions(horseGuard);
    }

    @EventHandler
    public void onEntityTame(EntityTameEvent event) {
        if (event.getEntity() instanceof Horse horse) {
            Player player = (Player) event.getOwner();
            UUID horseUUID = horse.getUniqueId();
            UUID playerUUID = player.getUniqueId();

            // If the horse isn't in the config, check if it has an owner
            if (helperFunctions.getHorseOwner(horseUUID) == null && horse.isTamed() && horse.getOwner() != null) {
                // Register the horse and owner in the config
                Player horseOwner = (Player) horse.getOwner();
                UUID ownerUUID = horseOwner.getUniqueId();
                helperFunctions.setHorseOwner(horseUUID, ownerUUID);
                player.sendMessage("This horse is owned by " + horseOwner.getName() + " and has now been registered.");

                // If the player is not the owner and lacks bypass permissions, deny taming
                if (!player.hasPermission("shantek.horseguard.ride")) {
                    event.setCancelled(true);
                    player.sendMessage("You cannot tame this horse as it is owned by " + horseOwner.getName() + ".");
                    return;
                }
            }

            // If the horse is not owned, proceed with taming
            player.sendMessage("You are now the owner of this horse.");
        }
    }

    @EventHandler
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        if (event.getRightClicked() instanceof Horse horse) {
            Player player = event.getPlayer();
            UUID horseUUID = horse.getUniqueId();
            UUID ownerUUID = helperFunctions.getHorseOwner(horseUUID);

            // If the horse isn't in the config, check if it has an owner in the game
            if (ownerUUID == null) {
                if (horse.isTamed() && horse.getOwner() != null) {
                    // Horse has an owner in-game but is not in the config
                    if (horse.getOwner() instanceof Player horseOwner) {
                        ownerUUID = horseOwner.getUniqueId();
                        helperFunctions.setHorseOwner(horseUUID, ownerUUID);
                        player.sendMessage("This horse is owned by " + horseOwner.getName() + " and has now been registered.");

                        // If the player is not the owner and lacks bypass permissions, deny interaction
                        if (!ownerUUID.equals(player.getUniqueId()) && !player.hasPermission("shantek.horseguard.ride")) {
                            event.setCancelled(true);
                            player.sendMessage("You cannot interact with this horse as it is owned by " + horseOwner.getName() + ".");
                            return;
                        }
                    } else {
                        // Handle if the owner is not online (OfflinePlayer)
                        OfflinePlayer offlineOwner = (OfflinePlayer) horse.getOwner();
                        ownerUUID = offlineOwner.getUniqueId();
                        helperFunctions.setHorseOwner(horseUUID, ownerUUID);
                        player.sendMessage("This horse is owned by " + getOwnerName(ownerUUID) + " and has now been registered.");

                        // If the player is not the owner and lacks bypass permissions, deny interaction
                        if (!ownerUUID.equals(player.getUniqueId()) && !player.hasPermission("shantek.horseguard.ride")) {
                            event.setCancelled(true);
                            player.sendMessage("You cannot interact with this horse as it is owned by " + getOwnerName(ownerUUID) + ".");
                            return;
                        }
                    }
                } else {
                    // Horse is not owned in-game, so allow interaction for taming
                    return; // Allow interaction (taming, etc.)
                }
            }

            // Allow interaction if the player has the bypass permission
            if (player.hasPermission("shantek.horseguard.ride")) {
                return; // Allow interaction
            }

            // Standard check for owner or trusted player
            if (ownerUUID != null && !helperFunctions.isOwner(player, horse) && !helperFunctions.isPlayerTrusted(horseUUID, player.getUniqueId())) {
                event.setCancelled(true);
                String ownerName = getOwnerName(ownerUUID);
                player.sendMessage("This horse is owned by " + ownerName + ". You cannot interact with it.");
            }
        }
    }

    private String getOwnerName(UUID ownerUUID) {
        OfflinePlayer owner = Bukkit.getOfflinePlayer(ownerUUID);
        String ownerName = owner.getName();
        if (ownerName == null) {
            ownerName = "Unknown";
        }
        return ownerName;
    }


    @EventHandler
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if (event.getEntity() instanceof Horse horse) {
            UUID horseUUID = horse.getUniqueId();
            UUID ownerUUID = helperFunctions.getHorseOwner(horseUUID);

            if (event.getDamager() instanceof Player damager) {
                // If the horse isn't in the config, check if it has an owner
                if (ownerUUID == null && horse.isTamed() && horse.getOwner() != null) {
                    // Register the horse and owner in the config
                    Player horseOwner = (Player) horse.getOwner();
                    ownerUUID = horseOwner.getUniqueId();
                    helperFunctions.setHorseOwner(horseUUID, ownerUUID);
                    damager.sendMessage("This horse is owned by " + horseOwner.getName() + " and has now been registered.");

                    // If the player is not the owner and lacks bypass permissions, deny damage
                    if (!damager.hasPermission("shantek.horseguard.damage")) {
                        event.setCancelled(true);
                        damager.sendMessage("You cannot damage this horse as it is owned by " + horseOwner.getName() + ".");
                        return;
                    }
                }

                // Check if player has permission to damage horses without ownership
                if (ownerUUID == null || damager.hasPermission("shantek.horseguard.damage")) {
                    return; // Allow damage
                }

                // Check if the horse has an owner and the damager isn't the owner
                if (!ownerUUID.equals(damager.getUniqueId())) {
                    event.setCancelled(true);
                    String ownerName = getOwnerName(ownerUUID);
                    damager.sendMessage("This horse is owned by " + ownerName + ". You cannot damage it.");
                }
            } else {
                // If the damager isn't a player, cancel the damage
                event.setCancelled(true);
            }
        }
    }

}
