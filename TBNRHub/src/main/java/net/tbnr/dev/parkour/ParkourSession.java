package net.tbnr.dev.parkour;

import lombok.EqualsAndHashCode;
import lombok.ToString;
import net.cogzmc.core.player.CPlayer;
import net.cogzmc.core.util.Point;
import net.tbnr.dev.TBNRHub;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.joda.time.Duration;
import org.joda.time.Instant;

import java.util.*;

@EqualsAndHashCode
@ToString
public final class ParkourSession implements Listener {
    private final CPlayer player;
    private final World world;
    private final Iterator<ParkourLevel> levelIterator;
    private final Parkour parkour;
    private final ParkourManager manager;

    private ParkourLevel level;
    private ParkourLevel nextLevel;
    private Integer levelNumber = 1;
    private Point checkpoint = null;
    private List<Point> hitBlocks;
    private boolean inAirLast = false;
    private Point lastPoint; //Last location tracked at
    private Point lastBlock; //Last block tracked at

    private Instant levelStart;
    private Map<ParkourLevel, Duration> levelTimes = new HashMap<>();

    public ParkourSession(Parkour parkour, ParkourManager manager, CPlayer player) {
        this.parkour = parkour;
        this.manager = manager;
        this.player = player;
        this.world = parkour.getWorld();
        this.levelIterator = parkour.iterator();
        level = levelIterator.next();
        nextLevel = levelIterator.next();
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player bukkitPlayer = player.getBukkitPlayer();
        if (!event.getPlayer().equals(bukkitPlayer)) return;
        Location to = event.getTo();
        Point current = Point.of(to);
        Block block = to.getBlock();
        Point currentBlock = Point.of(block);
        boolean inAirCurrent = block.getRelative(0, -1, 0).getType() == Material.AIR;
        //If you are leaving the start region, we're on the first level, and you're not going up in the air
        if (lastPoint != null
                && levelNumber == 1
                && parkour.getStartRegion().isWithin(lastPoint)
                && !parkour.getStartRegion().isWithin(current)
                && inAirLast == inAirCurrent) {
            cleanupParkour();
        }
        else if (inAirLast != inAirCurrent && lastPoint != null) {
            //They've landed or taken off
            //We're taking off
            if (inAirCurrent) {
                //If we're taking off from a start region, and thus starting a stage, let's add
                if (level.getStartRegion().isWithin(lastPoint) && !level.getStartRegion().isWithin(current)) {
                    checkpoint = lastPoint.deepCopy();
                    player.sendMessage(TBNRHub.getInstance().getFormat("parkour-level-start", new String[]{"<level>", String.valueOf(levelNumber)}));
                    hitBlocks = new ArrayList<>();
                    levelStart = new Instant();
                }
            } else {
                //They've hit a block
                boolean parkourCompleted = parkour.getEndRegion().isWithin(current) && nextLevel == null;
                boolean levelCompleted = nextLevel != null && nextLevel.getStartRegion().isWithin(current);
                if (parkourCompleted || levelCompleted) {
                    Duration levelTime = new Duration(levelStart, new Instant());
                    levelTimes.put(level, levelTime);
                    player.sendMessage(TBNRHub.getInstance().getFormat("parkour-level-complete", new String[]{"<level>", String.valueOf(levelNumber)}));
                    player.playSoundForPlayer(Sound.LEVEL_UP, 1f, 1.2f);
                    for (Point hitBlock : hitBlocks) {
                        Block block1 = hitBlock.getLocation(world).getBlock();
                        bukkitPlayer.sendBlockChange(block1.getLocation(), block1.getType(), block1.getData());
                    }
                }
                if (parkourCompleted) {
                    endParkour();
                }
                else if (levelCompleted) {
                    //Change levels
                    level = nextLevel;
                    if (levelIterator.hasNext()) nextLevel = levelIterator.next();
                    else nextLevel = null;
                    levelNumber++;

                }
                else if (block.getType() != Material.HARD_CLAY && block.getType() != Material.LADDER) {
                    resetParkour();
                }
                //If they have not re-hit the same block
                else if (!(lastBlock != null && lastBlock.equals(currentBlock))) {
                    //We know for sure that they're on clay or ladder and not on the last block.
                    //We can now deduce that the player has reached a new block that is a valid parkour block.
                    lastBlock = currentBlock.deepCopy();
                    player.playSoundForPlayer(Sound.ORB_PICKUP, 1F, 1.8F);
                    if (block.getType() == Material.HARD_CLAY) bukkitPlayer.sendBlockChange(block.getLocation(), Material.HARD_CLAY, (byte) 8);
                }
            }
        }
        lastPoint = Point.of(to);
        inAirLast = inAirCurrent;
    }

    private void resetParkour() {
        hitBlocks.clear();
        lastBlock = null;
        player.getBukkitPlayer().teleport(checkpoint.getLocation(world));
        inAirLast = false;
        lastPoint = checkpoint.deepCopy();
    }

    private void endParkour() {
        //TODO send feedback for parkour
    }

    void cleanupParkour() {

    }
}
