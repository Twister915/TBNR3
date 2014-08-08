package net.tbnr.dev.sg.game;

import lombok.Getter;
import net.cogzmc.core.Core;
import net.cogzmc.core.player.CPlayer;
import net.cogzmc.core.player.CPlayerConnectionListener;
import net.cogzmc.core.player.CPlayerJoinException;
import net.cogzmc.core.util.Point;
import net.cogzmc.util.RandomUtils;
import net.tbnr.dev.ServerHelper;
import net.tbnr.dev.sg.SurvivalGames;
import net.tbnr.dev.sg.command.VoteCommand;
import net.tbnr.dev.sg.game.map.SGMap;
import net.tbnr.dev.sg.game.util.Timer;
import net.tbnr.dev.sg.game.util.TimerDelegate;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.net.InetAddress;
import java.util.Iterator;

public final class GameManager implements Listener, CPlayerConnectionListener {
    private final static String PRE_GAME_STATUS = "pre_game";
    private final static String IN_GAME_STATUS = "in_game";
    private final static String GAME_OVER_STATUS = "game_over";

    @Getter private SGGame runningGame = null;
    @Getter public PreGameLobby preGameLobby;
    private final PreGameListener listener;
    private Iterator<Point> spawnPoints;
    @Getter private final VotingSession votingSession;
    private Timer gameTimer;

    public GameManager() {
        SurvivalGames.getInstance().registerListener(this);
        Core.getPlayerManager().registerCPlayerConnectionListener(this);
        preGameLobby = SurvivalGames.getInstance().getMapManager().getPreGameLobby();
        preGameLobby.getMap().load("PRE_GAME");
        World world = preGameLobby.getMap().getWorld();
        world.setTime(0);
        world.setStorm(false);
        world.setGameRuleValue("doDaylightCycle", "false");
        listener = SurvivalGames.getInstance().registerListener(new PreGameListener());
        spawnPoints = preGameLobby.getSpawnPoints().iterator();
        votingSession = new VotingSession(SurvivalGames.getInstance().getMapManager().getRandomMaps(5));
        SurvivalGames.getInstance().registerCommand(new VoteCommand());
        startTimer();
        Bukkit.getScheduler().runTaskLater(SurvivalGames.getInstance(), new Runnable() {
            @Override
            public void run() {
                if (Core.getNetworkManager() != null) ServerHelper.setStatus(PRE_GAME_STATUS);
            }
        }, 10L);
    }

    private void startTimer() {
        gameTimer = new Timer(120, new GameStartTimer()).start();
    }

    private Point getNextSpawnPoint() {
        if (!spawnPoints.hasNext()) spawnPoints = preGameLobby.getSpawnPoints().iterator();
        return spawnPoints.next();
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        CPlayer onlinePlayer = Core.getOnlinePlayer(event.getPlayer());
        onlinePlayer.clearChatAll();
        if (runningGame != null) {
            player.teleport(runningGame.getMap().getCornicopiaSpawnPoints().iterator().next().getLocation(runningGame.getWorld()));
            runningGame.makeSpectator(Core.getOnlinePlayer(player));
            return;
        }
        player.teleport(getNextSpawnPoint().getLocation(preGameLobby.getMap().getWorld()));
        event.setJoinMessage(SurvivalGames.getInstance().getFormat("join-message", new String[]{"<player>", Core.getOnlinePlayer(player).getDisplayName()}));
        sendMapBlock(onlinePlayer);
        onlinePlayer.resetPlayer();
        event.getPlayer().getInventory().clear();
    }

    void beginGame() {
        beginGame(votingSession.getMostVotedFor());
    }

    void beginGame(final SGMap map) {
        if (gameTimer != null && gameTimer.isRunning()) gameTimer.cancel();
        String format = SurvivalGames.getInstance().getFormat("game-starting");
        for (CPlayer cPlayer : Core.getOnlinePlayers()) {
            cPlayer.playSoundForPlayer(Sound.ENDERDRAGON_GROWL, 1f, 1.3f);
            cPlayer.sendMessage(format);
        }
        Bukkit.getScheduler().runTaskLater(SurvivalGames.getInstance(), new Runnable() {
            @Override
            public void run() {
                startGame(map);
            }
        }, 40L);
    }

