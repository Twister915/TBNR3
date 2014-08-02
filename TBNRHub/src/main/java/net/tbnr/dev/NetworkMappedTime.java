package net.tbnr.dev;

import net.cogzmc.core.Core;
import net.cogzmc.core.player.CPlayer;
import net.cogzmc.core.player.CPlayerConnectionListener;
import net.cogzmc.core.player.CPlayerJoinException;
import org.bukkit.Bukkit;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import java.net.InetAddress;
import java.util.Map;
import java.util.TimeZone;
import java.util.WeakHashMap;

public final class NetworkMappedTime implements CPlayerConnectionListener {
    private final static long TICKS_IN_DAY = 24000;
    private final static long SECONDS_IN_DAY = 86400;
    private final static long OFFSET = 18000;

    private static Map<CPlayer, DateTimeZone> playerTimezones = new WeakHashMap<>();

    private static void synchronizeTimeWithLocalTime(CPlayer player) {
        DateTimeZone playerTimezone = getPlayerTimezone(player);
        if (playerTimezone == null) return;
        DateTime dateTime = new DateTime(playerTimezone);
        int secondOfDay = dateTime.getSecondOfDay();
        float partsOfDay = (float) secondOfDay / (float) SECONDS_IN_DAY;
        long ticksOfDay = (long) (TICKS_IN_DAY * partsOfDay);
        ticksOfDay = (ticksOfDay + OFFSET) % TICKS_IN_DAY;
        player.getBukkitPlayer().setPlayerTime(ticksOfDay, false);
    }

    private static DateTimeZone getPlayerTimezone(CPlayer player) {
        if (playerTimezones.containsKey(player)) return playerTimezones.get(player);
        TimeZone timeZone;
        try {
            timeZone = TimeZone.getTimeZone(player.getGeoIPInfo().getResponse().getLocation().getTimeZone());
        } catch (NullPointerException e) {
            return null;
        }
        DateTimeZone dateTimeZone = DateTimeZone.forTimeZone(timeZone);
        playerTimezones.put(player, dateTimeZone);
        return dateTimeZone;
    }

    public static void enable() {
        Bukkit.getScheduler().runTaskTimer(TBNRHub.getInstance(), new Runnable() {
            @Override
            public void run() {
                for (CPlayer cPlayer : Core.getPlayerManager()) {
                    try {
                        synchronizeTimeWithLocalTime(cPlayer);
                    } catch (Exception e) {
                        e.printStackTrace();
                        continue;
                    }
                }
            }
        }, 100L, 100L);
        Core.getPlayerManager().registerCPlayerConnectionListener(new NetworkMappedTime());
    }

    private NetworkMappedTime() {}

    @Override
    public void onPlayerLogin(CPlayer player, InetAddress address) throws CPlayerJoinException {
        synchronizeTimeWithLocalTime(player);
    }

    @Override
    public void onPlayerDisconnect(CPlayer player) {
        playerTimezones.remove(player);
    }
}
