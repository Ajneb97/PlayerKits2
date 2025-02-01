package pk.ajneb97.managers;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import pk.ajneb97.PlayerKits2;
import pk.ajneb97.database.MySQLConnection;
import pk.ajneb97.model.PlayerData;
import pk.ajneb97.model.internal.PlayerKitsMessageResult;
import pk.ajneb97.utils.OtherUtils;

import java.util.Collection;
import java.util.Map;
import java.util.UUID;

public final class PlayerDataManager {

    private final PlayerKits2 plugin;
    private Map<UUID, PlayerData> players;

    public PlayerDataManager(PlayerKits2 plugin){
        this.plugin = plugin;
    }

    public Collection<PlayerData> getPlayers() {
        return players.values();
    }

    public void setPlayers(Map<UUID, PlayerData> players) {
        this.players = players;
    }

    private PlayerData getPlayer(final Player player, final boolean createIfAbsent){
        PlayerData data = players.get(player.getUniqueId());
        if (data != null) {
            return data;
        }

        if(createIfAbsent){
            data = new PlayerData(player.getName(),player.getUniqueId());
            data.setModified(true);
            players.put(player.getUniqueId(), data);
            return data;
        }

        return null;
    }

    public PlayerData getPlayerByUUID(final UUID uuid){
        return players.get(uuid);
    }

    public void removePlayerByUUID(final UUID uuid){
        players.remove(uuid);
    }

    public PlayerData getPlayerByName(final String name){
        final Collection<PlayerData> values = players.values();
        for(final PlayerData player : values){
            if(player.getName() != null && player.getName().equals(name)){
                return player;
            }
        }
        return null;
    }

    public void setKitCooldown(final Player player, final String kitName, final long cooldown){
        PlayerData playerData = getPlayer(player,true);
        boolean creating = playerData.setKitCooldown(kitName,cooldown);
        playerData.setModified(true);
        if(plugin.getMySQLConnection() != null){
            plugin.getMySQLConnection().updateKit(playerData,playerData.getKit(kitName),creating);
        }
    }

    public long getKitCooldown(final Player player, final String kitName){
        final PlayerData playerData = getPlayerByUUID(player.getUniqueId());
        return playerData == null ? 0 : playerData.getKitCooldown(kitName);
    }

    public String getKitCooldownString(final long playerCooldown){
        long currentMillis = System.currentTimeMillis();
        long millisDif = playerCooldown-currentMillis;
        String timeStringMillisDif = OtherUtils.getTime(millisDif/1000, plugin.getMessagesManager());
        return timeStringMillisDif;
    }

    public void setKitOneTime(final Player player, final String kitName){
        final PlayerData playerData = getPlayer(player,true);
        final boolean creating = playerData.setKitOneTime(kitName);
        playerData.setModified(true);
        if(plugin.getMySQLConnection() != null){
            plugin.getMySQLConnection().updateKit(playerData,playerData.getKit(kitName),creating);
        }
    }

    public boolean isKitOneTime(final Player player, final String kitName){
        final PlayerData playerData = getPlayerByUUID(player.getUniqueId());
        return playerData == null ? false : playerData.getKitOneTime(kitName);
    }

    public void setKitBought(Player player,String kitName){
        PlayerData playerData = getPlayer(player,true);
        boolean creating = playerData.setKitBought(kitName);
        playerData.setModified(true);
        if(plugin.getMySQLConnection() != null){
            plugin.getMySQLConnection().updateKit(playerData,playerData.getKit(kitName),creating);
        }
    }

    public boolean isKitBought(final Player player, final String kitName){
        final PlayerData playerData = getPlayerByUUID(player.getUniqueId());
        return playerData == null ? false : playerData.getKitHasBought(kitName);
    }

    public PlayerKitsMessageResult resetKitForPlayer(String name, String kitName, boolean all){
        PlayerData playerData = getPlayerByName(name);
        FileConfiguration messagesConfig = plugin.getConfigsManager().getMessagesConfigManager().getConfig();
        if(playerData == null && !all){
            return PlayerKitsMessageResult.error(messagesConfig.getString("playerDataNotFound")
                    .replace("%player%",name));
        }

        if(all){
            for(PlayerData p : players.values()){
                p.resetKit(kitName);
            }
        }else{
            playerData.resetKit(kitName);
        }

        if(plugin.getMySQLConnection() != null){
            if(all){
                plugin.getMySQLConnection().resetKit(null,kitName,true);
            }else{
                plugin.getMySQLConnection().resetKit(playerData.getUuid(),kitName,false);
            }

        }

        return PlayerKitsMessageResult.success();
    }

    public void manageJoin(Player player){
        // Create or update data
        if(plugin.getMySQLConnection() != null){
            handleMysqlPlayer(player);
            return;
        }

        PlayerData playerData = getPlayerByUUID(player.getUniqueId());

        if(playerData == null){
            playerData = new PlayerData(player.getName(),player.getUniqueId());
            playerData.setModified(true);
            players.put(playerData.getUuid(), playerData);
            plugin.getKitsManager().giveFirstJoinKit(player);
            return;
        }

        if(playerData.getName() == null || !playerData.getName().equals(player.getName())){
            playerData.setName(player.getName());
            playerData.setModified(true);
        }
    }

    private void handleMysqlPlayer(final Player player) {
        final MySQLConnection mySQLConnection = plugin.getMySQLConnection();
        final UUID uuid = player.getUniqueId();

        mySQLConnection.getPlayer(uuid, playerData -> {
            removePlayerByUUID(uuid); //Remove data if already exists
            if (playerData == null) {
                playerData = new PlayerData(player.getName(),uuid);
                players.put(playerData.getUuid(), playerData);
    
                //Create if it doesn't exist
                mySQLConnection.createPlayer(playerData, () -> plugin.getKitsManager().giveFirstJoinKit(player));
                return;                    
            }

            players.put(playerData.getUuid(), playerData);
            //Update name if different
            if(!playerData.getName().equals(player.getName())){
                playerData.setName(player.getName());
                mySQLConnection.updatePlayerName(playerData);
            }
        });
    }
}
