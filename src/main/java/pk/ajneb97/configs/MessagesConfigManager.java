package pk.ajneb97.configs;

import org.bukkit.configuration.file.FileConfiguration;
import pk.ajneb97.PlayerKits2;
import pk.ajneb97.configs.model.CommonConfig;
import pk.ajneb97.managers.MessagesManager;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class MessagesConfigManager {

    private PlayerKits2 plugin;
    private CommonConfig configFile;

    public MessagesConfigManager(PlayerKits2 plugin){
        this.plugin = plugin;
        this.configFile = new CommonConfig("messages.yml",plugin,null, false);
        this.configFile.registerConfig();
        checkUpdate();
    }

    public void configure(){
        // Load default messages (messages.yml) to use as fallback
        CommonConfig defaultConfigFile = new CommonConfig("messages.yml", plugin, null, false);
        defaultConfigFile.registerConfig();
        FileConfiguration defaultConfig = defaultConfigFile.getConfig();

        // Determine language from main config (default to en)
        String lang = "en";
        try{
            if(this.plugin.getConfigsManager() != null && this.plugin.getConfigsManager().getMainConfigManager() != null) {
                FileConfiguration mainConfig = this.plugin.getConfigsManager().getMainConfigManager().getConfig();
                if(mainConfig != null && mainConfig.contains("language")){
                    lang = mainConfig.getString("language","en");
                }
            }
        }catch(Exception e){
            lang = "en";
        }

        // If language is not english, try to load messages_<lang>.yml, otherwise use default
        if(lang != null && !lang.equalsIgnoreCase("en")){
            CommonConfig langConfig = new CommonConfig("messages_"+lang+".yml", plugin, null, false);
            langConfig.registerConfig();
            this.configFile = langConfig;
        }else{
            this.configFile = defaultConfigFile;
        }

        FileConfiguration config = this.configFile.getConfig();

        //Configure messages with fallback to default messages.yml
        MessagesManager msgManager = new MessagesManager();
        msgManager.setTimeSeconds(config.contains("seconds") ? config.getString("seconds") : defaultConfig.getString("seconds"));
        msgManager.setTimeMinutes(config.contains("minutes") ? config.getString("minutes") : defaultConfig.getString("minutes"));
        msgManager.setTimeHours(config.contains("hours") ? config.getString("hours") : defaultConfig.getString("hours"));
        msgManager.setTimeDays(config.contains("days") ? config.getString("days") : defaultConfig.getString("days"));
        msgManager.setPrefix(config.contains("prefix") ? config.getString("prefix") : defaultConfig.getString("prefix"));
        msgManager.setRequirementsMessageStatusSymbolTrue(config.contains("requirementsMessageStatusSymbolTrue") ?
                config.getString("requirementsMessageStatusSymbolTrue") : defaultConfig.getString("requirementsMessageStatusSymbolTrue"));
        msgManager.setRequirementsMessageStatusSymbolFalse(config.contains("requirementsMessageStatusSymbolFalse") ?
                config.getString("requirementsMessageStatusSymbolFalse") : defaultConfig.getString("requirementsMessageStatusSymbolFalse"));
        msgManager.setCooldownPlaceholderReady(config.contains("cooldownPlaceholderReady") ?
                config.getString("cooldownPlaceholderReady") : defaultConfig.getString("cooldownPlaceholderReady"));

        this.plugin.setMessagesManager(msgManager);
    }

    public void saveConfig(){
        configFile.saveConfig();
    }

    public boolean reloadConfig(){
        // Reload main config first may be needed by caller; here we just select the appropriate messages file
        String lang = "en";
        try{
            if(this.plugin.getConfigsManager() != null && this.plugin.getConfigsManager().getMainConfigManager() != null){
                FileConfiguration mainConfig = this.plugin.getConfigsManager().getMainConfigManager().getConfig();
                if(mainConfig != null && mainConfig.contains("language")){
                    lang = mainConfig.getString("language","en");
                }
            }
        }catch(Exception e){
            lang = "en";
        }

        if(lang != null && !lang.equalsIgnoreCase("en")){
            this.configFile = new CommonConfig("messages_"+lang+".yml", plugin, null, false);
            this.configFile.registerConfig();
        }else{
            this.configFile = new CommonConfig("messages.yml", plugin, null, false);
            this.configFile.registerConfig();
        }

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
        // Always check and update the default messages.yml file (do not modify language-specific files)
        Path pathConfig = Paths.get(plugin.getDataFolder().getPath(), "messages.yml");
        try{
            String text = new String(Files.readAllBytes(pathConfig));
            // Ensure default messages file contains new keys, add them if missing
            CommonConfig defaultCfg = new CommonConfig("messages.yml", plugin, null, false);
            defaultCfg.registerConfig();
            FileConfiguration cfg = defaultCfg.getConfig();

            boolean changed = false;
            if(!text.contains("commandPreviewOtherCorrect:")){
                cfg.set("onlyPlayerCommand", "&cOnly a player can use this command.");
                cfg.set("commandPreviewOtherCorrect", "&aPreviewing kit &7%kit% &ato &e%player%&a.");
                changed = true;
            }
            if(!text.contains("kitResetCorrectAll:")){
                cfg.set("kitResetCorrectAll", "&aKit &7%kit% &areset for &7all players&a!");
                changed = true;
            }
            if(!text.contains("commandOpenError:")){
                cfg.set("commandOpenError", "&cYou need to use: &7/kit open <inventory> <player>");
                cfg.set("inventoryNotExists", "&cThat inventory doesn't exists.");
                changed = true;
            }
            if(!text.contains("commandPreviewError:")){
                cfg.set("commandPreviewError", "&cYou need to use: &7/kit preview <kit>");
                cfg.set("kitPreviewDisabled", "&cKit preview is disabled.");
                changed = true;
            }
            if(!text.contains("pluginCriticalErrors:")){
                cfg.set("pluginCriticalErrors", "&cThe plugin has detected some errors. Check them using &7/kit verify");
                changed = true;
            }

            if(changed){
                defaultCfg.saveConfig();
            }

        }catch(IOException e){
            e.printStackTrace();
        }
    }
}
