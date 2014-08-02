package net.tbnr.dev.inventory.player;

import net.cogzmc.core.Core;
import net.cogzmc.core.gui.InventoryGraphicalInterface;
import net.cogzmc.core.player.CPlayer;
import net.cogzmc.hub.Hub;
import net.tbnr.dev.TBNRHub;
import net.tbnr.dev.inventory.HubInventory;
import net.tbnr.dev.inventory.HubInventoryButton;
import net.tbnr.dev.setting.PlayerSetting;
import net.tbnr.dev.setting.SettingChangeEvent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.List;

public final class PlayerInventory extends HubInventory implements Listener {
    public PlayerInventory() {
        Bukkit.getPluginManager().registerEvents(this, TBNRHub.getInstance());
    }

    private final InventoryGraphicalInterface warpStarMenu = new InventoryGraphicalInterface(9, ChatColor.GREEN + "TBNR");

    @EventHandler
    public void onSettingChange(SettingChangeEvent event) {
        updateForPlayer(event.getPlayer());
    }

    @Override
    protected HubInventoryButton getNewButtonAt(Integer slot) {
        switch (slot) {
            case 0:
                return new HubInventoryButton() {
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
                        warpStarMenu.open(player);
                    }
                };
            case 2:
                //Hide players
                return new ToggleItem(PlayerSetting.PLAYERS) {
                    @Override
                    public String getName() {
                        return "Players";
                    }

                    @Override
                    public List<String> getDescription() {
                        return Arrays.asList("Hides players (when off) from your view.", "Does not hide staff members.");
                    }
                };
            case 4:
                //Toggle snowball game
                return new ToggleItem(PlayerSetting.SNOWBALL_GAME) {
                    @Override
                    public String getName() {
                        return "Snowball Mini-Game";
                    }

                    @Override
                    public List<String> getDescription() {
                        return Arrays.asList("Disables you from being hit", "or throwing snowballs");
                    }
                };
            case 6:
                //Toggle chat
                return new ToggleItem(PlayerSetting.CHAT) {
                    @Override
                    public String getName() {
                        return "Chat";
                    }

                    @Override
                    public List<String> getDescription() {
                        return Arrays.asList("Mutes the chat when off.", "You will not see any chat messages", "nor can you send any.");
                    }
                };
            case 8:
                //Return to spawn
                return new HubInventoryButton() {
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
                        Hub.getInstance().getSpawnHandler().sendToSpawn(player.getBukkitPlayer());
                    }
                };
        }
        return null;
    }
}
