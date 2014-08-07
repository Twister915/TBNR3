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
import net.tbnr.dev.sg.game.map.SGMap;
import net.tbnr.dev.sg.game.util.Timer;
import net.tbnr.dev.sg.game.util.TimerDelegate;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.net.InetAddress;
import java.util.Iterator;

public final class GameManager implements Listener, CPlayerConnectionListener {
    @Getter private SGGame runningGame = null;
    @Getter public PreGameLobby preGameLobby;
    private final PreGameListener listener;
    private Iterator<Point> spawnPoints;
    @Getter private final VotingSession votingSession;

    public GameManager() {
        SurvivalGames.getInstance().registerListener(this);
        preGameLobby = SurvivalGames.getInstance().getMapManager().getPreGameLobby();
        listener = SurvivalGames.getInstance().registerListener(new PreGameListener());
        spawnPoints = preGameLobby.getSpawnPoints().iterator();
        votingSession = new VotingSession(SurvivalGames.getInstance().getMapManager().getRandomMaps(5));
        startTimer();
    }

    private void startTimer() {
        new Timer(60, new GameStartTimer());
    }

    private Point getNextSpawnPoint() {
        if (!spawnPoints.hasNext()) spawnPoints = preGameLobby.getSpawnPoints().iterator();
        return spawnPoints.next();
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        event.getPlayer().teleport(getNextSpawnPoint().getLocation(preGameLobby.getMap().getWorld()));
        event.setJoinMessage(SurvivalGames.getInstance().getFormat("join-message", new String[]{"<player>", Core.getOnlinePlayer(event.getPlayer()).getDisplayName()}));
    }

    void beginGame() {
        beginGame(votingSession.getMostVotedFor());
    }

    void beginGame(final SGMap map) {
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
    }

    void gameEnded() {
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

    }

    @Override
    public void onPlayerDisconnect(CPlayer player) {
        votingSession.removeVoteFor(player);
    }

    private class GameStartTimer implements TimerDelegate {
        private final Integer[] broadcastSeconds = new Integer[]{60, 30, 10, 9, 8, 7, 6, 5, 4, 3, 2, 1};

        @Override
        public void countdownStarted(Timer timer, Integer totalSeconds) {
            handleDisplay(totalSeconds);
        }

        @Override
        public void countdownEnded(Timer timer, Integer totalSeconds) {
            if (Core.getOnlinePlayers().size() > SurvivalGames.getInstance().getConfig().getInt("min-players", 12)) {
                String format = SurvivalGames.getInstance().getFormat("fail-start");
                for (CPlayer cPlayer : Core.getOnlinePlayers()) {
                    cPlayer.sendMessage(format);
                    cPlayer.playSoundForPlayer(Sound.CLICK);
                    startTimer();
                }
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
                String format = SurvivalGames.getInstance().getFormat("lobby-format", new String[]{"<serconds>", String.valueOf(second)});
                for (CPlayer cPlayer : Core.getOnlinePlayers()) {
                    cPlayer.sendMessage(format);
                    cPlayer.playSoundForPlayer(Sound.ORB_PICKUP);
                }
            }
        }
    }
}
