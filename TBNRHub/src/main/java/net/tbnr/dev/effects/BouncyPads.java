package net.tbnr.dev.effects;

import net.cogzmc.core.Core;
import net.cogzmc.core.player.CPlayer;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.util.Vector;

/**
 * <p/>
 * Latest Change: 04/08/2014.
 * <p/>
 *
 * @author Noy
 * @since 04/08/2014.
 */
public final class BouncyPads extends ModuleListener {

    public BouncyPads() {
        super("bouncy-pads");
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        Block block = event.getClickedBlock();
        CPlayer cPlayer = Core.getOnlinePlayer(event.getPlayer());
        Player player = cPlayer.getBukkitPlayer();
        if (!(event.getAction() == Action.PHYSICAL)) return;
        if (!(block.getType().equals(Material.STONE_PLATE) || block.getType().equals(Material.WOOD_PLATE))) return;
        if (!block.getRelative(BlockFace.DOWN).getType().equals(Material.WOOL)) return;
        player.setVelocity(player.getLocation().getDirection().multiply(2).add(new Vector(0, 0.5, 0)));
        cPlayer.playSoundForPlayer(Sound.FIREWORK_LAUNCH);
        event.setCancelled(true);
    }
}
