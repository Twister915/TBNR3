package net.tbnr.dev;

import lombok.Data;
import net.cogzmc.core.Core;
import net.cogzmc.core.network.NetworkServer;
import net.cogzmc.core.player.CPlayer;
import org.bukkit.Bukkit;

public final class AutoRestart implements Runnable {
    @Override
    public void run() {
        for (CPlayer cPlayer : Core.getOnlinePlayers()) {
            cPlayer.sendMessage(TBNRHub.getInstance().getFormat("restart-transfer"));
            NetworkServer lobbyServer;
            int tries = 0;
            do {
                lobbyServer = ServerHelper.getLobbyServer(false);
                tries++;
            } while (lobbyServer.equals(Core.getNetworkManager().getThisServer()) && tries < 10);
        }
        Bukkit.getScheduler().runTaskLater(TBNRHub.getInstance(), new Runnable() {
            @Override
            public void run() {
                Bukkit.shutdown();
            }
        }, 200);
    }

    @Data
    public final static class AutoRestartWarning implements Runnable {
        private final Integer minutes;

        @Override
        public void run() {
            Bukkit.broadcastMessage(TBNRHub.getInstance().getFormat("restart-warning", new String[]{"<time>", String.valueOf(minutes)}));
        }
    }
}
