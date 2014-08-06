package net.tbnr.dev.effects;

import net.cogzmc.core.Core;
import net.tbnr.dev.TBNRHub;
import org.bukkit.event.Listener;

/**
 * <p/>
 * Latest Change: 04/08/2014.
 * <p/>
 *
 * @author Noy
 * @since 04/08/2014.
 */
public abstract class ModuleListener implements Listener {

    private final String configString;

    public ModuleListener(String configString) {
        this.configString = configString;
    }

    public final boolean enable() {
        Core.logDebug("Enabling listener " + getClass().getSimpleName());
        if (!TBNRHub.getInstance().getConfig().getBoolean("listeners-enable." + configString)) return false;
        Core.logDebug("Enabled listener " + getClass().getSimpleName());
        TBNRHub.getInstance().registerListener(this);
        return true;
    }

}
