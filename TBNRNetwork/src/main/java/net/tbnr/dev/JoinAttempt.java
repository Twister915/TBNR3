package net.tbnr.dev;

import net.cogzmc.core.network.NetCommand;
import net.cogzmc.core.network.NetCommandField;

@NetCommandField
public class JoinAttempt implements NetCommand {
    public String playerUUID;
    public String game;
}
