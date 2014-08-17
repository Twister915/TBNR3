package net.tbnr.dev.effects;

import net.cogzmc.core.Core;
import net.cogzmc.core.effect.particle.ParticleEffect;
import net.cogzmc.core.effect.particle.ParticleEffectType;
import net.cogzmc.core.player.CPlayer;
import net.cogzmc.core.util.Point;
import net.tbnr.dev.TBNRHub;
import net.tbnr.dev.setting.PlayerSetting;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

/**
 * <p/>
 * Latest Change: 17/08/2014.
 * <p/>
 *
 * @author Noy
 * @since 17/08/2014.
 */
public final class RainbowEffectListener implements Listener {
    private static short[] woolDataValues = new short[]{14, 1, 4, 5, 11, 2, 10};

    public static void enable() {
        TBNRHub.getInstance().registerListener(new RainbowEffectListener());
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        CPlayer onlinePlayer = Core.getOnlinePlayer(event.getPlayer());
        if (!TBNRHub.getInstance().getSettingsManager().getStateFor(PlayerSetting.RAINBOW_PARTICLE_EFFECT, onlinePlayer)) return;
        ParticleEffect[] effects = new ParticleEffect[7];
        Location[] locations = new Location[effects.length];
        Point point = onlinePlayer.getPoint();
        World world = onlinePlayer.getBukkitPlayer().getWorld();
        float yInc = 2.0f/locations.length;
        for (int i = 0; i < effects.length ; i++) {
            effects[i] = new ParticleEffect(ParticleEffectType.customTileCrack(Material.WOOL, woolDataValues[i]));
            locations[i] = point.deepCopy().getLocation(world).add(0, yInc, 0);
            effects[i].setAmount(10);
            effects[i].setYSpread(0.3f);
            effects[i].setZSpread(0.3f);
            effects[i].setXSpread(0.3f);
            effects[i].setSpeed(1f);
        }
        for (CPlayer player : Core.getOnlinePlayers()) {
            if (!TBNRHub.getInstance().getSettingsManager().getStateFor(PlayerSetting.PLAYERS, player)) continue;
            if (player.getPoint().distanceSquared(point) > 400) continue;
            for (int i = 0; i < effects.length; i++) {
                effects[i].emitToPlayer(player, locations[i]);
            }
        }
    }
}
