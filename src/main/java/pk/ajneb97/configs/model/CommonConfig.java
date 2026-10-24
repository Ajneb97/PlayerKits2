package pk.ajneb97.configs.model;

import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import pk.ajneb97.PlayerKits2;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;

public class CommonConfig {

    private final String fileName;
    private FileConfiguration fileConfiguration = null;
    private File file = null;
    private String route;
    private final PlayerKits2 plugin;
    private final String folderName;
    private final boolean newFile;
    private boolean isFirstTime;

    public CommonConfig(String fileName, PlayerKits2 plugin, String folderName, boolean newFile){
        this.fileName = fileName;
        this.plugin = plugin;
        this.newFile = newFile;
        this.folderName = folderName;
        this.isFirstTime = false;
    }

    public String getPath(){
        return this.fileName;
    }

    public void registerConfig(){
        if(folderName != null){
            file = new File(plugin.getDataFolder() +File.separator + folderName,fileName);
        }else{
            file = new File(plugin.getDataFolder(), fileName);
        }

        route = file.getPath();

        if(!file.exists()){
            isFirstTime = true;
            if(newFile) {
                try {
                    if(!file.createNewFile()){
                        plugin.getLogger().warning("Could not create "+file.getPath()+" because it already exists.");
                    }
                } catch (IOException e) {
                    plugin.getLogger().log(Level.SEVERE,"Could not create "+file.getPath(),e);
                }
            }else {
                if(folderName != null){
                    plugin.saveResource(folderName+File.separator+fileName, false);
                }else{
                    plugin.saveResource(fileName, false);
                }

            }
        }

        fileConfiguration = new YamlConfiguration();
        try {
            fileConfiguration.load(file);
        } catch (IOException | InvalidConfigurationException e) {
            plugin.getLogger().log(Level.SEVERE,"Could not load "+file.getPath(),e);
        }
    }
    public void saveConfig() {
        try {
            fileConfiguration.save(file);
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE,"Could not save "+file.getPath(),e);
        }
    }

    public FileConfiguration getConfig() {
        if (fileConfiguration == null) {
            reloadConfig();
        }
        return fileConfiguration;
    }

    public boolean reloadConfig() {
        if (fileConfiguration == null) {
            if(folderName != null){
                file = new File(plugin.getDataFolder() +File.separator + folderName, fileName);
            }else{
                file = new File(plugin.getDataFolder(), fileName);
            }

        }
        YamlConfiguration loadedConfiguration = new YamlConfiguration();
        try {
            loadedConfiguration.load(file);
        } catch (IOException | InvalidConfigurationException e) {
            plugin.getLogger().log(Level.SEVERE,"Could not reload "+file.getPath(),e);
            if(fileConfiguration == null) {
                fileConfiguration = loadedConfiguration;
            }
            return false;
        }
        loadedConfiguration.setDefaults(YamlConfiguration.loadConfiguration(file));
        fileConfiguration = loadedConfiguration;
        return true;
    }

    public String getRoute() {
        return route;
    }

    public boolean isFirstTime() {
        return isFirstTime;
    }
}
