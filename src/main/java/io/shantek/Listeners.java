package io.shantek;

import org.bukkit.Bukkit;
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

            helperFunctions.addTrustedPlayer(horseUUID, playerUUID);
            player.sendMessage("You are now the owner of this horse.");
        }
    }

    @EventHandler
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        if (event.getRightClicked() instanceof Horse horse) {
            Player player = event.getPlayer();

            // Check if player has permission to ride or lead horses without ownership
            if (player.hasPermission("shantek.horseguard.ride")) {
                return; // Player can ride or lead any horse
            }

            UUID horseUUID = horse.getUniqueId();
            UUID playerUUID = player.getUniqueId();

            if (!helperFunctions.isOwner(player, horse) && !helperFunctions.isPlayerTrusted(horseUUID, playerUUID)) {
                event.setCancelled(true);
                player.sendMessage("You cannot interact with this horse.");
            }
        }
    }

    @EventHandler
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if (event.getEntity() instanceof Horse horse) {
            UUID horseUUID = horse.getUniqueId();
            UUID ownerUUID = helperFunctions.getHorseOwner(horseUUID);

            if (event.getDamager() instanceof Player damager) {
                if (damager.hasPermission("shantek.horseguard.damage")) {
                    return; // Player can damage any horse
                }

                if (ownerUUID != null && !ownerUUID.equals(damager.getUniqueId())) {
                    event.setCancelled(true);
                    damager.sendMessage("This horse is owned by " + Bukkit.getPlayer(ownerUUID).getName());
                }
            } else {
                event.setCancelled(true);
            }
        }
    }
}
