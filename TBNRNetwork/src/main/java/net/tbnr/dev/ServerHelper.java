package net.tbnr.dev;

import net.cogzmc.core.Core;
import net.cogzmc.core.network.NetCommandHandler;
import net.cogzmc.core.network.NetworkServer;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.regex.Pattern;

public final class ServerHelper {
    private static Map<String, String> statusMap = new HashMap<>();
    private static String current_status = null;

    private static String getRegexForLobby(boolean vip) {
        return "^" + (vip ? "vip" : "") + "lobby[0-9]{1,4}$";
    }

    public static NetworkServer getLobbyServer(boolean vip) {
        List<NetworkServer> serversMatchingRegex = Core.getNetworkManager().getServersMatchingRegex(getRegexForLobby(vip));
        if (serversMatchingRegex.size() == 0) return null;
        return serversMatchingRegex.get(Core.getRandom().nextInt(serversMatchingRegex.size()));
    }

    public static NetworkServer getServer(Game game) {
        List<NetworkServer> serversMatchingRegex = getServers(game);
        if (serversMatchingRegex.size() == 0) return null;
        return serversMatchingRegex.get(Core.getRandom().nextInt(serversMatchingRegex.size()));
    }

    public static List<NetworkServer> getServers(Game game) {
        String gameName = game.name().toLowerCase().replaceAll("_", "");
        return Core.getNetworkManager().getServersMatchingRegex("^" + gameName + "[0-9]{1,4}$");
    }

    private static void setStatus(NetworkServer server, String status) {
        statusMap.put(server.getName(), status);
    }

    public static String getStatus(NetworkServer server) {
        if (!statusMap.containsKey(server.getName())) return null;
        return statusMap.get(server.getName());
    }

    public static void requestStatus(NetworkServer server) {
        server.sendNetCommand(new RequestStatusNetCommand());
    }

    public static void setStatus(String status) {
        current_status = status;
        ServerStatusNetCommand serverStatusNetCommand = new ServerStatusNetCommand(status);
        Core.getNetworkManager().sendMassNetCommand(serverStatusNetCommand);
    }

    public static boolean isLobbyServer(NetworkServer server, boolean vip) {
        return server.getName().matches(getRegexForLobby(vip));
    }

    static class NetCommandHandlr implements NetCommandHandler<ServerStatusNetCommand> {
        @Override
        public void handleNetCommand(NetworkServer sender, ServerStatusNetCommand netCommand) {
            setStatus(sender, netCommand.getStatus());
        }
    }

    static class ReqCommandHandlr implements NetCommandHandler<RequestStatusNetCommand> {
        @Override
        public void handleNetCommand(NetworkServer sender, RequestStatusNetCommand netCommand) {
            if (current_status != null) sender.sendNetCommand(new ServerStatusNetCommand(current_status));
        }
    }

    static class OfflineCommandHandlr implements NetCommandHandler<ServerOfflineNetCommand> {
        @Override
        public void handleNetCommand(NetworkServer sender, ServerOfflineNetCommand netCommand) {
            statusMap.remove(sender.getName());
        }
    }
}
