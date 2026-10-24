package pk.ajneb97.platform;

import org.bukkit.scheduler.BukkitTask;

import java.lang.reflect.Method;

public final class PlatformTask {

    private static final Method FOLIA_CANCEL = resolveCancel();

    private final BukkitTask bukkitTask;
    private final Object regionTask;

    private PlatformTask(BukkitTask bukkitTask, Object regionTask) {
        this.bukkitTask = bukkitTask;
        this.regionTask = regionTask;
    }

    public static PlatformTask of(BukkitTask task) {
        return new PlatformTask(task, null);
    }

    public static PlatformTask ofRegion(Object task) {
        return new PlatformTask(null, task);
    }

    public static PlatformTask empty() {
        return new PlatformTask(null, null);
    }

    public void cancel() {
        if (bukkitTask != null) {
            bukkitTask.cancel();
            return;
        }
        if (regionTask != null && FOLIA_CANCEL != null) {
            try {
                FOLIA_CANCEL.invoke(regionTask);
            } catch (Throwable ignored) {
            }
        }
    }

    private static Method resolveCancel() {
        try {
            return Class.forName("io.papermc.paper.threadedregions.scheduler.ScheduledTask")
                    .getMethod("cancel");
        } catch (Throwable ignored) {
            return null;
        }
    }
}
