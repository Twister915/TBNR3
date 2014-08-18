package net.tbnr.dev.util;

import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;

public final class SecurityListener implements Listener {
    @EventHandler
    public void onPlayerBreakFrame(HangingBreakByEntityEvent event) {
        event.setCancelled(true);
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEntityEvent event) {
        if (event.getRightClicked().getType() == EntityType.ITEM_FRAME || event.getRightClicked().getType() == EntityType.PAINTING){
            if (event.getPlayer().hasPermission("hub.staff")) return;
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onFrameDamage(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player)) return;
        if (event.getEntityType() == EntityType.ITEM_FRAME || event.getEntityType() == EntityType.PAINTING) {
            if (((Player) event.getDamager()).hasPermission("hub.staff")) return;
            event.setCancelled(true);
        }
    }
}
