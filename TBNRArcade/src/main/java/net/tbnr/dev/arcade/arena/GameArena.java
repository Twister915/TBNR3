package net.tbnr.dev.arcade.arena;

import lombok.Data;
import org.bukkit.World;

@Data
public class GameArena<DataType extends ArenaData> {
    private final World world;
    private final String name;
    private final String author;
    private final DataType arenaData;
}
