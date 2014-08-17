package net.tbnr.dev.wardrobe;

import lombok.EqualsAndHashCode;
import net.cogzmc.core.effect.npc.ClickAction;
import net.cogzmc.core.gui.InventoryButton;
import net.cogzmc.core.gui.InventoryGraphicalInterface;
import net.cogzmc.core.modular.command.EmptyHandlerException;
import net.cogzmc.core.player.CPlayer;
import org.bukkit.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;

import java.util.*;

@EqualsAndHashCode(callSuper = false)
public final class WardrobeInventory extends InventoryGraphicalInterface {
    private final static Integer[] saveSlots = {9, 10, 18, 19, 27, 28};
    private final static Integer[] resetSlots = {16, 17, 25, 26, 34, 35};
    private final static Integer colorStart = 46;
    private final static Integer armorStart = 12;
    private final static Integer visibilitySlot = 4;

    private final CPlayer player;
    private ArmorPiece active = ArmorPiece.HAT;
    private Color activateColor = null;
    private Map<ArmorPiece, Color> colors;
    private Map<ArmorPiece, ActiveIndicatorButton> activityButtons = new HashMap<>();
    private Map<ArmorPiece, ArmorButton> armorButtons = new HashMap<>();
    private Set<ColorButton> colorButtons = new HashSet<>();
    private boolean ignoreClose = false;
    private boolean showingArmor;

    public WardrobeInventory(CPlayer player) {
        super(54, ChatColor.GRAY + "Wardrobe");
        this.player = player;
        readFromDatabase();
        ArmorPiece[] armors = ArmorPiece.values();
        for (ArmorPiece armorPiece : armors) {
            activityButtons.put(armorPiece, new ActiveIndicatorButton(armorPiece));
            armorButtons.put(armorPiece, new ArmorButton(armorPiece));
        }
        for (Integer saveSlot : saveSlots) {
            addButton(new StateButton(true), saveSlot);
        }
        for (Integer resetSlot : resetSlots) {
            addButton(new StateButton(false), resetSlot);
        }
        ColorChooser[] choosers = ColorChooser.values();
        for (int s = colorStart, i = 0; i < choosers.length && s < getInventory().getSize(); i++, s++) {
            ColorButton colorButton = new ColorButton(choosers[i]);
            colorButtons.add(colorButton);
            addButton(colorButton, s);
        }
        for (int i = 0; i < armors.length; i++) {
            ArmorPiece piece = armors[i];
            for (int x = armorStart+(i*9), z = 0; z < 3; z++, x++) {
                InventoryButton button = null;
                switch (z) {
                    case 0:
                        button = activityButtons.get(piece);
                        break;
                    case 1:
                        button = armorButtons.get(piece);
                        break;
                    case 2:
                        button = new ClearButton(piece);
                        break;
                }
                if (button == null) continue;
                addButton(button, x);
            }
        }
        addButton(new VisibilityButton(), visibilitySlot);
        for (int i = 0; i < getInventory().getSize(); i++) {
            if (!isFilled(i)) addButton(new BlankButton(), i);
        }
        updateInventory();
    }

    private void readFromDatabase() {
        WardrobeState wardrobeState = new WardrobeState(player);
        colors = new HashMap<>();
        for (ArmorPiece armorPiece : ArmorPiece.values()) {
            colors.put(armorPiece, wardrobeState.getColor(armorPiece));
        }
        showingArmor = wardrobeState.isShowingArmor();
        activateColor = colors.get(active);
    }

    @Override
    public void onClose(CPlayer onlinePlayer) {
        if (ignoreClose) {
            ignoreClose = false;
            return;
        }
        writeToDatabase();
        player.playSoundForPlayer(Sound.CHEST_CLOSE);
    }

    private void writeToDatabase() {
        WardrobeState wardrobeState = new WardrobeState(player, colors, showingArmor);
        wardrobeState.save();
        wardrobeState.display();
    }

    private void setActive(ArmorPiece active) {
        this.active = active;
        this.activateColor = colors.get(active);
        for (ActiveIndicatorButton activeIndicatorButton : activityButtons.values()) {
            activeIndicatorButton.updateStack();
            markForUpdate(activeIndicatorButton);
        }
        for (ColorButton colorButton : colorButtons) {
            colorButton.updateStack();
            markForUpdate(colorButton);
        }
        player.playSoundForPlayer(Sound.NOTE_PIANO);
        updateInventory();
    }

    private void mixColor(ColorChooser color) {
        if (activateColor == null) activateColor = color.dyeColor.getColor();
        else activateColor = activateColor.mixDyes(color.dyeColor);
        colors.put(active, activateColor);
        updatePiece(active);
    }

