package net.tbnr.dev;

import lombok.Data;
import lombok.NonNull;
import net.cogzmc.core.network.NetCommand;
import net.cogzmc.core.network.NetCommandField;

@NetCommandField
@Data
public final class ServerStatusNetCommand implements NetCommand {
    public String status;

    public ServerStatusNetCommand() {

    }

    public ServerStatusNetCommand(String status) {
        this.status = status;
    }
}
