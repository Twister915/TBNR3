package net.tbnr.dev.signs;

import lombok.AccessLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Setter;
import net.cogzmc.core.network.NetworkServer;
import net.cogzmc.core.player.CPlayer;
import net.cogzmc.core.util.Point;
import net.tbnr.dev.Game;
import net.tbnr.dev.JoinAttemptHandler;
import net.tbnr.dev.ServerHelper;
import net.tbnr.dev.TBNRHub;
import org.bukkit.ChatColor;
import org.bukkit.block.Sign;

@Data
@EqualsAndHashCode(of = {"point"})
@Setter(AccessLevel.NONE)
public final class ServerSign implements JoinAttemptHandler.JoinAttemptDelegate {
    private final Point point;
    private final ServerSignMatrix matrix;

    private NetworkServer currentlyDisplaying;

    public void onClick(CPlayer player) throws IllegalStateException {
        if (currentlyDisplaying == null) return;
        currentlyDisplaying.sendPlayerToServer(player);
    }

    public void update(NetworkServer server) {
        Sign sign = getSign();
        if (server == null) {
            sign.setLine(0, $("&4░░░░░░░░░░░"));
            sign.setLine(1, $("&aTBNR"));
            sign.setLine(2, $("&aNo Server..."));
            sign.setLine(3, $("&4░░░░░░░░░░░"));
            sign.update(true);
            return;
        }
        String status = ServerHelper.getStatus(server);
        SignState aFor = SignState.getFor(status);
        if (status == null || aFor == SignState.Restarting) ServerHelper.requestStatus(server);
        sign.setLine(1, getAbreviationFor(matrix.getGame()) + ServerSignMatrix.getServerNumber(server));
        if (aFor == SignState.Restarting) {
            sign.setLine(0, $("&4░░░░░░░░░░░"));
            sign.setLine(1, "");
            sign.setLine(2, $("&2»RESTARTING«"));
            sign.setLine(3, $("&4░░░░░░░░░░░"));
            sign.update(true);
            return;
        }
        sign.setLine(0, ChatColor.GREEN + (aFor == SignState.Lobby && server.getOnlineCount() < matrix.getGame().getMaxPlayers() ? ChatColor.BOLD.toString() : "") + "»Join«");
        sign.setLine(2, aFor.name().replaceAll("_", " "));
        sign.setLine(3, $("&2" + server.getOnlineCount() + "/" + matrix.getGame().getMaxPlayers()));
        sign.update(true);
        currentlyDisplaying = server;
    }

    private Sign getSign() {
        return (Sign) point.getLocation(matrix.getWorld()).getBlock().getState();
    }

    private static String $(String s) {
        return ChatColor.translateAlternateColorCodes('&', s);
    }

    @Override
    public void sentAttempt(NetworkServer server, CPlayer player) {
        player.sendMessage(TBNRHub.getInstance().getFormat("connecting", new String[]{"<server>", getAbreviationFor(matrix.getGame()) + ServerSignMatrix.getServerNumber(server)}));
    }

    @Override
    public void couldNotJoin(NetworkServer server, CPlayer player) {
        player.sendMessage(TBNRHub.getInstance().getFormat("could-not-make-room"));
    }

    @Override
    public void joining(NetworkServer server, CPlayer player) {
        player.sendMessage(TBNRHub.getInstance().getFormat("found-spot"));
    }

    private enum SignState {
        Lobby(true, "pre_game", ChatColor.GREEN),
        In_Game(true, "in_game", ChatColor.RED),
        Restarting(false, "game_over", ChatColor.DARK_PURPLE),
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

    private static String getAbreviationFor(Game game) {
        String name = game.name();
        StringBuilder builder = new StringBuilder();
        for (String s : name.split("_")) {
            builder.append(s.charAt(0));
        }
        return builder.toString();
    }
}
