package net.tbnr.dev.setting;

import net.cogzmc.core.player.CPlayer;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;

public enum PlayerSetting {
    CHAT("chat_diabled", true),
    PLAYERS("hide_players", true, new SettingChangeObserver() {
        @Override
        public void settingChanged(boolean value, CPlayer target) {
            if (!value) HidePlayerListener.hidePlayersFor(target);
            else HidePlayerListener.unHidePlayersFor(target);
        }
    }),
    SNOWBALL_GAME("snowball_game", true),
    FLY_IN_HUB("fly_in_hub", false, new SettingChangeObserver() {
        @Override
        public void settingChanged(boolean value, CPlayer target) {
            Player bukkitPlayer = target.getBukkitPlayer();
            bukkitPlayer.setAllowFlight(value);
            if (bukkitPlayer.isFlying() && !value) bukkitPlayer.setFlying(false);
        }
    }),
    JUMP_BOOST("jump_boost", false, new SettingChangeObserver() {
        @Override
        public void settingChanged(boolean value, CPlayer target) {
            if (value) {
                target.addStatusEffect(PotionEffectType.JUMP, 2);
                target.addStatusEffect(PotionEffectType.SPEED, 2);
            } else {
                target.removeStatusEffect(PotionEffectType.JUMP);
                target.removeStatusEffect(PotionEffectType.SPEED);
            }
        }
    });

    final String settingKey;
    final SettingChangeObserver observer;
    final Boolean defaultValue;

    PlayerSetting(String key, Boolean defaultValue) {
        this.settingKey = key;
        this.defaultValue = defaultValue;
        this.observer = null;
    }

    PlayerSetting(String key, Boolean defaultValue, SettingChangeObserver observer) {
        this.settingKey = key;
        this.defaultValue = defaultValue;
        this.observer = observer;
    }
}
