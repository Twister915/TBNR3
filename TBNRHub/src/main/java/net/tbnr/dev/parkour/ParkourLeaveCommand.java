package net.tbnr.dev.parkour;

import net.cogzmc.core.modular.command.CommandException;
import net.cogzmc.core.modular.command.ModuleCommand;
import net.cogzmc.core.player.CPlayer;
import net.tbnr.dev.TBNRHub;

public final class ParkourLeaveCommand extends ModuleCommand {
    public ParkourLeaveCommand() {
        super("leave");
    }

    @Override
    protected void handleCommand(CPlayer player, String[] args) throws CommandException {
        ParkourSession parkourFor = TBNRHub.getInstance().getParkourManager().getParkourFor(player);
        if (parkourFor == null) {
            player.sendMessage(TBNRHub.getInstance().getFormat("no-parkour"));
            return;
        }
        parkourFor.cleanupParkour();
    }
}
