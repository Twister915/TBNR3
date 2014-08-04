package net.tbnr.dev.parkour;

import lombok.Data;
import net.cogzmc.core.util.Point;
import net.cogzmc.core.util.Region;
import org.joda.time.Duration;

@Data
public final class ParkourLevel {
    private final Region startRegion;
    private final Duration targetDuration;
    private final Point checkpoint;
}
