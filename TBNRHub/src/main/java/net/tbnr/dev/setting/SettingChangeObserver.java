package net.tbnr.dev.setting;

import net.cogzmc.core.player.CPlayer;

public interface SettingChangeObserver {
    void settingChanged(boolean value, CPlayer target);
}
