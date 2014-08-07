package net.tbnr.dev.sg.setup;

import lombok.Data;
import net.cogzmc.core.Core;
import net.cogzmc.core.maps.CMap;
import net.cogzmc.core.maps.CoreMaps;
import net.cogzmc.core.player.CPlayer;
import net.cogzmc.core.util.Point;
import net.cogzmc.core.util.Region;
import net.tbnr.dev.sg.game.map.SGMap;
import net.tbnr.dev.sg.SurvivalGames;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.HashSet;
import java.util.Set;

@Data
public final class SGSetupSession implements Listener {
    private final CPlayer player;
    private final World world;

    private final Set<Point> cornicopiaPoints = new HashSet<>();
    private Region mapRegion;
    private final Set<Point> cornicopiaChests = new HashSet<>();
    private final Set<Point> tier1 = new HashSet<>();
    private final Set<Point> tier2 = new HashSet<>();
    private final Set<Point> deathmatchSpawn = new HashSet<>();
    private String name;
    private String author;
    private String authorsLink;

    private boolean listeningForMeta = false;
    private boolean waitingForChests = false;
    private boolean listeningForDMSetup = false;

    private Point p1;

    @EventHandler(priority = EventPriority.LOW)
    public void onPlayerInteract(PlayerInteractEvent event) {
        CPlayer onlinePlayer = Core.getOnlinePlayer(event.getPlayer());
        if (!onlinePlayer.equals(player)) return;
        if (event.getPlayer().getItemInHand() != null && event.getPlayer().getItemInHand().getType() != Material.DIAMOND_HOE) return;
        event.setCancelled(true);
        if (waitingForChests) return;
        Point point = player.getPoint();
        if (cornicopiaPoints.size() < 24) {
            for (Point cornicopiaPoint : cornicopiaPoints) {
                if (cornicopiaPoint.equals(point)) {
                    player.playSoundForPlayer(Sound.NOTE_PLING, 1f, 0.5f);
                    player.sendMessage(SurvivalGames.getInstance().getFormat("setup.point-same"));
                    return;
                }
            }
            cornicopiaPoints.add(point);
            player.sendMessage(SurvivalGames.getInstance().getFormat("setup.cornicopia-point-next"));
            if(cornicopiaPoints.size() == 24) player.sendMessage(SurvivalGames.getInstance().getFormat("setup.region-select"));
        } else if (mapRegion == null) {
            if (event.getAction() != Action.RIGHT_CLICK_BLOCK && event.getAction() != Action.LEFT_CLICK_BLOCK) return;
            if (p1 == null) {
                player.sendMessage(SurvivalGames.getInstance().getFormat("setup.point-selected"));
                p1 = Point.of(event.getClickedBlock());
            } else {
                player.sendMessage(SurvivalGames.getInstance().getFormat("setup.point-selected"));
                mapRegion = new Region(p1, Point.of(event.getClickedBlock()));
                setupChests();
            }
        } else if (deathmatchSpawn.size() < 24) {
            for (Point cornicopiaPoint : deathmatchSpawn) {
                if (cornicopiaPoint.equals(point)) {
                    player.playSoundForPlayer(Sound.NOTE_PLING, 1f, 0.5f);
                    player.sendMessage(SurvivalGames.getInstance().getFormat("setup.point-same"));
                    return;
                }
            }
            deathmatchSpawn.add(point);
            if (deathmatchSpawn.size() == 24) {
                player.sendMessage(SurvivalGames.getInstance().getFormat("setup.start-name"));
                listeningForMeta = true;
            }
            else player.sendMessage(SurvivalGames.getInstance().getFormat("setup.select-deathmatch"));
        }
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        CPlayer onlinePlayer = Core.getOnlinePlayer(event.getPlayer());
        if (!onlinePlayer.equals(player)) return;
        if (listeningForDMSetup && event.getMessage().equalsIgnoreCase("same")) {
            deathmatchSpawn.addAll(cornicopiaPoints);
            listeningForDMSetup = false;
            player.sendMessage(SurvivalGames.getInstance().getFormat("setup.start-name"));
            listeningForMeta = true;
        } else if (listeningForDMSetup) {
            listeningForDMSetup = false;
            player.sendMessage(SurvivalGames.getInstance().getFormat("setup.select-deathmatch"));
        }
        else if (listeningForMeta) {
            if (name == null) {
                name = event.getMessage();
                player.sendMessage(SurvivalGames.getInstance().getFormat("setup.start-author"));
            } else if (author == null) {
                author = event.getMessage();
                player.sendMessage(SurvivalGames.getInstance().getFormat("setup.start-link"));
            } else if (authorsLink == null) {
                authorsLink = (event.getMessage().equalsIgnoreCase("none") ? "" : event.getMessage());
                player.sendMessage(SurvivalGames.getInstance().getFormat("setup.setup-complete"));
                listeningForMeta = false;
                storeArena();
            }
        }
    }

