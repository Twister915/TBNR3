package net.tbnr.dev;

import net.cogzmc.core.player.CPlayer;
import org.bukkit.ChatColor;
import org.bukkit.Sound;

public final class StatsManager {
    public static <T> T getStat(Game game, Stat stat, CPlayer player, Class<T> clazz) {
        return player.getSettingValue("stat_" + game.name() + "_" + stat.name(), clazz);
    }

    public static void setStat(Game game, Stat stat, CPlayer player, Object value) {
        player.storeSettingValue("stat_" + game.name() + "_" + stat.name(), value);
    }

    public static void statChanged(Stat stat, Integer delta, CPlayer player) {
        player.sendMessage(TBNRNetwork.getInstance().getFormat("stat-changed", false,
                new String[]{"<stat>", stat.name().replaceAll("_", " ").trim().toUpperCase()},
                new String[]{"<delta>", String.valueOf(delta)},
                new String[]{"<color>", (delta > 0 ? ChatColor.GREEN : ChatColor.RED).toString()},
                new String[]{"<symbol>", (delta > 0 ? "+" : "")})
        );
        player.playSoundForPlayer(Sound.ORB_PICKUP, 1f, 1 + ((delta > 0 ? 1f : -1f) * 0.2f));
    }
}
