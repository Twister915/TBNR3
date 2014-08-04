package net.tbnr.dev.parkour;

import lombok.Data;
import net.cogzmc.core.util.Point;
import net.cogzmc.core.util.Region;
import org.bukkit.World;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Data
public final class Parkour implements Iterable<ParkourLevel> {
    private final Region startRegion;
    private final Region endRegion;
    private final Point spawnPoint;
    private final Point villagerPoint;
    private final World world;
    private final List<ParkourLevel> levels = new ArrayList<>();

    @Override
    public Iterator<ParkourLevel> iterator() {
        return levels.iterator();
    }
}
