package net.tbnr.dev.sg;

import lombok.Getter;
import net.cogzmc.core.Core;
import net.cogzmc.core.modular.ModularPlugin;
import net.cogzmc.core.modular.ModuleMeta;
import net.cogzmc.core.player.CPlayer;
import net.cogzmc.core.player.CPlayerConnectionListener;
import net.cogzmc.core.player.CPlayerJoinException;
import net.cogzmc.core.player.mongo.CMongoDatabase;
import net.tbnr.dev.sg.command.SGAdminCommand;
import net.tbnr.dev.sg.game.GameManager;
import net.tbnr.dev.sg.game.map.SGMongoMapManager;
import net.tbnr.dev.sg.setup.SGSetupManager;
import org.bukkit.ChatColor;

import java.net.InetAddress;

@ModuleMeta(name = "SurvivalGames", description = "TBNR's SurvivalGames plugin.")
public final class SurvivalGames extends ModularPlugin {
    @Getter private static SurvivalGames instance;
    @Getter private SGMongoMapManager mapManager;
    @Getter private SGSetupManager setupManager;
    @Getter private GameManager gameManager;
    @Getter private boolean setupOnlyMode = false;

    @Override
    protected void onModuleEnable() throws Exception {
        instance = this;
        this.mapManager = new SGMongoMapManager((CMongoDatabase) Core.getInstance().getCDatabase());
        try {
            mapManager.reloadMaps();
        } catch (Exception e) {
            e.printStackTrace();
            setupOnlyMode = true;
        }
        this.setupManager = new SGSetupManager();
        registerCommand(new SGAdminCommand());
        registerListener(new WorldListener());
        if (!setupOnlyMode) gameManager = new GameManager();
        else Core.getPlayerManager().registerCPlayerConnectionListener(new SetupModeListener());
    }

    private static class SetupModeListener implements CPlayerConnectionListener {
        @Override
        public void onPlayerLogin(CPlayer player, InetAddress address) throws CPlayerJoinException {
            if (!player.getBukkitPlayer().isOp()) throw new CPlayerJoinException(ChatColor.RED + "This server appears to be in setup only mode!");
        }

        @Override
        public void onPlayerDisconnect(CPlayer player) {

        }
    }
}
