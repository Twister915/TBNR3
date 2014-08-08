package net.tbnr.dev.inventory.player;

import lombok.Value;
import net.cogzmc.core.util.Point;
import org.bukkit.Material;

@Value
public final class Warp {
    private Point point;
    private String name;
    private Material[] materials;
}
