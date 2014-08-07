package net.tbnr.dev.sg.setup;

import lombok.Data;
import net.cogzmc.core.maps.CMap;
import net.cogzmc.core.maps.CoreMaps;
import net.cogzmc.core.player.CPlayer;
import net.cogzmc.core.util.Point;
import net.tbnr.dev.sg.SurvivalGames;
import net.tbnr.dev.sg.game.PreGameLobby;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.HashSet;
import java.util.Set;

@Data
public final class SGLobbySetupSession implements SetupSession, Listener {
    private final CPlayer player;
    private final World world;

    private final Set<Point> spawns = new HashSet<>();
    private final Set<Point> villagerPoints = new HashSet<>();

    private boolean doingSpawns = true;
    private boolean saving = false;

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (!event.getPlayer().equals(player.getBukkitPlayer())) return;
        if (event.getAction() == Action.PHYSICAL) return;
        if (event.getPlayer().getItemInHand() == null || event.getPlayer().getItemInHand().getType() != Material.DIAMOND_HOE) return;
        if (doingSpawns) {
            spawns.add(Point.of(event.getPlayer().getLocation()));
            player.sendMessage(SurvivalGames.getInstance().getFormat("setup.pre-game.point-selected"));
        } else {
            villagerPoints.add(Point.of(event.getPlayer().getLocation()));
            player.sendMessage(SurvivalGames.getInstance().getFormat("setup.pre-game.point-selected"));
        }
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent event) {
        if (!event.getPlayer().equals(player.getBukkitPlayer())) return;
        if (doingSpawns && event.getMessage().equalsIgnoreCase("done")) {
            doingSpawns = false;
            player.sendMessage(SurvivalGames.getInstance().getFormat("setup.pre-game.start-villager"));
        } else if (event.getMessage().equalsIgnoreCase("done") && villagerPoints.size() > 0 && !saving) {
            save();
            saving = true;
        }
    }

    private void save() {
        CMap cMap = CoreMaps.getInstance().getMapManager().importWorld(world);
        PreGameLobby preGameLobby = new PreGameLobby(cMap, spawns, villagerPoints);
        SurvivalGames.getInstance().getMapManager().savePreGameLobby(preGameLobby);
        player.sendMessage(SurvivalGames.getInstance().getFormat("setup.pre-game.setup"));
        HandlerList.unregisterAll(this);
        SurvivalGames.getInstance().getSetupManager().setupComplete(this);
    }

    @Override
    public void cancel() {
        HandlerList.unregisterAll(this);
        player.sendMessage(SurvivalGames.getInstance().getFormat("setup.setup-cancelled"));
    }

    @Override
    public void start() {
        player.sendMessage(SurvivalGames.getInstance().getFormat("setup.pre-game.start-spawn"));
        SurvivalGames.getInstance().registerListener(this);
    }
}
