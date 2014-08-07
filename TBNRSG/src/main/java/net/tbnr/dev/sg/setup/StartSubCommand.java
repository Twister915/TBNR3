package net.tbnr.dev.sg.setup;

import net.cogzmc.core.modular.command.ArgumentRequirementException;
import net.cogzmc.core.modular.command.CommandException;
import net.cogzmc.core.modular.command.ModuleCommand;
import net.cogzmc.core.player.CPlayer;
import net.tbnr.dev.sg.SurvivalGames;

public final class StartSubCommand extends ModuleCommand {
    public StartSubCommand() {
        super("start");
    }

    @Override
    protected void handleCommand(CPlayer player, String[] args) throws CommandException {
        if (args.length < 1) throw new ArgumentRequirementException("You have specified too few arguments!");
        SurvivalGames.getInstance().getSetupManager().startSetup(player, args[0]);
    }
}
