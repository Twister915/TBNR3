package net.tbnr.dev.setting;

import net.cogzmc.core.Core;
import net.cogzmc.core.player.CPlayer;
import net.tbnr.dev.TBNRHub;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public final class HidePlayerListener implements Listener {
    public static void enable() {
        TBNRHub.getInstance().registerListener(new HidePlayerListener());
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerJoin(PlayerJoinEvent event) {
        PlayerSettingsManager settingsManager = TBNRHub.getInstance().getSettingsManager();
        CPlayer onlinePlayer = Core.getOnlinePlayer(event.getPlayer());
        if (!settingsManager.getStateFor(PlayerSetting.PLAYERS, onlinePlayer)) {
            hidePlayersFor(onlinePlayer);
        }
        if (!shouldHidePlayer(onlinePlayer)) return;
        Player bukkitPlayer = onlinePlayer.getBukkitPlayer();
        for (CPlayer cPlayer : settingsManager.getOnlinePlayersWithSetting(PlayerSetting.PLAYERS, false)) {
            cPlayer.getBukkitPlayer().hidePlayer(bukkitPlayer);
        }
    }

    public static void hidePlayersFor(CPlayer player) {
        Player bukkitPlayer = player.getBukkitPlayer();
        for (CPlayer cPlayer : Core.getPlayerManager()) {
            if (cPlayer.equals(player)) continue;
            if (shouldHidePlayer(cPlayer)) bukkitPlayer.hidePlayer(cPlayer.getBukkitPlayer());
        }
    }

    public static void unHidePlayersFor(CPlayer player) {
        Player bukkitPlayer = player.getBukkitPlayer();
        for (Player player1 : Bukkit.getOnlinePlayers()) {
            if (bukkitPlayer.equals(player1)) continue;
            bukkitPlayer.showPlayer(player1);
        }
    }

    private static boolean shouldHidePlayer(CPlayer player) {
        return !player.hasPermission("hub.staff");
    }
}
