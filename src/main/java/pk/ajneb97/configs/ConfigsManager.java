package pk.ajneb97.configs;

import pk.ajneb97.PlayerKits2;

public class ConfigsManager {

    private PlayerKits2 plugin;

    private KitsConfigManager kitsConfigManager;
    private MessagesConfigManager messagesConfigManager;
    private MainConfigManager mainConfigManager;
    private PlayersConfigManager playersConfigManager;
    private InventoryConfigManager inventoryConfigManager;

    public ConfigsManager(PlayerKits2 plugin){
        this.plugin = plugin;
        this.kitsConfigManager = new KitsConfigManager(plugin,"kits");
        this.messagesConfigManager = new MessagesConfigManager(plugin);
        this.mainConfigManager = new MainConfigManager(plugin);
        this.playersConfigManager = new PlayersConfigManager(plugin,"players");
        this.inventoryConfigManager = new InventoryConfigManager(plugin);
    }

    public void configure(){
        // Load main config first so 'language' and other options are available for other config managers
        this.mainConfigManager.configure();
        this.messagesConfigManager.configure();
        this.kitsConfigManager.configure();
        if(!mainConfigManager.isMySQL()){
            this.playersConfigManager.configure();
        }
        this.inventoryConfigManager.configure();
    }

    public KitsConfigManager getKitsConfigManager() {
        return kitsConfigManager;
    }

    public MessagesConfigManager getMessagesConfigManager() {
        return messagesConfigManager;
    }

    public MainConfigManager getMainConfigManager() {
        return mainConfigManager;
    }

    public PlayersConfigManager getPlayersConfigManager() {
        return playersConfigManager;
    }

    public InventoryConfigManager getInventoryConfigManager() {
        return inventoryConfigManager;
    }

    public boolean reload(){
        // Reload main config first so messages can be reloaded based on language
        if(!mainConfigManager.reloadConfig()){
            return false;
        }
        if(!messagesConfigManager.reloadConfig()){
            return false;
        }
        if(!inventoryConfigManager.reloadConfig()){
            return false;
        }
        kitsConfigManager.loadConfigs();
        if(plugin.getMySQLConnection() == null){
            plugin.reloadPlayerDataSaveTask();
        }

        plugin.getVerifyManager().verify();

        return true;
    }
}
