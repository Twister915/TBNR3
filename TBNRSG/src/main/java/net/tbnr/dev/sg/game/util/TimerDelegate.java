package net.tbnr.dev.sg.game.util;

public interface TimerDelegate {
    void countdownStarted(Timer timer, Integer totalSeconds);
    void countdownEnded(Timer timer, Integer totalSeconds);
    void countdownChanged(Timer timer, Integer secondsPassed, Integer totalSeconds);
}
