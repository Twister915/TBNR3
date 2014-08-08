package net.tbnr.dev;

import lombok.Getter;
import net.cogzmc.core.Core;
import net.cogzmc.core.modular.ModularPlugin;
import net.cogzmc.core.modular.ModuleMeta;

@ModuleMeta(description = "Manages the TBNR network.", name = "TBNRNetwork")
public final class TBNRNetwork extends ModularPlugin {
    @Getter private static TBNRNetwork instance;

    @Override
    protected void onModuleEnable() throws Exception {
        instance = this;
        if (Core.getNetworkManager() != null) {
            Core.getNetworkManager().registerNetCommandHandler(new ServerHelper.NetCommandHandlr(), ServerStatusNetCommand.class);
            Core.getNetworkManager().registerNetCommandHandler(new ServerHelper.ReqCommandHandlr(), RequestStatusNetCommand.class);
            registerCommand(new HubCommand());
        }
    }
}
