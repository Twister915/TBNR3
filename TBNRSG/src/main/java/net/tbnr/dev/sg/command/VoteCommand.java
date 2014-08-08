package net.tbnr.dev.sg.command;

import com.google.api.client.repackaged.com.google.common.base.Joiner;
import net.cogzmc.core.modular.command.ArgumentRequirementException;
import net.cogzmc.core.modular.command.CommandException;
import net.cogzmc.core.modular.command.CommandMeta;
import net.cogzmc.core.modular.command.ModuleCommand;
import net.cogzmc.core.player.CPlayer;
import net.tbnr.dev.sg.SurvivalGames;
import net.tbnr.dev.sg.game.map.SGMap;
import org.bukkit.Sound;

@CommandMeta(aliases = {"v", "vo"})
public final class VoteCommand extends ModuleCommand {
    public VoteCommand() {
        super("vote");
    }

    @Override
    protected void handleCommand(CPlayer player, String[] args) throws CommandException {
        if (args.length < 1) throw new ArgumentRequirementException("You have supplied too few arguments.");
        if (SurvivalGames.getInstance().getGameManager().getRunningGame() != null) throw new CommandException("You cannot vote at this time!");
        String mapName = Joiner.on(' ').join(args);
        SGMap map = null;
        try {
            SGMap mapFor = SurvivalGames.getInstance().getGameManager().getVotingSession().getMapFor(Integer.valueOf(args[0]));
            if (mapFor == null) throw new ArgumentRequirementException("The map number you specified is invalid!");
            map = mapFor;
        } catch (NumberFormatException ignored) {}
        if (map == null) {
            for (SGMap sgMap : SurvivalGames.getInstance().getGameManager().getVotingSession().getMapSelection()) {
                if (sgMap.getName().toLowerCase().startsWith(mapName.toLowerCase())) {
                    if (map == null) map = sgMap;
                    else throw new ArgumentRequirementException("You have specified a map that is not specific enough, please write more of the name!");
                }
            }
        }
        if (map == null) throw new ArgumentRequirementException("Please type the name of a map!");
        if (map.equals(SurvivalGames.getInstance().getGameManager().getVotingSession().getVoteFor(player))) return;
        SurvivalGames.getInstance().getGameManager().getVotingSession().castVote(player, map);
        player.sendMessage(SurvivalGames.getInstance().getFormat("vote-cast", new String[]{"<map>", map.getName()}));
        player.playSoundForPlayer(Sound.ORB_PICKUP);
    }
}
