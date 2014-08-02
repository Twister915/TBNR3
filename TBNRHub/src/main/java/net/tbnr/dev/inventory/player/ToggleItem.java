package net.tbnr.dev.inventory.player;

import com.google.api.client.repackaged.com.google.common.base.Joiner;
import lombok.AccessLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import net.cogzmc.core.player.CPlayer;
import net.cogzmc.core.player.message.FHoverActionType;
import net.cogzmc.core.player.message.FancyMessage;
import net.tbnr.dev.TBNRHub;
import net.tbnr.dev.inventory.HubInventoryButton;
import net.tbnr.dev.setting.PlayerSetting;
import net.tbnr.dev.setting.PlayerSettingsManager;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
@Getter(AccessLevel.NONE)
public abstract class ToggleItem extends HubInventoryButton {
    protected final PlayerSetting setting;

    public abstract String getName();
    public abstract List<String> getDescription();

    @Override
    protected ItemStack getStack(CPlayer player) {
        boolean state = TBNRHub.getInstance().getSettingsManager().getStateFor(setting, player);
        ItemStack itemStack = new ItemStack(Material.INK_SACK);
        itemStack.setDurability(state ? (short) 10 : (short) 8);
        ItemMeta itemMeta = itemStack.getItemMeta();
        itemMeta.setDisplayName((state ?
                (ChatColor.GREEN.toString() + ChatColor.BOLD + "ON") :
                (ChatColor.RED.toString() + ChatColor.BOLD + "OFF"))
                + " " + ChatColor.GRAY + getName());
        itemMeta.setLore(getDescription());
        itemStack.setItemMeta(itemMeta);
        return itemStack;
    }

    protected boolean canUse(CPlayer player) {return true;}

    @Override
    protected final void onUse(CPlayer player) {
        if (!canUse(player)) {
            player.playSoundForPlayer(Sound.NOTE_PIANO, 1f, 0.75f);
            player.sendMessage(TBNRHub.getInstance().getFormat("cannot-use-toggle"));
            return;
        }
        PlayerSettingsManager settingsManager = TBNRHub.getInstance().getSettingsManager();
        settingsManager.toggleStateFor(setting, player);
        boolean stateFor = settingsManager.getStateFor(setting, player);
        player.playSoundForPlayer(Sound.NOTE_PLING, 1f, stateFor ? 1.2F : 0.8F);
        StringBuilder builder = new StringBuilder(getName().toLowerCase());
        builder.setCharAt(0, Character.toUpperCase(builder.charAt(0)));
        String name = builder.toString();
        FancyMessage.start(">>").bold().color(ChatColor.GREEN).done()
                .addMore(" " + (stateFor ? "ON" : "OFF")).color((stateFor ? ChatColor.GREEN : ChatColor.RED)).bold().done()
                .addMore(" " + name).withHoverAction(FHoverActionType.SHOW_TEXT, Joiner.on("\n").join(getDescription())).color(ChatColor.GRAY).done()
                .complete().sendTo(player);
    }
}
