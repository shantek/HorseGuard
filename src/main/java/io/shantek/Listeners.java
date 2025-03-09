package io.shantek;

import io.shantek.functions.HelperFunctions;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.AbstractHorse;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.SkeletonHorse;
import org.bukkit.entity.ZombieHorse;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityTameEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;

import java.util.HashSet;
import java.util.UUID;

public class Listeners implements Listener {

    private final HorseGuard plugin; // Reference to the main plugin
    private final HelperFunctions helperFunctions;

    public Listeners(HorseGuard horseGuard) {
        this.plugin = horseGuard;
        this.helperFunctions = new HelperFunctions(horseGuard);
    }

    @EventHandler
    public void onEntityTame(EntityTameEvent event) {
        if (helperFunctions.isWorldDisabled(event.getEntity().getWorld()))
            return;
        if (event.getEntity() instanceof AbstractHorse horse) {
            Player player = (Player) event.getOwner();
            UUID entityUUID = horse.getUniqueId();
            UUID playerUUID = player.getUniqueId();
            helperFunctions.setHorseOwner(entityUUID, playerUUID);
            player.sendMessage(plugin.getMessagePrefix() + "You tamed a new " + horse.getType().name().toLowerCase() + ".");
        }
    }

    @EventHandler
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        if (helperFunctions.isWorldDisabled(event.getPlayer().getWorld()))
            return;
        if (event.getRightClicked() instanceof AbstractHorse entity) {
            if (entity instanceof ZombieHorse) {
                // Ignore interactions with ZombieHorse
                return;
            }
            Player player = event.getPlayer();
            UUID entityUUID = entity.getUniqueId();

            if (entity.getOwner() != null && player.hasPermission("shantek.horseguard.ride")) {
                return;
            }

            if (handleEntityInteraction(player, entity, entityUUID)) {
                event.setCancelled(true);
            }
        }
    }

    private boolean handleEntityInteraction(Player player, AbstractHorse entity, UUID entityUUID) {
        UUID ownerUUID = helperFunctions.getHorseOwner(entityUUID);

        if (ownerUUID == null) {
            // No owner, allow the player to claim it
            claimEntity(player, entity, entityUUID);
            entity.setOwner(player); // Set player as the owner of the entity
            player.sendMessage(plugin.getMessagePrefix() + "You have tamed the " + helperFunctions.formatEntityType(entity) + ".");
            return false;
        } else if (!ownerUUID.equals(player.getUniqueId())) {
            // Check if the player is trusted
            if (!isTrustedPlayer(player.getUniqueId(), entityUUID)) {
                String ownerName = getOwnerName(ownerUUID);
                player.sendMessage(plugin.getMessagePrefix() + "This " + helperFunctions.formatEntityType(entity) + " belongs to " + ownerName + ".");
                return true;
            } else {
                return false;
            }
        } else {
            // Owner is the player themselves
            return false;
        }
    }

    @EventHandler
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if (helperFunctions.isWorldDisabled(event.getEntity().getWorld()))
            return;
        if (event.getEntity() instanceof AbstractHorse entity && event.getDamager() instanceof Player player) {

            if (player.hasPermission("shantek.horseguard.damage")) {
                return;
            }

            UUID entityUUID = entity.getUniqueId();
            UUID ownerUUID = helperFunctions.getHorseOwner(entityUUID);
            if (ownerUUID != null) {
                String ownerName = getOwnerName(ownerUUID);
                if (!ownerUUID.equals(player.getUniqueId())) {
                    event.setCancelled(true);
                    player.sendMessage(plugin.getMessagePrefix() + "This " + helperFunctions.formatEntityType(entity) + " belongs to " + ownerName);
                }
            }
        }
    }
    
    // Event handler for horse death event. When a horse dies, remove it from the database.
    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        if (event.getEntity() instanceof AbstractHorse entity) {
            UUID entityUUID = entity.getUniqueId();
            helperFunctions.removeHorse(entityUUID);
        }
    }


    private void claimEntity(Player player, LivingEntity entity, UUID entityUUID) {
        UUID playerUUID = player.getUniqueId();
        helperFunctions.setHorseOwner(entityUUID, playerUUID);
    }

    private boolean isTrustedPlayer(UUID playerUUID, UUID entityUUID) {
        // Check if the player is a trusted rider of the entity
        HashSet<UUID> trustedPlayers = helperFunctions.getTrustedPlayers(entityUUID);
        return trustedPlayers != null && trustedPlayers.contains(playerUUID);
    }

    public String getOwnerName(UUID ownerUUID) {
        if (ownerUUID == null) {
            return "Unknown";
        }
        OfflinePlayer owner = Bukkit.getOfflinePlayer(ownerUUID);
        return owner.getName() != null ? owner.getName() : "Unknown";
    }
}