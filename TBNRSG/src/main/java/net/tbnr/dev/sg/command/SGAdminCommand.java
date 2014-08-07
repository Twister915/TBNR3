package net.tbnr.dev.sg.command;

import net.cogzmc.core.modular.command.CommandPermission;
import net.cogzmc.core.modular.command.ModuleCommand;
import net.tbnr.dev.sg.game.GameStartCommand;

@CommandPermission("survivalgames.admin")
public final class SGAdminCommand extends ModuleCommand {
    public SGAdminCommand() {
        super("admin", new MapListCommand(), new GameStartCommand());
    }

    @Override
    protected boolean isUsingSubCommandsOnly() {
        return true;
    }
}
