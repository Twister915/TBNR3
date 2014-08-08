package net.tbnr.dev.sg.game;

import com.google.common.collect.ImmutableSet;
import lombok.Data;
import net.cogzmc.core.player.CPlayer;
import net.tbnr.dev.sg.game.map.SGMap;

import java.util.*;

@Data
public final class VotingSession {
    private final ImmutableSet<SGMap> mapSelection;
    private final Map<CPlayer, SGMap> votes = new WeakHashMap<>();
    private final Map<Integer, SGMap> numbers = new HashMap<>();

    public VotingSession(List<SGMap> maps) {
        mapSelection = ImmutableSet.copyOf(maps);
        for (int i = 0; i < maps.size(); i++) {
            numbers.put(i+1, maps.get(i));
        }
    }

    public SGMap getVoteFor(CPlayer player) {
        return votes.get(player);
    }

    public void castVote(CPlayer player, SGMap map) {
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
            if (sgMapIntegerEntry.getValue() > mostVotes) {
                mostVotes = sgMapIntegerEntry.getValue();
                votedFor = sgMapIntegerEntry.getKey();
            }
        }
        return votedFor == null ? mapSelection.iterator().next() : votedFor;
    }

    public Integer getVotesFor(SGMap map) {
        Integer voteCount = 0;
        for (Map.Entry<CPlayer, SGMap> cPlayerSGMapEntry : votes.entrySet()) {
            if (!cPlayerSGMapEntry.getValue().equals(map)) continue;
            voteCount += getVoteCountFor(cPlayerSGMapEntry.getKey());
        }
        return voteCount;
    }

    public Integer getNumberFor(SGMap sgMap) {
        for (Map.Entry<Integer, SGMap> integerSGMapEntry : numbers.entrySet()) {
            if (integerSGMapEntry.getValue().equals(sgMap)) return integerSGMapEntry.getKey();
        }
        return null;
    }

    public SGMap getMapFor(Integer integer) {
        return numbers.get(integer);
    }
}
