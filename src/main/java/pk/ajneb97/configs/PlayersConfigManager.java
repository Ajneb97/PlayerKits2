package pk.ajneb97.configs;

import org.bukkit.configuration.file.FileConfiguration;
import pk.ajneb97.PlayerKits2;
import pk.ajneb97.configs.model.CommonConfig;
import pk.ajneb97.model.PlayerData;
import pk.ajneb97.model.PlayerDataKit;
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
        Map<UUID, PlayerData> players = new HashMap<>();

        ArrayList<CommonConfig> configs = getConfigs();
        for(CommonConfig commonConfig : configs){
            FileConfiguration config = commonConfig.getConfig();
            String uuidString = commonConfig.getPath().replace(".yml", "");
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

            UUID uuid = UUID.fromString(uuidString);
            PlayerData playerData = new PlayerData(uuid, name);
            playerData.setKits(playerDataKits);

            players.put(uuid, playerData);
        }

        plugin.getPlayerDataManager().setPlayers(players);
    }

    public void saveConfig(PlayerData playerData){
        String playerName = playerData.getName();
        CommonConfig playerConfig = getConfigFile(playerData.getUuid()+".yml");
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

}
