package net.tbnr.dev.bungee;

import net.cogzmc.bungee.Controller;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public final class TBNRController implements Controller {
    private final static Random random = new Random();

    @Override
    public ServerInfo getConnectServer(ProxiedPlayer player) {
        List<ServerInfo> lobbyServers = getLobbyServers(false);
        return lobbyServers.get(random.nextInt(lobbyServers.size()));
    }

    @Override
    public ServerInfo getFallbackServer(ProxiedPlayer player) {
        return getConnectServer(player);
    }

    private static List<ServerInfo> getLobbyServers(boolean vip) {
        List<ServerInfo> infoSet = new ArrayList<>();
        for (ServerInfo serverInfo : ProxyServer.getInstance().getServers().values()) {
            if (serverInfo.getName().matches("^lobby" + (vip ? "vip" : "") + "[0-9]{1,4}")) infoSet.add(serverInfo);
        }
        return infoSet;
    }
}
