package net.tbnr.dev.bungee;

import net.cogzmc.bungee.CoreBungeeDriver;
import net.md_5.bungee.api.plugin.Plugin;

public final class TBNRBungee extends Plugin {
    @Override
    public void onEnable() {
        CoreBungeeDriver.getInstance().setController(new TBNRController());
    }
}
