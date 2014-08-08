package net.tbnr.dev;

public enum Stat {
    WINS(0),
    GAMES_PLAYED(0),
    KILLS(0),
    DEATHS(0),
    POINTS(100);

    public final Object defaultValue;
    Stat(Object defaultValue) {
        this.defaultValue = defaultValue;
    }
}
