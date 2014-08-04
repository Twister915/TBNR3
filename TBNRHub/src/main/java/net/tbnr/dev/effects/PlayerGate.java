package net.tbnr.dev.effects;

import net.cogzmc.core.Core;
import net.cogzmc.core.player.CPlayer;
import net.tbnr.dev.TBNRHub;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

/**
 * I called it a gate because everyone passes through it.
 */
public final class PlayerGate implements Listener {

    public static void enable() {
        TBNRHub.getInstance().getServer().getPluginManager().registerEvents(new PlayerGate(), TBNRHub.getInstance());
    }


    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        event.getPlayer().getInventory().clear();
        CPlayer onlinePlayer = Core.getOnlinePlayer(event.getPlayer());
        TBNRHub.getInstance().getPlayerInventory().setActive(onlinePlayer);
        //TODO setup scoreboard
    }
}
