package net.tbnr.dev.inventory.player;

import net.cogzmc.core.Core;
import net.cogzmc.core.effect.npc.ClickAction;
import net.cogzmc.core.gui.InventoryButton;
import net.cogzmc.core.gui.InventoryGraphicalInterface;
import net.cogzmc.core.modular.command.EmptyHandlerException;
import net.cogzmc.core.network.NetworkServer;
import net.cogzmc.core.player.CPlayer;
import net.cogzmc.core.player.CooldownUnexpiredException;
import net.cogzmc.hub.Hub;
import net.tbnr.dev.ServerHelper;
import net.tbnr.dev.TBNRHub;
import net.tbnr.dev.ControlledInventory;
import net.tbnr.dev.ControlledInventoryButton;
import net.tbnr.dev.setting.PlayerSetting;
import net.tbnr.dev.setting.SettingChangeEvent;
import net.tbnr.dev.signs.ServerSignMatrix;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.TimeUnit;

public final class PlayerInventory extends ControlledInventory implements Listener {
    private final static PlayerSetting[] perks = new PlayerSetting[]{PlayerSetting.FLY_IN_HUB};

    public PlayerInventory() {
        Bukkit.getPluginManager().registerEvents(this, TBNRHub.getInstance());
    }

    private final InventoryGraphicalInterface warpStarMenu = getNewWarpMenu();
    private final InventoryGraphicalInterface lobbyChooser = getNewLobbyChooser();

    {
        if (Core.getNetworkManager() != null) {
            Bukkit.getScheduler().runTaskTimer(TBNRHub.getInstance(), new Runnable() {
                @Override
                public void run() {
                    for (InventoryButton inventoryButton : lobbyChooser.getButtons()) {
                        lobbyChooser.removeButton(inventoryButton);
                    }
                    List<NetworkServer> lobbyServers = ServerHelper.getLobbyServers();
                    Collections.sort(lobbyServers, new Comparator<NetworkServer>() {
                        @Override
                        public int compare(NetworkServer o1, NetworkServer o2) {
                            return ServerSignMatrix.getServerNumber(o1) - ServerSignMatrix.getServerNumber(o2);
                        }
                    });
                    for (NetworkServer networkServer : lobbyServers) {
                        lobbyChooser.addButton(new LobbyButton(networkServer));
                    }
                    lobbyChooser.updateInventory();
                }
            }, 40L, 40L);
        }
    }

    private InventoryGraphicalInterface getNewWarpMenu() {
        InventoryGraphicalInterface graphicalInterface = new InventoryGraphicalInterface(9, ChatColor.GRAY + ChatColor.BOLD.toString() + "Warp Star");
        for (Warp warp : TBNRHub.getInstance().getWarpRepository()) {
            graphicalInterface.addButton(new WarpStarButton(warp), warp.getPosition());
        }
        graphicalInterface.updateInventory();
        return graphicalInterface;
    }

    private InventoryGraphicalInterface getPerkMenuFor(CPlayer player) {
        InventoryGraphicalInterface graphicalInterface = new InventoryGraphicalInterface(9, ChatColor.DARK_GRAY + "Perk Menu - Perks Coming Soon");
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
                        itemMeta.setDisplayName(ChatColor.GRAY  + ChatColor.BOLD.toString() + "Warp Star");
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
            case 2:
                //Hide players
                return new ToggleItem(PlayerSetting.PLAYERS);
            case 0:
                //Toggle jump boost
                return new ControlledInventoryButton() {
                    @Override
                    protected ItemStack getStack(CPlayer player) {
                        ItemStack itemStack = new ItemStack(Material.WOOL);
                        itemStack.setDurability((short)3);
                        ItemMeta itemMeta = itemStack.getItemMeta();
                        NetworkServer thisServer = Core.getNetworkManager().getThisServer();
                        Integer serverNumber = ServerSignMatrix.getServerNumber(thisServer);
                        itemMeta.setDisplayName(ChatColor.GRAY + ChatColor.BOLD.toString() + "Lobby Selector " + ChatColor.GREEN + "- " + ChatColor.GRAY + ChatColor.BOLD.toString() + "Lobby #" + serverNumber);
                        itemStack.setItemMeta(itemMeta);
                        return itemStack;
                    }

                    @Override
                    protected void onUse(CPlayer player) {
                        lobbyChooser.open(player);
                    }
                };
            case 6:
                //Perk Menu
                return new ControlledInventoryButton() {
                    @Override
                    protected ItemStack getStack(CPlayer player) {
                        ItemStack stack = new ItemStack(Material.CHEST);
                        ItemMeta itemMeta = stack.getItemMeta();
                        itemMeta.setDisplayName(ChatColor.GRAY + ChatColor.BOLD.toString() + "Perk Menu");
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
                        ItemStack itemStack = new ItemStack(Material.ARROW);
                        ItemMeta itemMeta = itemStack.getItemMeta();
                        itemMeta.setDisplayName(ChatColor.GRAY + ChatColor.BOLD.toString() + "Return to Spawn");
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

    public InventoryGraphicalInterface getNewLobbyChooser() {
        return new InventoryGraphicalInterface(9, "Lobbies");
    }

    private class LobbyButton extends InventoryButton {
        private final NetworkServer server;

        private LobbyButton(NetworkServer server) {
            super(stackFor(server));
            this.server = server;
        }

        @Override
        protected void onPlayerClick(CPlayer player, ClickAction action) throws EmptyHandlerException {
            try {
                player.getCooldownManager().testCooldown("server_telepeport" + server.hashCode(), 1L, TimeUnit.SECONDS);
            } catch (CooldownUnexpiredException e) {
                return;
            }
            if (Core.getNetworkManager().getThisServer().equals(server)) return;
            else server.sendPlayerToServer(player);
        }
    }

    private static ItemStack stackFor(NetworkServer server) {
        byte data;
        boolean isThisServer = server.equals(Core.getNetworkManager().getThisServer());
        if (isThisServer) data = 4;
        else data = 5;
        ItemStack stack = new ItemStack(Material.STAINED_GLASS);
        ItemMeta itemMeta = stack.getItemMeta();
        itemMeta.setDisplayName(ChatColor.GREEN + (isThisServer ? ChatColor.BOLD.toString() : "") + "Lobby #" + ServerSignMatrix.getServerNumber(server));
        itemMeta.setLore(Arrays.asList(ChatColor.GREEN + (isThisServer ? "You are on this server" : "Click to connect!"), ChatColor.GREEN + "There are " + ChatColor.RED + server.getOnlineCount() + ChatColor.GREEN + " on this server."));
        stack.setItemMeta(itemMeta);
        stack.setDurability(data);
        return stack;
    }
}
