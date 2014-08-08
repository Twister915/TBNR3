package net.tbnr.dev.spawn;

import net.cogzmc.core.modular.command.CommandException;
import net.cogzmc.core.modular.command.CommandMeta;
import net.cogzmc.core.modular.command.ModuleCommand;
import net.cogzmc.core.player.CPlayer;
import net.tbnr.dev.TBNRHub;
import net.tbnr.dev.parkour.ParkourSession;

@CommandMeta(aliases = {"s"})
public final class SpawnCommand extends ModuleCommand {
    public SpawnCommand() {
        super("spawn");
    }

    @Override
    protected void handleCommand(CPlayer player, String[] args) throws CommandException {
        TBNRHub.getInstance().getSpawnManager().teleportToSpawn(player);
    }
}
