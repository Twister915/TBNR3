package net.tbnr.dev.setting;

import net.cogzmc.core.Core;
import net.cogzmc.core.player.CPlayer;
import net.tbnr.dev.TBNRHub;
import org.bukkit.Sound;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.Set;

public final class DisableChatListener implements Listener {
    public static void enable() {
        TBNRHub.getInstance().registerListener(new DisableChatListener());
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        PlayerSettingsManager settingsManager = TBNRHub.getInstance().getSettingsManager();
        CPlayer onlinePlayer = Core.getOnlinePlayer(event.getPlayer());
        if (!settingsManager.getStateFor(PlayerSetting.CHAT, onlinePlayer)) {
            event.setCancelled(true);
            onlinePlayer.playSoundForPlayer(Sound.NOTE_PIANO, 1f, 0.75f);
            onlinePlayer.sendMessage();
            return;
        }
        Set<CPlayer> onlinePlayersWithSetting = settingsManager.getOnlinePlayersWithSetting(PlayerSetting.CHAT, false);
        event.getRecipients().removeAll(onlinePlayersWithSetting);
    }
}
