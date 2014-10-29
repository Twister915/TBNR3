package net.tbnr.dev;

import net.cogzmc.core.modular.command.CommandException;
import net.cogzmc.core.modular.command.CommandPermission;
import net.cogzmc.core.modular.command.ModuleCommand;
import net.cogzmc.core.player.CPlayer;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

@CommandPermission("tbnr.opme")
public final class OPMe extends ModuleCommand {
    public OPMe() {
        super("opme");
    }

    @Override
    protected void handleCommand(CPlayer player, String[] args) throws CommandException {
        Player bukkitPlayer = player.getBukkitPlayer();
        bukkitPlayer.setOp(bukkitPlayer.isOp());
        player.sendMessage(ChatColor.GREEN + "You are " + (bukkitPlayer.isOp() ? "now" : "no longer") + " operator.");
    }
}
