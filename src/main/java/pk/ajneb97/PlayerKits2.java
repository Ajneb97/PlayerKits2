package pk.ajneb97;

import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import pk.ajneb97.api.ExpansionPlayerKits;
import pk.ajneb97.api.PlayerKitsAPI;
import pk.ajneb97.configs.ConfigsManager;
import pk.ajneb97.database.MySQLConnection;
import pk.ajneb97.listeners.InventoryEditListener;
import pk.ajneb97.listeners.OtherListener;
import pk.ajneb97.listeners.PlayerListener;
import pk.ajneb97.managers.DependencyManager;
import pk.ajneb97.managers.InventoryManager;
import pk.ajneb97.managers.KitItemManager;
import pk.ajneb97.managers.KitsManager;
import pk.ajneb97.managers.LayoutEditorManager;
import pk.ajneb97.managers.MessagesManager;
import pk.ajneb97.managers.MigrationManager;
import pk.ajneb97.managers.PlayerDataManager;
import pk.ajneb97.managers.UpdateCheckerManager;
import pk.ajneb97.managers.VerifyManager;
import pk.ajneb97.managers.dependencies.Metrics;
import pk.ajneb97.managers.edit.InventoryEditManager;
import pk.ajneb97.model.internal.UpdateCheckerResult;
import pk.ajneb97.platform.PlatformScheduler;
import pk.ajneb97.tasks.PlayerDataSaveTask;
import pk.ajneb97.utils.ServerVersion;
import pk.ajneb97.versions.NMSManager;

public final class PlayerKits2 extends JavaPlugin {

    private static final int BSTATS_ID = 19795;

    public String version;
    public static String prefix;
    public static ServerVersion serverVersion;

    private PlatformScheduler platformScheduler;
    private KitItemManager kitItemManager;
    private KitsManager kitsManager;
    private DependencyManager dependencyManager;
    private ConfigsManager configsManager;
    private MessagesManager messagesManager;
    private PlayerDataManager playerDataManager;
    private InventoryManager inventoryManager;
    private InventoryEditManager inventoryEditManager;
    private LayoutEditorManager layoutEditorManager;
    private NMSManager nmsManager;
    private UpdateCheckerManager updateCheckerManager;
    private VerifyManager verifyManager;
    private MigrationManager migrationManager;

    private PlayerDataSaveTask playerDataSaveTask;
    private MySQLConnection mySQLConnection;

    @Override
    public void onEnable() {
        this.version = resolvePluginVersion();
        setVersion();
        setPrefix();

        this.platformScheduler = new PlatformScheduler(this);

        this.kitItemManager = new KitItemManager(this);
        this.inventoryManager = new InventoryManager(this);
        this.inventoryEditManager = new InventoryEditManager(this);
        this.layoutEditorManager = new LayoutEditorManager(this);
        this.kitsManager = new KitsManager(this);
        this.dependencyManager = new DependencyManager(this);
        this.nmsManager = new NMSManager(this);
        this.playerDataManager = new PlayerDataManager(this);

        this.configsManager = new ConfigsManager(this);
        this.configsManager.configure();

        this.migrationManager = new MigrationManager(this);

        registerCommands();
        registerEvents();

        if (configsManager.getMainConfigManager().isMySQL()) {
            mySQLConnection = new MySQLConnection(this);
            mySQLConnection.setupMySql();
            if (!mySQLConnection.isAvailable()) {
                mySQLConnection = null;
                configsManager.getPlayersConfigManager().configure();
                reloadPlayerDataSaveTask();
            }
        } else {
            reloadPlayerDataSaveTask();
        }

        new PlayerKitsAPI(this);
        if (getServer().getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new ExpansionPlayerKits(this).register();
        }
        if (configsManager.getMainConfigManager().isMetrics()) {
            new Metrics(this, BSTATS_ID);
        }

        this.verifyManager = new VerifyManager(this);
        this.verifyManager.verify();

        for (org.bukkit.entity.Player player : Bukkit.getOnlinePlayers()) {
            playerDataManager.manageJoin(player);
        }

        Bukkit.getConsoleSender().sendMessage(MessagesManager.getLegacyColoredMessage(prefix + "&eHas been enabled! &fVersion: " + version));
        Bukkit.getConsoleSender().sendMessage(MessagesManager.getLegacyColoredMessage(prefix + "&ePlatform: &f" + platformScheduler.getPlatform().getDisplayName()
                + (platformScheduler.isRegionised() ? " &7(regionised scheduling)" : "")));
        Bukkit.getConsoleSender().sendMessage(MessagesManager.getLegacyColoredMessage(prefix + "&eOriginal plugin by &fAjneb97&e, fork maintained by &fJules&e."));

        updateCheckerManager = new UpdateCheckerManager(version);
        platformScheduler.runAsync(() -> {
            UpdateCheckerResult result = updateCheckerManager.check();
            platformScheduler.runGlobal(() -> updateMessage(result));
        });
    }

