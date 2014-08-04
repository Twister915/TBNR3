package net.tbnr.dev.effects;

import net.tbnr.dev.TBNRHub;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerMoveEvent;

/**
 * <p/>
 * Latest Change: 04/08/2014.
 * <p/>
 *
 * @author Noy
 * @since 04/08/2014.
 */
public final class BelowZero extends ModuleListener {

    public BelowZero() {
        super("below-zero");
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        if (player.hasPermission("hub.below-zero")) return;
        if (player.getLocation().getY() < 0) {
            player.teleport(player.getWorld().getSpawnLocation());
            player.playSound(player.getLocation(), Sound.CHICKEN_EGG_POP, 10, 0.5F);
            player.sendMessage(TBNRHub.getInstance().getFormat("formats.tpd-spawn"));
        }
    }
}
