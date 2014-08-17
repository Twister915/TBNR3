package net.tbnr.dev.wardrobe;

import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import net.cogzmc.core.player.CPlayer;
import net.cogzmc.core.player.DatabaseConnectException;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.LeatherArmorMeta;

import java.util.HashMap;
import java.util.Map;

@Data
public final class WardrobeState {
    private final CPlayer player;
    @Getter(AccessLevel.NONE) private final Map<ArmorPiece, Color> colors = new HashMap<>();
    private final boolean showingArmor;

    public WardrobeState(CPlayer player) {
        this.player = player;
        for (ArmorPiece armorPiece : ArmorPiece.values()) {
            String settingValue = player.getSettingValue("wardrobe_armor_color_" + armorPiece.name().toLowerCase(), String.class, null);
            if (settingValue == null) colors.put(armorPiece, null);
            else {
                java.awt.Color decode;
                try {
                    decode = java.awt.Color.decode("#" + settingValue.toUpperCase());
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                    colors.put(armorPiece, null);
                    continue;
                }
                colors.put(armorPiece, Color.fromRGB(decode.getRed(), decode.getGreen(), decode.getBlue()));
            }
        }
        showingArmor = player.getSettingValue("wardrobe_armor_display", Boolean.class, false);
    }

    public WardrobeState(CPlayer player, Map<ArmorPiece, Color> colors, boolean showingArmor) {
        this.player = player;
        this.colors.putAll(colors);
        this.showingArmor = showingArmor;
    }

    public Color getColor(ArmorPiece piece) {
        return colors.get(piece);
    }

    public void save() {
        for (ArmorPiece armorPiece : ArmorPiece.values()) {
            Color color = colors.get(armorPiece);
            if (color == null) {
                player.removeSettingValue("wardrobe_armor_color_" + armorPiece.name().toLowerCase());
                continue;
            }
            String s = Integer.toHexString(color.asRGB());
            player.storeSettingValue("wardrobe_armor_color_" + armorPiece.name().toLowerCase(), s);
        }
        player.storeSettingValue("wardrobe_armor_display", showingArmor);
        try {
            player.saveIntoDatabase();
        } catch (DatabaseConnectException e) {
            e.printStackTrace();
        }
    }

    public void display() {
        PlayerInventory inventory = player.getBukkitPlayer().getInventory();
        ItemStack[] itemStacks = new ItemStack[4];
        if (!showingArmor) {
            inventory.setArmorContents(itemStacks);
            return;
        }
        ArmorPiece[] values = ArmorPiece.values();
        for (int i = values.length - 1, n = 0; i >= 0; i--, n++) {
            ItemStack itemStack = new ItemStack(values[i].representing);
            LeatherArmorMeta itemMeta = (LeatherArmorMeta) itemStack.getItemMeta();
            if (colors.containsKey(values[i])) itemMeta.setColor(colors.get(values[i]));
            itemMeta.setDisplayName(ChatColor.GREEN + player.getName() + "'s " + values[i].humanName);
            itemStack.setItemMeta(itemMeta);
            itemStacks[n] = itemStack;
        }
        inventory.setArmorContents(itemStacks);
    }
}
