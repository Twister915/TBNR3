package net.tbnr.dev.inventory;

import com.google.api.client.repackaged.com.google.common.base.Joiner;
import net.cogzmc.core.player.CPlayer;
import net.cogzmc.core.player.message.FHoverActionType;
import net.cogzmc.core.player.message.FancyMessage;
import net.tbnr.dev.TBNRHub;
import net.tbnr.dev.setting.PlayerSetting;
import net.tbnr.dev.setting.PlayerSettingsManager;
import org.bukkit.ChatColor;
import org.bukkit.Sound;

public class SettingUtils {
    public static void onSettingToggle(CPlayer player, PlayerSetting setting) {
        PlayerSettingsManager settingsManager = TBNRHub.getInstance().getSettingsManager();
        boolean stateFor = settingsManager.getStateFor(setting, player);
        player.playSoundForPlayer(Sound.NOTE_PLING, 1f, stateFor ? 1.2F : 0.8F);
        StringBuilder builder = new StringBuilder(setting.getName().toLowerCase());
        builder.setCharAt(0, Character.toUpperCase(builder.charAt(0)));
        String name = builder.toString();
        FancyMessage.start(">>").bold().color(ChatColor.GREEN).done()
                .addMore(" " + (stateFor ? "ON" : "OFF")).color((stateFor ? ChatColor.GREEN : ChatColor.RED)).bold().done()
                .addMore(" " + name).withHoverAction(FHoverActionType.SHOW_TEXT, Joiner.on("\n").join(setting.getDescription())).color(ChatColor.GRAY).done()
                .complete().sendTo(player);
    }

    public static void onSettingDeny(CPlayer player) {
        player.playSoundForPlayer(Sound.NOTE_PIANO, 1f, 0.75f);
        player.sendMessage(TBNRHub.getInstance().getFormat("cannot-use-toggle"));
    }
}
