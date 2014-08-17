package net.tbnr.dev.wardrobe;

import net.cogzmc.core.Core;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerJoinEvent;

public final class WardrobeListener implements Listener {
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        new WardrobeState(Core.getOnlinePlayer(event.getPlayer())).display();
    }

    @EventHandler
    public void onPlayerRemoveArmor(InventoryClickEvent event) {
        if (event.getSlotType() == InventoryType.SlotType.ARMOR) event.setCancelled(true);
    }
}
