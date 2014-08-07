package net.tbnr.dev.sg.game;

import lombok.Getter;
import net.cogzmc.core.Core;
import net.cogzmc.core.player.CPlayer;
import net.tbnr.dev.ServerHelper;
import net.tbnr.dev.sg.SurvivalGames;
import net.tbnr.dev.sg.game.map.SGMap;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public final class GameManager implements Listener {
    @Getter private SGGame runningGame = null;

    public GameManager() {
        SurvivalGames.getInstance().registerListener(this);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        //TODO
    }

    void startGame() {
        SGMap arena = getArena();
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

    public SGMap getArena() {
        return null;
    }
}
