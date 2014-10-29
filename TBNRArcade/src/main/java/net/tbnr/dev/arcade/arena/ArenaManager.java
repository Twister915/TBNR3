package net.tbnr.dev.arcade.arena;

import com.google.gson.Gson;
import net.tbnr.dev.arcade.game.ArcadeGameSession;

public final class ArenaManager {
    public <T extends ArenaData> GameArena<T> getArenaFor(ArcadeGameSession game, Class<T> dataType) {
        Gson gson = new Gson();
        return null;
    }
}
