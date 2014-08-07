package net.tbnr.dev.sg.game.map;

import lombok.Value;
import net.cogzmc.core.maps.CMap;
import net.cogzmc.core.util.Point;

import java.util.Set;

@Value
public final class SGMap {
    private CMap map;
    private String name;
    private String author;
    private String socialLink;
    private Set<Point> cornicopiaSpawnPoints;
    private Set<Point> tier1chests;
    private Set<Point> tier2chests;
    private Set<Point> cornicopiaChests;
    private Set<Point> deathmatchSpawn;
}