    private void storeArena() {
        HandlerList.unregisterAll(this);
        CMap cMap = CoreMaps.getInstance().getMapManager().importWorld(world);
        SGMap sgMap = new SGMap(cMap, name, author, authorsLink, cornicopiaPoints, tier1, tier2, cornicopiaChests, deathmatchSpawn);
        SurvivalGames.getInstance().getMapManager().saveMap(sgMap);
        player.sendMessage(SurvivalGames.getInstance().getFormat("setup.save-complete"));
        player.playSoundForPlayer(Sound.LEVEL_UP, 1f, 1.3f);
    }

    private void setupChests() {
        waitingForChests = true;
        new ChestDetector().schedule();
    }

    private void setupChestsComplete() {
        waitingForChests = false;
        player.sendMessage(SurvivalGames.getInstance().getFormat("setup.chests-done", new String[]{"<count>", String.valueOf(tier1.size() + tier2.size() + cornicopiaChests.size())}));
        listeningForDMSetup = true;
    }

    public void start() {
        SurvivalGames.getInstance().registerListener(this);
        player.sendMessage(SurvivalGames.getInstance().getFormat("setup.setup-start"));
        Player bukkitPlayer = player.getBukkitPlayer();
        bukkitPlayer.setGameMode(GameMode.CREATIVE);
        bukkitPlayer.setAllowFlight(true);
        bukkitPlayer.setFlying(true);
        bukkitPlayer.setVelocity(new Vector(0, 2, 0));
        bukkitPlayer.getInventory().clear();
        player.giveItem(Material.DIAMOND_HOE);
        player.playSoundForPlayer(Sound.ORB_PICKUP, 1f, 1.4f);
    }

    void cancel() {
        player.sendMessage(SurvivalGames.getInstance().getFormat("setup.setup-cancelled"));
        HandlerList.unregisterAll(this);
    }

    private class ChestDetector extends BukkitRunnable {
        private final static int INCREMENT = 50;
        private Double x = SGSetupSession.this.mapRegion.getMin().getX();

        @Override
        public void run() {
            Double maxX = SGSetupSession.this.mapRegion.getMax().getX(), maxZ = SGSetupSession.this.mapRegion.getMax().getZ();
            for (; x < Math.min(maxX, x + INCREMENT); x++) {
                for (int y = 0; y < 256; y++) {
                    for (int z = 0; z < maxZ; z++) {
                        Block b = world.getBlockAt(x.intValue(), y, z);
                        if (b.getType() == Material.CHEST) {
                            Inventory inventory = ((Chest) b.getState()).getInventory();
                            if (inventory.contains(Material.GOLD_INGOT)) tier2.add(Point.of(b));
                            else if (inventory.contains(Material.DIAMOND)) cornicopiaChests.add(Point.of(b));
                            else tier1.add(Point.of(b));
                        } else if (b.getType() == Material.ENDER_CHEST) {
                            tier2.add(Point.of(b));
                        }
                    }
                }
            }
            if (x < maxX) schedule();
            else setupChestsComplete();
        }

        public void schedule() {
            runTaskLater(SurvivalGames.getInstance(), 2L);
        }
    }
}
