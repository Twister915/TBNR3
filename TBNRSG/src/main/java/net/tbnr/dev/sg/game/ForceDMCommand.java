package net.tbnr.dev.sg.game;

import net.cogzmc.core.modular.command.CommandException;
import net.cogzmc.core.modular.command.ModuleCommand;
import net.tbnr.dev.sg.SurvivalGames;
import org.bukkit.command.CommandSender;

public final class ForceDMCommand extends ModuleCommand {
    public ForceDMCommand() {
        super("forcedeathmatch");
    }

    @Override
    protected void handleCommandUnspecific(CommandSender sender, String[] args) throws CommandException {
        SGGame runningGame = SurvivalGames.getInstance().getGameManager().getRunningGame();
        if (runningGame == null) throw new CommandException("The game has not started!");
        runningGame.state = SGGameState.PRE_DEATHMATCH_1;
        runningGame.updateState();
    }
}
