package sh.tmb.EpicSpleef.objects;

import sh.tmb.EpicSpleef.EpicSpleef;

import java.util.function.IntConsumer;

public class Countdown implements Runnable {
    private int countFrom;
    private int timeRemaining;
    private int taskId;
    private boolean active = true;
    private EpicSpleef plugin;
    private IntConsumer everySecond;
    private Runnable afterTimer;

    public Countdown(EpicSpleef plugin, int countFrom, IntConsumer everySecond, Runnable afterTimer) {
        this.plugin = plugin;
        this.countFrom = countFrom;
        this.timeRemaining = this.countFrom;
        this.everySecond = everySecond;
        this.afterTimer = afterTimer;
        taskId = plugin.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, this, 0L, 20L);
    }

    public Countdown(EpicSpleef plugin, int countFrom, IntConsumer everySecond, Runnable afterTimer, int delay) {
        this.plugin = plugin;
        this.countFrom = countFrom;
        this.timeRemaining = this.countFrom;
        this.everySecond = everySecond;
        this.afterTimer = afterTimer;
        taskId = plugin.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, this, 20L * delay, 20L);
    }

    @Override
    public void run() {
        if (timeRemaining < 1) {
            afterTimer.run();
            cancel();
            return;
        }
        everySecond.accept(timeRemaining);
        timeRemaining--;
    }

    public void cancel() {
        plugin.getServer().getScheduler().cancelTask(taskId);
        active = false;
    }

    public boolean getActive() {
        return active;
    }
}
