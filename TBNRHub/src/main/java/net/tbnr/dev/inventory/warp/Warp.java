package net.tbnr.dev.inventory.warp;

import lombok.Value;
import net.cogzmc.core.util.Point;
import org.bukkit.Material;

@Value
public final class Warp {
    private Point point;
    private String name;
    private Material[] materials;
    private short dataValue;
    private String title;
}
