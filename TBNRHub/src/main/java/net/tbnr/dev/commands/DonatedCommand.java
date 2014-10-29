package net.tbnr.dev.commands;

import com.google.api.client.repackaged.com.google.common.base.Joiner;
import net.cogzmc.core.Core;
import net.cogzmc.core.modular.command.ArgumentRequirementException;
import net.cogzmc.core.modular.command.CommandException;
import net.cogzmc.core.modular.command.ModuleCommand;
import net.cogzmc.core.network.NetCommand;
import net.cogzmc.core.network.NetCommandHandler;
import net.cogzmc.core.network.NetworkServer;
import net.cogzmc.core.player.COfflinePlayer;
import net.cogzmc.core.player.CPlayer;
import net.tbnr.dev.ServerHelper;
import net.tbnr.dev.TBNRHub;
import net.tbnr.dev.enderBar.EnderBarManager;
import org.bukkit.*;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.inventory.meta.FireworkMeta;

import java.util.*;

public final class DonatedCommand extends ModuleCommand implements NetCommandHandler<DonatedCommand.DonationBroadcastNetCommand> {
    private final static Integer DONATION_PRIORITY = 10;

    public DonatedCommand() {
        super("donated");
        if (Core.getNetworkManager() != null) Core.getNetworkManager().registerNetCommandHandler(this, DonationBroadcastNetCommand.class);
    }

    @Override
    protected void handleCommand(ConsoleCommandSender commandSender, String[] args) throws CommandException {
        if (args.length < 2) throw new ArgumentRequirementException("You have specified an invalid number of arguments!");
        String packag3 = Joiner.on(' ').join(Arrays.copyOfRange(args, 1, args.length));
        List<COfflinePlayer> offlinePlayerByName = Core.getPlayerManager().getOfflinePlayerByName(args[0]);
        if (offlinePlayerByName.size() != 1) throw new ArgumentRequirementException("You have specified an invalid player name!");
        COfflinePlayer donor = offlinePlayerByName.get(0);
        broadcastDonation(donor, packag3);
        if (Core.getNetworkManager() == null) return;
        DonationBroadcastNetCommand donationBroadcastNetCommand = new DonationBroadcastNetCommand();
        donationBroadcastNetCommand.rank = packag3;
        donationBroadcastNetCommand.donorUUID = donor.getUniqueIdentifier().toString();
        for (NetworkServer networkServer : ServerHelper.getLobbyServers()) {
            if (networkServer.equals(Core.getNetworkManager().getThisServer())) continue;
            networkServer.sendNetCommand(donationBroadcastNetCommand);
        }

    }

    @Override
    public void handleNetCommand(NetworkServer sender, DonationBroadcastNetCommand netCommand) {
        broadcastDonation(Core.getOfflinePlayerByUUID(UUID.fromString(netCommand.donorUUID)), netCommand.rank);
    }

    private void broadcastDonation(COfflinePlayer donor, String rank) {
        new DonationBroadcastRunnable(donor, rank).run();
    }

    private static class DonationBroadcastRunnable implements Runnable {
        private final String rank;
        private final COfflinePlayer player;

        private float progress = 1.0f;
        private Integer times = 0;
        private Long resetToTime;

        public DonationBroadcastRunnable(COfflinePlayer player, String rank) {
            this.rank = rank;
            this.player = player;
        }

