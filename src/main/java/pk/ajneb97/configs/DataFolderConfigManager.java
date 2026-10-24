package pk.ajneb97.configs;

import pk.ajneb97.PlayerKits2;
import pk.ajneb97.configs.model.CommonConfig;

import java.io.File;
import java.util.ArrayList;

public abstract class DataFolderConfigManager {
    protected final String folderName;
    protected final PlayerKits2 plugin;

    public DataFolderConfigManager(PlayerKits2 plugin, String folderName){
        this.plugin = plugin;
        this.folderName = folderName;
    }

    public void configure() {
        createFolder();
        loadConfigs();
    }

    public void createFolder(){
        try {
            File folder = new File(plugin.getDataFolder() + File.separator + folderName);
            if(!folder.exists()){
                if(!folder.mkdirs()){
                    plugin.getLogger().warning("Could not create folder "+folder.getPath());
                    return;
                }
                createFiles();
            }
        } catch(SecurityException e) {
            plugin.getLogger().warning("Could not create folder "+folderName+": "+e.getMessage());
        }
    }

    public CommonConfig getConfigFile(String pathName,boolean create) {
        String pathFile = plugin.getDataFolder() + File.separator + folderName;
        File folder = new File(pathFile);
        File file = new File(folder, pathName);
        if(!file.exists() && !create) {
            return null;
        }

        CommonConfig commonConfig = new CommonConfig(pathName, plugin, folderName, true);
        commonConfig.registerConfig();
        return commonConfig;
    }

    public ArrayList<CommonConfig> getConfigs(){
        ArrayList<CommonConfig> configs = new ArrayList<>();

        String pathFile = plugin.getDataFolder() + File.separator + folderName;
        File folder = new File(pathFile);
        File[] listOfFiles = folder.listFiles();
        if (listOfFiles == null) {
            return configs;
        }
        for (File file : listOfFiles) {
            if (file.isFile() && file.getName().endsWith(".yml")) {
                CommonConfig commonConfig = new CommonConfig(file.getName(), plugin, folderName, true);
                commonConfig.registerConfig();
                configs.add(commonConfig);
            }
        }

        return configs;
    }

    public abstract void createFiles();

    public abstract void loadConfigs();
}
