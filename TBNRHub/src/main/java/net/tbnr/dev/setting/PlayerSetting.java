package net.tbnr.dev.setting;

import net.cogzmc.core.player.CPlayer;

public enum PlayerSetting {
    CHAT("chat_diabled", true),
    PLAYERS("hide_players", true, new SettingChangeObserver() {
        @Override
        public void settingChanged(boolean value, CPlayer target) {
            if (!value) HidePlayerListener.hidePlayersFor(target);
            else HidePlayerListener.unHidePlayersFor(target);
        }
    }),
    SNOWBALL_GAME("snowball_game", true);

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
