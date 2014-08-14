package net.tbnr.dev.sg.game.deathperks;

import lombok.Data;
import net.cogzmc.core.effect.npc.ClickAction;
import net.cogzmc.core.gui.InventoryButton;
import net.cogzmc.core.gui.InventoryGraphicalInterface;
import net.cogzmc.core.modular.command.EmptyHandlerException;
import net.cogzmc.core.player.CPlayer;
import net.cogzmc.core.player.DatabaseConnectException;
import net.tbnr.dev.PassManager;
import net.tbnr.dev.sg.SurvivalGames;
import net.tbnr.dev.sg.game.GameManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.SkullType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Map;
import java.util.WeakHashMap;

@Data
public final class DeathPerkManager {
    private final Perk[] perks = Perk.values();
    private final GameManager gameManager;
    private final Map<CPlayer, DeathPerk> chosenPerks = new WeakHashMap<>();
    private static final Integer DEFAULT_PASSES = 3;
    private final Map<CPlayer, InventoryGraphicalInterface> deathPerkInterfaces = new WeakHashMap<>();
    private boolean locked = false;

    public void setLocked(boolean value) {
        if (locked) {
            for (Player player : Bukkit.getOnlinePlayers()) {
                player.closeInventory();
            }
        }
        locked = value;
    }

    public DeathPerk get(CPlayer player) {
        return chosenPerks.get(player);
    }

    private InventoryGraphicalInterface getNewInventoryInterface(CPlayer player) {
        InventoryGraphicalInterface graphicalInterface = new InventoryGraphicalInterface(27, ChatColor.DARK_GRAY + ChatColor.BOLD.toString() + "Death Perks");
        for (Perk perk : perks) {
            graphicalInterface.addButton(new DeathPerkButton(player, perk.getPerk()));
        }
        return graphicalInterface;
    }

    public InventoryGraphicalInterface getInterfaceFor(CPlayer player) {
        InventoryGraphicalInterface graphicalInterface = deathPerkInterfaces.get(player);
        if (graphicalInterface == null) {
            graphicalInterface = getNewInventoryInterface(player);
            deathPerkInterfaces.put(player, graphicalInterface);
        }
        return graphicalInterface;
    }

    private ItemStack getStackForPerk(CPlayer player, DeathPerk perk) {
        ItemStack stack = new ItemStack(Material.SKULL_ITEM);
        stack.setDurability((short) SkullType.SKELETON.ordinal());
        ItemMeta itemMeta = stack.getItemMeta();
        DeathPerk deathPerk = get(player);
        itemMeta.setDisplayName(ChatColor.GRAY + (deathPerk.equals(perk) ? ChatColor.BOLD.toString() : "") + deathPerk.getName());
        itemMeta.setLore(new ArrayList<String>());
        for (String s : perk.getDescription()) {
            itemMeta.getLore().add(ChatColor.DARK_GRAY + s);
        }
        Integer countFor = getCountFor(player, perk);
        itemMeta.getLore().add("\n" + ChatColor.GREEN + "You have " + (countFor == -1 ? "unlimited" : countFor.toString()) + " passes.");
        if (deathPerk.equals(perk)) itemMeta.getLore().add(ChatColor.DARK_GREEN + "You have selected this perk!");
        stack.setItemMeta(itemMeta);
        stack.setAmount(countFor <= 0 ? 1 : countFor);
        return stack;
    }

    private Integer getCountFor(CPlayer player, DeathPerk perk) {
        Integer passesForClass = PassManager.getPassesForClass(perk.getClass().getName(), player);
        if (player.hasPermission("survivalgames.unlimitedpasses")) return -1;
        else if (passesForClass == 0 && !player.getSettingValue("given_default_" + perk.getClass().getName(), Boolean.class, false)) {
            passesForClass = DEFAULT_PASSES;
            try {
                PassManager.setPassesForClass(DEFAULT_PASSES, perk.getClass().getName(), player);
            } catch (DatabaseConnectException e) {
                return 0;
            }
        }
        return passesForClass;
    }

    public void onUse(DeathPerk deathPerk, CPlayer player) throws DatabaseConnectException {
        Integer countFor = getCountFor(player, deathPerk);
        if (countFor <= 0) return;
        PassManager.setPassesForClass(countFor-1, deathPerk.getClass().getName(), player);
    }

    private class DeathPerkButton extends InventoryButton {
        private final DeathPerk perk;

        public DeathPerkButton(CPlayer player, DeathPerk perk) {
            super(getStackForPerk(player, perk));
            this.perk = perk;
        }

        @Override
        protected void onPlayerClick(CPlayer player, ClickAction action) throws EmptyHandlerException {
            DeathPerk deathPerk = get(player);
            if (deathPerk.equals(perk)) return;
            if (getCountFor(player, perk) == 0) {
                player.sendMessage(SurvivalGames.getInstance().getFormat("no-credits"));
            }
            InventoryGraphicalInterface interfaceFor = getInterfaceFor(player);
            for (InventoryButton inventoryButton : interfaceFor.getButtons()) {
                if (((DeathPerkButton) inventoryButton).perk.equals(deathPerk)) {
                    interfaceFor.markForUpdate(inventoryButton);
                    break;
                }
            }
            interfaceFor.markForUpdate(this);
            chosenPerks.put(player, perk);
            player.sendMessage(SurvivalGames.getInstance().getFormat("chosen-perk", new String[]{"<perk>", perk.getName()}));
            interfaceFor.updateInventory();
        }
    }


    private static enum Perk {
        LET_IT_RIP(new LetItRip()),
        LAST_STAND(new LastStand());

        public DeathPerk getPerk() {
            return perk;
        }

        private final DeathPerk perk;

        Perk(DeathPerk perk) {
            this.perk = perk;
        }
    }
}
