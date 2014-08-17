package net.tbnr.dev.sg.game;

import net.cogzmc.core.Core;
import net.cogzmc.core.network.NetworkServer;
import net.cogzmc.core.player.CPlayer;
import net.tbnr.dev.sg.SurvivalGames;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.hanging.HangingBreakEvent;
import org.bukkit.event.player.PlayerJoinEvent;
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

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        CPlayer onlinePlayer = Core.getOnlinePlayer(event.getPlayer());
        Core.getEnderBarManager().setTextFor(onlinePlayer, SurvivalGames.getInstance().getFormat("enderbar-login", new String[]{"<server>", "SG-" + getServerNumber(Core.getNetworkManager().getThisServer())}));
        Core.getEnderBarManager().setHealthPercentageFor(onlinePlayer, 1F);
    }

    public static Integer getServerNumber(NetworkServer server) {
        String name = server.getName();
        StringBuilder number = new StringBuilder();
        for (int x = name.length()-1; x >= 0; x--) {
            char c = name.charAt(x);
            if (c > '9' || c < '0') break;
            number.append(c);
        }
        String serverNumber = number.reverse().toString();
        return Integer.valueOf(serverNumber);
    }
}