        @Override
        public void run() {
            ArrayList<CPlayer> onlinePlayers = new ArrayList<>(Core.getOnlinePlayers());
            Collections.shuffle(onlinePlayers);
            IF: if (times == 0) {
                World w = null;
                for (CPlayer cPlayer : onlinePlayers) {
                    cPlayer.playSoundForPlayer(Sound.ENDERDRAGON_DEATH, 20f, 1.2f);
                    w = cPlayer.getBukkitPlayer().getWorld();
                    cPlayer.sendMessage(TBNRHub.getInstance().getFormat("donate-prompt", new String[]{"<name>", player.getName()}, new String[]{"<package>", ChatColor.translateAlternateColorCodes('&', rank)}));
                }
                if (w == null) break IF;
                resetToTime = w.getTime();
                w.setTime(14000);
            }
            IF: if (times >= 31) {
                World w = null;
                for (CPlayer cPlayer : onlinePlayers) {
                    EnderBarManager.clearId(cPlayer, DONATION_PRIORITY);
                    w = cPlayer.getBukkitPlayer().getWorld();
                }
                if (w == null) break IF;
                w.setTime(resetToTime);
                return;
            }
            progress -= 1.0f/30;
            for (CPlayer cPlayer : onlinePlayers) {
                EnderBarManager.setStateForID(cPlayer, DONATION_PRIORITY, TBNRHub.getInstance().getFormat("donation-ender-bar", false, new String[]{"<name>", player.getName()}, new String[]{"<package>", ChatColor.translateAlternateColorCodes('&', rank)}), progress);
            }
            CPlayer onlineCPlayerForUUID = Core.getPlayerManager().getOnlineCPlayerForUUID(player.getUniqueIdentifier());
            if (times % 5 == 0) {
                for (int i = 0; i < Math.min(onlinePlayers.size(), 10); i++) {
                    CPlayer cPlayer = onlinePlayers.get(i);
                    Location location = cPlayer.getBukkitPlayer().getLocation();
                    for (int z = 0; z < 3; z++) {
                        Firework firework = (Firework) location.getWorld().spawnEntity(location, EntityType.FIREWORK);
                        FireworkMeta fireworkMeta = firework.getFireworkMeta();
                        fireworkMeta.addEffect(getRandomFireworkEffect(Core.getRandom().nextInt(5)+1, Core.getRandom().nextInt(5)+1));
                        fireworkMeta.setPower(1);
                        firework.setFireworkMeta(fireworkMeta);
                    }
                }
                for (CPlayer onlinePlayer : onlinePlayers) {
                    onlinePlayer.playSoundForPlayer(Sound.ENDERDRAGON_GROWL);
                }
            }
            else if (times % 2 == 0 && onlineCPlayerForUUID != null) {
                Location location = onlineCPlayerForUUID.getBukkitPlayer().getLocation();
                Firework firework = (Firework) location.getWorld().spawnEntity(location, EntityType.FIREWORK);
                FireworkMeta fireworkMeta = firework.getFireworkMeta();
                fireworkMeta.setPower(2);
                fireworkMeta.addEffect(getRandomFireworkEffect(4, 4));
                firework.setFireworkMeta(fireworkMeta);
            }
            times++;
            Bukkit.getScheduler().runTaskLater(TBNRHub.getInstance(), this, 20L);
        }
    }

    private static FireworkEffect getRandomFireworkEffect(int colors, int fades) {
        Color[] colorsArray = new Color[colors];
        Color[] fadesArray = new Color[fades];
        fillColorArray(colorsArray);
        fillColorArray(fadesArray);
        FireworkEffect.Type[] values1 = FireworkEffect.Type.values();
        FireworkEffect.Type type = values1[Core.getRandom().nextInt(values1.length)];
        boolean flicker = Core.getRandom().nextBoolean();
        boolean trail = Core.getRandom().nextBoolean();
        FireworkEffect.Builder builder = FireworkEffect.builder();
        for (Color color : colorsArray) {
            builder.withColor(color);
        }
        for (Color color : fadesArray) {
            builder.withFade(color);
        }
        if (flicker) builder.withFlicker();
        if (trail) builder.withTrail();
        builder.with(type);
        return builder.build();
    }

    private static void fillColorArray(Color[] colorz) {
        DyeColor[] values = DyeColor.values();
        for (int i = 0; i < colorz.length; i++) {
            colorz[i] = values[Core.getRandom().nextInt(values.length)].getFireworkColor();
        }
    }

    public static class DonationBroadcastNetCommand implements NetCommand {
        public String rank;
        public String donorUUID;
    }
}
