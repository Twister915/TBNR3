package net.tbnr.dev.sg;

import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockSpreadEvent;
import org.bukkit.event.block.LeavesDecayEvent;

public final class WorldListener implements Listener {
    @EventHandler
    public void onFireSpread(BlockSpreadEvent event) {
        if (event.getBlock().getType() == Material.FIRE || event.getBlock().getType() == Material.LAVA) event.setCancelled(true);
    }

    @EventHandler
    public void onBlockBurn(BlockBurnEvent event) {
        event.setCancelled(true);
    }

    @EventHandler
    public void onLeafDecay(LeavesDecayEvent event) {
        event.setCancelled(true);
    }
}
