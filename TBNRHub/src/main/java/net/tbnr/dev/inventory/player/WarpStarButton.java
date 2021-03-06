package net.tbnr.dev.inventory.player;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import net.cogzmc.core.effect.npc.ClickAction;
import net.cogzmc.core.gui.InventoryButton;
import net.cogzmc.core.modular.command.EmptyHandlerException;
import net.cogzmc.core.player.CPlayer;
import net.tbnr.dev.TBNRHub;
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
        ItemMeta itemMeta = stack.getItemMeta();
        itemMeta.setDisplayName(w.getName());
        itemMeta.setLore(Arrays.asList(ChatColor.DARK_AQUA + "Click to warp!"));
        stack.setItemMeta(itemMeta);
        return stack;
    }

    public void update() {
        materialIndice++;
        if (materialIndice > warp.getMaterials().length) materialIndice = 0;
        setStack(stackForWarp(warp, materialIndice));
    }

    @Override
    protected void onPlayerClick(CPlayer player, ClickAction action) throws EmptyHandlerException {
        Player bukkitPlayer = player.getBukkitPlayer();
        if (TBNRHub.getInstance().getParkourManager().getParkourFor(player) != null) TBNRHub.getInstance().getParkourManager().getParkourFor(player).cleanupParkour();
        bukkitPlayer.teleport(warp.getPoint().getLocation(bukkitPlayer.getWorld()));
        player.playSoundForPlayer(Sound.ENDERMAN_TELEPORT, 1f, 0.7f);
        bukkitPlayer.closeInventory();
    }
}
