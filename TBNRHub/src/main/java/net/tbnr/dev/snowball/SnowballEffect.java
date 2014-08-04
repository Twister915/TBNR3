package net.tbnr.dev.snowball;

import net.cogzmc.core.Core;
import net.cogzmc.core.effect.particle.ParticleEffect;
import net.cogzmc.core.effect.particle.ParticleEffectType;
import net.cogzmc.core.player.CPlayer;
import net.tbnr.dev.TBNRHub;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public enum SnowballEffect {
    DEFAULT("Disapear", new EffectPlayer() {
        @Override
        public void play(final CPlayer target, final CPlayer thrower) {
            ParticleEffect effect = new ParticleEffect(ParticleEffectType.HEART);
            effect.setAmount(10);
            effect.setXSpread(1.4F);
            effect.setYSpread(1F);
            effect.setZSpread(1.4F);
            effect.setSpeed(2F);
            Player taBukkitPlayer = target.getBukkitPlayer();
            Collection<CPlayer> onlinePlayers = new ArrayList<>(Core.getOnlinePlayers());
            onlinePlayers.remove(target);
            effect.emitToPlayers(onlinePlayers, taBukkitPlayer.getLocation());
            thrower.getBukkitPlayer().hidePlayer(taBukkitPlayer);
            thrower.playSoundForPlayer(Sound.LEVEL_UP, 1F, 1.5F);
            Bukkit.getScheduler().runTaskLater(TBNRHub.getInstance(), new Runnable() {
                @Override
                public void run() {
                    if (target.isOnline() && thrower.isOnline()) thrower.getBukkitPlayer().showPlayer(target.getBukkitPlayer());
                }
            }, 1200L);
        }
    }, "Makes a player disappear leaving a heart behind.", "Players return after 60 seconds.");

    final EffectPlayer player;
    final String name;
    final List<String> description;

    SnowballEffect(String name, EffectPlayer player, String... description) {
        this.player = player;
        this.name = name;
        this.description = Arrays.asList(description);
    }
}
