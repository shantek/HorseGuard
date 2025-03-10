package io.shantek.functions;

import io.shantek.HorseGuard;
import org.bukkit.*;
import org.bukkit.entity.AbstractHorse;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class HelperFunctions {

    private final HorseGuard horseGuard;

    public HelperFunctions(HorseGuard horseGuard) {
        this.horseGuard = horseGuard;
    }

    public AbstractHorse getHorsePlayerOwns(Player player) {
        if (!(player.getVehicle() instanceof AbstractHorse horse)) {
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

    public boolean isOwner(Player player, AbstractHorse horse) {
        UUID horseUUID = horse.getUniqueId();
        UUID ownerUUID = getHorseOwner(horseUUID);
        return ownerUUID != null && ownerUUID.equals(player.getUniqueId());
    }

    public List<String> getTrustedPlayerNames(AbstractHorse horse) {
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

    public void clearTrustedPlayers(UUID horseUUID) {
        if (horseGuard.trustedPlayers.containsKey(horseUUID)) {
            horseGuard.trustedPlayers.get(horseUUID).clear();
            horseGuard.getConfiguration().saveHorseData(); // Save data after modifying
        }
    }

    public String formatEntityType(AbstractHorse entity) {
        String entityType = entity.getType().name().toLowerCase().replace('_', ' ');
        String[] words = entityType.split(" ");
        StringBuilder formattedEntityType = new StringBuilder();

        for (String word : words) {
            if (!word.isEmpty()) {
                formattedEntityType.append(Character.toUpperCase(word.charAt(0)))
                        .append(word.substring(1))
                        .append(" ");
            }
        }

        // Remove the trailing space
        return formattedEntityType.toString().trim();
    }

    public HashSet<UUID> getTrustedPlayers(UUID horseUUID) {
        return horseGuard.trustedPlayers.getOrDefault(horseUUID, new HashSet<>());
    }

    public void setHorseOwner(UUID horseUUID, UUID playerUUID) {
        horseGuard.horseOwners.put(horseUUID, playerUUID);
        horseGuard.getConfiguration().saveHorseData(); // Save data after modifying
    }

    public void removeHorse(UUID horseUUID) {
        horseGuard.horseOwners.remove(horseUUID);
        horseGuard.trustedPlayers.remove(horseUUID);
        horseGuard.getConfiguration().saveHorseData(); // Save data after modifying
    }

    public boolean isWorldDisabled(World world) {
        for (String disabledWorld : horseGuard.disabledWorlds) {
            if (disabledWorld.equalsIgnoreCase(world.getName())) {
                return true;
            }
        }
        return false;
    }

    public void notifyTrust(Player player, OfflinePlayer target) {
        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);
        player.sendTitle("§aTrusted " + target.getName(), "They can now ride your horse!", 10, 40, 10);
    }

    public void notifyUntrust(Player player, OfflinePlayer target) {

        player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
        player.sendTitle("§cUntrusted " + target.getName(), "They can no longer ride your horse.", 10, 40, 10);
    }

    //region Horse Management GUI

    // Main Horse Management Menu
    public void openHorseManagement(Player player, AbstractHorse horse) {
        Inventory gui = Bukkit.createInventory(null, 27, "Horse Management");

        gui.setItem(11, createMenuItem(Material.PLAYER_HEAD, "Trust a Player"));
        gui.setItem(13, createMenuItem(Material.BARRIER, "Untrust a Player"));
        gui.setItem(15, createMenuItem(Material.NAME_TAG, "Transfer Ownership"));

        player.openInventory(gui);
    }

    // Trust a Player Menu
    public void openTrustMenu(Player player, AbstractHorse horse, int page) {
        Inventory gui = Bukkit.createInventory(null, 54, "Trust a Player");

        List<Player> onlinePlayers = Bukkit.getOnlinePlayers().stream()
                .filter(p -> !p.getUniqueId().equals(player.getUniqueId())) // Exclude self
                .filter(p -> !isPlayerTrusted(horse.getUniqueId(), p.getUniqueId())) // Exclude already trusted
                .collect(Collectors.toList());

        fillPagedGui(gui, onlinePlayers, page, "Click to trust player", horse);
        player.openInventory(gui);
    }

    // Untrust a Player Menu
    public void openUntrustMenu(Player player, AbstractHorse horse, int page) {
        Inventory gui = Bukkit.createInventory(null, 54, "Untrust a Player");

        List<UUID> trustedUUIDs = getTrustedPlayers(horse.getUniqueId()).stream()
                .filter(uuid -> !uuid.equals(player.getUniqueId())) // Exclude the owner
                .collect(Collectors.toList());

        List<OfflinePlayer> trustedPlayers = trustedUUIDs.stream()
                .map(Bukkit::getOfflinePlayer)
                .collect(Collectors.toList());

        fillPagedGui(gui, trustedPlayers, page, "Untrust this player", horse);
        player.openInventory(gui);
    }

    // Transfer Ownership Menu
    public void openTransferMenu(Player player, AbstractHorse horse, int page) {
        Inventory gui = Bukkit.createInventory(null, 54, "Transfer Horse Ownership");

        List<Player> onlinePlayers = Bukkit.getOnlinePlayers().stream()
                .filter(p -> !p.getUniqueId().equals(player.getUniqueId())) // Exclude self
                .collect(Collectors.toList());

        fillPagedGui(gui, onlinePlayers, page, "Transfer horse to player", horse);
        player.openInventory(gui);
    }

    // Confirmation Menu for Horse Transfer
    public void openConfirmTransfer(Player player, AbstractHorse horse, Player target) {
        Inventory gui = Bukkit.createInventory(null, 27, "Confirm Transfer");

        gui.setItem(11, createMenuItem(Material.GREEN_WOOL, "Transfer to " + target.getName()));
        gui.setItem(15, createMenuItem(Material.RED_WOOL, "Cancel"));

        player.openInventory(gui);
    }

    // Handle Main Horse Management Menu Clicks
    public void handleHorseManagementClick(InventoryClickEvent event, Player player, AbstractHorse horse) {
        switch (event.getSlot()) {
            case 11 -> openTrustMenu(player, horse, 0);
            case 13 -> openUntrustMenu(player, horse, 0);
            case 15 -> openTransferMenu(player, horse, 0);
        }
    }

    // Handle Trust Clicks
    public void handleTrustClick(InventoryClickEvent event, Player player, AbstractHorse horse) {
        ItemStack clickedItem = event.getCurrentItem();
        if (clickedItem == null || clickedItem.getType() != Material.PLAYER_HEAD) return;

        String targetName = clickedItem.getItemMeta().getDisplayName();
        Player target = Bukkit.getPlayer(targetName);
        if (target == null) return;

        addTrustedPlayer(horse.getUniqueId(), target.getUniqueId());
        notifyTrust(player, target);

        openTrustMenu(player, horse, 0);
    }

    // Handle Untrust Clicks
    public void handleUntrustClick(InventoryClickEvent event, Player player, AbstractHorse horse) {
        ItemStack clickedItem = event.getCurrentItem();
        if (clickedItem == null || clickedItem.getType() != Material.PLAYER_HEAD) return;

        String targetName = clickedItem.getItemMeta().getDisplayName();
        OfflinePlayer target = Bukkit.getOfflinePlayer(targetName);
        if (target == null) return;

        removeTrustedPlayer(horse.getUniqueId(), target.getUniqueId());

        notifyUntrust(player, target);
        openUntrustMenu(player, horse, 0);
    }


    // Handle Transfer Clicks
    public void handleTransferClick(InventoryClickEvent event, Player player, AbstractHorse horse) {
        ItemStack clickedItem = event.getCurrentItem();
        if (clickedItem == null || clickedItem.getType() != Material.PLAYER_HEAD) return;

        String targetName = clickedItem.getItemMeta().getDisplayName();
        Player target = Bukkit.getPlayer(targetName);
        if (target == null) return;

        openConfirmTransfer(player, horse, target);
    }

    // Handle Confirm Transfer Clicks
    public void handleConfirmTransferClick(InventoryClickEvent event, Player player, AbstractHorse horse, String targetName) {
        ItemStack clickedItem = event.getCurrentItem();
        if (clickedItem == null) return;

        Player target = Bukkit.getPlayer(targetName);
        if (target == null) return;

        if (clickedItem.getType() == Material.GREEN_WOOL) {
            setHorseOwner(horse.getUniqueId(), target.getUniqueId());
            clearTrustedPlayers(horse.getUniqueId());
            player.sendMessage("Ownership transferred to " + target.getName() + ".");
            target.sendMessage("You are now the owner of the horse.");
            horse.eject();
        }

        // Ensure proper navigation back to Horse Management
        openHorseManagement(player, horse);
    }


    private void fillPagedGui(Inventory gui, List<? extends OfflinePlayer> players, int page, String lore, AbstractHorse horse) {
        int startIndex = page * 45;
        int endIndex = Math.min(startIndex + 45, players.size());

        for (int i = startIndex; i < endIndex; i++) {
            OfflinePlayer target = players.get(i);
            gui.setItem(i - startIndex, createPlayerHead(target.getName(), lore));
        }

        gui.setItem(49, createMenuItem(Material.BARRIER, "Back to Horse Management"));

        if (page > 0) {
            gui.setItem(45, createMenuItem(Material.ARROW, "Previous Page"));
        }
        if (endIndex < players.size()) {
            gui.setItem(53, createMenuItem(Material.ARROW, "Next Page"));
        }
    }

    public ItemStack createMenuItem(Material material, String name) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            item.setItemMeta(meta);
        }
        return item;
    }

    // Utility function for creating player heads
    public ItemStack createPlayerHead(String playerName, String lore) {
        ItemStack head = new ItemStack(Material.PLAYER_HEAD, 1);
        SkullMeta meta = (SkullMeta) head.getItemMeta();

        if (meta != null) {
            meta.setDisplayName(playerName);

            List<String> loreList = new ArrayList<>();
            loreList.add(lore);
            meta.setLore(loreList);

            // Assign player skin to skull
            meta.setOwningPlayer(Bukkit.getOfflinePlayer(playerName));

            head.setItemMeta(meta);
        }
        return head;
    }

    //endregion

}