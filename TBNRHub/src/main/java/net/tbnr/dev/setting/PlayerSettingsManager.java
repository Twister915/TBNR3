package net.tbnr.dev.setting;

import net.cogzmc.core.Core;
import net.cogzmc.core.player.CPlayer;
import net.cogzmc.core.player.CPlayerConnectionListener;
import net.cogzmc.core.player.CPlayerJoinException;
import net.tbnr.dev.TBNRHub;
import org.bukkit.Bukkit;
import org.bukkit.event.Listener;

import java.net.InetAddress;
import java.util.*;

public final class PlayerSettingsManager implements CPlayerConnectionListener, Listener {
    private final Map<CPlayer, Map<PlayerSetting, Boolean>> playerSettings = new WeakHashMap<>();
    private final Map<CPlayer, Set<PlayerSetting>> lockedSettings = new WeakHashMap<>();

    public PlayerSettingsManager() {
        Core.getPlayerManager().registerCPlayerConnectionListener(this);
        TBNRHub.getInstance().registerListener(this);
    }

    public Boolean getStateFor(PlayerSetting setting, CPlayer player) {
        Map<PlayerSetting, Boolean> playerSettingBooleanMap = playerSettings.get(player);
        if (playerSettingBooleanMap == null || !playerSettingBooleanMap.containsKey(setting)) return setting.defaultValue;
        return playerSettingBooleanMap.get(setting);
    }

    public void toggleStateFor(PlayerSetting setting, CPlayer player) throws SettingChangeException {
        if (getLockedSettings(player).contains(setting)) throw new SettingChangeException();
        boolean b = !getStateFor(setting, player);
        playerSettings.get(player).put(setting, b);
        fireObservers(player, setting, b);
    }

    public void setStateFor(PlayerSetting setting, CPlayer player, Boolean value) throws SettingChangeException {
        if (getLockedSettings(player).contains(setting)) throw new SettingChangeException();
        playerSettings.get(player).put(setting, value);
        fireObservers(player, setting, value);
    }

    public Set<CPlayer> getOnlinePlayersWithSetting(PlayerSetting setting, Boolean value) {
        Set<CPlayer> players = new HashSet<>();
        for (CPlayer cPlayer : playerSettings.keySet()) {
            if (getStateFor(setting, cPlayer).equals(value)) players.add(cPlayer);
        }
        return players;
    }

    public void lockSetting(PlayerSetting setting, CPlayer player) {
        getLockedSettings(player).add(setting);
    }

    public void unlockSetting(PlayerSetting setting, CPlayer player) {
        getLockedSettings(player).remove(setting);
    }

    private Set<PlayerSetting> getLockedSettings(CPlayer player) {
        if (lockedSettings.containsKey(player)) return lockedSettings.get(player);
        lockedSettings.put(player, new HashSet<PlayerSetting>());
        return getLockedSettings(player);
    }

    @Override
    public void onPlayerLogin(CPlayer player, InetAddress address) throws CPlayerJoinException {
        HashMap<PlayerSetting, Boolean> playerSettingBooleanHashMap = new HashMap<>();
        for (PlayerSetting playerSetting : PlayerSetting.values()) {
            if (player.containsSetting(playerSetting.settingKey)) {
                Boolean settingValue = player.getSettingValue(playerSetting.settingKey, Boolean.class, playerSetting.defaultValue);
                playerSettingBooleanHashMap.put(playerSetting, settingValue);
                Core.logDebug(playerSetting.settingKey + ":" + player.getName() + " = " + settingValue);
            }
        }
        playerSettings.put(player, playerSettingBooleanHashMap);
    }

    @Override
    public void onPlayerDisconnect(CPlayer player) {
        for (Map.Entry<PlayerSetting, Boolean> setting : playerSettings.get(player).entrySet()) {
            if (setting.getValue() != setting.getKey().defaultValue) player.storeSettingValue(setting.getKey().settingKey, setting.getValue());
            else player.removeSettingValue(setting.getKey().settingKey);
        }
        playerSettings.remove(player);
        lockedSettings.remove(player);
    }

    private void fireObservers(CPlayer player, PlayerSetting setting, Boolean value) {
        if (setting.observer != null) setting.observer.settingChanged(value, player);
        Bukkit.getPluginManager().callEvent(new SettingChangeEvent(player, setting, value));
    }
}
