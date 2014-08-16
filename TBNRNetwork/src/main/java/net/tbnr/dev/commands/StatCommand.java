package net.tbnr.dev.commands;

import net.cogzmc.core.Core;
import net.cogzmc.core.modular.command.ArgumentRequirementException;
import net.cogzmc.core.modular.command.CommandException;
import net.cogzmc.core.modular.command.CommandMeta;
import net.cogzmc.core.modular.command.ModuleCommand;
import net.cogzmc.core.player.COfflinePlayer;
import net.cogzmc.core.player.CPlayer;
import net.tbnr.dev.Game;
import net.tbnr.dev.Stat;
import net.tbnr.dev.StatsManager;
import net.tbnr.dev.TBNRNetwork;

import java.util.List;

@CommandMeta(aliases = {"records"})
public final class StatCommand extends ModuleCommand {

    public StatCommand() {
        super("stats");
    }

    @Override
    protected void handleCommand(CPlayer player, String[] args) throws CommandException {
        if (args.length > 1) throw new ArgumentRequirementException("Too many arguments!");
        COfflinePlayer target;
        if (args.length == 0) target = player;
        else {
            List<COfflinePlayer> players = Core.getPlayerManager().getOfflinePlayerByName(args[0]);
            if (players.size() != 1) throw new ArgumentRequirementException("The player you specified does not exist (or is not specific enough)!");
            target = players.get(0);
        }
        if (target == null) throw new ArgumentRequirementException("Invalid player name!");
        for (Game game : Game.values()) {
            StringBuilder sb = new StringBuilder();
            String[] split = game.name().split("_");
            for (String s1 : split) {
                char[] chars = s1.toLowerCase().toCharArray();
                for (int i = 0; i < chars.length; i++) {
                    sb.append(i == 0 ? Character.toUpperCase(chars[i]) : chars[i]);
                }
                sb.append(" ");
            }
            TBNRNetwork instance = TBNRNetwork.getInstance();
            player.sendMessage(instance.getFormat("stat.game", new String[]{"<game>", sb.toString().trim()}));
            for (Stat stat : Stat.values()) {
                Integer stat1 = StatsManager.getStat(game, stat, target, Integer.class);
                StringBuilder builder = new StringBuilder(stat.name().toLowerCase().replaceAll("_", " "));
                builder.setCharAt(0, Character.toUpperCase(builder.charAt(0)));
                player.sendMessage(instance.getFormat("stat.stat", new String[]{"<stat>", builder.toString()}, new String[]{"<value>", String.valueOf(stat1 == null ? stat.defaultValue : stat1)}));
            }
        }
    }
}

