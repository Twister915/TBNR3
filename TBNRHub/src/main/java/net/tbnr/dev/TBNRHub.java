package net.tbnr.dev;

import lombok.Getter;
import net.cogzmc.core.Core;
import net.cogzmc.core.modular.ModularPlugin;
import net.cogzmc.core.modular.ModuleMeta;
import net.tbnr.dev.inventory.player.DonorInventory;
import net.tbnr.dev.inventory.player.PlayerInventory;
import net.tbnr.dev.setting.DisableChatListener;
import net.tbnr.dev.setting.HidePlayerListener;
import net.tbnr.dev.setting.PlayerSettingsManager;

@ModuleMeta(description = "TBNR's Hub Plugin!", name = "TBNRHub")
public final class TBNRHub extends ModularPlugin {
    @Getter private static TBNRHub instance;
    @Getter private PlayerSettingsManager settingsManager;
    @Getter private PlayerInventory playerInventory;
    @Getter private DonorInventory donorInventory;

    @Override
    protected void onModuleEnable() throws Exception {
        instance = this;
        if (Core.getPlayerManager().getGeoIPManager() != null) NetworkMappedTime.enable();
        settingsManager = new PlayerSettingsManager();
        playerInventory = new PlayerInventory();
        donorInventory = new DonorInventory();
        HidePlayerListener.enable();
        DisableChatListener.enable();
        PlayerGate.enable();
    }
}
