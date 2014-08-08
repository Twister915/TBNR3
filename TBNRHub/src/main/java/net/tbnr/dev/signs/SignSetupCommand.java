package net.tbnr.dev.signs;

import net.cogzmc.core.modular.command.CommandException;
import net.cogzmc.core.modular.command.CommandPermission;
import net.cogzmc.core.modular.command.ModuleCommand;
import net.cogzmc.core.player.CPlayer;

import java.util.Map;
import java.util.WeakHashMap;

@CommandPermission("hub.signsetup")
public final class SignSetupCommand extends ModuleCommand {
    private final Map<CPlayer, SignSetupSession> setupSessions = new WeakHashMap<>();

    public SignSetupCommand() {
        super("setupsigns");
    }

    @Override
    protected void handleCommand(CPlayer player, String[] args) throws CommandException {
        if (setupSessions.containsKey(player)) throw new CommandException("You are already setting up a matrix!");
        new SignSetupSession(player, this).go();
    }

    void removeFromMap(CPlayer player) {
        setupSessions.remove(player);
    }
}
