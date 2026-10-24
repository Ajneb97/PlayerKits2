package pk.ajneb97.platform;

import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.logging.Level;

public final class PlatformScheduler {

    private final Plugin plugin;
    private final ServerPlatform platform;
    private final boolean regionised;

    private Object globalScheduler;
    private Object asyncScheduler;

    private Method entitySchedulerGetter;
    private Method globalRun;
    private Method globalCancelTasks;
    private Method entityRunDelayed;
    private Method entityRunAtFixedRate;
    private Method asyncRunNow;
    private Method asyncRunAtFixedRate;
    private Method asyncCancelTasks;

    public PlatformScheduler(Plugin plugin) {
        this.plugin = plugin;
        this.platform = ServerPlatform.detect();
        this.regionised = ServerPlatform.hasRegionisedScheduler() && resolveRegionisedApi();
    }

    public ServerPlatform getPlatform() {
        return platform;
    }

    public boolean isRegionised() {
        return regionised;
    }


    public void runGlobal(Runnable runnable) {
        if (regionised) {
            invokeRegion(globalRun, globalScheduler, plugin, wrap(runnable));
            return;
        }
        Bukkit.getScheduler().runTask(plugin, runnable);
    }

    public void runForEntityLater(Entity entity, Runnable runnable, long delayTicks) {
        runForEntityLater(entity, runnable, null, delayTicks);
    }

    public void runForEntityLater(Entity entity, Runnable runnable, Runnable retired, long delayTicks) {
        long delay = Math.max(1L, delayTicks);
        if (regionised) {
            Object scheduler = entityScheduler(entity);
            if (scheduler != null) {
                invokeRegion(entityRunDelayed, scheduler, plugin, wrap(runnable), retired, delay);
            }
            return;
        }
        Bukkit.getScheduler().runTaskLater(plugin, runnable, delay);
    }

    public PlatformTask runForEntityTimer(Entity entity, Runnable runnable, long delayTicks, long periodTicks) {
        long delay = Math.max(1L, delayTicks);
        long period = Math.max(1L, periodTicks);
        if (regionised) {
            Object scheduler = entityScheduler(entity);
            if (scheduler == null) {
                return PlatformTask.empty();
            }
            return invokeRegion(entityRunAtFixedRate, scheduler, plugin, wrap(runnable), null, delay, period);
        }
        return PlatformTask.of(Bukkit.getScheduler().runTaskTimer(plugin, runnable, delay, period));
    }

    public void runAsync(Runnable runnable) {
        if (regionised) {
            invokeRegion(asyncRunNow, asyncScheduler, plugin, wrap(runnable));
            return;
        }
        Bukkit.getScheduler().runTaskAsynchronously(plugin, runnable);
    }

    public PlatformTask runAsyncTimer(Runnable runnable, long delayTicks, long periodTicks) {
        long delay = Math.max(1L, delayTicks);
        long period = Math.max(1L, periodTicks);
        if (regionised) {
            return invokeRegion(asyncRunAtFixedRate, asyncScheduler, plugin, wrap(runnable),
                    delay * 50L, period * 50L, TimeUnit.MILLISECONDS);
        }
        return PlatformTask.of(Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, runnable, delay, period));
    }

    public void shutdown() {
        if (regionised) {
            invokeQuietly(globalCancelTasks, globalScheduler, plugin);
            invokeQuietly(asyncCancelTasks, asyncScheduler, plugin);
            return;
        }
        try {
            Bukkit.getScheduler().cancelTasks(plugin);
        } catch (Throwable ignored) {
        }
    }

    private Object entityScheduler(Entity entity) {
        if (entitySchedulerGetter == null) {
            return null;
        }
        try {
            return entitySchedulerGetter.invoke(entity);
        } catch (Throwable ignored) {
            return null;
        }
    }

    private Consumer<Object> wrap(Runnable runnable) {
        return task -> {
            try {
                runnable.run();
            } catch (Throwable error) {
                plugin.getLogger().log(Level.SEVERE, "Unhandled error in scheduled task", error);
            }
        };
    }

    private PlatformTask invokeRegion(Method method, Object target, Object... args) {
        if (method == null || target == null) {
            return PlatformTask.empty();
        }
        try {
            Object task = method.invoke(target, args);
            return task == null ? PlatformTask.empty() : PlatformTask.ofRegion(task);
        } catch (Throwable error) {
            plugin.getLogger().severe("Failed to schedule regionised task: " + error);
            return PlatformTask.empty();
        }
    }

    private void invokeQuietly(Method method, Object target, Object... args) {
        if (method == null || target == null) {
            return;
        }
        try {
            method.invoke(target, args);
        } catch (Throwable ignored) {
        }
    }

    private boolean resolveRegionisedApi() {
        try {
            Class.forName("io.papermc.paper.threadedregions.scheduler.ScheduledTask");
            Class<?> globalClass = Class.forName("io.papermc.paper.threadedregions.scheduler.GlobalRegionScheduler");
            Class<?> entityClass = Class.forName("io.papermc.paper.threadedregions.scheduler.EntityScheduler");
            Class<?> asyncClass = Class.forName("io.papermc.paper.threadedregions.scheduler.AsyncScheduler");

            globalScheduler = Bukkit.class.getMethod("getGlobalRegionScheduler").invoke(null);
            asyncScheduler = Bukkit.class.getMethod("getAsyncScheduler").invoke(null);

            entitySchedulerGetter = Entity.class.getMethod("getScheduler");

            globalRun = globalClass.getMethod("run", Plugin.class, Consumer.class);
            globalCancelTasks = globalClass.getMethod("cancelTasks", Plugin.class);

            entityRunDelayed = entityClass.getMethod("runDelayed", Plugin.class, Consumer.class, Runnable.class, long.class);
            entityRunAtFixedRate = entityClass.getMethod("runAtFixedRate", Plugin.class, Consumer.class, Runnable.class, long.class, long.class);

            asyncRunNow = asyncClass.getMethod("runNow", Plugin.class, Consumer.class);
            asyncRunAtFixedRate = asyncClass.getMethod("runAtFixedRate", Plugin.class, Consumer.class, long.class, long.class, TimeUnit.class);
            asyncCancelTasks = asyncClass.getMethod("cancelTasks", Plugin.class);
            return true;
        } catch (Throwable ignored) {
            return false;
        }
    }
}
