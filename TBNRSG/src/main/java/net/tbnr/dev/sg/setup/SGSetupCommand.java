package net.tbnr.dev.sg.setup;

import net.cogzmc.core.modular.command.CommandMeta;
import net.cogzmc.core.modular.command.CommandPermission;
import net.cogzmc.core.modular.command.ModuleCommand;

@CommandPermission("survivalgames.admin")
@CommandMeta(aliases = {"setup", "mapcreate"})
public final class SGSetupCommand extends ModuleCommand {
    public SGSetupCommand() {
        super("setupmap", new StartSubCommand(), new CancelSubCommand(), new LobbySubCommand());
    }

    @Override
    protected boolean isUsingSubCommandsOnly() {
        return true;
    }

    @Override
    protected boolean shouldGenerateHelpCommand() {
        return false;
    }
}
