package pk.ajneb97.configs;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.scheduler.BukkitRunnable;
import pk.ajneb97.PlayerKits2;
import pk.ajneb97.configs.model.CommonConfig;
import pk.ajneb97.model.PlayerData;
import pk.ajneb97.model.PlayerDataKit;
import pk.ajneb97.model.internal.GenericCallback;
import pk.ajneb97.model.layout.PlayerKitLayout;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayersConfigManager extends DataFolderConfigManager{

    public PlayersConfigManager(PlayerKits2 plugin, String folderName){
        super(plugin, folderName);
    }

    @Override
    public void createFiles() {

    }

    @Override
    public void loadConfigs(){
        // No use for player config
    }

    public void loadConfig(UUID uuid, GenericCallback<PlayerData> callback) {
        new BukkitRunnable() {
            @Override
            public void run() {
                PlayerData playerData = null;
                CommonConfig playerConfig = getConfigFile(uuid+".yml",false);
                if(playerConfig != null) {
                    // If config exists
                    FileConfiguration config = playerConfig.getConfig();
                    String name = config.getString("name");
                    ArrayList<PlayerDataKit> playerDataKits = new ArrayList<>();
                    if (config.contains("kits")) {
                        for (String key : config.getConfigurationSection("kits").getKeys(false)) {
                            long cooldown = config.getLong("kits." + key + ".cooldown");
                            boolean oneTime = config.getBoolean("kits." + key + ".one_time");
                            boolean bought = config.getBoolean("kits." + key + ".bought");

                            PlayerDataKit playerDataKit = new PlayerDataKit(key);
                            playerDataKit.setCooldown(cooldown);
                            playerDataKit.setOneTime(oneTime);
                            playerDataKit.setBought(bought);

                            playerDataKits.add(playerDataKit);
                        }
                    }

                    playerData = new PlayerData(uuid, name);
                    playerData.setKits(playerDataKits);

                    if (config.contains("layouts")) {
                        for (String key : config.getConfigurationSection("layouts").getKeys(false)) {
                            playerData.putLayout(PlayerKitLayout.deserialize(key, config.getString("layouts." + key)));
                        }
                    }
                }

                PlayerData finalPlayer = playerData;

                new BukkitRunnable(){
                    @Override
                    public void run() {
                        callback.onDone(finalPlayer);
                    }
                }.runTask(plugin);
            }
        }.runTaskAsynchronously(plugin);
    }

    public void saveConfig(PlayerData playerData){
        String playerName = playerData.getName();
        CommonConfig playerConfig = getConfigFile(playerData.getUuid()+".yml",true);
        FileConfiguration config = playerConfig.getConfig();

        config.set("name", playerName);
        config.set("kits",null);

        for(PlayerDataKit playerDataKit : playerData.getKits()){
            String kitName = playerDataKit.getName();
            config.set("kits."+kitName+".cooldown",playerDataKit.getCooldown());
            config.set("kits."+kitName+".one_time",playerDataKit.isOneTime());
            config.set("kits."+kitName+".bought",playerDataKit.isBought());
        }

        config.set("layouts",null);
        for(PlayerKitLayout layout : playerData.getLayouts()){
            config.set("layouts."+layout.getKitName(),layout.serialize());
        }

        playerConfig.saveConfig();
    }

    @Override
    public void saveConfigs(){
        Map<UUID, PlayerData> players = plugin.getPlayerDataManager().getPlayers();
        boolean isMySQL = plugin.getConfigsManager().getMainConfigManager().isMySQL();
        if(!isMySQL){
            for(Map.Entry<UUID, PlayerData> entry : players.entrySet()) {
                PlayerData playerData = entry.getValue();
                if(playerData.isModified()){
                    saveConfig(playerData);
                }
                playerData.setModified(false);
            }
        }
    }

    public void resetKitForAllPlayers(String kitName){
        ArrayList<CommonConfig> configs = getConfigs();
        for(CommonConfig commonConfig : configs) {
            FileConfiguration config = commonConfig.getConfig();
            if(!config.contains("kits."+kitName)){
                continue;
            }
            config.set("kits."+kitName,null);

            commonConfig.saveConfig();
        }
    }
}
