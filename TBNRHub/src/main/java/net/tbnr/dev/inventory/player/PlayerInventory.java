package net.tbnr.dev.inventory.player;

import net.cogzmc.core.Core;
import net.cogzmc.core.effect.inventory.ControlledInventory;
import net.cogzmc.core.effect.inventory.ControlledInventoryButton;
import net.cogzmc.core.effect.npc.ClickAction;
import net.cogzmc.core.gui.InventoryButton;
import net.cogzmc.core.gui.InventoryGraphicalInterface;
import net.cogzmc.core.modular.command.EmptyHandlerException;
import net.cogzmc.core.network.NetworkServer;
import net.cogzmc.core.player.CPlayer;
import net.cogzmc.core.player.CooldownUnexpiredException;
import net.tbnr.dev.ServerHelper;
import net.tbnr.dev.TBNRHub;
import net.tbnr.dev.setting.PlayerSetting;
import net.tbnr.dev.setting.SettingChangeEvent;
import net.tbnr.dev.signs.ServerSignMatrix;
import net.tbnr.dev.wardrobe.WardrobeInventory;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
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
        InventoryGraphicalInterface graphicalInterface = new InventoryGraphicalInterface(9, ChatColor.GRAY + ChatColor.BOLD.toString() + "WARP STAR");
        for (Warp warp : TBNRHub.getInstance().getWarpRepository()) {
            graphicalInterface.addButton(new WarpStarButton(warp), warp.getPosition());
        }
        graphicalInterface.updateInventory();
        return graphicalInterface;
    }

    public InventoryGraphicalInterface getPerkMenuFor(CPlayer player) {
        InventoryGraphicalInterface graphicalInterface = new InventoryGraphicalInterface(9, ChatColor.GRAY + ChatColor.BOLD.toString() + "PERK MENU");
        graphicalInterface.addButton(new PerkButton(PlayerSetting.FLY_IN_HUB, player, graphicalInterface), 0);
        graphicalInterface.addButton(new ParticleButton(player), 4);
        graphicalInterface.addButton(new WardrobeButton(player), 8);
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
                        itemMeta.setDisplayName(ChatColor.GRAY  + ChatColor.BOLD.toString() + "WARP STAR");
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
                if (Core.getNetworkManager() == null) return null;
                return new ControlledInventoryButton() {
                    @Override
                    protected ItemStack getStack(CPlayer player) {
                        ItemStack itemStack = new ItemStack(Material.MAP);
                        ItemMeta itemMeta = itemStack.getItemMeta();
                        itemMeta.setDisplayName(ChatColor.GRAY + ChatColor.BOLD.toString() + "LOBBY SELECTOR");
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
                        itemMeta.setDisplayName(ChatColor.GRAY + ChatColor.BOLD.toString() + "PERK MENU");
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
                        itemMeta.setDisplayName(ChatColor.GRAY + ChatColor.BOLD.toString() + "RETURN TO SPAWN");
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
        return new InventoryGraphicalInterface(9, ChatColor.GREEN + ChatColor.BOLD.toString() + "LOBBIES");
    }

    private class LobbyButton extends InventoryButton {
        private final NetworkServer server;

        private LobbyButton(NetworkServer server) {
            super(stackForServer(server));
            this.server = server;
        }

        @Override
        protected void onPlayerClick(CPlayer player, ClickAction action) throws EmptyHandlerException {
            try {
                player.getCooldownManager().testCooldown("server_telepeport" + server.hashCode(), 1L, TimeUnit.SECONDS);
            } catch (CooldownUnexpiredException e) {
                return;
            }
            if (!Core.getNetworkManager().getThisServer().equals(server)) server.sendPlayerToServer(player);
        }
    }

    private class WardrobeButton extends InventoryButton {
        public WardrobeButton(CPlayer player) {
            super(new ItemStack(Material.LEATHER_HELMET));
            ItemStack stack = getStack();
            ItemMeta itemMeta = stack.getItemMeta();
            itemMeta.setDisplayName(ChatColor.GREEN + ChatColor.BOLD.toString() + "WARDROBE");
            if (!canUse(player)) {
                itemMeta.setLore(Arrays.asList("", ChatColor.RED + "Donate for access to this!"));
            }
            stack.setItemMeta(itemMeta);
        }

        private boolean canUse(CPlayer player) {
            return player.hasPermission("hub.wardrobe");
        }

        @Override
        protected void onPlayerClick(CPlayer player, ClickAction action) throws EmptyHandlerException {
            if (!canUse(player)) {
                player.playSoundForPlayer(Sound.CAT_HISS);
                return;
            }
            WardrobeInventory wardrobeInventory = new WardrobeInventory(player);
            wardrobeInventory.open(player);
            player.playSoundForPlayer(Sound.CHEST_OPEN);
        }
    }

    private class ParticleButton extends InventoryButton {
        public ParticleButton(CPlayer player) {
            super(new ItemStack(Material.FIREWORK));
            ItemStack stack = getStack();
            ItemMeta itemMeta = stack.getItemMeta();
            itemMeta.setDisplayName(ChatColor.GREEN + ChatColor.BOLD.toString() + "PARTICLE PACKS");
            if (!canUse(player)) {
                itemMeta.setLore(Arrays.asList("", ChatColor.RED + "Donate for access to this!", ChatColor.RED + "http://tbnr.net/shop"));
            }
            stack.setItemMeta(itemMeta);
        }

        @Override
        protected void onPlayerClick(CPlayer player, ClickAction action) throws EmptyHandlerException {
            if (!canUse(player)) {
                player.playSoundForPlayer(Sound.CAT_HISS);
                return;
            }
            TBNRHub.getInstance().getParticleEffectManager().getPackChooser(player).open(player);
        }

        private boolean canUse(CPlayer player) {
            return player.hasPermission(PlayerSetting.PARTICLE_EFFECT.getPermission());
        }
    }

    private static ItemStack stackForServer(NetworkServer server) {
        byte data;
        boolean isThisServer = server.equals(Core.getNetworkManager().getThisServer());
        if (isThisServer) data = 4;
        else data = 5;
        ItemStack stack = new ItemStack(Material.STAINED_GLASS);
        ItemMeta itemMeta = stack.getItemMeta();
        itemMeta.setDisplayName(ChatColor.GREEN + ChatColor.BOLD.toString() + "Lobby #" + ServerSignMatrix.getServerNumber(server));
        itemMeta.setLore(Arrays.asList( (isThisServer ? ChatColor.GOLD + "You are on this server" : ChatColor.GREEN + "Click to connect!"), "", ChatColor.GREEN + "There are " + ChatColor.RED + server.getOnlineCount() + ChatColor.GREEN + " players on this server."));
        stack.setItemMeta(itemMeta);
        stack.setDurability(data);
        return stack;
    }
}
