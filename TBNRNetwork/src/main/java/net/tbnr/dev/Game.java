package net.tbnr.dev;

public enum Game {
    SURVIVAL_GAMES(24);

    private final Integer maxPlayers;

    Game(Integer maxPlayers) {
        this.maxPlayers = maxPlayers;
    }

    public Integer getMaxPlayers() {
        return maxPlayers;
    }
}
