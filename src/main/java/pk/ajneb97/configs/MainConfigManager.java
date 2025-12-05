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
    private String databaseType; // Changed from boolean isMySQL to String databaseType
    private boolean updateNotify;
    private boolean useMiniMessage;
    private boolean dropItemsIfFullInventory;

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

        // Get database type from new config structure
        databaseType = getDatabaseType();

        updateNotify = config.getBoolean("update_notify");
        claimKitShortCommand = config.getBoolean("claim_kit_short_command");
        useMiniMessage = config.getBoolean("use_minimessage");
        newKitDefaultSaveModeOriginal = config.getBoolean("new_kit_default_save_mode_original");
        dropItemsIfFullInventory = config.getBoolean("drop_items_if_full_inventory");
    }

    // Helper method to get database type with backward compatibility
    public String getDatabaseType() {
        FileConfiguration config = configFile.getConfig();
        // Check new database.type configuration first
        if (config.contains("database.type")) {
            String type = config.getString("database.type", "sqlite").toLowerCase();
            // Validate the type
            if (type.equals("sqlite") || type.equals("mysql") || type.equals("mariadb")) {
                return type;
            } else {
                // Invalid type, default to sqlite
                plugin.getLogger().warning("Invalid database type: " + type + ". Defaulting to sqlite.");
                return "sqlite";
            }
        }

        // Fallback to legacy mysql_database.enabled for backward compatibility
        if (config.contains("mysql_database.enabled") && config.getBoolean("mysql_database.enabled")) {
            plugin.getLogger().info("Using legacy mysql_database configuration. Consider migrating to new database structure.");
            return "mysql";
        }

        // Default to sqlite
        return "sqlite";
    }

    // Method to check if using any SQL database (MySQL or MariaDB)
    public boolean isUsingSQLDatabase() {
        return databaseType.equals("mysql") || databaseType.equals("mariadb");
    }

    // Legacy method for backward compatibility
    public boolean isMySQL() {
        // Check legacy config first
        FileConfiguration config = configFile.getConfig();
        if (config.contains("mysql_database.enabled") && config.getBoolean("mysql_database.enabled")) {
            return true;
        }

        // Check new config
        return databaseType.equals("mysql") || databaseType.equals("mariadb");
    }

    // Method to migrate legacy config to new format
    public void migrateLegacyDatabaseConfig() {
        FileConfiguration config = configFile.getConfig();

        // Only migrate if new database config doesn't exist but legacy does
        if (!config.contains("database.type") && config.contains("mysql_database.enabled")) {
            if (config.getBoolean("mysql_database.enabled")) {
                // Migrate legacy MySQL config to new format
                config.set("database.type", "mysql");
                config.set("database.host", config.getString("mysql_database.host", "localhost"));
                config.set("database.port", config.getInt("mysql_database.port", 3306));
                config.set("database.database", config.getString("mysql_database.database", "database"));
                config.set("database.username", config.getString("mysql_database.username", "root"));
                config.set("database.password", config.getString("mysql_database.password", "root"));

                // Migrate pool settings if they exist
                if (config.contains("mysql_database.pool.connectionTimeout")) {
                    config.set("database.pool.connectionTimeout", config.getLong("mysql_database.pool.connectionTimeout"));
                }

                // Migrate advanced settings if they exist
                if (config.contains("mysql_database.advanced")) {
                    for (String key : config.getConfigurationSection("mysql_database.advanced").getKeys(false)) {
                        config.set("database.advanced." + key, config.get("mysql_database.advanced." + key));
                    }
                }

                // Save the config
                configFile.saveConfig();

                plugin.getLogger().info("Migrated legacy MySQL configuration to new database structure.");

                // Optional: Keep legacy config for compatibility or remove it
                // config.set("mysql_database", null);
            }
        }
    }

    // Get database configuration values
    public String getDatabaseHost() {
        FileConfiguration config = configFile.getConfig();
        if (config.contains("database.host")) {
            return config.getString("database.host");
        }
        // Fallback to legacy
        return config.getString("mysql_database.host", "localhost");
    }

    public int getDatabasePort() {
        FileConfiguration config = configFile.getConfig();
        if (config.contains("database.port")) {
            return config.getInt("database.port");
        }
        // Fallback to legacy
        return config.getInt("mysql_database.port", 3306);
    }

    public String getDatabaseName() {
        FileConfiguration config = configFile.getConfig();
        if (config.contains("database.database")) {
            return config.getString("database.database");
        }
        // Fallback to legacy
        return config.getString("mysql_database.database", "playerkits");
    }

    public String getDatabaseUsername() {
        FileConfiguration config = configFile.getConfig();
        if (config.contains("database.username")) {
            return config.getString("database.username");
        }
        // Fallback to legacy
        return config.getString("mysql_database.username", "root");
    }

    public String getDatabasePassword() {
        FileConfiguration config = configFile.getConfig();
        if (config.contains("database.password")) {
            return config.getString("database.password");
        }
        // Fallback to legacy
        return config.getString("mysql_database.password", "");
    }

    // Get pool configuration
    public long getPoolConnectionTimeout() {
        FileConfiguration config = configFile.getConfig();
        if (config.contains("database.pool.connectionTimeout")) {
            return config.getLong("database.pool.connectionTimeout");
        }
        // Fallback to legacy
        return config.getLong("mysql_database.pool.connectionTimeout", 30000L);
    }

    public int getPoolMaximumSize() {
        FileConfiguration config = configFile.getConfig();
        if (config.contains("database.pool.maximumPoolSize")) {
            return config.getInt("database.pool.maximumPoolSize");
        }
        return 10; // Default
    }

    // Check if database configuration exists
    public boolean hasDatabaseConfig() {
        FileConfiguration config = configFile.getConfig();
        return config.contains("database.type") ||
                (config.contains("mysql_database.enabled") && config.getBoolean("mysql_database.enabled"));
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

            // Check for new config structure
            if(!text.contains("database:")){
                // Initialize new database section if it doesn't exist
                getConfig().set("database.type", "sqlite");
                getConfig().set("database.host", "localhost");
                getConfig().set("database.port", 3306);
                getConfig().set("database.database", "playerkits");
                getConfig().set("database.username", "root");
                getConfig().set("database.password", "");
                configFile.saveConfig();
            }

            if(!text.contains("drop_items_if_full_inventory:")){
                getConfig().set("drop_items_if_full_inventory", false);
                configFile.saveConfig();
            }

            if(!text.contains("use_minimessage:")){
                getConfig().set("use_minimessage",false);
                configFile.saveConfig();
            }

            if(!text.contains("verifyServerCertificate:")){
                // Update legacy mysql_database config
                getConfig().set("mysql_database.pool.connectionTimeout",5000);
                getConfig().set("mysql_database.advanced.verifyServerCertificate",false);
                getConfig().set("mysql_database.advanced.useSSL",true);
                getConfig().set("mysql_database.advanced.allowPublicKeyRetrieval",true);

                // Also set defaults for new database config
                if (!text.contains("database.pool.connectionTimeout")) {
                    getConfig().set("database.pool.connectionTimeout", 30000L);
                }
                configFile.saveConfig();
            }

            if(!text.contains("new_kit_default_save_mode_original:")){
                getConfig().set("new_kit_default_save_mode_original", true);
                configFile.saveConfig();
            }

            // Auto-migrate legacy config
            if (text.contains("mysql_database:") && !text.contains("database.type:")) {
                migrateLegacyDatabaseConfig();
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

    public boolean isDropItemsIfFullInventory() {
        return dropItemsIfFullInventory;
    }
}