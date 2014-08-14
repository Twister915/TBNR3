package net.tbnr.dev;

import net.cogzmc.core.player.COfflinePlayer;
import net.cogzmc.core.player.CPlayer;
import net.cogzmc.core.player.DatabaseConnectException;

public final class PassManager {
    public static Integer getPassesForClass(String clazzName, COfflinePlayer player) {
        return player.getSettingValue(clazzName + "_pass", Integer.class, 0);
    }

    public static void addPassesForClass(Integer passes, String clazz, COfflinePlayer player) throws DatabaseConnectException {
        setPassesForClass(getPassesForClass(clazz, player) + passes, clazz, player);
    }

    public static void setPassesForClass(Integer passes, String clazz, COfflinePlayer player) throws DatabaseConnectException {
        player.storeSettingValue(clazz + "_pass", passes);
        player.saveIntoDatabase();
    }
}
