package net.tbnr.dev.sg.setup;

import net.cogzmc.core.player.CPlayer;

public interface SetupSession {
    void cancel();
    void start();
    CPlayer getPlayer();
}
