package net.tbnr.dev.sg.game;

import com.google.api.client.repackaged.com.google.common.base.Joiner;
import net.cogzmc.core.modular.command.CommandException;
import net.cogzmc.core.modular.command.ModuleCommand;
import net.tbnr.dev.sg.SurvivalGames;
import net.tbnr.dev.sg.game.map.SGMap;
import org.bukkit.command.CommandSender;

public final class GameStartCommand extends ModuleCommand {
    public GameStartCommand() {
        super("forcestart");
    }

    @Override
    protected void handleCommandUnspecific(CommandSender sender, String[] args) throws CommandException {
        if (SurvivalGames.getInstance().getGameManager().getRunningGame() != null) throw new CommandException("You cannot start a game when there is one running!");
        SGMap map = null;
        if (args.length > 0) {
            String join = Joiner.on(' ').join(args);
            for (SGMap sgMap : SurvivalGames.getInstance().getMapManager().getMaps()) {
                if (sgMap.getName().equalsIgnoreCase(join)) {
                    map = sgMap;
                    break;
                }
            }
        }
        sender.sendMessage(SurvivalGames.getInstance().getFormat("game-force-started"));
        if (map != null) SurvivalGames.getInstance().getGameManager().beginGame(map);
        else SurvivalGames.getInstance().getGameManager().beginGame();
    }
}