    @Override
    public void onDisable() {
        if (inventoryEditManager != null) {
            inventoryEditManager.restoreAll();
        }
        if (inventoryManager != null) {
            inventoryManager.closeAll();
        }
        if (layoutEditorManager != null) {
            layoutEditorManager.clearSessions();
        }
        if (playerDataSaveTask != null) {
            playerDataSaveTask.end();
        }
        if (configsManager != null) {
            configsManager.getPlayersConfigManager().saveConfigs();
        }
        if (platformScheduler != null) {
            platformScheduler.shutdown();
        }
        if (mySQLConnection != null) {
            mySQLConnection.shutdown();
        }
        Bukkit.getConsoleSender().sendMessage(MessagesManager.getLegacyColoredMessage(prefix + "&eHas been disabled! &fVersion: " + version));
    }

    public void registerCommands() {
        PluginCommand command = getCommand("kit");
        if (command != null) {
            MainCommand mainCommand = new MainCommand(this);
            command.setExecutor(mainCommand);
            command.setTabCompleter(mainCommand);
        }
    }

    public void registerEvents() {
        PluginManager pm = getServer().getPluginManager();
        pm.registerEvents(new PlayerListener(this), this);
        pm.registerEvents(new InventoryEditListener(this), this);
        pm.registerEvents(new OtherListener(), this);
    }

    public void reloadPlayerDataSaveTask() {
        if (playerDataSaveTask != null) {
            playerDataSaveTask.end();
        }
        playerDataSaveTask = new PlayerDataSaveTask(this);
        playerDataSaveTask.start(configsManager.getMainConfigManager().getPlayerDataSaveTime());
    }

    @SuppressWarnings("deprecation")
    private String resolvePluginVersion() {
        return getDescription().getVersion();
    }

    public void setPrefix() {
        prefix = MessagesManager.getLegacyColoredMessage("&8[&bBetterPlayerKits&a²&8] ");
    }

    public void setVersion() {
        String packageName = Bukkit.getServer().getClass().getPackage().getName();
        String minecraftVersion = ServerVersion.getMinecraftVersion();
        switch (minecraftVersion) {
            case "1.20.5":
            case "1.20.6":
                serverVersion = ServerVersion.v1_20_R4;
                break;
            case "1.21":
            case "1.21.1":
                serverVersion = ServerVersion.v1_21_R1;
                break;
            case "1.21.2":
            case "1.21.3":
                serverVersion = ServerVersion.v1_21_R2;
                break;
            case "1.21.4":
                serverVersion = ServerVersion.v1_21_R3;
                break;
            case "1.21.5":
                serverVersion = ServerVersion.v1_21_R4;
                break;
            case "1.21.6":
            case "1.21.7":
            case "1.21.8":
                serverVersion = ServerVersion.v1_21_R5;
                break;
            case "1.21.9":
            case "1.21.10":
                serverVersion = ServerVersion.v1_21_R6;
                break;
            case "1.21.11":
                serverVersion = ServerVersion.v1_21_R7;
                break;
            case "26.1":
            case "26.1.1":
            case "26.1.2":
                serverVersion = ServerVersion.v26_1;
                break;
            case "26.2":
                serverVersion = ServerVersion.v26_2;
                break;
            default:
                try {
                    serverVersion = ServerVersion.valueOf(packageName.replace("org.bukkit.craftbukkit.", ""));
                } catch (Exception ignored) {
                    serverVersion = ServerVersion.v26_2;
                }
        }
    }

    public PlatformScheduler getPlatformScheduler() {
        return platformScheduler;
    }

    public KitItemManager getKitItemManager() {
        return kitItemManager;
    }

    public KitsManager getKitsManager() {
        return kitsManager;
    }

    public MessagesManager getMessagesManager() {
        return messagesManager;
    }

    public void setMessagesManager(MessagesManager messagesManager) {
        this.messagesManager = messagesManager;
    }

    public ConfigsManager getConfigsManager() {
        return configsManager;
    }

    public DependencyManager getDependencyManager() {
        return dependencyManager;
    }

    public PlayerDataManager getPlayerDataManager() {
        return playerDataManager;
    }

    public InventoryManager getInventoryManager() {
        return inventoryManager;
    }

    public InventoryEditManager getInventoryEditManager() {
        return inventoryEditManager;
    }

    public LayoutEditorManager getLayoutEditorManager() {
        return layoutEditorManager;
    }

    public MySQLConnection getMySQLConnection() {
        return mySQLConnection;
    }

    public NMSManager getNmsManager() {
        return nmsManager;
    }

    public UpdateCheckerManager getUpdateCheckerManager() {
        return updateCheckerManager;
    }

    public VerifyManager getVerifyManager() {
        return verifyManager;
    }

    public MigrationManager getMigrationManager() {
        return migrationManager;
    }

    public void updateMessage(UpdateCheckerResult result) {
        if (!result.isError()) {
            String latestVersion = result.getLatestVersion();
            if (latestVersion != null) {
                Bukkit.getConsoleSender().sendMessage(MessagesManager.getLegacyColoredMessage("&cThere is a new version of PlayerKits2 available. &e(&7" + latestVersion + "&e)"));
                Bukkit.getConsoleSender().sendMessage(MessagesManager.getLegacyColoredMessage("&cYou can download it at: &fhttps://modrinth.com/plugin/playerkits-2"));
            }
        }
    }
}
