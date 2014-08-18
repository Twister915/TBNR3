package net.tbnr.dev.signs;

import lombok.Data;
import net.cogzmc.core.Core;
import net.cogzmc.core.player.CPlayer;
import net.cogzmc.core.util.Point;
import net.tbnr.dev.TBNRHub;
import org.bukkit.Sound;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

@Data
public final class ServerSignListener implements Listener {
    private final ServerSignMatrixManager manager;

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (!(event.getAction() == Action.RIGHT_CLICK_BLOCK || event.getAction() == Action.LEFT_CLICK_BLOCK)) return;
        Point of = Point.of(event.getClickedBlock());
        for (ServerSignMatrix serverSignMatrix : manager.getMatricies()) {
            if (!serverSignMatrix.getRegion().isWithin(of)) continue;
            CPlayer onlinePlayer = Core.getOnlinePlayer(event.getPlayer());
            try {
                ServerSign signAt = serverSignMatrix.getSignAt(of);
                if (signAt == null) continue;
                signAt.onClick(onlinePlayer);
            } catch (IllegalStateException e) {
                onlinePlayer.playSoundForPlayer(Sound.NOTE_PLING, 1f, 0.8f);
                onlinePlayer.sendMessage(TBNRHub.getInstance().getFormat("sign-join-deny", new String[]{"<error>", e.getMessage()}));
            }
            break;
        }
    }
}
