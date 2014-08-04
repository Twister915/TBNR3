package net.tbnr.dev;

import lombok.Getter;
import net.cogzmc.core.Core;
import net.cogzmc.core.modular.ModularPlugin;
import net.cogzmc.core.modular.ModuleMeta;
import net.tbnr.dev.inventory.player.PlayerInventory;
import net.tbnr.dev.parkour.ParkourCommand;
import net.tbnr.dev.parkour.ParkourManager;
import net.tbnr.dev.setting.HidePlayerListener;
import net.tbnr.dev.setting.PlayerSettingsManager;
import net.tbnr.dev.snowball.SnowballMinigame;

@ModuleMeta(description = "TBNR's Hub Plugin!", name = "TBNRHub")
public final class TBNRHub extends ModularPlugin {
    @Getter private static TBNRHub instance;
    @Getter private PlayerSettingsManager settingsManager;
    @Getter private PlayerInventory playerInventory;
    @Getter private ParkourManager parkourManager;

    @Override
    protected void onModuleEnable() throws Exception {
        instance = this;
        if (Core.getPlayerManager().getGeoIPManager() != null) NetworkMappedTime.enable();
        settingsManager = new PlayerSettingsManager();
        parkourManager = new ParkourManager();
        registerCommand(new ParkourCommand());
        playerInventory = new PlayerInventory();
        HidePlayerListener.enable();
        PlayerGate.enable();
        SnowballMinigame.enable();
    }

    @Override
    protected void onModuleDisable() throws Exception {
        parkourManager.save();
    }
}
