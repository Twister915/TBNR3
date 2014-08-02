package net.tbnr.dev;

import net.cogzmc.core.Core;
import net.cogzmc.core.player.CPlayer;
import net.tbnr.dev.inventory.HubInventory;
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
        event.getPlayer().getInventory().clear();
        CPlayer onlinePlayer = Core.getOnlinePlayer(event.getPlayer());
        TBNRHub instance = TBNRHub.getInstance();
        HubInventory inventory = onlinePlayer.hasPermission("hub.inventory.donor") ? instance.getDonorInventory() : instance.getPlayerInventory();
        inventory.setActive(onlinePlayer);
        //TODO setup scoreboard
    }
}
