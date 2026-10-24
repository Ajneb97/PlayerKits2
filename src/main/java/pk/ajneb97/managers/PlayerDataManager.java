package pk.ajneb97.managers;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import pk.ajneb97.PlayerKits2;
import pk.ajneb97.configs.PlayersConfigManager;
import pk.ajneb97.database.MySQLConnection;
import pk.ajneb97.model.PlayerData;
import pk.ajneb97.model.internal.GenericCallback;
import pk.ajneb97.model.internal.PlayerKitsMessageResult;
import pk.ajneb97.model.layout.PlayerKitLayout;
import pk.ajneb97.utils.OtherUtils;

import java.util.ArrayList;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class PlayerDataManager {

    private final PlayerKits2 plugin;
    private final Map<UUID, PlayerData> players;
    private final Map<String, UUID> playerNames;

    public PlayerDataManager(PlayerKits2 plugin) {
        this.plugin = plugin;
        this.players = new ConcurrentHashMap<>();
        this.playerNames = new ConcurrentHashMap<>();
    }

    public Map<UUID, PlayerData> getPlayers() {
        return players;
    }

    public void addPlayer(PlayerData playerData) {
        players.put(playerData.getUuid(), playerData);
        if (playerData.getName() != null) {
            playerNames.put(playerData.getName().toLowerCase(), playerData.getUuid());
        }
    }

    public PlayerData getPlayer(Player player, boolean create) {
        PlayerData playerData = players.get(player.getUniqueId());
        if (playerData == null && create) {
            PlayerData fresh = new PlayerData(player.getUniqueId(), player.getName());
            PlayerData race = players.putIfAbsent(player.getUniqueId(), fresh);
            playerData = race != null ? race : fresh;
            if (race == null) {
                playerNames.put(player.getName().toLowerCase(), player.getUniqueId());
            }
        }
        return playerData;
    }

    public boolean isDataReady(Player player) {
        PlayerData playerData = players.get(player.getUniqueId());
        return playerData != null && playerData.isLoaded();
    }

    private void updatePlayerName(String oldName, String newName, UUID uuid) {
        if (oldName != null) {
            playerNames.remove(oldName.toLowerCase(), uuid);
        }
        if (newName != null) {
            playerNames.put(newName.toLowerCase(), uuid);
        }
    }

    public PlayerData getPlayerByUUID(UUID uuid) {
        return players.get(uuid);
    }

    public PlayerData getPlayerByName(String name) {
        if (name == null) {
            return null;
        }
        UUID uuid = playerNames.get(name.toLowerCase());
        return uuid == null ? null : players.get(uuid);
    }

    public void removePlayer(PlayerData playerData) {
        players.remove(playerData.getUuid(), playerData);
        if (playerData.getName() != null) {
            playerNames.remove(playerData.getName().toLowerCase(), playerData.getUuid());
        }
    }

    public void setKitCooldown(Player player, String kitName, long cooldown) {
        PlayerData playerData = getPlayer(player, true);
        boolean creating = playerData.setKitCooldown(kitName, cooldown);
        playerData.setModified(true);
        MySQLConnection mySQLConnection = plugin.getMySQLConnection();
        if (mySQLConnection != null) {
            mySQLConnection.updateKit(playerData, playerData.getKit(kitName), creating);
        }
    }

    public long getKitCooldown(Player player, String kitName) {
        PlayerData playerData = getPlayerByUUID(player.getUniqueId());
        return playerData == null ? 0 : playerData.getKitCooldown(kitName);
    }

    public String getKitCooldownString(long playerCooldown) {
        long millisDif = playerCooldown - System.currentTimeMillis();
        return OtherUtils.getTime(millisDif / 1000, plugin.getMessagesManager());
    }

    public void setKitOneTime(Player player, String kitName) {
        PlayerData playerData = getPlayer(player, true);
        boolean creating = playerData.setKitOneTime(kitName);
        playerData.setModified(true);
        MySQLConnection mySQLConnection = plugin.getMySQLConnection();
        if (mySQLConnection != null) {
            mySQLConnection.updateKit(playerData, playerData.getKit(kitName), creating);
        }
    }

    public boolean isKitOneTime(Player player, String kitName) {
        PlayerData playerData = getPlayerByUUID(player.getUniqueId());
        return playerData != null && playerData.getKitOneTime(kitName);
    }

    public void setKitBought(Player player, String kitName) {
        PlayerData playerData = getPlayer(player, true);
        boolean creating = playerData.setKitBought(kitName);
        playerData.setModified(true);
        MySQLConnection mySQLConnection = plugin.getMySQLConnection();
        if (mySQLConnection != null) {
            mySQLConnection.updateKit(playerData, playerData.getKit(kitName), creating);
        }
    }

    public boolean isKitBought(Player player, String kitName) {
        PlayerData playerData = getPlayerByUUID(player.getUniqueId());
        return playerData != null && playerData.getKitHasBought(kitName);
    }

    public PlayerKitLayout getLayout(Player player, String kitName) {
        PlayerData playerData = getPlayerByUUID(player.getUniqueId());
        return playerData == null ? null : playerData.getLayout(kitName);
    }

    public void saveLayout(Player player, PlayerKitLayout layout) {
        PlayerData playerData = getPlayer(player, true);
        playerData.putLayout(layout);
        MySQLConnection mySQLConnection = plugin.getMySQLConnection();
        if (mySQLConnection != null) {
            mySQLConnection.updateLayout(playerData, layout.getKitName(),
                    layout.isEmpty() ? null : layout.serialize());
        }
    }

    public void clearLayout(Player player, String kitName) {
        PlayerData playerData = getPlayer(player, true);
        playerData.removeLayout(kitName);
        MySQLConnection mySQLConnection = plugin.getMySQLConnection();
        if (mySQLConnection != null) {
            mySQLConnection.updateLayout(playerData, kitName, null);
        }
    }

    public PlayerKitsMessageResult resetKitForPlayer(String playerName, String kitName) {
        FileConfiguration messagesConfig = plugin.getConfigsManager().getMessagesConfigManager().getConfig();
        if (Bukkit.getPlayer(playerName) == null) {
            return PlayerKitsMessageResult.error(MessagesManager.getMessage(messagesConfig,"playerNotOnline")
                    .replace("%player%", playerName));
        }

        PlayerData playerData = getPlayerByName(playerName);
        if (playerData == null) {
            return PlayerKitsMessageResult.error(MessagesManager.getMessage(messagesConfig,"playerDataNotFound")
                    .replace("%player%", playerName));
        }

        playerData.resetKit(kitName);
        MySQLConnection mySQLConnection = plugin.getMySQLConnection();
        if (mySQLConnection != null) {
            mySQLConnection.resetKit(playerData.getUuid().toString(), kitName, false);
        }

        return PlayerKitsMessageResult.success();
    }

    public void resetKitForAllPlayers(String kitName, GenericCallback<PlayerKitsMessageResult> callback) {
        plugin.getPlatformScheduler().runAsync(() -> {
            if (plugin.getMySQLConnection() == null) {
                PlayersConfigManager playerConfigsManager = plugin.getConfigsManager().getPlayersConfigManager();
                playerConfigsManager.resetKitForAllPlayers(kitName);
            }

            plugin.getPlatformScheduler().runGlobal(() -> {
                for (PlayerData playerData : new ArrayList<>(players.values())) {
                    playerData.resetKit(kitName);
                }
                MySQLConnection mySQLConnection = plugin.getMySQLConnection();
                if (mySQLConnection != null) {
                    mySQLConnection.resetKit(null, kitName, true);
                }
                callback.onDone(PlayerKitsMessageResult.success());
            });
        });
    }

    public void manageJoin(Player player) {
        UUID uuid = player.getUniqueId();
        PlayerData placeholder = new PlayerData(uuid, player.getName());
        addPlayer(placeholder);

        MySQLConnection mySQLConnection = plugin.getMySQLConnection();
        if (mySQLConnection != null) {
            mySQLConnection.getPlayer(uuid.toString(), storedData -> {
                if (storedData != null) {
                    applyLoadedData(player, uuid, storedData);
                } else {
                    PlayerData current = players.get(uuid);
                    if (current != null) {
                        current.setLoaded(true);
                    }
                    mySQLConnection.createPlayer(placeholder, () -> plugin.getKitsManager().giveFirstJoinKit(player));
                }
            });
            return;
        }

        plugin.getConfigsManager().getPlayersConfigManager().loadConfig(uuid, storedData -> {
            if (storedData != null) {
                applyLoadedData(player, uuid, storedData);
            } else {
                PlayerData current = players.get(uuid);
                if (current != null) {
                    current.setLoaded(true);
                    current.setModified(true);
                }
                plugin.getKitsManager().giveFirstJoinKit(player);
            }
        });
    }

    private void applyLoadedData(Player player, UUID uuid, PlayerData storedData) {
        PlayerData current = players.get(uuid);
        if (current == null) {
            return;
        }

        current.mergeFrom(storedData);
        current.setLoaded(true);

        String currentName = player.getName();
        String storedName = storedData.getName();
        if (storedName == null || !storedName.equals(currentName)) {
            updatePlayerName(storedName, currentName, uuid);
            current.setName(currentName);
            current.setModified(true);
            MySQLConnection mySQLConnection = plugin.getMySQLConnection();
            if (mySQLConnection != null) {
                mySQLConnection.updatePlayerName(current);
            }
        } else {
            current.setModified(false);
        }
    }

    public void manageLeave(Player player) {
        PlayerData playerData = players.get(player.getUniqueId());
        if (playerData == null) {
            return;
        }

        removePlayer(playerData);

        if (plugin.getMySQLConnection() == null && playerData.isModified()) {
            playerData.setModified(false);
            plugin.getPlatformScheduler().runAsync(() ->
                    plugin.getConfigsManager().getPlayersConfigManager().saveConfig(playerData));
        }
    }
}
