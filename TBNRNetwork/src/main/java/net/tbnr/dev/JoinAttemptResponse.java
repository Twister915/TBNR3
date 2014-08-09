package net.tbnr.dev;

import net.cogzmc.core.network.NetCommand;
import net.cogzmc.core.network.NetCommandField;

@NetCommandField
public class JoinAttemptResponse implements NetCommand {
    public String playerUUID;
    public boolean allowed;
}
