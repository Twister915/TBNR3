package net.tbnr.dev.sg.game;

import com.comphenix.protocol.ProtocolLibrary;
import lombok.Data;
import lombok.Getter;
import lombok.NonNull;
import net.cogzmc.core.Core;
import net.cogzmc.core.effect.npc.ClickAction;
import net.cogzmc.core.gui.InventoryButton;
import net.cogzmc.core.gui.InventoryGraphicalInterface;
import net.cogzmc.core.modular.command.EmptyHandlerException;
import net.cogzmc.core.player.CPlayer;
import net.cogzmc.core.util.Point;
import net.cogzmc.core.util.TimeUtils;
import net.cogzmc.util.RandomUtils;
import net.tbnr.dev.*;
import net.tbnr.dev.sg.SurvivalGames;
import net.tbnr.dev.sg.game.loots.Tier;
import net.tbnr.dev.sg.game.map.SGMap;
import net.tbnr.dev.sg.game.util.Timer;
import net.tbnr.dev.sg.game.util.TimerDelegate;
import org.bukkit.*;
import org.bukkit.block.Chest;
import org.bukkit.entity.*;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.*;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.hanging.HangingPlaceEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.Vector;
import org.joda.time.Duration;
import org.joda.time.Instant;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.ref.WeakReference;
import java.util.*;

public final class SGGame implements Listener {
    private final static Integer DEFAULT_POINTS = (Integer) Stat.POINTS.defaultValue;
    private SurvivalGames plugin = SurvivalGames.getInstance();

    private final ControlledInventory spectatorInventory = new ControlledInventory() {
        {
            plugin.registerListener(this);
        }

        @Override
        protected ControlledInventoryButton getNewButtonAt(Integer slot) {
            switch (slot) {
                case 4:
                    return new ControlledInventoryButton() {
                        @Override
                        protected ItemStack getStack(CPlayer player) {
                            ItemStack spectator = new ItemStack(Material.COMPASS);
                            ItemMeta itemMeta = spectator.getItemMeta();
                            itemMeta.setDisplayName(ChatColor.GRAY + "Spectator Selector");
                            spectator.setItemMeta(itemMeta);
                            return spectator;
                        }

                        @Override
                        protected void onUse(CPlayer player) {
                            spectatorGUI.open(player);
                        }
                    };
                case 8:
                    return new ControlledInventoryButton() {
                        @Override
                        protected ItemStack getStack(CPlayer player) {
                            ItemStack stack = new ItemStack(Material.INK_SACK);
                            stack.setDurability((short)12);
                            ItemMeta itemMeta = stack.getItemMeta();
                            itemMeta.setDisplayName(ChatColor.DARK_AQUA + "Return to Lobby");
                            stack.setItemMeta(itemMeta);
                            return stack;
                        }

                        @Override
                        protected void onUse(CPlayer player) {
                            ServerHelper.getLobbyServer(false).sendPlayerToServer(player);
                        }
                    };
            }
            return null;
        }
    };

    private final InventoryGraphicalInterface spectatorGUI = new InventoryGraphicalInterface(27, "Tributes");

    private final GameManager manager;
    private final Set<CPlayer> tributes = new HashSet<>();
    private final Set<CPlayer> spectators = new HashSet<>();
    private final Set<WeakReference<CPlayer>> limbo = new HashSet<>(); //These players have died, and will soon either respawn or disconnect. Keep an eye on them.
    @Getter private final SGMap map;
    @Getter private final World world;

    /* tier stuff */
    private final Tier tier1;
    private final Tier tier2;

    /* game stuff */
    @Getter private Instant gameStart;
    @Getter private Integer startedWith;
    @Getter SGGameState state = SGGameState.PRE_GAME;
    private Map<CPlayer, Point> cornicopiaPoints = new WeakHashMap<>();
    private Map<CPlayer, Integer> hungerFlags = new WeakHashMap<>();
    private Timer deathmatchCountdown;
    @Getter(lazy = true) private final Double maxCornDistanceSquared = _getMaxCornDistance();
    private Map<CPlayer, Long> timesStruckDeathmatch = new WeakHashMap<>();

