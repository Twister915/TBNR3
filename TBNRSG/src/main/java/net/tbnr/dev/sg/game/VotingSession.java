package net.tbnr.dev.sg.game;

import com.google.common.collect.ImmutableSet;
import lombok.Data;
import net.cogzmc.core.player.CPlayer;
import net.tbnr.dev.sg.game.map.SGMap;

import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;

@Data
public final class VotingSession {
    private final ImmutableSet<SGMap> mapSelection;
    private final Map<CPlayer, SGMap> votes = new WeakHashMap<>();

    public VotingSession(Iterable<SGMap> maps) {
        mapSelection = ImmutableSet.copyOf(maps);
    }

    public SGMap getVoteFor(CPlayer player) {
        return votes.get(player);
    }

    void castVote(CPlayer player, SGMap map) {
        votes.put(player, map);
    }

    void removeVoteFor(CPlayer player) {
        votes.remove(player);
    }

    private Integer getVoteCountFor(CPlayer player) {
        return 1;
    }

    SGMap getMostVotedFor() {
        Map<SGMap, Integer> mapVotes = new HashMap<>();
        for (Map.Entry<CPlayer, SGMap> cPlayerSGMapEntry : votes.entrySet()) {
            SGMap value = cPlayerSGMapEntry.getValue();
            Integer votes;
            if (!mapVotes.containsKey(value)) votes = 0;
            else votes = mapVotes.get(value);
            votes += getVoteCountFor(cPlayerSGMapEntry.getKey());
            mapVotes.put(value, votes);
        }
        SGMap votedFor = null;
        Integer mostVotes = 0;
        for (Map.Entry<SGMap, Integer> sgMapIntegerEntry : mapVotes.entrySet()) {
            if (sgMapIntegerEntry.getValue() > mostVotes) votedFor = sgMapIntegerEntry.getKey();
        }
        return votedFor;
    }
}
