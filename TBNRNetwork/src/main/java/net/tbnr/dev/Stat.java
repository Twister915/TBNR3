package net.tbnr.dev;

public enum Stat {
    GAMES_PLAYED(0),
    POINTS(100),
    WINS(0),
    KILLS(0),
    DEATHS(0);

    public final Object defaultValue;
    Stat(Object defaultValue) {
        this.defaultValue = defaultValue;
    }
}
