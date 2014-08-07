package net.tbnr.dev.sg.command;

import net.cogzmc.core.modular.command.CommandPermission;
import net.cogzmc.core.modular.command.ModuleCommand;

@CommandPermission("survivalgames.admin")
public final class SGAdminCommand extends ModuleCommand {
    public SGAdminCommand() {
        super("admin", new MapListCommand());
    }

    @Override
    protected boolean isUsingSubCommandsOnly() {
        return true;
    }
}
