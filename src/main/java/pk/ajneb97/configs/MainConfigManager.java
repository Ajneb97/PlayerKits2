package pk.ajneb97.configs;

import org.bukkit.configuration.file.FileConfiguration;
import pk.ajneb97.PlayerKits2;
import pk.ajneb97.configs.model.CommonConfig;
import pk.ajneb97.model.Kit;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class MainConfigManager {

    private PlayerKits2 plugin;
    private CommonConfig configFile;

    //Options
    private Kit newKitDefault;
    private boolean kitPreview;
    private boolean closeInventoryOnClaim;
    private boolean claimKitShortCommand;
    private boolean kitPreviewRequiresKitPermission;
    private boolean newKitDefaultSaveModeOriginal;
    private String firstJoinKit;
    private String newKitDefaultInventory;
    private boolean isMySQL;
    private boolean updateNotify;
    private boolean useMiniMessage;

    public MainConfigManager(PlayerKits2 plugin){
        this.plugin = plugin;
        this.configFile = new CommonConfig("config.yml",plugin,null, false);
        this.configFile.registerConfig();
        checkUpdate();
    }

    public void configure(){
        FileConfiguration config = configFile.getConfig();
        newKitDefault = KitsConfigManager.getKitFromConfig(config,plugin,null,"new_kit_default_values.");
        kitPreview = config.getBoolean("kit_preview");
        closeInventoryOnClaim = config.getBoolean("close_inventory_on_claim");
        kitPreviewRequiresKitPermission = config.getBoolean("kit_preview_requires_kit_permission");
        firstJoinKit = config.getString("first_join_kit");
        newKitDefaultInventory = config.getString("new_kit_default_inventory");
        isMySQL = config.getBoolean("mysql_database.enabled");
        updateNotify = config.getBoolean("update_notify");
        claimKitShortCommand = config.getBoolean("claim_kit_short_command");
        useMiniMessage = config.getBoolean("use_minimessage");
        newKitDefaultSaveModeOriginal = config.getBoolean("new_kit_default_save_mode_original");
    }

    public boolean reloadConfig(){
        if(!configFile.reloadConfig()){
            return false;
        }
        configure();
        return true;
    }

    public FileConfiguration getConfig(){
        return configFile.getConfig();
    }

    public void checkUpdate(){
        Path pathConfig = Paths.get(configFile.getRoute());
        try{
            String text = new String(Files.readAllBytes(pathConfig));
            if(!text.contains("use_minimessage:")){
                getConfig().set("use_minimessage",false);
                configFile.saveConfig();
            }
            if(!text.contains("verifyServerCertificate:")){
                getConfig().set("mysql_database.pool.connectionTimeout",5000);
                getConfig().set("mysql_database.advanced.verifyServerCertificate",false);
                getConfig().set("mysql_database.advanced.useSSL",true);
                getConfig().set("mysql_database.advanced.allowPublicKeyRetrieval",true);
                configFile.saveConfig();
            }
            if(!text.contains("new_kit_default_save_mode_original:")){
                getConfig().set("new_kit_default_save_mode_original", true);
                configFile.saveConfig();
            }
            // Add language option defaulting to english (en)
            if(!text.contains("language:")){
                getConfig().set("language","en");
                configFile.saveConfig();
            }
        }catch(IOException e){
            e.printStackTrace();
        }
    }

    public Kit getNewKitDefault() {
        return newKitDefault;
    }

    public boolean isKitPreview() {
        return kitPreview;
    }

    public boolean isCloseInventoryOnClaim() {
        return closeInventoryOnClaim;
    }

    public boolean isKitPreviewRequiresKitPermission() {
        return kitPreviewRequiresKitPermission;
    }

    public String getFirstJoinKit() {
        return firstJoinKit;
    }

    public String getNewKitDefaultInventory() {
        return newKitDefaultInventory;
    }

    public boolean isMySQL() {
        return isMySQL;
    }

    public boolean isUpdateNotify() {
        return updateNotify;
    }

    public boolean isClaimKitShortCommand() {
        return claimKitShortCommand;
    }

    public boolean isNewKitDefaultSaveModeOriginal() {
        return newKitDefaultSaveModeOriginal;
    }

    public boolean isUseMiniMessage() {
        return useMiniMessage;
    }

    // Allow changing the language at runtime and persist it to the main config
    public void setLanguage(String lang){
        if(lang == null) return;
        getConfig().set("language", lang);
        configFile.saveConfig();
        // reload internal fields
        configure();
    }
}
