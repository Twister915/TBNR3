package net.tbnr.dev.effects;

import org.bukkit.event.EventHandler;
import org.bukkit.event.block.LeavesDecayEvent;

/**
 * <p/>
 * Latest Change: 04/08/2014.
 * <p/>
 *
 * @author Noy
 * @since 04/08/2014.
 */
public final class AntiLeafDecay extends ModuleListener {

    public AntiLeafDecay() {
        super("leaf-decay");
    }

    @EventHandler
    public void onLeafDecay(LeavesDecayEvent event) { event.setCancelled(true); }
}
