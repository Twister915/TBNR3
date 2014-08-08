package net.tbnr.dev.signs;

import lombok.Data;
import net.cogzmc.core.player.CPlayer;
import net.cogzmc.core.util.Point;
import net.cogzmc.core.util.Region;
import net.tbnr.dev.Game;
import net.tbnr.dev.TBNRHub;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerInteractEvent;

@Data
public final class SignSetupSession implements Listener {
    private final CPlayer player;
    private final SignSetupCommand command;

    private Point p1;
    private Region region;
    private boolean listenForGame = false;

    public void go() {
        player.sendMessage(TBNRHub.getInstance().getFormat("setup-signs"));
        TBNRHub.getInstance().registerListener(this);
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (!event.getPlayer().equals(player.getBukkitPlayer())) return;
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK && event.getAction() != Action.LEFT_CLICK_BLOCK) return;
        if (region != null) return;
        if (p1 == null) {
            p1 = Point.of(event.getClickedBlock());
            event.getPlayer().sendMessage(TBNRHub.getInstance().getFormat("setup-signs-point-selected"));
        } else {
            region = new Region(p1, Point.of(event.getClickedBlock()));
            event.getPlayer().sendMessage(TBNRHub.getInstance().getFormat("setup-signs-point-selected"));
            event.getPlayer().sendMessage(TBNRHub.getInstance().getFormat("setup-signs-game"));
            listenForGame = true;
        }
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        if (!event.getPlayer().equals(player.getBukkitPlayer())) return;
        if (listenForGame) {
            event.setCancelled(true);
            Game game;
            try {
                game = Game.valueOf(event.getMessage().trim().toUpperCase());
            } catch (IllegalArgumentException e) {
                player.sendMessage(TBNRHub.getInstance().getFormat("setup-signs-no-game"));
                return;
            }
            ServerSignMatrix serverSignMatrix = new ServerSignMatrix(region, player.getBukkitPlayer().getWorld(), game);
            TBNRHub.getInstance().getMatrixManager().save(serverSignMatrix);
            player.sendMessage(TBNRHub.getInstance().getFormat("setup-signs-done"));
            HandlerList.unregisterAll(this);
            command.removeFromMap(player);
        }
    }
}
