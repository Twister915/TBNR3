package net.tbnr.dev.effects;

import net.cogzmc.core.Core;
import net.cogzmc.core.player.CPlayer;
import net.cogzmc.hub.model.SettingsManager;
import net.tbnr.dev.TBNRHub;
import net.tbnr.dev.inventory.SettingUtils;
import net.tbnr.dev.setting.PlayerSetting;
import net.tbnr.dev.setting.PlayerSettingsManager;
import net.tbnr.dev.setting.SettingChangeException;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

/**
 * <p/>
 * Latest Change: 06/08/2014.
 * <p/>
 *
 * @author Noy
 * @since 06/08/2014.
 */
public final class HeightTracker implements Listener {

    public static void enable() {
        TBNRHub.getInstance().registerListener(new HeightTracker());
    }

    private final Integer maxHeight;

    private HeightTracker() {
        maxHeight = TBNRHub.getInstance().getConfig().getInt("max-height");
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        if (event.getTo().getY() < maxHeight) return;
        Player player = event.getPlayer();
        if (player.hasPermission("hub.bypass-height")) return;
        if (TBNRHub.getInstance().getParkourManager().getParkourFor(Core.getOnlinePlayer(player)) != null) return;
        TBNRHub.getInstance().getSpawnManager().teleportToSpawn(Core.getOnlinePlayer(player));
    }
}