
package pk.ajneb97;

import org.bukkit.Bukkit;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import pk.ajneb97.api.ExpansionPlayerKits;
import pk.ajneb97.api.PlayerKitsAPI;
import pk.ajneb97.configs.ConfigsManager;
import pk.ajneb97.database.MySQLConnection;
import pk.ajneb97.listeners.InventoryEditListener;
import pk.ajneb97.listeners.OtherListener;
import pk.ajneb97.listeners.PlayerListener;
import pk.ajneb97.managers.*;
import pk.ajneb97.managers.dependencies.Metrics;
import pk.ajneb97.managers.edit.InventoryEditManager;
import pk.ajneb97.model.internal.UpdateCheckerResult;
import pk.ajneb97.tasks.InventoryUpdateTaskManager;
import pk.ajneb97.tasks.PlayerDataSaveTask;
import pk.ajneb97.versions.NMSManager;
import pk.ajneb97.utils.ServerVersion;

public class PlayerKits2 extends JavaPlugin {

    public String version = getDescription().getVersion();
    public static String prefix;
    public static ServerVersion serverVersion;

    private KitItemManager kitItemManager;
    private KitsManager kitsManager;
    private DependencyManager dependencyManager;
    private ConfigsManager configsManager;
    private MessagesManager messagesManager;
    private PlayerDataManager playerDataManager;
    private InventoryManager inventoryManager;
    private InventoryEditManager inventoryEditManager;
    private NMSManager nmsManager;
    private UpdateCheckerManager updateCheckerManager;
    private VerifyManager verifyManager;
    private MigrationManager migrationManager;

    private InventoryUpdateTaskManager inventoryUpdateTaskManager;
    private PlayerDataSaveTask playerDataSaveTask;
    private MySQLConnection mySQLConnection;

    public void onEnable(){
        setVersion();
        setPrefix();
        registerCommands();
        registerEvents();

        this.kitItemManager = new KitItemManager(this);
        this.inventoryManager = new InventoryManager(this);
        this.inventoryEditManager = new InventoryEditManager(this);
        this.kitsManager = new KitsManager(this);
        this.dependencyManager = new DependencyManager(this);
        this.nmsManager = new NMSManager(this);
        this.playerDataManager = new PlayerDataManager(this);

        this.configsManager = new ConfigsManager(this);
        this.configsManager.configure();

        this.migrationManager = new MigrationManager(this);

        this.inventoryUpdateTaskManager = new InventoryUpdateTaskManager(this);
        this.inventoryUpdateTaskManager.start();

        this.verifyManager = new VerifyManager(this);
        this.verifyManager.verify();

        if(configsManager.getMainConfigManager().isMySQL()){
            mySQLConnection = new MySQLConnection(this);
            mySQLConnection.setupMySql();
        }else{
            reloadPlayerDataSaveTask();
        }

        PlayerKitsAPI api = new PlayerKitsAPI(this);
        if(getServer().getPluginManager().getPlugin("PlaceholderAPI") != null){
            new ExpansionPlayerKits(this).register();
        }
        Metrics metrics = new Metrics(this,19795);

        Bukkit.getConsoleSender().sendMessage(MessagesManager.getColoredMessage(prefix+"&eHas been enabled! &fVersion: "+version));
        Bukkit.getConsoleSender().sendMessage(MessagesManager.getColoredMessage(prefix+"&eThanks for using my plugin!   &f~Ajneb97"));

        updateCheckerManager = new UpdateCheckerManager(version);
        updateMessage(updateCheckerManager.check());
    }

    public void onDisable(){
        this.configsManager.getPlayersConfigManager().saveConfigs();
        Bukkit.getConsoleSender().sendMessage(MessagesManager.getColoredMessage(prefix+"&eHas been disabled! &fVersion: "+version));
    }

    public void registerCommands(){
        this.getCommand("kit").setExecutor(new MainCommand(this));
    }

    public void registerEvents() {
        PluginManager pm = getServer().getPluginManager();
        pm.registerEvents(new PlayerListener(this), this);
        pm.registerEvents(new InventoryEditListener(this), this);
        pm.registerEvents(new OtherListener(), this);
    }

    public void reloadPlayerDataSaveTask() {
        if(playerDataSaveTask != null) {
            playerDataSaveTask.end();
        }
        playerDataSaveTask = new PlayerDataSaveTask(this);
        playerDataSaveTask.start(configsManager.getMainConfigManager().getConfig().getInt("player_data_save_time"));
    }

    public void setPrefix(){
        prefix = MessagesManager.getColoredMessage("&8[&bPlayerKits&a²&8] ");
    }

    public void setVersion(){
        String packageName = Bukkit.getServer().getClass().getPackage().getName();
        String bukkitVersion = Bukkit.getServer().getBukkitVersion().split("-")[0];
        switch(bukkitVersion){
            case "1.20.5":
            case "1.20.6":
                serverVersion = ServerVersion.v1_20_R4;
                break;
            case "1.21":
                serverVersion = ServerVersion.v1_21_R1;
                break;
            default:
                serverVersion = ServerVersion.valueOf(packageName.replace("org.bukkit.craftbukkit.", ""));
        }
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

    public void updateMessage(UpdateCheckerResult result){
        if(!result.isError()){
            String latestVersion = result.getLatestVersion();
            if(latestVersion != null){
                Bukkit.getConsoleSender().sendMessage(MessagesManager.getColoredMessage("&cThere is a new version available. &e(&7"+latestVersion+"&e)"));
                Bukkit.getConsoleSender().sendMessage(MessagesManager.getColoredMessage("&cYou can download it at: &fhttps://modrinth.com/plugin/playerkits-2"));
            }
        }else{
            Bukkit.getConsoleSender().sendMessage(MessagesManager.getColoredMessage(prefix+"&cError while checking update."));
        }

    }
}
