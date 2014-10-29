package net.tbnr.dev.sg.game;

import net.cogzmc.core.effect.inventory.ControlledInventory;
import net.cogzmc.core.effect.inventory.ControlledInventoryButton;
import net.cogzmc.core.player.CPlayer;
import net.tbnr.dev.sg.SurvivalGames;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public final class PreGameInventoryController extends ControlledInventory {
    @Override
    protected ControlledInventoryButton getNewButtonAt(Integer slot) {
        switch (slot) {
            case 0:
                return new ControlledInventoryButton() {
                    @Override
                    protected ItemStack getStack(CPlayer player) {
                        ItemStack bone = new ItemStack(Material.BONE);
                        ItemMeta itemMeta = bone.getItemMeta();
                        itemMeta.setDisplayName(ChatColor.GRAY + ChatColor.BOLD.toString() + "Death Perks");
                        bone.setItemMeta(itemMeta);
                        return bone;
                    }

                    @Override
                    protected void onUse(CPlayer player) {
                        if (SurvivalGames.getInstance().getGameManager().getDeathPerkManager().isLocked()) return;
                        SurvivalGames.getInstance().getGameManager().getDeathPerkManager().getInterfaceFor(player).open(player);
                    }
                };
        }
        return null;
    }
}
