package pk.ajneb97.managers;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import pk.ajneb97.PlayerKits2;
import pk.ajneb97.database.MySQLConnection;
import pk.ajneb97.model.PlayerData;
import pk.ajneb97.model.internal.PlayerKitsMessageResult;
import pk.ajneb97.utils.OtherUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerDataManager {

    private PlayerKits2 plugin;
    private Map<UUID, PlayerData> players;
    private Map<String,UUID> playerNames;

    public PlayerDataManager(PlayerKits2 plugin){
        this.plugin = plugin;
        this.playerNames = new HashMap<>();
    }

    public Map<UUID,PlayerData> getPlayers() {
        return players;
    }

    public void setPlayers(Map<UUID,PlayerData> players) {
        this.players = players;
        for(Map.Entry<UUID, PlayerData> entry : players.entrySet()){
            playerNames.put(entry.getValue().getName(),entry.getKey());
        }
    }

    public void addPlayer(PlayerData p){
        players.put(p.getUuid(),p);
        playerNames.put(p.getName(), p.getUuid());
    }

    public PlayerData getPlayer(Player player, boolean create){
        PlayerData playerData = players.get(player.getUniqueId());
        if(playerData == null && create){
            playerData = new PlayerData(player.getUniqueId(),player.getName());
            addPlayer(playerData);
        }
        return playerData;
    }

    private void updatePlayerName(String oldName,String newName,UUID uuid){
        if(oldName != null){
            playerNames.remove(oldName);
        }
        playerNames.put(newName,uuid);
    }

    public PlayerData getPlayerByUUID(UUID uuid){
        return players.get(uuid);
    }

    private UUID getPlayerUUID(String name){
        return playerNames.get(name);
    }

    public PlayerData getPlayerByName(String name){
        UUID uuid = getPlayerUUID(name);
        return players.get(uuid);
    }

    public void removePlayerByUUID(UUID uuid){
        players.remove(uuid);
    }

    public void setKitCooldown(Player player,String kitName,long cooldown){
        PlayerData playerData = getPlayer(player,true);
        boolean creating = playerData.setKitCooldown(kitName,cooldown);
        playerData.setModified(true);
        if(plugin.getMySQLConnection() != null){
            plugin.getMySQLConnection().updateKit(playerData,playerData.getKit(kitName),creating);
        }
    }

    public long getKitCooldown(Player player,String kitName){
        PlayerData playerData = getPlayerByUUID(player.getUniqueId());
        if(playerData == null){
            return 0;
        }else{
            return playerData.getKitCooldown(kitName);
        }
    }

    public String getKitCooldownString(long playerCooldown){
        long currentMillis = System.currentTimeMillis();
        long millisDif = playerCooldown-currentMillis;
        String timeStringMillisDif = OtherUtils.getTime(millisDif/1000, plugin.getMessagesManager());
        return timeStringMillisDif;
    }

    public void setKitOneTime(Player player,String kitName){
        PlayerData playerData = getPlayer(player,true);
        boolean creating = playerData.setKitOneTime(kitName);
        playerData.setModified(true);
        if(plugin.getMySQLConnection() != null){
            plugin.getMySQLConnection().updateKit(playerData,playerData.getKit(kitName),creating);
        }
    }

    public boolean isKitOneTime(Player player,String kitName){
        PlayerData playerData = getPlayerByUUID(player.getUniqueId());
        if(playerData == null){
            return false;
        }else{
            return playerData.getKitOneTime(kitName);
        }
    }

    public void setKitBought(Player player,String kitName){
        PlayerData playerData = getPlayer(player,true);
        boolean creating = playerData.setKitBought(kitName);
        playerData.setModified(true);
        if(plugin.getMySQLConnection() != null){
            plugin.getMySQLConnection().updateKit(playerData,playerData.getKit(kitName),creating);
        }
    }

    public boolean isKitBought(Player player,String kitName){
        PlayerData playerData = getPlayerByUUID(player.getUniqueId());
        if(playerData == null){
            return false;
        }else{
            return playerData.getKitHasBought(kitName);
        }
    }

    public PlayerKitsMessageResult resetKitForPlayer(String name, String kitName, boolean all){
        PlayerData playerData = getPlayerByName(name);
        FileConfiguration messagesConfig = plugin.getConfigsManager().getMessagesConfigManager().getConfig();
        if(playerData == null && !all){
            return PlayerKitsMessageResult.error(messagesConfig.getString("playerDataNotFound")
                    .replace("%player%",name));
        }

        if(all){
            for(Map.Entry<UUID, PlayerData> entry : players.entrySet()){
                entry.getValue().resetKit(kitName);
            }
        }else{
            playerData.resetKit(kitName);
        }

        if(plugin.getMySQLConnection() != null){
            if(all){
                plugin.getMySQLConnection().resetKit(null,kitName,true);
            }else{
                plugin.getMySQLConnection().resetKit(playerData.getUuid().toString(),kitName,false);
            }

        }

        return PlayerKitsMessageResult.success();
    }

    public void manageJoin(Player player){
        // Create or update data
        if(plugin.getMySQLConnection() != null){
            MySQLConnection mySQLConnection = plugin.getMySQLConnection();
            UUID uuid = player.getUniqueId();
            mySQLConnection.getPlayer(uuid.toString(), playerData -> {
                removePlayerByUUID(uuid); //Remove data if already exists
                if(playerData != null) {
                    addPlayer(playerData);
                    //Update name if different
                    if (!playerData.getName().equals(player.getName())) {
                        updatePlayerName(playerData.getName(), player.getName(), player.getUniqueId());
                        playerData.setName(player.getName());
                        mySQLConnection.updatePlayerName(playerData);
                    }
                }else {
                    playerData = new PlayerData(uuid, player.getName());
                    addPlayer(playerData);

                    //Create if it doesn't exist
                    mySQLConnection.createPlayer(playerData, () -> plugin.getKitsManager().giveFirstJoinKit(player));
                }
            });
        }else{
            PlayerData playerData = getPlayer(player,false);
            if(playerData == null){
                playerData = new PlayerData(player.getUniqueId(),player.getName());
                playerData.setModified(true);
                addPlayer(playerData);
                plugin.getKitsManager().giveFirstJoinKit(player);
            }else{
                if(playerData.getName() == null || !playerData.getName().equals(player.getName())){
                    updatePlayerName(playerData.getName(),player.getName(),player.getUniqueId());
                    playerData.setName(player.getName());
                    playerData.setModified(true);
                }
            }
        }
    }
}
