package net.tbnr.dev.commands;

import net.cogzmc.core.Core;
import net.cogzmc.core.modular.command.*;
import net.cogzmc.core.player.CPlayer;
import net.tbnr.dev.TBNRNetwork;
import org.bukkit.Sound;
import org.bukkit.command.CommandSender;

/**
 * <p/>
 * Latest Change: 04/08/2014.
 * <p/>
 *
 * @author Noy
 * @since 04/08/2014.
 */
@CommandMeta(aliases = {"cc", "cchat"}, description = "The ClearChat Command", usage = "/cc")
@CommandPermission("hub.clear-chat")
public final class ClearChatCommand extends ModuleCommand {

    public ClearChatCommand() {
        super("clearchat");
    }

    @Override
    protected void handleCommandUnspecific(CommandSender sender, String[] args) throws CommandException {
        if (args.length > 0) throw new ArgumentRequirementException("Too many arguments!");
        for (CPlayer player : Core.getPlayerManager()) {
            if (player.hasPermission("hub.clear-chat")) continue;
            player.clearChatAll();
            player.playSoundForPlayer(Sound.FIZZ, 0.5F);
        }
        sender.sendMessage(TBNRNetwork.getInstance().getFormat("clear-chat"));
    }
}
