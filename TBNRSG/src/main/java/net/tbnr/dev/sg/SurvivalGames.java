package net.tbnr.dev.sg;

import lombok.Getter;
import net.cogzmc.core.Core;
import net.cogzmc.core.modular.ModularPlugin;
import net.cogzmc.core.modular.ModuleMeta;
import net.cogzmc.core.player.mongo.CMongoDatabase;
import net.tbnr.dev.sg.command.SGAdminCommand;
import net.tbnr.dev.sg.game.map.SGMongoMapManager;
import net.tbnr.dev.sg.setup.SGSetupManager;

@ModuleMeta(name = "SurvivalGames", description = "TBNR's SurvivalGames plugin.")
public final class SurvivalGames extends ModularPlugin {
    @Getter private static SurvivalGames instance;
    @Getter private SGMongoMapManager mapManager;
    @Getter private SGSetupManager setupManager;

    @Override
    protected void onModuleEnable() throws Exception {
        instance = this;
        this.mapManager = new SGMongoMapManager((CMongoDatabase) Core.getInstance().getCDatabase());
        mapManager.reloadMaps();
        this.setupManager = new SGSetupManager();
        registerCommand(new SGAdminCommand());
        registerListener(new WorldListener());
    }
}
