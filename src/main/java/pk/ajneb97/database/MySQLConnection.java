package pk.ajneb97.database;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import pk.ajneb97.PlayerKits2;
import pk.ajneb97.managers.MessagesManager;
import pk.ajneb97.model.PlayerData;
import pk.ajneb97.model.PlayerDataKit;
import pk.ajneb97.model.internal.GenericCallback;
import pk.ajneb97.model.internal.SimpleCallback;
import pk.ajneb97.model.layout.PlayerKitLayout;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public class MySQLConnection {

    private final PlayerKits2 plugin;
    private HikariConnection connection;

    public MySQLConnection(PlayerKits2 plugin) {
        this.plugin = plugin;
    }

    public void setupMySql() {
        FileConfiguration config = plugin.getConfigsManager().getMainConfigManager().getConfig();
        try {
            connection = new HikariConnection(config);
            try (Connection test = connection.getHikari().getConnection()) {
                if (test == null) {
                    throw new SQLException("No connection available.");
                }
            }
            createTables();
            Bukkit.getConsoleSender().sendMessage(MessagesManager.getLegacyColoredMessage(PlayerKits2.prefix + " &aSuccessfully connected to the Database."));
        } catch (Exception error) {
            connection = null;
            Bukkit.getConsoleSender().sendMessage(MessagesManager.getLegacyColoredMessage(PlayerKits2.prefix + " &cError while connecting to the Database."));
            plugin.getLogger().severe("Database connection failed: " + error);
        }
    }

    public boolean isAvailable() {
        return connection != null;
    }

    public Connection getConnection() {
        try {
            return connection.getHikari().getConnection();
        } catch (Exception error) {
            plugin.getLogger().severe("Could not obtain a database connection: " + error);
            return null;
        }
    }

    public void shutdown() {
        if (connection != null) {
            connection.disable();
            connection = null;
        }
    }

    public void createTables() {
        try (Connection connection = getConnection()) {
            if (connection == null) {
                return;
            }
            try (PreparedStatement statement = connection.prepareStatement(
                    "CREATE TABLE IF NOT EXISTS playerkits_players" +
                            " (UUID varchar(36) NOT NULL, " +
                            " PLAYER_NAME varchar(50), " +
                            " PRIMARY KEY ( UUID ))")) {
                statement.executeUpdate();
            }
            try (PreparedStatement statement = connection.prepareStatement(
                    "CREATE TABLE IF NOT EXISTS playerkits_players_kits" +
                            " (ID int NOT NULL AUTO_INCREMENT, " +
                            " UUID varchar(36) NOT NULL, " +
                            " NAME varchar(100), " +
                            " COOLDOWN BIGINT, " +
                            " ONE_TIME BOOLEAN, " +
                            " BOUGHT BOOLEAN, " +
                            " PRIMARY KEY ( ID ), " +
                            " FOREIGN KEY (UUID) REFERENCES playerkits_players(UUID))")) {
                statement.executeUpdate();
            }
            try (PreparedStatement statement = connection.prepareStatement(
                    "CREATE TABLE IF NOT EXISTS playerkits_players_layouts" +
                            " (UUID varchar(36) NOT NULL, " +
                            " NAME varchar(100) NOT NULL, " +
                            " LAYOUT varchar(255) NOT NULL, " +
                            " PRIMARY KEY ( UUID, NAME ), " +
                            " FOREIGN KEY (UUID) REFERENCES playerkits_players(UUID))")) {
                statement.executeUpdate();
            }
        } catch (SQLException error) {
            plugin.getLogger().severe("Could not create database tables: " + error);
        }
    }

    public void getPlayer(String uuid, GenericCallback<PlayerData> callback) {
        plugin.getPlatformScheduler().runAsync(() -> {
            PlayerData player = null;
            try (Connection connection = getConnection()) {
                if (connection != null) {
                    player = readPlayer(connection, uuid);
                }
            } catch (SQLException error) {
                plugin.getLogger().severe("Could not load player data for " + uuid + ": " + error);
            }

            PlayerData finalPlayer = player;
            plugin.getPlatformScheduler().runGlobal(() -> callback.onDone(finalPlayer));
        });
    }

    private PlayerData readPlayer(Connection connection, String uuid) throws SQLException {
        PlayerData player = null;
        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT playerkits_players.UUID, playerkits_players.PLAYER_NAME, " +
                        "playerkits_players_kits.NAME, " +
                        "playerkits_players_kits.COOLDOWN, " +
                        "playerkits_players_kits.ONE_TIME, " +
                        "playerkits_players_kits.BOUGHT " +
                        "FROM playerkits_players LEFT JOIN playerkits_players_kits " +
                        "ON playerkits_players.UUID = playerkits_players_kits.UUID " +
                        "WHERE playerkits_players.UUID = ?")) {

            statement.setString(1, uuid);
            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    if (player == null) {
                        player = new PlayerData(UUID.fromString(result.getString("UUID")),
                                result.getString("PLAYER_NAME"));
                    }
                    String kitName = result.getString("NAME");
                    if (kitName != null) {
                        PlayerDataKit playerDataKit = new PlayerDataKit(kitName);
                        playerDataKit.setCooldown(result.getLong("COOLDOWN"));
                        playerDataKit.setOneTime(result.getBoolean("ONE_TIME"));
                        playerDataKit.setBought(result.getBoolean("BOUGHT"));
                        player.addKit(playerDataKit);
                    }
                }
            }
        }

        if (player == null) {
            return null;
        }

        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT NAME, LAYOUT FROM playerkits_players_layouts WHERE UUID = ?")) {
            statement.setString(1, uuid);
            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    PlayerKitLayout layout = PlayerKitLayout.deserialize(
                            result.getString("NAME"), result.getString("LAYOUT"));
                    if (layout != null) {
                        player.putLayout(layout);
                    }
                }
            }
        }

        player.setModified(false);
        return player;
    }

    public void createPlayer(PlayerData player, SimpleCallback callback) {
        plugin.getPlatformScheduler().runAsync(() -> {
            boolean created = false;
            try (Connection connection = getConnection()) {
                if (connection != null) {
                    try (PreparedStatement statement = connection.prepareStatement(
                            "INSERT INTO playerkits_players (UUID, PLAYER_NAME) VALUES (?,?) " +
                                    "ON DUPLICATE KEY UPDATE PLAYER_NAME=VALUES(PLAYER_NAME)")) {
                        statement.setString(1, player.getUuid().toString());
                        statement.setString(2, player.getName());
                        statement.executeUpdate();
                        created = true;
                    }
                }
            } catch (SQLException error) {
                plugin.getLogger().severe("Could not create player row for " + player.getUuid() + ": " + error);
            }

            if (created) {
                plugin.getPlatformScheduler().runGlobal(callback::onDone);
            }
        });
    }

    public void updatePlayerName(PlayerData player) {
        plugin.getPlatformScheduler().runAsync(() -> {
            try (Connection connection = getConnection()) {
                if (connection == null) {
                    return;
                }
                try (PreparedStatement statement = connection.prepareStatement(
                        "UPDATE playerkits_players SET PLAYER_NAME=? WHERE UUID=?")) {
                    statement.setString(1, player.getName());
                    statement.setString(2, player.getUuid().toString());
                    statement.executeUpdate();
                }
            } catch (SQLException error) {
                plugin.getLogger().severe("Could not update player name for " + player.getUuid() + ": " + error);
            }
        });
    }

    public void updateKit(PlayerData player, PlayerDataKit kit, boolean mustCreate) {
        if (kit == null) {
            return;
        }
        String uuid = player.getUuid().toString();
        String kitName = kit.getName();
        long cooldown = kit.getCooldown();
        boolean oneTime = kit.isOneTime();
        boolean bought = kit.isBought();

        plugin.getPlatformScheduler().runAsync(() -> {
            try (Connection connection = getConnection()) {
                if (connection == null) {
                    return;
                }
                if (mustCreate) {
                    try (PreparedStatement statement = connection.prepareStatement(
                            "INSERT INTO playerkits_players_kits (UUID, NAME, COOLDOWN, ONE_TIME, BOUGHT) VALUES (?,?,?,?,?)")) {
                        statement.setString(1, uuid);
                        statement.setString(2, kitName);
                        statement.setLong(3, cooldown);
                        statement.setBoolean(4, oneTime);
                        statement.setBoolean(5, bought);
                        statement.executeUpdate();
                    }
                } else {
                    try (PreparedStatement statement = connection.prepareStatement(
                            "UPDATE playerkits_players_kits SET COOLDOWN=?, ONE_TIME=?, BOUGHT=? WHERE UUID=? AND NAME=?")) {
                        statement.setLong(1, cooldown);
                        statement.setBoolean(2, oneTime);
                        statement.setBoolean(3, bought);
                        statement.setString(4, uuid);
                        statement.setString(5, kitName);
                        statement.executeUpdate();
                    }
                }
            } catch (SQLException error) {
                plugin.getLogger().severe("Could not update kit data for " + uuid + ": " + error);
            }
        });
    }

    public void updateLayout(PlayerData player, String kitName, String serialized) {
        String uuid = player.getUuid().toString();
        plugin.getPlatformScheduler().runAsync(() -> {
            try (Connection connection = getConnection()) {
                if (connection == null) {
                    return;
                }
                if (serialized == null || serialized.isEmpty()) {
                    try (PreparedStatement statement = connection.prepareStatement(
                            "DELETE FROM playerkits_players_layouts WHERE UUID=? AND NAME=?")) {
                        statement.setString(1, uuid);
                        statement.setString(2, kitName);
                        statement.executeUpdate();
                    }
                    return;
                }
                try (PreparedStatement statement = connection.prepareStatement(
                        "INSERT INTO playerkits_players_layouts (UUID, NAME, LAYOUT) VALUES (?,?,?) " +
                                "ON DUPLICATE KEY UPDATE LAYOUT=VALUES(LAYOUT)")) {
                    statement.setString(1, uuid);
                    statement.setString(2, kitName);
                    statement.setString(3, serialized);
                    statement.executeUpdate();
                }
            } catch (SQLException error) {
                plugin.getLogger().severe("Could not update layout data for " + uuid + ": " + error);
            }
        });
    }

    public void resetKit(String uuid, String kitName, boolean all) {
        plugin.getPlatformScheduler().runAsync(() -> {
            try (Connection connection = getConnection()) {
                if (connection == null) {
                    return;
                }
                if (all) {
                    try (PreparedStatement statement = connection.prepareStatement(
                            "DELETE FROM playerkits_players_kits WHERE NAME=?")) {
                        statement.setString(1, kitName);
                        statement.executeUpdate();
                    }
                } else {
                    try (PreparedStatement statement = connection.prepareStatement(
                            "DELETE FROM playerkits_players_kits WHERE UUID=? AND NAME=?")) {
                        statement.setString(1, uuid);
                        statement.setString(2, kitName);
                        statement.executeUpdate();
                    }
                }
            } catch (SQLException error) {
                plugin.getLogger().severe("Could not reset kit data: " + error);
            }
        });
    }
}