    private void reset(ArmorPiece piece) {
        if (piece == active) activateColor = null;
        colors.put(piece, null);
        updatePiece(piece);
    }

    private void updatePiece(ArmorPiece piece) {
        ArmorButton armorButton = armorButtons.get(piece);
        armorButton.updateStack();
        ActiveIndicatorButton activeIndicatorButton = activityButtons.get(piece);
        activeIndicatorButton.updateStack();
        markForUpdate(armorButton);
        markForUpdate(activeIndicatorButton);
        updateInventory();
    }

    private enum ColorChooser {
        RED(DyeColor.RED, 14, "Red", ChatColor.RED),
        ORANGE(DyeColor.ORANGE, 1, "Orange", ChatColor.GOLD),
        YELLOW(DyeColor.YELLOW, 4, "Yellow", ChatColor.YELLOW),
        GREEN(DyeColor.GREEN, 5, "Green", ChatColor.GREEN),
        BLUE(DyeColor.BLUE, 3, "Blue", ChatColor.DARK_AQUA),
        INDIGO(DyeColor.MAGENTA, 2, "Indigo", ChatColor.LIGHT_PURPLE),
        VIOLET(DyeColor.PURPLE, 10, "Violet", ChatColor.DARK_PURPLE);

        private final DyeColor dyeColor;
        private final short dataValue;
        private final String humanName;
        private final ChatColor chatColor;

        ColorChooser(DyeColor dyeColor, int dataValue, String name, ChatColor chatColor) {
            this.dyeColor = dyeColor;
            this.dataValue = (short)dataValue;
            this.humanName = name;
            this.chatColor = chatColor;
        }
    }

    private class BlankButton extends InventoryButton {
        public BlankButton() {
            super(null);
            ItemStack itemStack = new ItemStack(Material.STAINED_GLASS_PANE, 1, (short) 0);
            ItemMeta itemMeta = itemStack.getItemMeta();
            itemMeta.setDisplayName(" ");
            itemStack.setItemMeta(itemMeta);
            setStack(itemStack);
        }

        @Override
        protected void onPlayerClick(CPlayer player, ClickAction action) throws EmptyHandlerException {
        }
    }

    @EqualsAndHashCode(callSuper = false)
    private class ActiveIndicatorButton extends InventoryButton {
        private final ArmorPiece representing;

        public ActiveIndicatorButton(ArmorPiece representing) {
            super(null);
            this.representing = representing;
            updateStack();
        }

        public void updateStack() {
            ItemStack stack = new ItemStack(Material.INK_SACK, 1);
            boolean currentlyActive = active == representing;
            stack.setDurability((short) (currentlyActive ? 10 : 8));
            ItemMeta itemMeta = stack.getItemMeta();
            if (currentlyActive) itemMeta.setDisplayName(ChatColor.GREEN + ChatColor.BOLD.toString() + "You are currently editing your " + representing.humanName.toLowerCase() + "!");
            else itemMeta.setDisplayName(ChatColor.GREEN + ChatColor.BOLD.toString() + "Click to start editing your " + representing.humanName.toLowerCase() + "!");
            List<String> lore = new ArrayList<>();
            lore.add("");
            lore.add((colors.get(representing) == null) ? ChatColor.RED + "You have not set a color for this!" : ChatColor.GREEN + "The color you have set for this is #" + Integer.toHexString(colors.get(representing).asRGB()).toUpperCase());
            if (currentlyActive) {
                lore.add("");
                lore.add(ChatColor.GREEN + "Click one of the colors below to dye this armor");
                lore.add("");
                lore.add(ChatColor.GREEN + "Click the sugar to the right of the armor");
                lore.add(ChatColor.GREEN + "  to clear the color.");
            }
            itemMeta.setLore(lore);
            stack.setItemMeta(itemMeta);
            setStack(stack);
        }

        @Override
        protected void onPlayerClick(CPlayer player, ClickAction action) throws EmptyHandlerException {
            if (active == representing) return;
            setActive(representing);
        }
    }

    @EqualsAndHashCode(callSuper = false)
    private class ArmorButton extends InventoryButton {
        private final ArmorPiece representing;

        public ArmorButton(ArmorPiece piece) {
            super(new ItemStack(piece.representing));
            this.representing = piece;
            updateStack();
        }

        private void updateStack() {
            ItemStack stack = getStack();
            LeatherArmorMeta itemMeta = ((LeatherArmorMeta) stack.getItemMeta());
            itemMeta.setDisplayName(ChatColor.GREEN + ChatColor.BOLD.toString() + "Your " + representing.humanName.toLowerCase());
            itemMeta.setColor(colors.get(representing));
            stack.setItemMeta(itemMeta);
        }