    private void startGame(SGMap arena) {
        if (runningGame != null) throw new IllegalStateException("There is a game already running!");
        HandlerList.unregisterAll(listener);
        arena.getMap().load("SG_MAP_" + Core.getRandom().nextInt(100));
        runningGame = new SGGame(this, Core.getOnlinePlayers(), arena);
        runningGame.startGame();
        if (Core.getNetworkManager() != null) ServerHelper.setStatus(IN_GAME_STATUS);
    }

    void gameEnded() {
        if (Core.getNetworkManager() != null) ServerHelper.setStatus(GAME_OVER_STATUS);
        Bukkit.getScheduler().runTaskLater(SurvivalGames.getInstance(), new Runnable() {
            @Override
            public void run() {
                if (Core.getNetworkManager() != null) {
                    for (CPlayer cPlayer : Core.getOnlinePlayers()) {
                        try {
                            ServerHelper.getLobbyServer(cPlayer.getSettingValue("vip_server", Boolean.class, false) && cPlayer.hasPermission("tbnr.vip"))
                                    .sendPlayerToServer(cPlayer);
                        } catch (Exception e) {
                            cPlayer.getBukkitPlayer().kickPlayer("Unable to send you back to the lobby! Please reconnect!");
                        }
                    }
                }
            }
        }, 200L);
        Bukkit.getScheduler().runTaskLater(SurvivalGames.getInstance(), new Runnable() {
            @Override
            public void run() {
                Bukkit.shutdown();
            }
        }, 220L);
    }

    @Override
    public void onPlayerLogin(CPlayer player, InetAddress address) throws CPlayerJoinException {
        if (Core.getOnlinePlayers().size() >= SurvivalGames.getInstance().getConfig().getInt("max-players")){
            //TODO priority join
            throw new CPlayerJoinException("The server is full!");
        }
    }

    @Override
    public void onPlayerDisconnect(CPlayer player) {
        if (runningGame == null) votingSession.removeVoteFor(player);
        else {
            runningGame.removeTribute(player);
            runningGame.checkForWin();
        }
    }

    private void sendMapBlock(CPlayer player) {
        player.sendMessage(SurvivalGames.getInstance().getFormat("lobby-message", new String[]{"<seconds>", String.valueOf(gameTimer.getLength() - gameTimer.getSecondsPassed())}));
        player.sendMessage(SurvivalGames.getInstance().getFormat("voting-options.header"));
        for (SGMap sgMap : votingSession.getMapSelection()) {
            player.sendMessage(SurvivalGames.getInstance().getFormat("voting-options.map-line",
                    new String[]{"<name>", sgMap.getName()},
                    new String[]{"<votes>", String.valueOf(votingSession.getVotesFor(sgMap))},
                    new String[]{"<n>", String.valueOf(votingSession.getNumberFor(sgMap))}
            ));
        }
    }

    private class GameStartTimer implements TimerDelegate {
        private final Integer[] broadcastSeconds = new Integer[]{60, 30, 10, 9, 8, 7, 6, 5, 4, 3, 2, 1};

        @Override
        public void countdownStarted(Timer timer, Integer totalSeconds) {
            handleDisplay(totalSeconds);
        }

        @Override
        public void countdownEnded(Timer timer, Integer totalSeconds) {
            if (Core.getOnlinePlayers().size() < SurvivalGames.getInstance().getConfig().getInt("min-players", 12)) {
                String format = SurvivalGames.getInstance().getFormat("fail-start");
                for (CPlayer cPlayer : Core.getOnlinePlayers()) {
                    cPlayer.sendMessage(format);
                    cPlayer.playSoundForPlayer(Sound.CLICK);
                }
                startTimer();
            } else {
                beginGame();
            }
        }

        @Override
        public void countdownChanged(Timer timer, Integer secondsPassed, Integer totalSeconds) {
            handleDisplay(totalSeconds-secondsPassed);
        }

        private void handleDisplay(Integer second) {
            if (RandomUtils.contains(broadcastSeconds, second)) {
                String format = SurvivalGames.getInstance().getFormat("lobby-message", new String[]{"<seconds>", String.valueOf(second)});
                for (CPlayer cPlayer : Core.getOnlinePlayers()) {
                    cPlayer.sendMessage(format);
                    cPlayer.playSoundForPlayer(Sound.ORB_PICKUP);
                }
            }
            if (second % 20 == 0) {
                for (CPlayer player : Core.getOnlinePlayers()) {
                    sendMapBlock(player);
                }
            }
        }
    }
}
