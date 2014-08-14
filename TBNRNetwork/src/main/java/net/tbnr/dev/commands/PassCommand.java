package net.tbnr.dev.commands;

import net.cogzmc.core.Core;
import net.cogzmc.core.modular.command.ArgumentRequirementException;
import net.cogzmc.core.modular.command.CommandException;
import net.cogzmc.core.modular.command.ModuleCommand;
import net.cogzmc.core.player.COfflinePlayer;
import net.cogzmc.core.player.DatabaseConnectException;
import net.tbnr.dev.PassManager;
import org.bukkit.command.CommandSender;

import java.util.List;

public final class PassCommand extends ModuleCommand {
    public PassCommand() {
        super("passctrl");
    }

    // /passctrl Twister915 net.cogjdas add 2
    @Override
    protected void handleCommandUnspecific(CommandSender sender, String[] args) throws CommandException {
        if (args.length != 4) throw new ArgumentRequirementException("You have specified too few arguments!");
        List<COfflinePlayer> offlinePlayers = Core.getPlayerManager().getOfflinePlayerByName(args[0]);
        if (offlinePlayers.size() != 1) throw new ArgumentRequirementException("You have specified an invalid target.");
        COfflinePlayer cOfflinePlayer = offlinePlayers.get(0);
        Integer delta;
        try {
            delta = Integer.valueOf(args[3]);
        } catch (NumberFormatException e) {
            throw new ArgumentRequirementException("The number you specified is invalid!");
        }
        switch (args[2]) {
            case "add":
                break;
            case "subtract":
            case "remove":
                delta = delta * -1;
                break;
            default:
                throw new ArgumentRequirementException("You have specified an invalid action!");
        }
        try {
            PassManager.addPassesForClass(delta, args[1], cOfflinePlayer);
        } catch (DatabaseConnectException e) {
            e.printStackTrace();
            throw new CommandException("Unable to save the player's records");
        }
    }
}
