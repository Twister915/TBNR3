package net.tbnr.dev.effects;

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
        if (!TBNRHub.getInstance().getConfig().getBoolean("listeners-enable." + configString)) return false;
        TBNRHub.getInstance().registerListener(this);
        return true;
    }

}
