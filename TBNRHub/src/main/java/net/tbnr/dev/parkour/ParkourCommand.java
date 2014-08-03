package net.tbnr.dev.parkour;

import net.cogzmc.core.modular.command.CommandMeta;
import net.cogzmc.core.modular.command.ModuleCommand;

@CommandMeta(description = "Manages and interacts with the parkour", aliases = {"pa", "park"}, usage = "/parkour")
public final class ParkourCommand extends ModuleCommand {
    public ParkourCommand() {
        super("parkour");
    }
}
