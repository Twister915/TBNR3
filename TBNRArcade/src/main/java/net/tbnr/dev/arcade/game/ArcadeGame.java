package net.tbnr.dev.arcade.game;

import net.tbnr.dev.arcade.arena.ArenaData;

public enum ArcadeGame {
    ;
    public final String cleanName;
    public final String key;
    public final Class<? extends ArenaData> dataType;

    ArcadeGame(String cleanName, String key, Class<? extends ArenaData> dataType) {
        this.cleanName = cleanName;
        this.key = key;
        this.dataType = dataType;
    }
}
