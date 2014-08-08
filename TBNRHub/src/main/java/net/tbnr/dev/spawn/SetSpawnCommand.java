package net.tbnr.dev.spawn;

import net.cogzmc.core.modular.command.CommandException;
import net.cogzmc.core.modular.command.CommandPermission;
import net.cogzmc.core.modular.command.ModuleCommand;
import net.cogzmc.core.player.CPlayer;
import net.tbnr.dev.TBNRHub;

@CommandPermission("hub.setspawn")
public final class SetSpawnCommand extends ModuleCommand {
    public SetSpawnCommand() {
        super("setspawn");
    }

    @Override
    protected void handleCommand(CPlayer player, String[] args) throws CommandException {
        TBNRHub.getInstance().getSpawnManager().setSpawn(player.getBukkitPlayer().getLocation());
        player.sendMessage(TBNRHub.getInstance().getFormat("spawn-set"));
    }
}
