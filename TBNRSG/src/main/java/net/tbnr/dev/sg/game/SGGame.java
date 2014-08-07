package net.tbnr.dev.sg.game;

import lombok.Data;
import lombok.Getter;
import lombok.NonNull;
import net.cogzmc.core.Core;
import net.cogzmc.core.player.CPlayer;
import net.cogzmc.core.util.Point;
import net.cogzmc.core.util.TimeUtils;
import net.cogzmc.util.RandomUtils;
import net.tbnr.dev.Game;
import net.tbnr.dev.Stat;
import net.tbnr.dev.StatsManager;
import net.tbnr.dev.sg.SurvivalGames;
import net.tbnr.dev.sg.game.map.SGMap;
import net.tbnr.dev.sg.game.util.*;
import net.tbnr.dev.sg.game.util.Timer;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.*;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.*;
import org.bukkit.util.Vector;
import org.joda.time.Instant;

import java.lang.ref.WeakReference;
import java.util.*;

public final class SGGame implements Listener {
    private final GameManager manager;
    private final Set<CPlayer> tributes = new HashSet<>();
    private final Set<CPlayer> spectators = new HashSet<>();
    private final Set<WeakReference<CPlayer>> limbo = new HashSet<>(); //These players have died, and will soon either respawn or disconnect. Keep an eye on them.
    private final SGMap map;
    private final World world;

    /* game stuff */
    @Getter private Instant gameStart;
    @Getter private Integer startedWith;
    @Getter private SGGameState state = SGGameState.PRE_GAME;
    private Map<CPlayer, Point> cornicopiaPoints = new WeakHashMap<>();
    private Map<CPlayer, Boolean> hungerFlags = new WeakHashMap<>();
    private Timer deathmatchCountdown;

    public SGGame(GameManager manager, Iterable<CPlayer> players, SGMap map) {
        this.manager = manager;
        for (CPlayer player : players) {
            tributes.add(player);
        }
        if (!map.getMap().isLoaded()) throw new IllegalStateException("The SGMap you have passed is not loaded into Bukkit!");
        this.map = map;
        world = map.getMap().getWorld();
    }

    public void startGame() {
        Iterator<CPlayer> iterator = tributes.iterator();
        while (iterator.hasNext()) {
            CPlayer next = iterator.next();
            if (!next.isOnline()) iterator.remove();
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
            point.getLocation(world).getBlock().setType(Material.AIR);
        }
        for (Point point : map.getTier1chests()) {
            point.getLocation(world).getBlock().setType(Material.CHEST);
        }
        for (Point point : map.getTier2chests()) {
            point.getLocation(world).getBlock().setType(Material.CHEST);
        }
        //Cleanup entities
        for (Entity e : world.getEntitiesByClasses(Item.class, LivingEntity.class)) {
            if (e instanceof Player) continue;
            e.remove();
        }
        //Start the countdown
        new Timer(60, new PreGameCountdown()).start();
    }

    private void updateState() {
        switch (state) {
            case GAMEPLAY:
                deathmatchCountdown = new Timer(1500, new GameplayTimeLimiter()).start();
                break;
            case PRE_DEATHMATCH_1:
                if (deathmatchCountdown.isRunning()) deathmatchCountdown.cancel();
                break;
            case PRE_DEATHMATCH_2:
                break;
            case DEATHMATCH:
                break;
            case POST_GAME:
                manager.gameEnded();
                break;
        }
    }

    private void broadcastSound(Sound sound, Float pitch) {
        for (CPlayer tribute : tributes) {
            tribute.playSoundForPlayer(sound, 1f, pitch);
        }
        for (CPlayer spectator : spectators) {
            spectator.playSoundForPlayer(sound, 1f, pitch);
        }
    }

    private void broadcastMessage(String message) {
        for (CPlayer tribute : tributes) {
            tribute.sendMessage(message);
        }
        for (CPlayer spectator : spectators) {
            spectator.sendMessage(message);
        }
    }

