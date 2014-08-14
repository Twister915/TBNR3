package net.tbnr.dev.sg.game.deathperks;

import net.cogzmc.core.player.CPlayer;
import net.tbnr.dev.sg.game.SGGame;

import java.util.List;

public interface DeathPerk {
    String getName();
    List<String> getDescription();

    boolean onDeath(SGGame game, CPlayer died, CPlayer killer);
}
