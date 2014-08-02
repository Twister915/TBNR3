package net.tbnr.dev.inventory;

import net.cogzmc.core.player.CPlayer;
import org.bukkit.inventory.ItemStack;

public abstract class HubInventoryButton {
    protected void onUse(CPlayer player) {}
    protected abstract ItemStack getStack(CPlayer player);
}
