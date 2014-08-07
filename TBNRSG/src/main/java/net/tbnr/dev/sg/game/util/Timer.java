package net.tbnr.dev.sg.game.util;

import lombok.Data;
import net.tbnr.dev.sg.SurvivalGames;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitTask;
import org.joda.time.Duration;

@Data
public final class Timer implements Runnable {
    private final Integer length;
    private final TimerDelegate delegate;

    private Integer secondsPassed;
    private BukkitTask task;

    public Timer start() {
        if (task != null) throw new IllegalStateException("The countdown is already running!");
        secondsPassed = 0;
        task = Bukkit.getScheduler().runTaskTimer(SurvivalGames.getInstance(), this, 20L, 20L);
        delegate.countdownStarted(this, length);
        return this;
    }

    @Override
    public void run() {
        secondsPassed++;
        if (secondsPassed >= length) {
            delegate.countdownEnded(this, length);
            task.cancel();
            task = null;
        }
        else delegate.countdownChanged(this, secondsPassed, length);
    }

    public Duration getTimeRemaining() {
        return new Duration((length-secondsPassed)*1000);
    }

    public void cancel() {
        task.cancel();
        task = null;
    }

    public boolean isRunning() { return task != null; }
}
