package net.tbnr.dev.bungee;

import net.cogzmc.bungee.CoreBungeeDriver;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.api.plugin.PluginManager;

public final class TBNRBungee extends Plugin {
    @Override
    public void onEnable() {
        CoreBungeeDriver.getInstance().setController(new TBNRController());
        MaintenanceMode maintenanceMode = new MaintenanceMode();
        MaintenanceMode.MaintenanceCommand maintenanceCommand = maintenanceMode.new MaintenanceCommand();
        PluginManager pluginManager = getProxy().getPluginManager();
        pluginManager.registerCommand(this, maintenanceCommand);
        pluginManager.registerListener(this, maintenanceMode);
    }
}