        @Override
        protected void onPlayerClick(CPlayer player, ClickAction action) throws EmptyHandlerException {
            if (active == representing) return;
            setActive(representing);
        }
    }

    @EqualsAndHashCode(callSuper = false)
    private class ColorButton extends InventoryButton {
        private final ColorChooser colorToMix;

        public ColorButton(ColorChooser choooser) {
            super(new ItemStack(Material.STAINED_GLASS_PANE));
            this.colorToMix = choooser;
            updateStack();
        }

        private void updateStack() {
            ItemStack stack = getStack();
            stack.setDurability(colorToMix.dataValue);
            ItemMeta itemMeta = stack.getItemMeta();
            itemMeta.setDisplayName(colorToMix.chatColor + ChatColor.BOLD.toString() + colorToMix.humanName);
            itemMeta.setLore(Arrays.asList(ChatColor.GREEN + "Click to mix this color on your " + active.humanName.toLowerCase()));
            stack.setItemMeta(itemMeta);
        }

        @Override
        protected void onPlayerClick(CPlayer player, ClickAction action) throws EmptyHandlerException {
            mixColor(colorToMix);
        }
    }

    @EqualsAndHashCode(callSuper = false)
    private class ClearButton extends InventoryButton {
        private final ArmorPiece representing;

        private ClearButton(ArmorPiece representing) {
            super(new ItemStack(Material.SUGAR));
            ItemStack stack = getStack();
            ItemMeta itemMeta = stack.getItemMeta();
            itemMeta.setDisplayName(ChatColor.GRAY + ChatColor.BOLD.toString() + "Reset your " + representing.humanName.toLowerCase() + "!");
            stack.setItemMeta(itemMeta);
            this.representing = representing;
        }

        @Override
        protected void onPlayerClick(CPlayer player, ClickAction action) throws EmptyHandlerException {
            reset(representing);
            player.playSoundForPlayer(Sound.FIZZ);
        }
    }

    @EqualsAndHashCode(callSuper = false)
    private class StateButton extends InventoryButton {
        private final boolean saves;

        public StateButton(boolean saves) {
            super(new ItemStack(Material.STAINED_GLASS_PANE));
            this.saves = saves;
            ItemStack stack = getStack();
            stack.setDurability((short) (saves ? 5 : 14));
            ItemMeta itemMeta = stack.getItemMeta();
            itemMeta.setDisplayName((saves ? ChatColor.GREEN : ChatColor.RED).toString() + ChatColor.BOLD + "Click to " + (saves ? "save" : "reset"));
            if (!saves) {
                List<String> lore = new ArrayList<>();
                lore.add("");
                lore.add(ChatColor.DARK_RED + ChatColor.BOLD.toString() + ChatColor.UNDERLINE.toString() + "THIS WILL RESET YOUR");
                lore.add(ChatColor.DARK_RED + ChatColor.BOLD.toString() + ChatColor.UNDERLINE.toString() + "RECENT CHANGES USE");
                lore.add(ChatColor.DARK_RED + ChatColor.BOLD.toString() + ChatColor.UNDERLINE.toString() + "WITH GREAT CARE!");
                itemMeta.setLore(lore);
            }
            stack.setItemMeta(itemMeta);
        }

        @Override
        protected void onPlayerClick(CPlayer player, ClickAction action) throws EmptyHandlerException {
            if (saves) {
                writeToDatabase();
                ignoreClose = true;
                player.getBukkitPlayer().closeInventory();
            }
            else {
                readFromDatabase();
                for (ArmorPiece armorPiece : ArmorPiece.values()) {
                    updatePiece(armorPiece);
                }
            }
            player.playSoundForPlayer(Sound.ANVIL_USE);
        }
    }

    private class VisibilityButton extends InventoryButton {
        public VisibilityButton() {
            super(null);
            updateStack();
        }

        private void updateStack() {
            ItemStack itemStack = new ItemStack(showingArmor ? Material.EYE_OF_ENDER : Material.ENDER_PEARL);
            ItemMeta itemMeta = itemStack.getItemMeta();
            itemMeta.setDisplayName((showingArmor ? ChatColor.GREEN : ChatColor.RED) + (showingArmor ? "Showing Armor" : "Hiding Armor"));
            itemStack.setItemMeta(itemMeta);
            setStack(itemStack);
        }

        @Override
        protected void onPlayerClick(CPlayer player, ClickAction action) throws EmptyHandlerException {
            showingArmor = !showingArmor;
            updateStack();
            markForUpdate(this);
            updateInventory();
            player.playSoundForPlayer(Sound.CHICKEN_EGG_POP);
        }
    }
}
