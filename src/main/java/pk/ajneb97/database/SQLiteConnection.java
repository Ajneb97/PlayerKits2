package pk.ajneb97.database;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import pk.ajneb97.PlayerKits2;
import pk.ajneb97.managers.MessagesManager;
import pk.ajneb97.model.PlayerData;
import pk.ajneb97.model.PlayerDataKit;

import java.io.File;
import java.io.IOException;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SQLiteConnection extends BaseDatabaseConnection {

    private Connection connection;
    private File databaseFile;

    public SQLiteConnection(PlayerKits2 plugin) {
        super(plugin);
    }

    @Override
    public void setup() {
        try {
            File dataFolder = plugin.getDataFolder();
            if (!dataFolder.exists()) {
                dataFolder.mkdirs();
            }

            databaseFile = new File(dataFolder, "data.db");
            if (!databaseFile.exists()) {
                try {
                    databaseFile.createNewFile();
                } catch (IOException e) {
                    Bukkit.getConsoleSender().sendMessage(MessagesManager.getLegacyColoredMessage(
                            plugin.prefix + " &cError creating SQLite database file."
                    ));
                    e.printStackTrace();
                    return;
                }
            }

            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:" + databaseFile.getAbsolutePath());
            createTables();
            connected = true;

            Bukkit.getConsoleSender().sendMessage(MessagesManager.getLegacyColoredMessage(
                    plugin.prefix + " &aSuccessfully connected to SQLite Database."
            ));
        } catch (ClassNotFoundException | SQLException e) {
            Bukkit.getConsoleSender().sendMessage(MessagesManager.getLegacyColoredMessage(
                    plugin.prefix + " &cError while connecting to SQLite Database."
            ));
            e.printStackTrace();
        }
    }

    @Override
    public void loadData() {
        Map<UUID, PlayerData> playerMap = new HashMap<>();
        try (Connection conn = getConnection()) {
            PreparedStatement statement = conn.prepareStatement(
                    "SELECT playerkits_players.UUID, playerkits_players.PLAYER_NAME, " +
                            "playerkits_players_kits.NAME, " +
                            "playerkits_players_kits.COOLDOWN, " +
                            "playerkits_players_kits.ONE_TIME, " +
                            "playerkits_players_kits.BOUGHT " +
                            "FROM playerkits_players LEFT JOIN playerkits_players_kits " +
                            "ON playerkits_players.UUID = playerkits_players_kits.UUID");

            ResultSet result = statement.executeQuery();

            while (result.next()) {
                UUID uuid = UUID.fromString(result.getString("UUID"));
                String playerName = result.getString("PLAYER_NAME");
                String kitName = result.getString("NAME");
                long cooldown = result.getLong("COOLDOWN");
                boolean oneTime = result.getBoolean("ONE_TIME");
                boolean bought = result.getBoolean("BOUGHT");

                PlayerData player = playerMap.get(uuid);
                if (player == null) {
                    player = new PlayerData(uuid, playerName);
                    playerMap.put(uuid, player);
                }

                if (kitName != null) {
                    PlayerDataKit playerDataKit = new PlayerDataKit(kitName);
                    playerDataKit.setCooldown(cooldown);
                    playerDataKit.setOneTime(oneTime);
                    playerDataKit.setBought(bought);
                    player.addKit(playerDataKit);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        plugin.getPlayerDataManager().setPlayers(playerMap);
    }

    private void createTables() {
        try (Connection conn = getConnection()) {
            // players table for SQLite
            String createPlayersTable = "CREATE TABLE IF NOT EXISTS playerkits_players (" +
                    "UUID varchar(36) NOT NULL PRIMARY KEY, " +
                    "PLAYER_NAME varchar(50)" +
                    ")";

            PreparedStatement statement1 = conn.prepareStatement(createPlayersTable);
            statement1.executeUpdate();

            // players_kits table for SQLite
            String createKitsTable = "CREATE TABLE IF NOT EXISTS playerkits_players_kits (" +
                    "ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "UUID varchar(36) NOT NULL, " +
                    "NAME varchar(100), " +
                    "COOLDOWN BIGINT, " +
                    "ONE_TIME BOOLEAN, " +
                    "BOUGHT BOOLEAN, " +
                    "FOREIGN KEY (UUID) REFERENCES playerkits_players(UUID) ON DELETE CASCADE" +
                    ")";

            PreparedStatement statement2 = conn.prepareStatement(createKitsTable);
            statement2.executeUpdate();

            // Create index for better performance
            String createIndex = "CREATE INDEX IF NOT EXISTS idx_uuid_name ON playerkits_players_kits(UUID, NAME)";
            PreparedStatement statement3 = conn.prepareStatement(createIndex);
            statement3.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public java.sql.Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            // Try to reconnect
            try {
                connection = DriverManager.getConnection("jdbc:sqlite:" + databaseFile.getAbsolutePath());
            } catch (SQLException e) {
                throw new SQLException("Failed to reconnect to SQLite database", e);
            }
        }
        return connection;
    }

    @Override
    public void disable() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        connected = false;
    }
}