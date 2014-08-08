package net.tbnr.dev.sg.game;

import net.tbnr.dev.sg.SurvivalGames;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.hanging.HangingBreakEvent;
import org.bukkit.event.player.PlayerMoveEvent;

public final class PreGameListener implements Listener {
    @EventHandler
    public void onPvP(EntityDamageEvent event) {
        switch (event.getCause()) {
            case VOID:
                break;
            default:
                event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        event.setCancelled(true);
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        event.setCancelled(true);
    }

    @EventHandler
    public void onHangingDestroy(HangingBreakEvent event) {
        event.setCancelled(true);
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        if (event.getTo().getY() <= 1)
            event.getPlayer().teleport(SurvivalGames.getInstance().getGameManager().getPreGameLobby().getSpawnPoints().iterator().next().getLocation(event.getTo().getWorld()));
    }

    @EventHandler
    public void onPlayerHungerLost(FoodLevelChangeEvent event) {
        event.setCancelled(true);
    }
}
