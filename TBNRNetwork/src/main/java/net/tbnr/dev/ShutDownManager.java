package net.tbnr.dev;

import net.cogzmc.core.network.NetCommandHandler;
import net.cogzmc.core.network.NetworkServer;
import org.bukkit.Bukkit;

public class ShutDownManager implements NetCommandHandler<ShutDownNetCommand> {
    @Override
    public void handleNetCommand(NetworkServer sender, ShutDownNetCommand netCommand) {
        Bukkit.shutdown();
    }
}
