package net.tbnr.dev.inventory.warp;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import net.cogzmc.core.gui.InventoryButton;
import net.cogzmc.core.modular.command.EmptyHandlerException;
import net.cogzmc.core.player.CPlayer;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;

@EqualsAndHashCode(callSuper = true)
@Data
public final class WarpStarButton extends InventoryButton {
    private Integer materialIndice = 0;
    @Getter private final Warp warp;
    public WarpStarButton(Warp w) {
        super(stackForWarp(w, 0));
        warp = w;
    }

    private static ItemStack stackForWarp(Warp w, Integer materialIndice) {
        ItemStack stack = new ItemStack(w.getMaterials()[materialIndice % w.getMaterials().length]);
        stack.setDurability(w.getDataValue());
        ItemMeta itemMeta = stack.getItemMeta();
        itemMeta.setDisplayName(w.getName());
        itemMeta.setLore(Arrays.asList(ChatColor.DARK_AQUA + "Click to warp!"));
        stack.setItemMeta(itemMeta);
        return stack;
    }

    public void update() {
        materialIndice = (materialIndice + 1) %  warp.getMaterials().length;
        setStack(stackForWarp(warp, materialIndice));
    }

    @Override
    protected void onPlayerClick(CPlayer player) throws EmptyHandlerException {
        Player bukkitPlayer = player.getBukkitPlayer();
        bukkitPlayer.teleport(warp.getPoint().getLocation(bukkitPlayer.getWorld()));
        player.playSoundForPlayer(Sound.ENDERMAN_TELEPORT, 1f, 0.7f);
        bukkitPlayer.closeInventory();
    }
}
