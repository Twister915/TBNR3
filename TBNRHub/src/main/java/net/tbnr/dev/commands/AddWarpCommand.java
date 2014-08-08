package net.tbnr.dev.commands;

import com.google.api.client.repackaged.com.google.common.base.Joiner;
import net.cogzmc.core.modular.command.ArgumentRequirementException;
import net.cogzmc.core.modular.command.CommandException;
import net.cogzmc.core.modular.command.CommandPermission;
import net.cogzmc.core.modular.command.ModuleCommand;
import net.cogzmc.core.player.CPlayer;
import net.tbnr.dev.TBNRHub;
import net.tbnr.dev.inventory.player.Warp;
import org.bukkit.Material;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@CommandPermission("hub.warp.set")
public final class AddWarpCommand extends ModuleCommand {
    public AddWarpCommand() {
        super("addwarp");
    }

    @Override
    protected void handleCommand(CPlayer player, String[] args) throws CommandException {
        if (args.length < 2) {
            player.sendMessage(TBNRHub.getInstance().getFormat("warp-help"));
            return;
        }
        List<Material> materials = new ArrayList<>();
        for (String s : args[args.length - 1].split(",")) {
            try {
                Material m = Material.valueOf(s);
                materials.add(m);
            } catch (IllegalArgumentException ignored) {
            }
        }
        if (materials.size() == 0) throw new ArgumentRequirementException("You specified no valid materials!");
        String name = Joiner.on(' ').join(Arrays.copyOfRange(args, 0, args.length - 1));
        Warp warp = new Warp(player.getPoint(), name, materials.toArray(new Material[materials.size()]));
        TBNRHub.getInstance().getWarpRepository().saveWarp(warp);
        player.sendMessage(TBNRHub.getInstance().getFormat("warp-added"));
    }
}
