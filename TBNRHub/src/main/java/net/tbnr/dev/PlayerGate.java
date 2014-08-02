package net.tbnr.dev;

import net.cogzmc.core.Core;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

/**
 * I called it a gate because everyone passes through it.
 */
public final class PlayerGate implements Listener {
    public static void enable() {
        Bukkit.getPluginManager().registerEvents(new PlayerGate(), TBNRHub.getInstance());
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        TBNRHub.getInstance().getPlayerInventory().setActive(Core.getOnlinePlayer(event.getPlayer()));
        //TODO setup scoreboard
    }
}
