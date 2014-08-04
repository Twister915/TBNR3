package net.tbnr.dev.snowball;

import net.cogzmc.core.Core;
import net.cogzmc.core.player.CPlayer;
import net.tbnr.dev.TBNRHub;
import net.tbnr.dev.setting.PlayerSetting;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;

public final class SnowballMinigame implements Listener {
    private final static String SNOWBALL_HITS = "hub_snowball_hits";
    private final static String SNOWBALL_THROWS = "hub_snowball_throws";
    private final static String SNOWBALL_TAKES = "hub_snowball_hitbys"; //Times you've been hit
    private final static String SNOWBALL_EFFECT = "hub_snowball_effect";
    private final static String SNOWBALL_COUNT = "hub_snowball_count";

    private final static Integer DEFAULT_SNOWBALLS = 32;

    public static void enable() {
        TBNRHub.getInstance().registerListener(new SnowballMinigame());
    }

    public static Integer getHitsBy(CPlayer player) {
        return player.getSettingValue(SNOWBALL_HITS, Integer.class, 0);
    }

    public static Integer getHitsOn(CPlayer player) {
        return player.getSettingValue(SNOWBALL_TAKES, Integer.class, 0);
    }

    public static Integer getThrows(CPlayer player) {
        return player.getSettingValue(SNOWBALL_THROWS, Integer.class, 0);
    }

    public static SnowballEffect getEffect(CPlayer player) {
        String name; return (name = player.getSettingValue(SNOWBALL_EFFECT, String.class)) == null ? SnowballEffect.DEFAULT : SnowballEffect.valueOf(name);
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onSnowballHit(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Snowball)) return;
        Snowball damager = (Snowball) event.getDamager();
        if (!(damager.getShooter() instanceof Player)) return;
        CPlayer shooter = Core.getOnlinePlayer((Player) damager.getShooter());
        if (!(event.getEntity() instanceof Player)) return;
        event.setCancelled(true);
        CPlayer hit = Core.getOnlinePlayer((Player) event.getEntity());
        if (!TBNRHub.getInstance().getSettingsManager().getStateFor(PlayerSetting.SNOWBALL_GAME, hit)) {
            shooter.playSoundForPlayer(Sound.NOTE_PIANO, 1F, 0.8F);
            shooter.sendMessage(TBNRHub.getInstance().getFormat("snowball-target-off", new String[]{"<target>", hit.getDisplayName()}));
            shooter.giveItem(Material.SNOW_BALL);
            damager.remove();
            return;
        }
        getEffect(shooter).player.play(hit, shooter);
        hit.giveItem(Material.SNOW_BALL);
        givePlayerSnowball(hit);
    }

    public static void givePlayerSnowball(CPlayer player) {
        modifySnowballs(player, 1);
    }

    public static void removePlayerSnowball(CPlayer player) {
        modifySnowballs(player, -1);
    }

    private static void modifySnowballs(CPlayer player, Integer snowBalls) {
        player.storeSettingValue(SNOWBALL_COUNT, Math.max(0, numberOfSnowballsFor(player)+snowBalls));
    }

    public static Integer numberOfSnowballsFor(CPlayer player) {
        if (!player.containsSetting(SNOWBALL_COUNT)) player.storeSettingValue(SNOWBALL_COUNT, DEFAULT_SNOWBALLS);
        return player.getSettingValue(SNOWBALL_COUNT, Integer.class);
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onPlayerInteract(PlayerInteractEvent event) {
        if ((event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) || event.getPlayer().getItemInHand().getType() != Material.SNOW_BALL) return;
        CPlayer onlinePlayer = Core.getOnlinePlayer(event.getPlayer());
        if (!TBNRHub.getInstance().getSettingsManager().getStateFor(PlayerSetting.SNOWBALL_GAME, onlinePlayer)) {
            onlinePlayer.playSoundForPlayer(Sound.NOTE_PIANO, 1F, 0.8F);
            onlinePlayer.sendMessage(TBNRHub.getInstance().getFormat("snowball-off"));
            event.setCancelled(true);
            event.getPlayer().updateInventory();
            return;
        }
        removePlayerSnowball(onlinePlayer);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        CPlayer onlinePlayer = Core.getOnlinePlayer(event.getPlayer());
        onlinePlayer.giveItem(Material.SNOW_BALL, numberOfSnowballsFor(onlinePlayer));
    }
}
