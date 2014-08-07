package net.tbnr.dev.sg.command;

import net.cogzmc.core.modular.command.CommandException;
import net.cogzmc.core.modular.command.ModuleCommand;
import net.tbnr.dev.sg.SurvivalGames;
import net.tbnr.dev.sg.game.map.SGMap;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

public final class MapListCommand extends ModuleCommand {
    public MapListCommand() {
        super("maplist");
    }

    @Override
    protected void handleCommandUnspecific(CommandSender sender, String[] args) throws CommandException {
        for (SGMap sgMap : SurvivalGames.getInstance().getMapManager().getMaps()) {
            sender.sendMessage(ChatColor.YELLOW + "> " + sgMap.getName() + " by " + sgMap.getAuthor() + "!");
        }
    }
}
