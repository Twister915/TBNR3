package net.tbnr.dev.parkour;

import lombok.Data;
import net.cogzmc.core.player.CPlayer;
import net.cogzmc.core.util.Point;
import net.cogzmc.core.util.Region;
import net.cogzmc.core.util.TimeUtils;
import net.tbnr.dev.TBNRHub;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.joda.time.Duration;

import java.util.ArrayList;
import java.util.List;

@Data
public final class ParkourSetupSession implements Listener {
    private final CPlayer player;

    /* stuff relating to actually setting it up */
    private final List<ParkourLevel> levels = new ArrayList<>();
    private Region startRegion;
    private Point spawnPoint;
    private Point villagerPoint;

    private Region levelStart;
    private Duration levelDuration;
    private Point checkpoint;
    private boolean checkingForDuration = false;
    private boolean checkingForTermination = false;

    private Region endRegion;

    private Point p1;
    private Point p2;

    public void start() {
        player.sendMessage(TBNRHub.getInstance().getFormat("parkour-start-region"));
        TBNRHub.getInstance().registerListener(this);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (!event.getPlayer().equals(player.getBukkitPlayer())) return;
        event.setCancelled(true);
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK && event.getAction() != Action.LEFT_CLICK_BLOCK) return;
        if (startRegion == null) {
            //we're setting up the start region
            player.sendMessage(TBNRHub.getInstance().getFormat("parkour-region-point-selected"));
            if (p1 == null) p1 = Point.of(event.getClickedBlock());
            else if (p2 == null) {
                p2 = Point.of(event.getClickedBlock());
                p1.setY(0d);
                p2.setY(256d);
                startRegion = new Region(p1, p2);
                p1 = null;
                p2 = null;
                player.sendMessage(TBNRHub.getInstance().getFormat("parkour-region-start-done"));
                player.sendMessage(TBNRHub.getInstance().getFormat("parkour-select-spawn"));
            }
        } else if (spawnPoint == null) {
            spawnPoint = Point.of(player.getBukkitPlayer().getLocation());
            player.sendMessage(TBNRHub.getInstance().getFormat("parkour-spawn-selected"));
            player.sendMessage(TBNRHub.getInstance().getFormat("parkour-select-villager"));
        } else if (villagerPoint == null) {
            villagerPoint = Point.of(player.getBukkitPlayer().getLocation());
            player.sendMessage(TBNRHub.getInstance().getFormat("parkour-villager-selected"));
            startNextLevel();
            player.sendMessage(TBNRHub.getInstance().getFormat("parkour-level-start-region"));
        } else if (levelStart == null) {
            player.sendMessage(TBNRHub.getInstance().getFormat("parkour-region-point-selected"));
            if (p1 == null) p1 = Point.of(event.getClickedBlock());
            else if (p2 == null) {
                p2 = Point.of(event.getClickedBlock());
                p1.setY(0d);
                p2.setY(256d);
                player.sendMessage(TBNRHub.getInstance().getFormat("parkour-level-start-region-selected"));
                levelStart = new Region(p1, p2);
                p1 = null;
                p2 = null;
                player.sendMessage(TBNRHub.getInstance().getFormat("parkour-level-select-checkpoint"));
            }
        } else if (checkpoint == null) {
            checkpoint = Point.of(player.getBukkitPlayer().getLocation());
            player.sendMessage(TBNRHub.getInstance().getFormat("parkour-level-checkpoint-selected"));
            checkingForDuration = true;
            player.sendMessage(TBNRHub.getInstance().getFormat("parkour-duration-enter"));
        } else if (levelDuration == null) {
            player.sendMessage(TBNRHub.getInstance().getFormat("parkour-duration-enter"));
        } else if (endRegion == null) {
            player.sendMessage(TBNRHub.getInstance().getFormat("parkour-region-point-selected"));
            if (p1 == null) p1 = Point.of(event.getClickedBlock());
            else if (p2 == null) {
                p2 = Point.of(event.getClickedBlock());
                player.sendMessage(TBNRHub.getInstance().getFormat("parkour-level-start-region-selected"));
                p1.setY(0d);
                p2.setY(256d);
                endRegion = new Region(p1, p2);
                p1 = null;
                p2 = null;
                player.sendMessage(TBNRHub.getInstance().getFormat("parkour-saved"));
                Parkour parkourLevels = new Parkour(startRegion, endRegion, spawnPoint, villagerPoint, player.getBukkitPlayer().getWorld());
                parkourLevels.getLevels().addAll(levels);
                TBNRHub.getInstance().getParkourManager().addParkour(parkourLevels);
                HandlerList.unregisterAll(this);
            }
        }
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        if (!event.getPlayer().equals(player.getBukkitPlayer())) return;
        if (checkingForDuration) {
            levelDuration = new Duration((long)(TimeUtils.parseTime(event.getMessage().trim().split(" ")[0]) * 1000));
            player.sendMessage(TBNRHub.getInstance().getFormat("parkour-duration-done"));
            player.sendMessage(TBNRHub.getInstance().getFormat("parkour-teminate-prompt"));
            checkingForDuration = false;
            checkingForTermination = true;
        }
        else if (checkingForTermination) {
            if (event.getMessage().equalsIgnoreCase("yes")) startNextLevel();
            else if (event.getMessage().equalsIgnoreCase("no")) {
                storeLevel();
                player.sendMessage(TBNRHub.getInstance().getFormat("parkour-end-region-prompt"));
            }
            else player.sendMessage(TBNRHub.getInstance().getFormat("parkour-teminate-prompt"));
        }
        else return;
        event.setCancelled(true);
    }

    private void startNextLevel() {
        player.sendMessage(TBNRHub.getInstance().getFormat("parkour-level-start"));
        if (levelStart != null) {
            storeLevel();
            levelStart = null;
            levelDuration = null;
            checkpoint = null;
        }
    }

    private void storeLevel() {
        levels.add(new ParkourLevel(levelStart, levelDuration, checkpoint));
    }
}
