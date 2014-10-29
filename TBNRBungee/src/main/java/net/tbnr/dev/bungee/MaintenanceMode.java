package net.tbnr.dev.bungee;

import net.cogzmc.bungee.CoreBungeeDriver;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.ServerPing;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.event.ProxyPingEvent;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import redis.clients.jedis.Jedis;

public final class MaintenanceMode implements Listener {
    private boolean active = false;

    {
        reload();
    }

    public void reload() {
        Jedis jedisClient = CoreBungeeDriver.getInstance().getJedisClient();
        this.active = Boolean.valueOf(jedisClient.get("tbnr_maintenance"));
        CoreBungeeDriver.getInstance().returnJedis(jedisClient);
    }

    public void setActive(boolean value) {
        if (value) {
            for (ProxiedPlayer proxiedPlayer : ProxyServer.getInstance().getPlayers()) {
                proxiedPlayer.disconnect(ChatColor.RED + ChatColor.BOLD.toString() + "Maintenance Mode Activated".toUpperCase());
            }
        }
        this.active = value;
        Jedis jedisClient = CoreBungeeDriver.getInstance().getJedisClient();
        jedisClient.set("tbnr_maintenance", String.valueOf(active));
        CoreBungeeDriver.getInstance().returnJedis(jedisClient);
    }

    class MaintenanceCommand extends Command {
        public MaintenanceCommand() {
            super("maintenance", "tbnr.bungee.maintenance.toggle");
        }

        @Override
        public void execute(CommandSender commandSender, String[] strings) {
            setActive(!active);
            commandSender.sendMessage("Made maintenance mode" + (active ? "active" : "inactive"));
        }
    }

    @EventHandler
    public void onPlayerPing(ProxyPingEvent event) {
        ServerPing.Protocol version = event.getResponse().getVersion();
        version.setName(ChatColor.DARK_GREEN + ChatColor.BOLD.toString() + "Maintenance".toUpperCase());
        version.setProtocol(12);
    }

    @EventHandler
    public void onPlayerJoin(PostLoginEvent event) {
        if (event.getPlayer().hasPermission("tbnr.bungee.maintenance.bypass")) return;
        event.getPlayer().disconnect(ChatColor.RED + "We're currently working on things, and will be right back!");
    }
}
