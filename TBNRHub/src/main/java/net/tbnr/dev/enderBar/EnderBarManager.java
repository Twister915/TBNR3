package net.tbnr.dev.enderBar;

import lombok.Value;
import net.cogzmc.core.Core;
import net.cogzmc.core.player.CPlayer;
import net.cogzmc.core.player.CPlayerConnectionListener;
import net.cogzmc.core.player.CPlayerJoinException;

import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;

public class EnderBarManager {
    private static final Map<CPlayer, Map<Integer, EnderBarItem>> prioritizedBarMessages = new WeakHashMap<>();
    private static final Map<CPlayer, Boolean> currentStates = new WeakHashMap<>();

    public static void setStateForID(CPlayer player, Integer id, String message, Float health) {
        Map<Integer, EnderBarItem> eMap = prioritizedBarMessages.get(player);
        if (eMap == null) {
            eMap = new HashMap<>();
            prioritizedBarMessages.put(player, eMap);
        }
        eMap.put(id, new EnderBarItem(message, health));
        ensureState(player);
    }

    public static void clearId(CPlayer player, Integer id) {
        Map<Integer, EnderBarItem> eMap = prioritizedBarMessages.get(player);
        eMap.remove(id);
        ensureState(player);
    }

    public static EnderBarItem getStateFor(CPlayer player) {
        Map<Integer, EnderBarItem> integerEnderBarItemMap = prioritizedBarMessages.get(player);
        Integer max = 0;
        for (Integer integer : integerEnderBarItemMap.keySet()) {
            if (integer > max) max = integer;
        }
        return integerEnderBarItemMap.get(max);
    }

    private static void ensureState(CPlayer player) {
        Boolean aBoolean = currentStates.get(player);
        EnderBarItem stateFor = getStateFor(player);
        if (aBoolean != null && aBoolean && stateFor == null) {
            Core.getEnderBarManager().hideBarFor(player);
            currentStates.remove(player);
            return;
        } else if (stateFor == null) return;
        Core.getEnderBarManager().setTextFor(player, stateFor.getMessage());
        Core.getEnderBarManager().setHealthPercentageFor(player, stateFor.getHealth());
        currentStates.put(player, true);
    }

    @Value
    public static class EnderBarItem {
        private String message;
        private Float health;
    }

    public static class EnderBarLoginObserver implements CPlayerConnectionListener {
        @Override
        public void onPlayerLogin(CPlayer player, InetAddress address) throws CPlayerJoinException {
            prioritizedBarMessages.put(player, new HashMap<Integer, EnderBarItem>());
        }

        @Override
        public void onPlayerDisconnect(CPlayer player) {
            prioritizedBarMessages.remove(player);
            currentStates.remove(player);
        }
    }
}
