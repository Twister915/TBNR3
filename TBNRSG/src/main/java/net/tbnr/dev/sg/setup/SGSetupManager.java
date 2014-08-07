package net.tbnr.dev.sg.setup;

import net.cogzmc.core.modular.command.ArgumentRequirementException;
import net.cogzmc.core.modular.command.CommandException;
import net.cogzmc.core.player.CPlayer;
import net.tbnr.dev.sg.SurvivalGames;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.WorldCreator;

import java.io.File;
import java.util.Map;
import java.util.WeakHashMap;

public final class SGSetupManager {
    private Map<CPlayer, SGSetupSession> setupSessions = new WeakHashMap<>();

    public SGSetupManager() {
        SurvivalGames.getInstance().registerCommand(new SGSetupCommand());
    }

    public void startSetup(CPlayer player, String world) throws CommandException {
        if (setupSessions.containsKey(player)) throw new CommandException("You are already setting up an arena!");
        if (!new File(Bukkit.getWorldContainer(), world).isDirectory()) throw new ArgumentRequirementException("The world does not currently exist, please specify the folder name; you will be able to name the map differently during setup.");
        World world2 = Bukkit.getWorld(world);
        World world1 = (world2 != null ? world2 : WorldCreator.name(world).createWorld());
        player.getBukkitPlayer().teleport(world1.getSpawnLocation());
        SGSetupSession sgSetupSession = new SGSetupSession(player, world1);
        setupSessions.put(player, sgSetupSession);
        sgSetupSession.start();
    }

    public void cancelSetup(CPlayer player) throws CommandException {
        if (!setupSessions.containsKey(player)) throw new CommandException("You are not currently setting up an arena!");
        setupSessions.get(player).cancel();
        setupSessions.remove(player);
    }

    public void setupComplete(SGSetupSession session) {
        setupSessions.remove(session.getPlayer());
    }
}
