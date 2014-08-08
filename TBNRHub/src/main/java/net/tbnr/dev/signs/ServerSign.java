package net.tbnr.dev.signs;

import lombok.AccessLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Setter;
import net.cogzmc.core.network.NetworkServer;
import net.cogzmc.core.player.CPlayer;
import net.cogzmc.core.util.Point;
import net.tbnr.dev.ServerHelper;
import org.bukkit.ChatColor;
import org.bukkit.block.Sign;

@Data
@EqualsAndHashCode(of = {"point"})
@Setter(AccessLevel.NONE)
public final class ServerSign {
    private final Point point;
    private final ServerSignMatrix matrix;

    private NetworkServer currentlyDisplaying;

    public void onClick(CPlayer player) throws IllegalStateException {
        if (currentlyDisplaying == null || currentlyDisplaying.getOnlineCount() >= matrix.getGame().getMaxPlayers()) throw new IllegalStateException("The server is full!");
        currentlyDisplaying.sendPlayerToServer(player);
    }

    public void update(NetworkServer server) {
        Sign sign = getSign();
        if (server == null) {
            sign.setLine(0, "");
            sign.setLine(1, $("&aTBNR"));
            sign.setLine(2, $("&aNo Server..."));
            sign.setLine(3, "");
            sign.update(true);
            return;
        }
        String status = ServerHelper.getStatus(server);
        if (status == null) ServerHelper.requestStatus(server);
        sign.setLine(0, $("&a[&2TBNR&a]"));
        StringBuilder builder = new StringBuilder();
        for (String s : matrix.getGame().name().split("_")) {
            builder.append(Character.toUpperCase(s.toCharArray()[0]));
        }
        sign.setLine(1, $("&2" + builder.toString()));
        SignState aFor = SignState.getFor(status);
        sign.setLine(2, $("&2[" + aFor.color + aFor.name().toUpperCase() + "&2]"));
        sign.setLine(3, $("&a" + server.getOnlineCount() + "&2/&a" + matrix.getGame().getMaxPlayers()));
        sign.update(true);
        currentlyDisplaying = server;
    }

    private Sign getSign() {
        return (Sign) point.getLocation(matrix.getWorld()).getBlock().getState();
    }

    private static String $(String s) {
        return ChatColor.translateAlternateColorCodes('&', s);
    }

    private enum SignState {
        JOIN(true, "pre_game", ChatColor.GREEN),
        SPECTATE(true, "in_game", ChatColor.RED),
        GAME_OVER(false, "game_over", ChatColor.DARK_PURPLE),
        UNKNOWN(false, null, ChatColor.GRAY);

        final boolean canJoin;
        final String statusName;
        final ChatColor color;


        SignState(boolean canJoin, String statusName, ChatColor color) {
            this.canJoin = canJoin;
            this.statusName = statusName;
            this.color = color;
        }

        public static SignState getFor(String s) {
            if (s == null) return UNKNOWN;
            for (SignState signState : values()) {
                if (signState == UNKNOWN) continue;
                if (signState.statusName.equals(s)) return signState;
            }
            return UNKNOWN;
        }
    }
}
