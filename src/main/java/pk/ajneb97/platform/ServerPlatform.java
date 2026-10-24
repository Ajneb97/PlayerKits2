package pk.ajneb97.platform;

import org.bukkit.Bukkit;

public enum ServerPlatform {

    BUKKIT("Bukkit"),
    SPIGOT("Spigot"),
    PAPER("Paper"),
    PURPUR("Purpur"),
    FOLIA("Folia"),
    CANVAS("CanvasMC");

    private static final boolean REGIONISED_SCHEDULER = detectRegionisedScheduler();

    private final String displayName;

    ServerPlatform(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static boolean hasRegionisedScheduler() {
        return REGIONISED_SCHEDULER;
    }

    public static ServerPlatform detect() {
        if (classPresent("io.canvasmc.canvas.Config")
                || classPresent("io.canvasmc.canvas.CanvasBootstrap")
                || serverNameContains("canvas")) {
            return CANVAS;
        }
        if (classPresent("io.papermc.paper.threadedregions.RegionizedServer")
                || serverNameContains("folia")) {
            return FOLIA;
        }
        if (classPresent("org.purpurmc.purpur.PurpurConfig")
                || classPresent("net.pl3x.purpur.PurpurConfig")) {
            return PURPUR;
        }
        if (classPresent("com.destroystokyo.paper.ParticleBuilder")
                || classPresent("io.papermc.paper.configuration.Configuration")) {
            return PAPER;
        }
        if (classPresent("org.spigotmc.SpigotConfig")) {
            return SPIGOT;
        }
        return BUKKIT;
    }

    private static boolean detectRegionisedScheduler() {
        try {
            Bukkit.class.getMethod("getGlobalRegionScheduler");
            Bukkit.class.getMethod("getRegionScheduler");
            Bukkit.class.getMethod("getAsyncScheduler");
            Class.forName("io.papermc.paper.threadedregions.scheduler.ScheduledTask");
            return true;
        } catch (Throwable ignored) {
            return false;
        }
    }

    private static boolean classPresent(String name) {
        try {
            Class.forName(name);
            return true;
        } catch (Throwable ignored) {
            return false;
        }
    }

    private static boolean serverNameContains(String needle) {
        try {
            return Bukkit.getName().toLowerCase().contains(needle)
                    || Bukkit.getVersion().toLowerCase().contains(needle);
        } catch (Throwable ignored) {
            return false;
        }
    }
}
