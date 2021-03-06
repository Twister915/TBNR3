package net.tbnr.dev.setting;

import net.cogzmc.core.player.CPlayer;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;

import java.util.Arrays;
import java.util.List;

public enum PlayerSetting {
    PLAYERS("hide_players", "Players", true, new SettingChangeObserver() {
        @Override
        public void settingChanged(boolean value, CPlayer target) {
            if (!value) SettingListener.hidePlayersFor(target);
            else SettingListener.unHidePlayersFor(target);
        }
    }, "Hides players (when off) from your view.", "Does not hide staff members."),
    FLY_IN_HUB("fly_in_hub", "Flight", false, "hub.perk.flight", new SettingChangeObserver() {
        @Override
        public void settingChanged(boolean value, CPlayer target) {
            Player bukkitPlayer = target.getBukkitPlayer();
            bukkitPlayer.setAllowFlight(value);
            if (bukkitPlayer.isFlying() && !value) bukkitPlayer.setFlying(false);
        }
    }, "Flight in the hub"),
    JUMP_BOOST("jump_boost", "Jump Boost", false, new SettingChangeObserver() {
        @Override
        public void settingChanged(boolean value, CPlayer target) {
            if (value) {
                target.addStatusEffect(PotionEffectType.JUMP, 5);
                target.addStatusEffect(PotionEffectType.SPEED, 5);
            } else {
                target.removeStatusEffect(PotionEffectType.JUMP);
                target.removeStatusEffect(PotionEffectType.SPEED);
            }
        }
    }, "Adds a jump and speed boost."),
    PARTICLE_EFFECT("particle_effect", "Particle Effect", "hub.perk.particle", false, "Makes beautiful particles", "follow you as you walk.");

    final String settingKey;
    final String name;
    final List<String> description;
    final SettingChangeObserver observer;
    final Boolean defaultValue;
    final String permission;

    PlayerSetting(String key, String name, Boolean defaultValue, SettingChangeObserver observer, String... description) {
        this.settingKey = key;
        this.defaultValue = defaultValue;
        this.observer = observer;
        this.name = name;
        this.description = Arrays.asList(description);
        this.permission = null;
    }

    PlayerSetting(String key, String name, Boolean defaultValue, String permission, SettingChangeObserver observer, String... description) {
        this.settingKey = key;
        this.defaultValue = defaultValue;
        this.observer = observer;
        this.name = name;
        this.description = Arrays.asList(description);
        this.permission = permission;
    }

    PlayerSetting(String key, String name,  String permission, Boolean defaultValue, String... description) {
        this.settingKey = key;
        this.defaultValue = defaultValue;
        this.name = name;
        this.observer = null;
        this.description = Arrays.asList(description);
        this.permission = permission;
    }

        public String getName() {
        return name;
    }

    public List<String> getDescription() {
        return description;
    }

    public String getPermission() {
        return permission;
    }
}
