package net.tbnr.dev.setting;

import lombok.Data;
import lombok.EqualsAndHashCode;
import net.cogzmc.core.player.CPlayer;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

@EqualsAndHashCode(callSuper = true)
@Data
public final class SettingChangeEvent extends Event {
    private static final HandlerList handlers = new HandlerList();

    private final CPlayer player;
    private final PlayerSetting setting;
    private final Boolean value;

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
