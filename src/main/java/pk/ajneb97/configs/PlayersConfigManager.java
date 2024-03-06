package pk.ajneb97.configs;

import org.bukkit.configuration.file.FileConfiguration;
import pk.ajneb97.PlayerKits2;
import pk.ajneb97.model.PlayerData;
import pk.ajneb97.model.PlayerDataKit;

import java.io.File;
import java.util.ArrayList;

public class PlayersConfigManager {
    private ArrayList<CustomConfig> configFiles;
    private String folderName;
    private PlayerKits2 plugin;

    public PlayersConfigManager(PlayerKits2 plugin, String folderName){
        this.plugin = plugin;
        this.folderName = folderName;
        this.configFiles = new ArrayList<>();
    }

    public void configure() {
        createFolder();
        reloadConfigs();
    }

    public void reloadConfigs(){
        this.configFiles = new ArrayList<>();
        registerConfigFiles();
        loadConfigs();
    }

    public void createFolder(){
        File folder;
        try {
            folder = new File(plugin.getDataFolder() + File.separator + folderName);
            if(!folder.exists()){
                folder.mkdirs();
            }
        } catch(SecurityException e) {
            folder = null;
        }
    }

    public void saveConfigFiles() {
        for(int i=0;i<configFiles.size();i++) {
            configFiles.get(i).saveConfig();
        }
    }

    public void registerConfigFiles(){
        String path = plugin.getDataFolder() + File.separator + folderName;
        File folder = new File(path);
        File[] listOfFiles = folder.listFiles();
        for (int i=0;i<listOfFiles.length;i++) {
            if(listOfFiles[i].isFile()) {
                String pathName = listOfFiles[i].getName();
                CustomConfig config = new CustomConfig(pathName, plugin, folderName, true);
                config.registerConfig();
                configFiles.add(config);
            }
        }
    }

    public ArrayList<CustomConfig> getConfigs(){
        return this.configFiles;
    }

    public boolean fileAlreadyRegistered(String pathName) {
        for(int i=0;i<configFiles.size();i++) {
            if(configFiles.get(i).getPath().equals(pathName)) {
                return true;
            }
        }
        return false;
    }

    public CustomConfig getConfigFile(String pathName) {
        for(int i=0;i<configFiles.size();i++) {
            if(configFiles.get(i).getPath().equals(pathName)) {
                return configFiles.get(i);
            }
        }
        return null;
    }

    public boolean registerConfigFile(String pathName) {
        if(!fileAlreadyRegistered(pathName)) {
            CustomConfig config = new CustomConfig(pathName, plugin, folderName, true);
            config.registerConfig();
            configFiles.add(config);
            return true;
        }else {
            return false;
        }
    }

    public void loadConfigs(){
        ArrayList<PlayerData> players = new ArrayList<>();

        for(CustomConfig configFile : configFiles) {
            FileConfiguration config = configFile.getConfig();

            String uuid = configFile.getPath().replace(".yml", "");
            String name = config.getString("name");
            ArrayList<PlayerDataKit> playerDataKits = new ArrayList<>();

            if(config.contains("kits")){
                for(String key : config.getConfigurationSection("kits").getKeys(false)){
                    long cooldown = config.getLong("kits."+key+".cooldown");
                    boolean oneTime = config.getBoolean("kits."+key+".one_time");
                    boolean bought = config.getBoolean("kits."+key+".bought");

                    PlayerDataKit playerDataKit = new PlayerDataKit(key);
                    playerDataKit.setCooldown(cooldown);
                    playerDataKit.setOneTime(oneTime);
                    playerDataKit.setBought(bought);

                    playerDataKits.add(playerDataKit);
                }
            }

            PlayerData playerData = new PlayerData(name,uuid);
            playerData.setKits(playerDataKits);

            players.add(playerData);
        }

        plugin.getPlayerDataManager().setPlayers(players);
    }

    public void saveConfig(PlayerData playerData){
        String playerName = playerData.getName();
        CustomConfig playerConfig = getConfigFile(playerData.getUuid()+".yml");
        if(playerConfig == null) {
            registerConfigFile(playerData.getUuid()+".yml");
            playerConfig = getConfigFile(playerData.getUuid()+".yml");
        }
        FileConfiguration config = playerConfig.getConfig();

        config.set("name", playerName);
        config.set("kits",null);

        for(PlayerDataKit playerDataKit : playerData.getKits()){
            String kitName = playerDataKit.getName();
            config.set("kits."+kitName+".cooldown",playerDataKit.getCooldown());
            config.set("kits."+kitName+".one_time",playerDataKit.isOneTime());
            config.set("kits."+kitName+".bought",playerDataKit.isBought());
        }

        playerConfig.saveConfig();
    }

    public void saveConfigs(){
        ArrayList<PlayerData> players = plugin.getPlayerDataManager().getPlayers();
        for(PlayerData playerData : players) {
            if(playerData.isModified()){
                saveConfig(playerData);
            }
            playerData.setModified(false);
        }
    }

}
