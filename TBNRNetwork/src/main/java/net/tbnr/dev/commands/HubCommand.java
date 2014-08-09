package net.tbnr.dev.commands;

import net.cogzmc.core.Core;
import net.cogzmc.core.modular.command.CommandException;
import net.cogzmc.core.modular.command.CommandMeta;
import net.cogzmc.core.modular.command.CommandPermission;
import net.cogzmc.core.modular.command.ModuleCommand;
import net.cogzmc.core.player.CPlayer;
import net.tbnr.dev.ServerHelper;
import net.tbnr.dev.TBNRNetwork;

@CommandMeta(aliases = {"lobby", "leave"})
public final class HubCommand extends ModuleCommand {
    public HubCommand() {
        super("hub");
    }

    @Override
    protected void handleCommand(CPlayer player, String[] args) throws CommandException {
        if (ServerHelper.isLobbyServer(Core.getNetworkManager().getThisServer(), false)) {
            player.sendMessage(TBNRNetwork.getInstance().getFormat("already-in-lobby", false));
            return;
        }
        ServerHelper.getLobbyServer(false).sendPlayerToServer(player);
    }
}
