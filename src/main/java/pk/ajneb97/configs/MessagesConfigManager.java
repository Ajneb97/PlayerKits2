package pk.ajneb97.configs;

import org.bukkit.configuration.file.FileConfiguration;
import pk.ajneb97.PlayerKits2;
import pk.ajneb97.managers.MessagesManager;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class MessagesConfigManager {

    private PlayerKits2 plugin;
    private CustomConfig configFile;

    public MessagesConfigManager(PlayerKits2 plugin){
        this.plugin = plugin;
        this.configFile = new CustomConfig("messages.yml",plugin,null, false);
        this.configFile.registerConfig();
        checkUpdate();
    }

    public void configure(){
        FileConfiguration config = configFile.getConfig();

        //Configure messages
        MessagesManager msgManager = new MessagesManager();
        msgManager.setTimeSeconds(config.getString("seconds"));
        msgManager.setTimeMinutes(config.getString("minutes"));
        msgManager.setTimeHours(config.getString("hours"));
        msgManager.setTimeDays(config.getString("days"));
        msgManager.setPrefix(config.getString("prefix"));
        msgManager.setRequirementsMessageStatusSymbolTrue(config.getString("requirementsMessageStatusSymbolTrue"));
        msgManager.setRequirementsMessageStatusSymbolFalse(config.getString("requirementsMessageStatusSymbolFalse"));
        msgManager.setCooldownPlaceholderReady(config.getString("cooldownPlaceholderReady"));

        this.plugin.setMessagesManager(msgManager);
    }

    public void saveConfig(){
        configFile.saveConfig();
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
            if(!text.contains("commandOpenError:")){
                getConfig().set("commandOpenError", "&cYou need to use: &7/kit open <inventory> <player>");
                getConfig().set("inventoryNotExists", "&cThat inventory doesn't exists.");
                saveConfig();
            }
            if(!text.contains("commandPreviewError:")){
                getConfig().set("commandPreviewError", "&cYou need to use: &7/kit preview <kit>");
                getConfig().set("kitPreviewDisabled", "&cKit preview is disabled.");
                saveConfig();
            }
            if(!text.contains("pluginCriticalErrors:")){
                getConfig().set("pluginCriticalErrors", "&cThe plugin has detected some errors. Check them using &7/kit verify");
                saveConfig();
            }

        }catch(IOException e){
            e.printStackTrace();
        }
    }
}
