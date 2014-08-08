package net.tbnr.dev.effects;

import net.cogzmc.core.Core;
import net.cogzmc.core.player.CPlayer;
import net.cogzmc.hub.Hub;
import net.tbnr.dev.TBNRHub;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
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
        Player player = event.getPlayer();
        player.getInventory().clear();
        CPlayer onlinePlayer = Core.getOnlinePlayer(player);
        TBNRHub.getInstance().getPlayerInventory().setActive(onlinePlayer);
        player.teleport(TBNRHub.getInstance().getSpawnManager().getSpawn());
        player.setFoodLevel(20);
        player.setHealth(20);
        player.setGameMode(GameMode.SURVIVAL);
        player.getInventory().setHeldItemSlot(4);
        Core.getOnlinePlayer(player).clearChatAll();
        //TODO setup scoreboard
    }
}
