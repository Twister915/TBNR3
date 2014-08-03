package net.tbnr.dev.inventory.player;

import net.cogzmc.core.gui.InventoryButton;
import net.cogzmc.core.gui.InventoryGraphicalInterface;
import net.cogzmc.core.modular.command.EmptyHandlerException;
import net.cogzmc.core.player.CPlayer;
import net.cogzmc.core.player.CooldownUnexpiredException;
import net.tbnr.dev.TBNRHub;
import net.tbnr.dev.inventory.SettingUtils;
import net.tbnr.dev.setting.PlayerSetting;
import net.tbnr.dev.setting.PlayerSettingsManager;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.concurrent.TimeUnit;

public final class PerkButton extends InventoryButton {
    private final PlayerSetting setting;
    private final InventoryGraphicalInterface inventory;

    public PerkButton(PlayerSetting setting, CPlayer player, InventoryGraphicalInterface inv) {
        super(getStackFor(setting, player));
        this.setting = setting;
        this.inventory = inv;
    }

    private static ItemStack getStackFor(PlayerSetting setting, CPlayer player) {
        Boolean state = TBNRHub.getInstance().getSettingsManager().getStateFor(setting, player);
        ItemStack itemStack = new ItemStack(Material.INK_SACK);
        itemStack.setDurability(state ? (short) 10 : (short) 8);
        ItemMeta itemMeta = itemStack.getItemMeta();
        itemMeta.setDisplayName((state ? ChatColor.GREEN : ChatColor.RED).toString() + ChatColor.BOLD + setting.getName());
        itemMeta.setLore(setting.getDescription());
        itemStack.setItemMeta(itemMeta);
        return itemStack;
    }

    @Override
    protected void onPlayerClick(CPlayer player) throws EmptyHandlerException {
        try {
            player.getCooldownManager().testCooldown(setting.name() + "_perk", 1L, TimeUnit.SECONDS);
        } catch (CooldownUnexpiredException e) {
            return;
        }
        if (setting.getPermission() != null && !player.hasPermission(setting.getPermission())) {
            SettingUtils.onSettingDeny(player);
            return;
        }
        PlayerSettingsManager settingsManager = TBNRHub.getInstance().getSettingsManager();
        settingsManager.toggleStateFor(setting, player);
        setStack(getStackFor(setting, player));
        inventory.markForUpdate(this);
        inventory.updateInventory();
        SettingUtils.onSettingToggle(player, setting);
    }
}