    private Double _getMaxCornDistance() {
        Double maxDistanceSquared = 0d;
        for (Point point : map.getCornicopiaSpawnPoints()) {
            for (Point point1 : map.getCornicopiaSpawnPoints()) {
                Double distance;
                if ((distance = point.distanceSquared(point1)) > maxDistanceSquared) maxDistanceSquared = distance;
            }
        }
        return (Math.ceil(maxDistanceSquared))+Math.pow(5,2);
    }

    public SGGame(GameManager manager, Iterable<CPlayer> players, SGMap map) {
        this.manager = manager;
        for (CPlayer player : players) {
            tributes.add(player);
        }
        if (!map.getMap().isLoaded()) throw new IllegalStateException("The SGMap you have passed is not loaded into Bukkit!");
        this.map = map;
        world = map.getMap().getWorld();
        try {
            tier1 = new Tier(readResource("tier1.json"));
            tier2 = new Tier(readResource("tier2.json"));
        } catch (Exception e) {
            throw new RuntimeException("Unable to start game", e);
        }
    }

    private static JSONObject readResource(String s) throws IOException {
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(SurvivalGames.getInstance().getResource(s)));
        StringBuilder builder = new StringBuilder();
        String line;
        while ((line = bufferedReader.readLine()) != null) {
            builder.append(line);
        }
        return (JSONObject) JSONValue.parse(builder.toString());
    }

    public void startGame() {
        plugin.registerListener(this);
        Iterator<CPlayer> iterator = tributes.iterator();
        while (iterator.hasNext()) {
            CPlayer next = iterator.next();
            if (!next.isOnline()) iterator.remove();
            next.clearChatAll();
        }
        startedWith = tributes.size();
        gameStart = new Instant();
        //Teleport to cornicopia
        Iterator<CPlayer> cornIterator = tributes.iterator();
        Iterator<Point> cornPointIterator = map.getCornicopiaSpawnPoints().iterator();
        while (cornIterator.hasNext() && cornPointIterator.hasNext()) {
            CPlayer player = cornIterator.next();
            Point point = cornPointIterator.next();
            player.getBukkitPlayer().teleport(point.getLocation(world));
            //TODO send a message or something
            player.playSoundForPlayer(Sound.ENDERMAN_TELEPORT, 1f, 0.9f);
            cornicopiaPoints.put(player, point);
        }
        //Kick the players who couldn't fit on the cornicopia.
        while (cornIterator.hasNext()) {
            cornIterator.next().getBukkitPlayer().kickPlayer(ChatColor.RED + "We couldn't make room for you on this map :(");
            cornIterator.remove();
        }
        //setup chests
        for (Point point : map.getCornicopiaChests()) {
            point.getLocation(world).getBlock().setType(Material.CHEST);
        }
        for (Point point : map.getTier1chests()) {
            point.getLocation(world).getBlock().setType(Material.CHEST);
        }
        for (Point point : map.getTier2chests()) {
            point.getLocation(world).getBlock().setType(Material.CHEST);
        }
        world.setTime(0);
        world.setGameRuleValue("doDaylightCycle", "false");
        world.setStorm(false);
        world.setDifficulty(Difficulty.EASY);
        //Cleanup entities
        for (Entity e : world.getEntitiesByClasses(Item.class, LivingEntity.class)) {
            if (e instanceof Player) continue;
            e.remove();
        }
        for (CPlayer tribute : tributes) {
            tribute.resetPlayer();
            tribute.getBukkitPlayer().setGameMode(GameMode.SURVIVAL);
            spectatorGUI.addButton(new TributeButton(tribute));
        }

        ensureHiddenAndShown();
        spectatorGUI.updateInventory();

        //Start the countdown
        new Timer(30, new PreGameCountdown()).start();
    }

    private void creditGameplay(CPlayer player) {
        Integer stat = StatsManager.getStat(Game.SURVIVAL_GAMES, Stat.GAMES_PLAYED, player, Integer.class);
        if (stat == null) stat = 0;
        StatsManager.setStat(Game.SURVIVAL_GAMES, Stat.GAMES_PLAYED, player, stat+1);
        if (player.isOnline()) StatsManager.statChanged(Stat.GAMES_PLAYED, 1, player);
    }

    void updateState() {
        switch (state) {
            case GAMEPLAY:
                for (CPlayer cPlayer : Core.getOnlinePlayers()) {
                    cPlayer.clearChatAll();
                }
                fillChests();
                ensureHiddenAndShown();
                broadcastMessage(plugin.getFormat("game-started"));
                deathmatchCountdown = new Timer(1500, new GameplayTimeLimiter()).start();
                break;
            case PRE_DEATHMATCH_1:
                if (deathmatchCountdown != null && deathmatchCountdown.isRunning()) deathmatchCountdown.cancel();
                new Timer(60, new PreDeathmatchCountdown(SGGameState.PRE_DEATHMATCH_2)).start();
                break;
            case PRE_DEATHMATCH_2:
                broadcastSound(Sound.LEVEL_UP, 1.5f);
                new Timer(10, new PreDeathmatchCountdown(SGGameState.DEATHMATCH)).start();
                Iterator<Point> iterator = map.getDeathmatchSpawn().iterator();
                Iterator<CPlayer> iterator1 = tributes.iterator();
                while (iterator.hasNext() && iterator1.hasNext()) {
                    Point next = iterator.next();
                    iterator1.next().getBukkitPlayer().teleport(next.getLocation(world));
                }
                while (iterator1.hasNext()) {
                    iterator1.next().getBukkitPlayer().kickPlayer(ChatColor.RED + "Your game has ended, there is not enough room for you on the cornicopia!");
                }
                ensureHiddenAndShown();
                break;
            case DEATHMATCH:
                broadcastMessage(plugin.getFormat("deathmatch-start"));
                break;
            case POST_GAME:
                if (tributes.size() == 1) {
                    CPlayer victor = tributes.iterator().next();
                    Integer stat = StatsManager.getStat(Game.SURVIVAL_GAMES, Stat.WINS, victor, Integer.class);
                    if (stat == null) stat = 0;
                    StatsManager.setStat(Game.SURVIVAL_GAMES, Stat.WINS, victor, stat + 1);
                    StatsManager.statChanged(Stat.WINS, 1, victor);
                    creditGameplay(victor);
                    broadcastMessage(plugin.getFormat("has-won", new String[]{"<name>", victor.getDisplayName()}));
                }
                broadcastSound(Sound.ENDERDRAGON_DEATH, 1.4f);
                broadcastMessage(plugin.getFormat("game-over", new String[]{"<time>", TimeUtils.formatDurationNicely(new Duration(gameStart, new Instant()))}));
                manager.gameEnded();
                break;
        }
    }

    void checkForWin() {
        if (tributes.size() <= 1 && this.state != SGGameState.POST_GAME) {
            this.state = SGGameState.POST_GAME;
            updateState();
        } else if (tributes.size() <= 4 && this.state == SGGameState.GAMEPLAY) {
            this.state = SGGameState.PRE_DEATHMATCH_1;
            updateState();
        }
    }

    private void fillChests() {
        for (Point point : map.getCornicopiaChests()) {
            tier2.fillChest((Chest) point.getLocation(world).getBlock().getState());
        }
        for (Point point : map.getTier2chests()) {
            tier2.fillChest((Chest)point.getLocation(world).getBlock().getState());
        }
        for (Point point : map.getTier1chests()) {
            tier1.fillChest((Chest)point.getLocation(world).getBlock().getState());
        }
    }

    private void broadcastSound(Sound sound, Float pitch) {
        for (CPlayer tribute : tributes) {
            tribute.playSoundForPlayer(sound, 50f, pitch);
        }
        for (CPlayer spectator : spectators) {
            spectator.playSoundForPlayer(sound, 50f, pitch);
        }
        for (WeakReference<CPlayer> cPlayerWeakReference : limbo) {
            CPlayer player;
            if ((player = cPlayerWeakReference.get()) != null) player.playSoundForPlayer(sound, 50f, pitch);
        }
    }

    private void broadcastMessage(String message) {
        for (CPlayer tribute : tributes) {
            tribute.sendMessage(message);
        }
        for (CPlayer spectator : spectators) {
            spectator.sendMessage(message);
        }
        for (WeakReference<CPlayer> cPlayerWeakReference : limbo) {
            CPlayer player;
            if ((player = cPlayerWeakReference.get()) != null) player.sendMessage(message);
        }
    }

    private boolean eventAppliesTo(Event event, Set<CPlayer> set) {
        if (event instanceof PlayerEvent) return set.contains(Core.getOnlinePlayer(((PlayerEvent) event).getPlayer()));
        if (event instanceof BlockBreakEvent) return set.contains(Core.getOnlinePlayer(((BlockBreakEvent) event).getPlayer()));
        if (event instanceof BlockPlaceEvent) return set.contains(Core.getOnlinePlayer(((BlockPlaceEvent) event).getPlayer()));
        if (event instanceof HangingBreakByEntityEvent && ((HangingBreakByEntityEvent) event).getRemover() instanceof Player)
            return set.contains(Core.getOnlinePlayer((Player) ((HangingBreakByEntityEvent) event).getRemover()));
        return false;
    }

    private boolean eventAppliesToTributes(Event event) {
        return eventAppliesTo(event, tributes);
    }

    private boolean eventAppliesToSpectators(Event event) {
        return eventAppliesTo(event, spectators);
    }

    void removeTribute(@NonNull CPlayer player) {
        if (!tributes.contains(player)) return;
        tributes.remove(player);
        for (InventoryButton inventoryButton : spectatorGUI.getButtons()) {
            if (((TributeButton) inventoryButton).tribute.equals(player)) {
                spectatorGUI.removeButton(inventoryButton);
                break;
            }
        }
        spectatorGUI.updateInventory();
        broadcastMessage(plugin.getFormat("tributes-remain", new String[]{"<tributes>", String.valueOf(tributes.size())}));
    }

    public void makeSpectator(final CPlayer player) {
        spectators.add(player);
        player.resetPlayer();
        Player bukkitPlayer = player.getBukkitPlayer();
        bukkitPlayer.setAllowFlight(true);
        bukkitPlayer.setFlying(true);
        bukkitPlayer.setVelocity(new Vector(0, 2, 0));
        for (ItemStack itemStack : bukkitPlayer.getInventory()) {
            if (itemStack == null) continue;
            world.dropItemNaturally(bukkitPlayer.getLocation(), itemStack);
        }
        bukkitPlayer.getInventory().clear();
        player.playSoundForPlayer(Sound.AMBIENCE_RAIN, 1f, 1.2f);
        spectatorInventory.setActive(player);
        spectatorInventory.updateItems();
        Bukkit.getScheduler().runTaskLater(plugin, new Runnable() {
            @Override
            public void run() {
                Player bukkitPlayer2 = player.getBukkitPlayer();
                for (CPlayer tribute : tributes) {
                    tribute.getBukkitPlayer().hidePlayer(bukkitPlayer2);
                }
                for (CPlayer spectator : spectators) {
                    Player bukkitPlayer1 = spectator.getBukkitPlayer();
                    if (bukkitPlayer2.canSee(bukkitPlayer1)) bukkitPlayer2.hidePlayer(bukkitPlayer1);
                    bukkitPlayer1.hidePlayer(bukkitPlayer2);
                }
                try {
                    Object handle = bukkitPlayer2.getClass().getMethod("getHandle").invoke(bukkitPlayer2);
                    handle.getClass().getField("height").set(handle, 0f);
                    handle.getClass().getField("width").set(handle, 0f);
                    handle.getClass().getField("length").set(handle, 0f);
                } catch (Exception e) {
                    plugin.logMessage(ChatColor.RED + "Unable to hide spectator from arrows!");
                    e.printStackTrace();
                }
            }
        }, 2L);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerMove(PlayerMoveEvent event) {
        CPlayer onlinePlayer1 = Core.getOnlinePlayer(event.getPlayer());
        if (state == SGGameState.DEATHMATCH && eventAppliesToTributes(event)) {
            if (!isWithinCornicopiaBuffer(Point.of(event.getTo()))) {
                Long instant = timesStruckDeathmatch.get(onlinePlayer1);
                if (instant == null || instant+2000 < System.currentTimeMillis()) {
                    world.strikeLightningEffect(event.getTo());
                    event.getPlayer().damage(6);
                    onlinePlayer1.sendMessage(plugin.getFormat("return-to-corn"));
                    timesStruckDeathmatch.put(onlinePlayer1, System.currentTimeMillis());
                }
            }
            return;
        }
        if (state != SGGameState.PRE_GAME && state != SGGameState.PRE_DEATHMATCH_2) return;
        if (eventAppliesToSpectators(event)) return;
        if (!eventAppliesToTributes(event)) return;
        Point point = cornicopiaPoints.get(onlinePlayer1);
        if (Math.abs(point.getX()-event.getTo().getX()) > 0.5 || Math.abs(point.getZ() - event.getTo().getZ()) > 0.5) {
            Location location = point.getLocation(world);
            location.setPitch(event.getTo().getPitch());
            location.setYaw(event.getTo().getYaw());
            event.getPlayer().teleport(location);
            onlinePlayer1.playSoundForPlayer(Sound.CREEPER_HISS, 1f, 1.3f);
        }
    }

    private boolean isWithinCornicopiaBuffer(Point point) {
        for (Point point1 : map.getCornicopiaSpawnPoints()) {
            if (point.distanceSquared(point1) > getMaxCornDistanceSquared()) return false;
        }
        return true;
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        if (spectators.contains(Core.getOnlinePlayer(event.getPlayer()))) {
            event.setCancelled(true);
            event.getPlayer().sendMessage(plugin.getFormat("specatator-no-chat"));
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerDeath(EntityDeathEvent event) {
        if (!(event.getEntity() instanceof Player)) return;
        Player bukkitPlayer = (Player) event.getEntity();
        CPlayer player = Core.getOnlinePlayer(bukkitPlayer);
        if (!tributes.contains(player)) return;
        for (ItemStack itemStack : bukkitPlayer.getInventory()) {
            if (itemStack == null) continue;
            if (itemStack.getType() == Material.AIR) continue;
            world.dropItemNaturally(bukkitPlayer.getLocation(), itemStack);
        }
        for (ItemStack itemStack : bukkitPlayer.getInventory().getArmorContents()) {
            if (itemStack == null) continue;
            if (itemStack.getType() == Material.AIR) continue;
            world.dropItemNaturally(bukkitPlayer.getLocation(), itemStack);
        }
        bukkitPlayer.getInventory().clear();
        event.getDrops().clear();
        limbo.add(new WeakReference<>(player));
        removeTribute(player);
        Integer deaths = StatsManager.getStat(Game.SURVIVAL_GAMES, Stat.DEATHS, player, Integer.class);
        if (deaths == null) deaths = 0;
        StatsManager.setStat(Game.SURVIVAL_GAMES, Stat.DEATHS, player, deaths + 1);
        StatsManager.statChanged(Stat.DEATHS, 1, player);
        Integer oldPoints = StatsManager.getStat(Game.SURVIVAL_GAMES, Stat.POINTS, player, Integer.class);
        if (oldPoints == null) oldPoints = DEFAULT_POINTS;
        if (bukkitPlayer.getKiller() != null) {
            CPlayer killer = Core.getOnlinePlayer(bukkitPlayer.getKiller());
            killer.getBukkitPlayer().setLevel(killer.getBukkitPlayer().getLevel() + 1);
            killer.playSoundForPlayer(Sound.LEVEL_UP);
            killer.sendMessage(plugin.getFormat("death-exp"));
            Integer killz = StatsManager.getStat(Game.SURVIVAL_GAMES, Stat.KILLS, killer, Integer.class);
            if (killz == null) killz = 0;
            StatsManager.setStat(Game.SURVIVAL_GAMES, Stat.KILLS, killer, killz + 1);
            StatsManager.statChanged(Stat.KILLS, 1, killer);
            int gainedPoints = ((int) Math.floor(oldPoints * .10)) + 10;
            Integer points = StatsManager.getStat(Game.SURVIVAL_GAMES, Stat.POINTS, killer, Integer.class);
            if (points == null) points = DEFAULT_POINTS;
            StatsManager.setStat(Game.SURVIVAL_GAMES, Stat.POINTS, killer,
                   points + gainedPoints
            );
            StatsManager.statChanged(Stat.POINTS, gainedPoints, killer);
            String health = String.format("%.1f", killer.getBukkitPlayer().getHealth() / 2f);
            player.sendMessage(plugin.getFormat("death-info", new String[]{"<killer>", killer.getDisplayName()}, new String[]{"<hearts>", health}));
            killer.sendMessage(plugin.getFormat("you-killed", new String[]{"<dead>", player.getDisplayName()}));
        }
        int newPoints = (int) (oldPoints * .9);
        StatsManager.setStat(Game.SURVIVAL_GAMES, Stat.POINTS, player, newPoints);
        StatsManager.statChanged(Stat.POINTS, newPoints-oldPoints, player);
        player.sendMessage(plugin.getFormat("you-died"));
        bukkitPlayer.getWorld().strikeLightningEffect(bukkitPlayer.getLocation());
        for (CPlayer tribute : tributes) {
            tribute.getBukkitPlayer().playSound(bukkitPlayer.getLocation(), Sound.FIREWORK_LARGE_BLAST, 35f, 0.5f);
            tribute.sendMessage(plugin.getFormat("death", new String[]{"<blocks>",
                    String.valueOf(Math.ceil(tribute.getBukkitPlayer().getLocation().distance(bukkitPlayer.getLocation())))}));
            //PERFORMANCE NOTE: SQUARE ROOT FUNCTION USED IN A LOOP
            //¯\_(ツ)_/¯
        }
        creditGameplay(player);
        checkForWin();
    }

    @EventHandler
    public void onExplode(EntityExplodeEvent event) {
        if (!event.getEntity() .getWorld().equals(world)) return;
        event.blockList().clear();
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        event.setDeathMessage(null);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        event.setQuitMessage(null);
    }

    @EventHandler
    public void onPlayerKick(PlayerKickEvent event) {
        event.setLeaveMessage(null);
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        if (eventAppliesToSpectators(event)) event.setCancelled(true);
        if (eventAppliesToTributes(event)) {
            switch (event.getBlock().getType()) {
                case LEAVES:
                case LEAVES_2:
                case CAKE_BLOCK:
                case WEB:
                case LONG_GRASS:
                case WHEAT:
                case CARROT:
                case POTATO:
                case VINE:
                    return;
            }
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        switch (event.getBlockPlaced().getType()) {
            case CAKE_BLOCK:
            case FIRE:
            case WEB:
                break;
            case TNT:
                event.setCancelled(true);
                Location location = event.getBlockPlaced().getLocation();
                location.getWorld().spawnEntity(location, EntityType.PRIMED_TNT);
                break;
            default:
                event.setCancelled(true);
        }
    }

    @EventHandler
    public void onHangingDestroy(HangingBreakByEntityEvent event) {
        if (event.getRemover() instanceof Player) {
            CPlayer onlinePlayer = Core.getOnlinePlayer((Player) event.getRemover());
            if (tributes.contains(onlinePlayer) || spectators.contains(onlinePlayer)) event.setCancelled(true);
        } else event.setCancelled(true);
    }

    @EventHandler
    public void onHangingPlace(HangingPlaceEvent event) {
        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        CPlayer onlinePlayer = Core.getOnlinePlayer(event.getPlayer());
        Iterator<WeakReference<CPlayer>> iterator = limbo.iterator();
        while (iterator.hasNext()) {
            WeakReference<CPlayer> cPlayerWeakReference = iterator.next();
            CPlayer cPlayer = cPlayerWeakReference.get();
            if (cPlayer == null) continue;
            if (!cPlayer.equals(onlinePlayer)) continue;
            //Should respawn them in the same location they died in? TODO Test
            event.setRespawnLocation(event.getPlayer().getLocation());
            makeSpectator(onlinePlayer);
            iterator.remove();
            ensureHiddenAndShown();
            return;
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (eventAppliesToSpectators(event)) {
            event.setCancelled(true);
            return;
        }
        if (!eventAppliesToTributes(event)) return;
        if (state == SGGameState.PRE_DEATHMATCH_2 || state == SGGameState.PRE_GAME) {
            event.setCancelled(true);
            return;
        }
        Player bukkitPlayer = event.getPlayer();
        ItemStack itemInHand = bukkitPlayer.getItemInHand();
        //Limit uses on flint and steel
        if (itemInHand != null && itemInHand.getType() == Material.FLINT_AND_STEEL) {
            itemInHand.setDurability((short) (itemInHand.getDurability() + 16));
        }
    }

    @EventHandler
    public void onPlayerInteractEntity(EntityInteractEvent event) {
        if (!event.getEntity().getWorld().equals(world)) return;
        if (event.getEntity() instanceof ItemFrame) event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerHunger(FoodLevelChangeEvent event) {
        HumanEntity entity = event.getEntity();
        if (!(entity instanceof Player)) return;
        if (((Player) event.getEntity()).getFoodLevel()-event.getFoodLevel() < 0) return;
        CPlayer onlinePlayer = Core.getOnlinePlayer((Player) entity);
        if (spectators.contains(onlinePlayer)) event.setCancelled(true);
        else if (tributes.contains(onlinePlayer)) {
            if (state == SGGameState.PRE_GAME) {
                event.setCancelled(true);
                return;
            }
            Integer integer = hungerFlags.get(onlinePlayer);
            if (integer == null) integer = 0;
            event.setCancelled(integer < 4);
            integer++;
            if (integer > 4) integer = 0;
            hungerFlags.put(onlinePlayer, integer);
            Core.logDebug("Survivalgames - cancel hunger for " + onlinePlayer.getName() + ":" + event.isCancelled());
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerDamage(EntityDamageEvent event) {
        Entity entity = event.getEntity();
        if (!(entity instanceof Player)) return;
        CPlayer onlinePlayer = Core.getOnlinePlayer((Player) entity);
        if (spectators.contains(onlinePlayer)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerDamagePlayer(EntityDamageByEntityEvent event) {
        Entity damager = event.getDamager();
        if (!(damager instanceof Player)) return;
        EntityType entityType = event.getEntityType();
        if (entityType == EntityType.ITEM_FRAME || entityType == EntityType.PAINTING) {
            event.setCancelled(true);
            return;
        }
        Player damager1 = (Player) damager;
        if (spectators.contains(Core.getOnlinePlayer(damager1))) event.setCancelled(true);
        if ((state == SGGameState.PRE_DEATHMATCH_2 || state == SGGameState.PRE_GAME) && tributes.contains(Core.getOnlinePlayer(damager1))) event.setCancelled(true);
    }

    @EventHandler
    public void onPlayerDrop(PlayerDropItemEvent event) {
        if (eventAppliesToSpectators(event)) event.setCancelled(true);
    }

    @EventHandler
    public void onPlayerPickup(PlayerPickupItemEvent event) {
        if (!eventAppliesToTributes(event) && event.getItem().getLocation().getWorld().equals(world)) event.setCancelled(true);
    }

    @EventHandler
    public void onCreatureSpawn(CreatureSpawnEvent event) {
        event.getEntity().remove();
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        event.setJoinMessage(null);
    }

    @Data
    private class TimerDelegateImplStateChange implements TimerDelegate {
        protected final SGGameState state;

        @Override
        public void countdownStarted(Timer timer, Integer totalSeconds) {
            handleDisplaySecond(totalSeconds);
        }

        @Override
        public void countdownEnded(Timer timer, Integer totalSeconds) {
            handleDisplaySecond(0);
            SGGame sgGame = SGGame.this;
            sgGame.state = state;
            sgGame.updateState();
        }

        @Override
        public void countdownChanged(Timer timer, Integer secondsPassed, Integer totalSeconds) {
            handleDisplaySecond(totalSeconds-secondsPassed);
        }

        private void handleDisplaySecond(Integer second) {
            announceSecond(second);
        }

        protected void announceSecond(Integer second) {}
    }

    private class PreDeathmatchCountdown extends TimerDelegateImplStateChange {
        private final Integer[] broadcastSeconds = new Integer[]{60, 30, 10, 9, 8, 7, 6, 5, 4, 3, 2, 1};
        public PreDeathmatchCountdown(SGGameState state) {
            super(state);
        }

        @Override
        protected void announceSecond(Integer second) {
            if (!(RandomUtils.contains(broadcastSeconds, second))) return;
            broadcastMessage(plugin.getFormat("pre-deathmatch" + (state == SGGameState.PRE_DEATHMATCH_2 ? "-2" : ""), new String[]{"<seconds>", String.valueOf(second)}));
            broadcastSound(Sound.ORB_PICKUP, 1f - (second < 10 ? 0.05f * second : 0f));
        }
    }

    private class PreGameCountdown extends TimerDelegateImplStateChange {
        private final Integer[] broadcastSeconds = new Integer[]{60, 30, 10, 9, 8, 7, 6, 5, 4, 3, 2, 1};
        private PreGameCountdown() {
            super(SGGameState.GAMEPLAY);
        }

        @Override
        protected void announceSecond(Integer second) {
            if (!RandomUtils.contains(broadcastSeconds, second)) return;
            SGGame.this.broadcastMessage(SGGame.this.plugin.getFormat("pre-game-countdown", new String[]{"<seconds>", String.valueOf(second)}));
            SGGame.this.broadcastSound(Sound.ORB_PICKUP, 1f - (second < 10 ? 0.05f * second : 0f));
        }
    }

    private class GameplayTimeLimiter implements TimerDelegate {
        private final Integer[] secondsToAnnounce = {1500, 900, 600, 300, 180, 120, 60, 30, 10, 9, 8, 7, 6, 5, 4, 3, 2, 1};

        @Override
        public void countdownStarted(Timer timer, Integer totalSeconds) {
            handleTime(timer);
        }

        @Override
        public void countdownEnded(Timer timer, Integer totalSeconds) {
            handleTime(timer);
            SGGame.this.state = SGGameState.PRE_DEATHMATCH_1;
            SGGame.this.updateState();
        }

        @Override
        public void countdownChanged(Timer timer, Integer secondsPassed, Integer totalSeconds) {
            handleTime(timer);
        }

        private void handleTime(Timer time) {
            if (time.getLength()-time.getSecondsPassed()%30 == 0) ensureHiddenAndShown();
            if (!RandomUtils.contains(secondsToAnnounce, time.getLength()-time.getSecondsPassed())) return;
            SGGame.this.broadcastMessage(plugin.getFormat("gameplay-time", new String[]{"<time>", TimeUtils.formatDurationNicely(time.getTimeRemaining())}));
            SGGame.this.broadcastSound(Sound.NOTE_PLING, 0.7f);
        }
    }

    private class TributeButton extends InventoryButton {
        private final WeakReference<CPlayer> tribute;
        private boolean isDead = false;
        private final String displayName;

        private TributeButton(CPlayer tribute) {
            super(getStackForTribute(tribute));
            this.tribute = new WeakReference<>(tribute);
            this.displayName = tribute.getDisplayName();
        }

        public void died() {
            isDead = true;
            ItemStack skull = new ItemStack(Material.SKULL_ITEM);
            skull.setDurability((short) SkullType.SKELETON.ordinal());
            ItemMeta itemMeta = skull.getItemMeta();
            itemMeta.setDisplayName(ChatColor.GREEN + displayName);
            itemMeta.setLore(Arrays.asList(ChatColor.RED + "This tribute has fallen"));
            this.setStack(skull);
        }

        @Override
        protected void onPlayerClick(CPlayer player, ClickAction action) throws EmptyHandlerException {
            CPlayer tribute = this.tribute.get();
            if (tribute == null || isDead) return;
            if (action == ClickAction.LEFT_CLICK) {
                player.getBukkitPlayer().teleport(tribute.getBukkitPlayer().getLocation().add(0, 4, 0));
                player.playSoundForPlayer(Sound.ENDERMAN_TELEPORT);
            } else {
                player.sendMessage(plugin.getFormat("coming-soon"));
            }
        }
    }

    private static ItemStack getStackForTribute(CPlayer tribute) {
        ItemStack stack = new ItemStack(Material.SKULL_ITEM);
        stack.setDurability((short) SkullType.PLAYER.ordinal());
        ItemMeta itemMeta = stack.getItemMeta();
        itemMeta.setDisplayName(ChatColor.GREEN + ChatColor.ITALIC.toString() + tribute.getDisplayName());
        itemMeta.setLore(Arrays.asList(ChatColor.GRAY + "Left click to teleport to this tribute.", ChatColor.GRAY + "Right click to sponsor this tribute."));
        stack.setItemMeta(itemMeta);
        return stack;
    }

    private void ensureHiddenAndShown() {
        List<Player> players = new ArrayList<>();
        for (CPlayer cPlayer : tributes) {
            players.add(cPlayer.getBukkitPlayer());
        }
        for (CPlayer spectator : spectators) {
            players.add(spectator.getBukkitPlayer());
        }
        for (CPlayer tribute : tributes) {
            Player bukkitPlayer = tribute.getBukkitPlayer();
            for (CPlayer spectator : spectators) {
                bukkitPlayer.hidePlayer(spectator.getBukkitPlayer());
            }
            for (CPlayer cPlayer : tributes) {
                if (cPlayer.equals(tribute)) continue;
                bukkitPlayer.showPlayer(cPlayer.getBukkitPlayer());
            }
            ProtocolLibrary.getProtocolManager().updateEntity(bukkitPlayer, players);
        }
    }
}
