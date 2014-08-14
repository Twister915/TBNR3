package net.tbnr.dev;

import lombok.Data;
import net.cogzmc.core.Core;
import net.cogzmc.core.network.NetCommandHandler;
import net.cogzmc.core.network.NetworkServer;
import net.cogzmc.core.player.COfflinePlayer;
import net.cogzmc.core.player.CPlayer;

import java.lang.ref.WeakReference;
import java.util.*;

public class JoinAttemptHandler implements NetCommandHandler<JoinAttemptResponse> {
    private static Set<JoinAttemptEntry> serverRequests = new LinkedHashSet<>();

    @Override
    public void handleNetCommand(NetworkServer sender, JoinAttemptResponse netCommand) {
        COfflinePlayer offlinePlayerByUUID = Core.getOfflinePlayerByUUID(UUID.fromString(netCommand.playerUUID));
        if ((offlinePlayerByUUID == null) || !(offlinePlayerByUUID instanceof CPlayer)) return;
        JoinAttemptEntry entry = null;
        for (JoinAttemptEntry serverRequest : serverRequests) {
            CPlayer cPlayer = serverRequest.player.get();
            if (cPlayer != null && cPlayer.equals(offlinePlayerByUUID) && serverRequest.server.getName().equals(sender.getName())) {
                entry = serverRequest;
                break;
            }
        }
        if (entry == null) return;
        JoinAttemptDelegate delegate = entry.delegate;
        if (netCommand.allowed) {
            delegate.joining(sender, (CPlayer) offlinePlayerByUUID);
            sender.sendPlayerToServer((CPlayer) offlinePlayerByUUID);
        } else {
            delegate.couldNotJoin(sender, (CPlayer) offlinePlayerByUUID);
        }
        serverRequests.remove(entry);
    }

    public static void attemptJoin(CPlayer player, NetworkServer currentlyDisplaying, JoinAttemptDelegate delegate) {
        JoinAttempt joinAttempt = new JoinAttempt();
        joinAttempt.playerUUID = player.getUniqueIdentifier().toString();
        currentlyDisplaying.sendNetCommand(joinAttempt);
        delegate.sentAttempt(currentlyDisplaying, player);
        serverRequests.add(new JoinAttemptEntry(new WeakReference<>(player), currentlyDisplaying, delegate));
    }

    public static interface JoinAttemptDelegate {
        void sentAttempt(NetworkServer server, CPlayer player);
        void couldNotJoin(NetworkServer server, CPlayer player);
        void joining(NetworkServer server, CPlayer player);
    }

    @Data
    static class JoinAttemptEntry {
        final WeakReference<CPlayer> player;
        final NetworkServer server;
        final JoinAttemptDelegate delegate;
    }
}
