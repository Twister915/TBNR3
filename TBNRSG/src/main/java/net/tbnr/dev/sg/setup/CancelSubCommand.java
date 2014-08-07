package net.tbnr.dev.sg.setup;

import net.cogzmc.core.modular.command.CommandException;
import net.cogzmc.core.modular.command.ModuleCommand;
import net.cogzmc.core.player.CPlayer;
import net.tbnr.dev.sg.SurvivalGames;

public final class CancelSubCommand extends ModuleCommand {
    public CancelSubCommand() {
        super("cancel");
    }

    @Override
    protected void handleCommand(CPlayer player, String[] args) throws CommandException {
        SurvivalGames.getInstance().getSetupManager().cancelSetup(player);
    }
}
