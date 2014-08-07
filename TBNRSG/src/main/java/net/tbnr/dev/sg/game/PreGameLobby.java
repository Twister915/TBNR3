package net.tbnr.dev.sg.game;

import lombok.Value;
import net.cogzmc.core.maps.CMap;
import net.cogzmc.core.util.Point;

import java.util.Set;

@Value
public final class PreGameLobby {
    private CMap map;
    private Set<Point> spawnPoints;
    private Set<Point> perkVillagers;
}
