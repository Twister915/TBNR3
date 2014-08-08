package net.tbnr.dev.commands;

import lombok.Data;
import net.cogzmc.core.Core;
import net.cogzmc.core.modular.command.CommandException;
import net.cogzmc.core.modular.command.CommandPermission;
import net.cogzmc.core.modular.command.ModuleCommand;
import net.cogzmc.core.player.CPlayer;
import net.tbnr.dev.TBNRNetwork;
import net.tbnr.dev.ShutDownNetCommand;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

@CommandPermission("tbnr.owner")
public final class EndCommand extends ModuleCommand {
    public EndCommand() {
        super("end");
    }

    @Override
    protected void handleCommand(final CPlayer player, String[] args) throws CommandException {
        player.sendMessage(ChatColor.DARK_RED + "Please type \"halt\" into the chat to confirm the shutdown. Otherwise, type something else or wait 10 seconds.");
        final Confirm confirm = TBNRNetwork.getInstance().registerListener(new Confirm(player));
        Bukkit.getScheduler().runTaskLater(TBNRNetwork.getInstance(), new Runnable() {
            @Override
            public void run() {
                HandlerList.unregisterAll(confirm);
                if (player.isOnline()) player.sendMessage(ChatColor.DARK_RED + "Shutdown cancelled.");
            }
        }, 200L);
    }

    @Data
    private class Confirm implements Listener {
        private final CPlayer player;

        @EventHandler
        public void onPlayerChat(AsyncPlayerChatEvent event) {
            if (!player.getBukkitPlayer().equals(event.getPlayer())) return;
            event.setCancelled(true);
            if (event.getMessage().equalsIgnoreCase("halt")) {
                Bukkit.getScheduler().runTaskLater(TBNRNetwork.getInstance(), new Runnable() {
                    @Override
                    public void run() {
                        Core.getNetworkManager().sendMassNetCommand(new ShutDownNetCommand());
                        Bukkit.shutdown();
                    }
                }, 100L);
                player.sendMessage(ChatColor.DARK_RED + "TBNR WILL NOW GO OFFLINE.");
            }
            HandlerList.unregisterAll(this);
        }
    }
}
