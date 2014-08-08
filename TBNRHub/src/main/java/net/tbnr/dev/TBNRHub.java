package net.tbnr.dev;

import lombok.Getter;
import net.cogzmc.core.Core;
import net.cogzmc.core.modular.ModularPlugin;
import net.cogzmc.core.modular.ModuleMeta;
import net.cogzmc.core.player.mongo.CMongoDatabase;
import net.tbnr.dev.commands.AddWarpCommand;
import net.tbnr.dev.commands.ClearChatCommand;
import net.tbnr.dev.effects.AntiLeafDecay;
import net.tbnr.dev.effects.BouncyPads;
import net.tbnr.dev.effects.HeightTracker;
import net.tbnr.dev.effects.PlayerGate;
import net.tbnr.dev.inventory.player.PlayerInventory;
import net.tbnr.dev.inventory.player.WarpMongoRepository;
import net.tbnr.dev.parkour.ParkourCommand;
import net.tbnr.dev.parkour.ParkourManager;
import net.tbnr.dev.setting.HidePlayerListener;
import net.tbnr.dev.setting.PlayerSettingsManager;
import net.tbnr.dev.signs.ServerSignMatrixManager;
import net.tbnr.dev.signs.SignSetupCommand;
import net.tbnr.dev.spawn.SetSpawnCommand;
import net.tbnr.dev.spawn.SpawnCommand;
import net.tbnr.dev.spawn.SpawnManager;
import org.bukkit.Bukkit;

@ModuleMeta(description = "TBNR's Hub Plugin!", name = "TBNRHub")
public final class TBNRHub extends ModularPlugin {
    @Getter private static TBNRHub instance;
    @Getter private PlayerSettingsManager settingsManager;
    @Getter private PlayerInventory playerInventory;
    @Getter private ParkourManager parkourManager;
    @Getter private WarpMongoRepository warpRepository;
    @Getter private ServerSignMatrixManager matrixManager;
    @Getter private SpawnManager spawnManager;

    @Override
    protected void onModuleEnable() throws Exception {
        instance = this;
        if (Core.getPlayerManager().getGeoIPManager() != null) NetworkMappedTime.enable();
        settingsManager = new PlayerSettingsManager();
        parkourManager = new ParkourManager();
        warpRepository = new WarpMongoRepository((CMongoDatabase) Core.getInstance().getCDatabase());
        warpRepository.reloadWarps();
        playerInventory = new PlayerInventory();
        matrixManager = new ServerSignMatrixManager((CMongoDatabase) Core.getInstance().getCDatabase());
        matrixManager.reload();
        spawnManager = new SpawnManager(Bukkit.getWorlds().get(0), (CMongoDatabase) Core.getInstance().getCDatabase());
        registerAllCommands();
        registerAllListeners();
    }

    @Override
    protected void onModuleDisable() throws Exception {
        parkourManager.save();
        spawnManager.onDisable();
    }

    private void registerAllCommands() {
        registerCommand(new ClearChatCommand());
        registerCommand(new ParkourCommand());
        registerCommand(new AddWarpCommand());
        registerCommand(new SignSetupCommand());
        registerCommand(new SpawnCommand());
        registerCommand(new SetSpawnCommand());
    }

    private void registerAllListeners() {
        new BouncyPads().enable();
        new AntiLeafDecay().enable();
        PlayerGate.enable();
        HeightTracker.enable();
        HidePlayerListener.enable();
    }
}