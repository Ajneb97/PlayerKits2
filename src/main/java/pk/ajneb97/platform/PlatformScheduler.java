package pk.ajneb97.platform;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public final class PlatformScheduler {

    private final Plugin plugin;
    private final ServerPlatform platform;
    private final boolean regionised;

    private Object globalScheduler;
    private Object regionScheduler;
    private Object asyncScheduler;

    private Method entitySchedulerGetter;
    private Method globalRun;
    private Method globalRunDelayed;
    private Method globalRunAtFixedRate;
    private Method globalCancelTasks;
    private Method regionRun;
    private Method regionRunDelayed;
    private Method entityRun;
    private Method entityRunDelayed;
    private Method entityRunAtFixedRate;
    private Method asyncRunNow;
    private Method asyncRunDelayed;
    private Method asyncRunAtFixedRate;
    private Method asyncCancelTasks;
    private Method isOwnedByCurrentRegionEntity;
    private Method isOwnedByCurrentRegionLocation;

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

    public boolean isOwnedByCurrentRegion(Entity entity) {
        if (!regionised) {
            return Bukkit.isPrimaryThread();
        }
        if (isOwnedByCurrentRegionEntity == null) {
            return false;
        }
        try {
            return (Boolean) isOwnedByCurrentRegionEntity.invoke(Bukkit.getServer(), entity);
        } catch (Throwable ignored) {
            return false;
        }
    }

    public boolean isOwnedByCurrentRegion(Location location) {
        if (!regionised) {
            return Bukkit.isPrimaryThread();
        }
        if (isOwnedByCurrentRegionLocation == null) {
            return false;
        }
        try {
            return (Boolean) isOwnedByCurrentRegionLocation.invoke(Bukkit.getServer(), location);
        } catch (Throwable ignored) {
            return false;
        }
    }

    public PlatformTask runGlobal(Runnable runnable) {
        if (regionised) {
            return invokeRegion(globalRun, globalScheduler, plugin, wrap(runnable));
        }
        return PlatformTask.of(Bukkit.getScheduler().runTask(plugin, runnable));
    }

    public PlatformTask runGlobalLater(Runnable runnable, long delayTicks) {
        long delay = Math.max(1L, delayTicks);
        if (regionised) {
            return invokeRegion(globalRunDelayed, globalScheduler, plugin, wrap(runnable), delay);
        }
        return PlatformTask.of(Bukkit.getScheduler().runTaskLater(plugin, runnable, delay));
    }

    public PlatformTask runGlobalTimer(Runnable runnable, long delayTicks, long periodTicks) {
        long delay = Math.max(1L, delayTicks);
        long period = Math.max(1L, periodTicks);
        if (regionised) {
            return invokeRegion(globalRunAtFixedRate, globalScheduler, plugin, wrap(runnable), delay, period);
        }
        return PlatformTask.of(Bukkit.getScheduler().runTaskTimer(plugin, runnable, delay, period));
    }

    public PlatformTask runAtLocation(Location location, Runnable runnable) {
        if (regionised) {
            return invokeRegion(regionRun, regionScheduler, plugin, location, wrap(runnable));
        }
        return PlatformTask.of(Bukkit.getScheduler().runTask(plugin, runnable));
    }

    public PlatformTask runAtLocationLater(Location location, Runnable runnable, long delayTicks) {
        long delay = Math.max(1L, delayTicks);
        if (regionised) {
            return invokeRegion(regionRunDelayed, regionScheduler, plugin, location, wrap(runnable), delay);
        }
        return PlatformTask.of(Bukkit.getScheduler().runTaskLater(plugin, runnable, delay));
    }

    public PlatformTask runForEntity(Entity entity, Runnable runnable) {
        return runForEntity(entity, runnable, null);
    }

    public PlatformTask runForEntity(Entity entity, Runnable runnable, Runnable retired) {
        if (regionised) {
            Object scheduler = entityScheduler(entity);
            if (scheduler == null) {
                return PlatformTask.empty();
            }
            return invokeRegion(entityRun, scheduler, plugin, wrap(runnable), retired);
        }
        return PlatformTask.of(Bukkit.getScheduler().runTask(plugin, runnable));
    }

    public PlatformTask runForEntityLater(Entity entity, Runnable runnable, long delayTicks) {
        return runForEntityLater(entity, runnable, null, delayTicks);
    }

    public PlatformTask runForEntityLater(Entity entity, Runnable runnable, Runnable retired, long delayTicks) {
        long delay = Math.max(1L, delayTicks);
        if (regionised) {
            Object scheduler = entityScheduler(entity);
            if (scheduler == null) {
                return PlatformTask.empty();
            }
            return invokeRegion(entityRunDelayed, scheduler, plugin, wrap(runnable), retired, delay);
        }
        return PlatformTask.of(Bukkit.getScheduler().runTaskLater(plugin, runnable, delay));
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

    public PlatformTask runAsync(Runnable runnable) {
        if (regionised) {
            return invokeRegion(asyncRunNow, asyncScheduler, plugin, wrap(runnable));
        }
        return PlatformTask.of(Bukkit.getScheduler().runTaskAsynchronously(plugin, runnable));
    }

    public PlatformTask runAsyncLater(Runnable runnable, long delayTicks) {
        long delay = Math.max(1L, delayTicks);
        if (regionised) {
            return invokeRegion(asyncRunDelayed, asyncScheduler, plugin, wrap(runnable),
                    delay * 50L, TimeUnit.MILLISECONDS);
        }
        return PlatformTask.of(Bukkit.getScheduler().runTaskLaterAsynchronously(plugin, runnable, delay));
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

    public void executeForEntity(Entity entity, Runnable runnable) {
        if (isOwnedByCurrentRegion(entity)) {
            runnable.run();
            return;
        }
        runForEntity(entity, runnable);
    }

    public void executeGlobal(Runnable runnable) {
        if (!regionised && Bukkit.isPrimaryThread()) {
            runnable.run();
            return;
        }
        runGlobal(runnable);
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
                plugin.getLogger().severe("Unhandled error in scheduled task: " + error);
                error.printStackTrace();
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
            Class<?> scheduledTask = Class.forName("io.papermc.paper.threadedregions.scheduler.ScheduledTask");
            Class<?> globalClass = Class.forName("io.papermc.paper.threadedregions.scheduler.GlobalRegionScheduler");
            Class<?> regionClass = Class.forName("io.papermc.paper.threadedregions.scheduler.RegionScheduler");
            Class<?> entityClass = Class.forName("io.papermc.paper.threadedregions.scheduler.EntityScheduler");
            Class<?> asyncClass = Class.forName("io.papermc.paper.threadedregions.scheduler.AsyncScheduler");

            globalScheduler = Bukkit.class.getMethod("getGlobalRegionScheduler").invoke(null);
            regionScheduler = Bukkit.class.getMethod("getRegionScheduler").invoke(null);
            asyncScheduler = Bukkit.class.getMethod("getAsyncScheduler").invoke(null);

            entitySchedulerGetter = Entity.class.getMethod("getScheduler");

            globalRun = globalClass.getMethod("run", Plugin.class, Consumer.class);
            globalRunDelayed = globalClass.getMethod("runDelayed", Plugin.class, Consumer.class, long.class);
            globalRunAtFixedRate = globalClass.getMethod("runAtFixedRate", Plugin.class, Consumer.class, long.class, long.class);
            globalCancelTasks = globalClass.getMethod("cancelTasks", Plugin.class);

            regionRun = regionClass.getMethod("run", Plugin.class, Location.class, Consumer.class);
            regionRunDelayed = regionClass.getMethod("runDelayed", Plugin.class, Location.class, Consumer.class, long.class);

            entityRun = entityClass.getMethod("run", Plugin.class, Consumer.class, Runnable.class);
            entityRunDelayed = entityClass.getMethod("runDelayed", Plugin.class, Consumer.class, Runnable.class, long.class);
            entityRunAtFixedRate = entityClass.getMethod("runAtFixedRate", Plugin.class, Consumer.class, Runnable.class, long.class, long.class);

            asyncRunNow = asyncClass.getMethod("runNow", Plugin.class, Consumer.class);
            asyncRunDelayed = asyncClass.getMethod("runDelayed", Plugin.class, Consumer.class, long.class, TimeUnit.class);
            asyncRunAtFixedRate = asyncClass.getMethod("runAtFixedRate", Plugin.class, Consumer.class, long.class, long.class, TimeUnit.class);
            asyncCancelTasks = asyncClass.getMethod("cancelTasks", Plugin.class);

            Class<?> serverClass = Class.forName("org.bukkit.Server");
            isOwnedByCurrentRegionEntity = serverClass.getMethod("isOwnedByCurrentRegion", Entity.class);
            isOwnedByCurrentRegionLocation = serverClass.getMethod("isOwnedByCurrentRegion", Location.class);

            return scheduledTask != null;
        } catch (Throwable ignored) {
            return false;
        }
    }
}
