package net.tbnr.dev.inventory.player;

import net.cogzmc.core.gui.InventoryButton;
import net.cogzmc.core.gui.InventoryGraphicalInterface;
import net.cogzmc.core.player.CPlayer;
import net.cogzmc.hub.Hub;
import net.tbnr.dev.TBNRHub;
import net.tbnr.dev.ControlledInventory;
import net.tbnr.dev.ControlledInventoryButton;
import net.tbnr.dev.setting.PlayerSetting;
import net.tbnr.dev.setting.SettingChangeEvent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public final class PlayerInventory extends ControlledInventory implements Listener {
    private final static PlayerSetting[] perks = new PlayerSetting[]{PlayerSetting.RAINBOW_PARTICLE_EFFECT, PlayerSetting.FLY_IN_HUB};

    public PlayerInventory() {
        Bukkit.getPluginManager().registerEvents(this, TBNRHub.getInstance());
    }

    private final InventoryGraphicalInterface warpStarMenu = getNewWarpMenu();

    {
        Bukkit.getScheduler().runTaskTimer(TBNRHub.getInstance(), new WarpUpdater(), 20L, 20L);
    }

    private InventoryGraphicalInterface getNewWarpMenu() {
        InventoryGraphicalInterface graphicalInterface = new InventoryGraphicalInterface(9, ChatColor.GREEN + "TBNR");
        for (Warp warp : TBNRHub.getInstance().getWarpRepository()) {
            graphicalInterface.addButton(new WarpStarButton(warp));
        }
        graphicalInterface.updateInventory();
        return graphicalInterface;
    }

    private InventoryGraphicalInterface getPerkMenuFor(CPlayer player) {
        InventoryGraphicalInterface graphicalInterface = new InventoryGraphicalInterface(9, ChatColor.GOLD + ChatColor.BOLD.toString() + "Perk Menu");
        for (PlayerSetting perk : perks) {
            graphicalInterface.addButton(new PerkButton(perk, player, graphicalInterface));
        }
        graphicalInterface.updateInventory();
        return graphicalInterface;
    }

    @EventHandler
    public void onSettingChange(SettingChangeEvent event) {
        if (!getPlayers().contains(event.getPlayer())) return;
        updateForPlayer(event.getPlayer());
    }

    @Override
    protected ControlledInventoryButton getNewButtonAt(Integer slot) {
        switch (slot) {
            case 4:
                return new ControlledInventoryButton() {
                    @Override
                    protected ItemStack getStack(CPlayer player) {
                        ItemStack stack = new ItemStack(Material.NETHER_STAR);
                        ItemMeta itemMeta = stack.getItemMeta();
                        itemMeta.setDisplayName(ChatColor.GRAY + "Warp Star");
                        stack.setItemMeta(itemMeta);
                        return stack;
                    }

                    @Override
                    protected void onUse(CPlayer player) {
                        try {
                            warpStarMenu.open(player);
                        } catch (IllegalStateException ignored) {
                        }
                    }
                };
            case 0:
                //Hide players
                return new ToggleItem(PlayerSetting.PLAYERS);
            case 2:
                //Toggle jump boost
                return new ToggleItem(PlayerSetting.JUMP_BOOST);
            case 6:
                //Toggle chat
                return new ControlledInventoryButton() {
                    @Override
                    protected ItemStack getStack(CPlayer player) {
                        ItemStack stack = new ItemStack(Material.INK_SACK);
                        stack.setDurability((short) 14);
                        ItemMeta itemMeta = stack.getItemMeta();
                        itemMeta.setDisplayName(ChatColor.GOLD + "Perk Menu");
                        stack.setItemMeta(itemMeta);
                        return stack;
                    }

                    @Override
                    protected void onUse(CPlayer player) {
                        getPerkMenuFor(player).open(player);
                    }
                };
            case 8:
                //Return to spawn
                return new ControlledInventoryButton() {
                    @Override
                    protected ItemStack getStack(CPlayer player) {
                        ItemStack itemStack = new ItemStack(Material.INK_SACK);
                        itemStack.setDurability((short) 12);
                        ItemMeta itemMeta = itemStack.getItemMeta();
                        itemMeta.setDisplayName(ChatColor.GRAY + "Return to spawn");
                        itemStack.setItemMeta(itemMeta);
                        return itemStack;
                    }

                    @Override
                    protected void onUse(CPlayer player) {
                        TBNRHub.getInstance().getSpawnManager().teleportToSpawn(player);
                    }
                };
        }
        return null;
    }

    private class WarpUpdater implements Runnable {
        @Override
        public void run() {
            for (InventoryButton inventoryButton : warpStarMenu.getButtons()) {
                ((WarpStarButton)inventoryButton).update();
            }
            warpStarMenu.updateInventory();
        }
    }
}
