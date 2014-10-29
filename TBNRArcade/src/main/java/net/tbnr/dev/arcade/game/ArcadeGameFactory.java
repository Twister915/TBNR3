package net.tbnr.dev.arcade.game;

import net.cogzmc.core.player.CPlayer;

public interface ArcadeGameFactory {
    ArcadeGameSession getNewArcadeGame(Iterable<CPlayer> players);
}