    private void dropChests() {
        for (Point point : map.getCornicopiaChests()) {
            world.spawnFallingBlock(point.getLocation(world).add(0, 12, 0), Material.CHEST, (byte)0);
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
        tributes.remove(player);

    }

    private void makeSpectator(CPlayer player) {
        spectators.add(player);
        Player bukkitPlayer = player.getBukkitPlayer();
        bukkitPlayer.setAllowFlight(true);
        bukkitPlayer.setVelocity(new Vector(0, 2, 0));
        player.playSoundForPlayer(Sound.AMBIENCE_RAIN, 1f, 1.2f);
        //TODO spectator logic
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerMove(PlayerMoveEvent event) {
        if (state != SGGameState.PRE_GAME && state != SGGameState.PRE_DEATHMATCH_2) return;
        if (eventAppliesToSpectators(event)) return;
        if (!eventAppliesToTributes(event)) return;
        if (Math.abs(event.getFrom().getX()-event.getTo().getX()) > 1.0 || Math.abs(event.getFrom().getZ() - event.getTo().getZ()) > 1.0) {
            CPlayer onlinePlayer = Core.getOnlinePlayer(event.getPlayer());
            event.getPlayer().teleport(cornicopiaPoints.get(onlinePlayer).getLocation(world));
            onlinePlayer.playSoundForPlayer(Sound.CREEPER_HISS, 1f, 1.3f);
            onlinePlayer.sendMessage(SurvivalGames.getInstance().getFormat("cornicopia-bounceback"));
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerDeath(EntityDeathEvent event) {
        if (!(event.getEntity() instanceof Player)) return;
        Player bukkitPlayer = (Player) event.getEntity();
        CPlayer player = Core.getOnlinePlayer(bukkitPlayer);
        if (!tributes.contains(player)) return;
        limbo.add(new WeakReference<>(player));
        removeTribute(player);
        StatsManager.setStat(Game.SURVIVAL_GAMES, Stat.DEATHS, player, StatsManager.getStat(Game.SURVIVAL_GAMES, Stat.DEATHS, player, Integer.class) + 1);
        StatsManager.statChanged(Stat.DEATHS, 1, player);
        Integer oldPoints = StatsManager.getStat(Game.SURVIVAL_GAMES, Stat.POINTS, player, Integer.class);
        if (bukkitPlayer.getKiller() != null) {
            CPlayer killer = Core.getOnlinePlayer(bukkitPlayer.getKiller());
            StatsManager.setStat(Game.SURVIVAL_GAMES, Stat.KILLS, killer, StatsManager.getStat(Game.SURVIVAL_GAMES, Stat.KILLS, killer, Integer.class) + 1);
            StatsManager.statChanged(Stat.KILLS, 1, killer);
            int gainedPoints = ((int) Math.floor(oldPoints * .15));
            StatsManager.setStat(Game.SURVIVAL_GAMES, Stat.POINTS, killer,
                    StatsManager.getStat(Game.SURVIVAL_GAMES, Stat.POINTS, killer, Integer.class) + gainedPoints
            );
            StatsManager.statChanged(Stat.POINTS, gainedPoints, killer);
        }
        int newPoints = (int) (oldPoints * .9);
        StatsManager.setStat(Game.SURVIVAL_GAMES, Stat.POINTS, player, newPoints);
        StatsManager.statChanged(Stat.POINTS, oldPoints-newPoints, player);
        bukkitPlayer.getWorld().strikeLightningEffect(bukkitPlayer.getLocation());
        for (CPlayer tribute : tributes) {
            tribute.getBukkitPlayer().playSound(bukkitPlayer.getLocation(), Sound.FIREWORK_LARGE_BLAST, 35f, 0.5f);
            tribute.sendMessage(SurvivalGames.getInstance().getFormat("death", new String[]{"<blocks>",
                    String.valueOf(tribute.getBukkitPlayer().getLocation().distance(bukkitPlayer.getLocation()))}));
            //PERFORMANCE NOTE: SQUARE ROOT FUNCTION USED IN A LOOP
            //¯\_(ツ)_/¯
        }
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
                    return;
            }
            event.setCancelled(true);
        }
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

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerHunger(FoodLevelChangeEvent event) {
        HumanEntity entity = event.getEntity();
        if (!(entity instanceof Player)) return;
        CPlayer onlinePlayer = Core.getOnlinePlayer((Player) entity);
        if (spectators.contains(onlinePlayer)) event.setCancelled(true);
        else if (tributes.contains(onlinePlayer)) {
            event.setCancelled(!(hungerFlags.containsKey(onlinePlayer) ? hungerFlags.get(onlinePlayer) : false));
            hungerFlags.put(onlinePlayer, event.isCancelled());
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerDamage(EntityDamageEvent event) {
        Entity entity = event.getEntity();
        if (!(entity instanceof Player)) return;
        CPlayer onlinePlayer = Core.getOnlinePlayer((Player) entity);
        if (spectators.contains(onlinePlayer)) {
            event.setCancelled(true);
            return;
        }
        if (!tributes.contains(onlinePlayer)) return;
    }

    @Data
    private class TimerDelegateImplStateChange implements TimerDelegate {
        private final SGGameState state;
        private final Integer[] broadcastSeconds = new Integer[]{60, 30, 10, 9, 8, 7, 6, 5, 4, 3, 2, 1};

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

    private class PreGameCountdown extends TimerDelegateImplStateChange {
        private PreGameCountdown() {
            super(SGGameState.GAMEPLAY);
        }

        @Override
        protected void announceSecond(Integer second) {
            if (second == 30) SGGame.this.dropChests();
            SGGame.this.broadcastMessage(SurvivalGames.getInstance().getFormat("pre-game-countdown", new String[]{"<seconds>", String.valueOf(second)}));
            SGGame.this.broadcastSound(Sound.ORB_PICKUP, 1f - (second < 10 ? 0.1f * second : 0f));
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
        }

        @Override
        public void countdownChanged(Timer timer, Integer secondsPassed, Integer totalSeconds) {
            handleTime(timer);
        }

        private void handleTime(Timer time) {
            if (!RandomUtils.contains(secondsToAnnounce, time.getLength()-time.getSecondsPassed())) return;
            SGGame.this.broadcastMessage(SurvivalGames.getInstance().getFormat("gameplay-time", new String[]{"<time>", TimeUtils.formatDurationNicely(time.getTimeRemaining())}));
            SGGame.this.broadcastSound(Sound.NOTE_PLING, 0.7f);
        }
    }
}
