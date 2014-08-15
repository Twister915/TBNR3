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
import java.util.List;
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
        graphicalInterface.updateInventory();
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
        itemMeta.setDisplayName(ChatColor.GRAY + (deathPerk != null && deathPerk.equals(perk) ? ChatColor.BOLD.toString() : "") + perk.getName());
        List<String> lore = new ArrayList<>();
        for (String s : perk.getDescription()) {
            lore.add(ChatColor.DARK_GRAY + s);
        }
        Integer countFor = getCountFor(player, perk);
        lore.add("");
        if (countFor != 0) lore.add(ChatColor.GREEN + "You have " + (countFor == -1 ? "unlimited" : countFor.toString()) + " passes.");
        else {
            lore.add(ChatColor.RED + "You have no passes for this perk");
            lore.add(ChatColor.RED + "Please purchase them on our store");
        }
        if (deathPerk != null && deathPerk.equals(perk)) lore.add(ChatColor.DARK_GREEN + "You have selected this perk!");
        else if (deathPerk != null) lore.add(ChatColor.GREEN + "You have " + deathPerk.getName() + " selected!");
        itemMeta.setLore(lore);
        stack.setItemMeta(itemMeta);
        stack.setAmount(countFor <= 0 ? 1 : countFor);
        return stack;
    }

    private Integer getCountFor(CPlayer player, DeathPerk perk) {
        Integer passesForClass = PassManager.getPassesForClass(perk.getClass().getName(), player);
        if (player.hasPermission("survivalgames.unlimitedpasses")) return -1;
        else if (passesForClass == 0 && !player.getSettingValue("given_default_" + perk.getClass().getName().replaceAll("\\.", ""), Boolean.class, false)) {
            passesForClass = getDefaultPassesFor(player);
            try {
                PassManager.setPassesForClass(passesForClass, perk.getClass().getName(), player);
            } catch (DatabaseConnectException e) {
                return 0;
            }
            player.storeSettingValue("given_default_" + perk.getClass().getName().replaceAll("\\.", ""), true);
        }
        return passesForClass;
    }

    protected Integer getDefaultPassesFor(CPlayer player) {
        for (int x = 0; x < 30; x += 5) {
            if (player.hasPermission("survivalgames.defaultpasses." + x)) return x;
        }
        return DEFAULT_PASSES;
    }

    public void onUse(DeathPerk deathPerk, CPlayer player) throws DatabaseConnectException {
        Integer countFor = getCountFor(player, deathPerk);
        if (countFor <= 0) return;
        PassManager.setPassesForClass(countFor-1, deathPerk.getClass().getName(), player);
    }

    public void unset(CPlayer player) {
        chosenPerks.remove(player);
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
            if (deathPerk != null && deathPerk.equals(perk)) return;
            if (getCountFor(player, perk) == 0) {
                player.sendMessage(SurvivalGames.getInstance().getFormat("no-credits"));
                return;
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
