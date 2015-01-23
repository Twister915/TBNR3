package net.tbnr.dev.skywars;

import lombok.Getter;
import net.cogzmc.core.modular.ModularPlugin;
import net.cogzmc.core.modular.ModuleMeta;

@ModuleMeta(description = "TBNR Sky Wars", name = "Sky Wars")
public final class SkyWars extends ModularPlugin {
    @Getter private static SkyWars instance;

    @Getter private ArenaManager arenaManager;
    @Getter private GameManager gameManager;

    @Override
    protected void onModuleEnable() throws Exception {
        instance = this;

        arenaManager = new ArenaManager();
        gameManager = new GameManager();
    }
}
