package net.tbnr.dev.parkour;

import net.cogzmc.core.modular.command.CommandException;
import net.cogzmc.core.modular.command.CommandPermission;
import net.cogzmc.core.modular.command.ModuleCommand;
import net.cogzmc.core.player.CPlayer;

@CommandPermission("hub.parkour.setup")
public final class ParkourSetupCommand extends ModuleCommand {
    public ParkourSetupCommand() {
        super("setup");
    }

    @Override
    protected void handleCommand(CPlayer player, String[] args) throws CommandException {
        new ParkourSetupSession(player).start();
    }
}
