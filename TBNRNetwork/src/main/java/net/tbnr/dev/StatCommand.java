package net.tbnr.dev;

import net.cogzmc.core.modular.command.CommandException;
import net.cogzmc.core.modular.command.ModuleCommand;
import net.cogzmc.core.player.CPlayer;

/**
 * <p/>
 * Latest Change: 08/08/2014.
 * <p/>
 *
 * @author Noy
 * @since 08/08/2014.
 */
public final class StatCommand extends ModuleCommand {

    public StatCommand() {
        super("stats");
    }

    @Override
    protected void handleCommand(CPlayer player, String[] args) throws CommandException {
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
                Integer stat1 = StatsManager.getStat(game, stat, player, Integer.class);
                StringBuilder builder = new StringBuilder(stat.name().toLowerCase());
                builder.setCharAt(0, Character.toUpperCase(builder.charAt(0)));
                player.sendMessage(instance.getFormat("stat.stat", new String[]{"<stat>", builder.toString()}, new String[]{"<value>", String.valueOf(stat1)}));
            }
        }
    }
}
