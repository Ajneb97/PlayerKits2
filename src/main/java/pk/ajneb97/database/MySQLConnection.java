package pk.ajneb97.database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import pk.ajneb97.PlayerKits2;
import pk.ajneb97.managers.MessagesManager;
import pk.ajneb97.model.PlayerData;
import pk.ajneb97.model.PlayerDataKit;

import java.sql.*;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class MySQLConnection extends BaseDatabaseConnection {

    private HikariDataSource hikari;
    private String databaseType;

    public MySQLConnection(PlayerKits2 plugin, String databaseType) {
        super(plugin);
        this.databaseType = databaseType;
    }

    @Override
    public void setup() {
        FileConfiguration config = plugin.getConfigsManager().getMainConfigManager().getConfig();

        try {
            HikariConfig hikariConfig = new HikariConfig();

            // Read configuration from MainConfigManager
            String host = plugin.getConfigsManager().getMainConfigManager().getDatabaseHost();
            int port = plugin.getConfigsManager().getMainConfigManager().getDatabasePort();
            String database = plugin.getConfigsManager().getMainConfigManager().getDatabaseName();
            String username = plugin.getConfigsManager().getMainConfigManager().getDatabaseUsername();
            String password = plugin.getConfigsManager().getMainConfigManager().getDatabasePassword();

            // Choose JDBC URL based on database type
            String jdbcUrl;
            if ("mariadb".equalsIgnoreCase(databaseType)) {
                jdbcUrl = "jdbc:mariadb://" + host + ":" + port + "/" + database;
            } else {
                jdbcUrl = "jdbc:mysql://" + host + ":" + port + "/" + database;
            }

            hikariConfig.setJdbcUrl(jdbcUrl);
            hikariConfig.setUsername(username);
            hikariConfig.setPassword(password);

            // Pool settings (optional)
            long connectionTimeout = plugin.getConfigsManager().getMainConfigManager().getPoolConnectionTimeout();
            hikariConfig.setConnectionTimeout(connectionTimeout);

            if (config.contains("database.pool.maximumPoolSize")) {
                hikariConfig.setMaximumPoolSize(config.getInt("database.pool.maximumPoolSize"));
            }
            if (config.contains("database.pool.keepaliveTime")) {
                hikariConfig.setKeepaliveTime(config.getLong("database.pool.keepaliveTime"));
            }
            if (config.contains("database.pool.idleTimeout")) {
                hikariConfig.setIdleTimeout(config.getLong("database.pool.idleTimeout"));
            }
            if (config.contains("database.pool.maxLifetime")) {
                hikariConfig.setMaxLifetime(config.getLong("database.pool.maxLifetime"));
            }

            // Advanced settings
            if (config.contains("database.advanced")) {
                for (String key : config.getConfigurationSection("database.advanced").getKeys(false)) {
                    hikariConfig.addDataSourceProperty(key, config.get("database.advanced." + key));
                }
            }

            hikari = new HikariDataSource(hikariConfig);
            createTables();
            connected = true;

            Bukkit.getConsoleSender().sendMessage(MessagesManager.getLegacyColoredMessage(
                    plugin.prefix + " &aSuccessfully connected to " + databaseType.toUpperCase() + " Database."
            ));
        } catch (Exception e) {
            Bukkit.getConsoleSender().sendMessage(MessagesManager.getLegacyColoredMessage(
                    plugin.prefix + " &cError while connecting to " + databaseType.toUpperCase() + " Database."
            ));
            e.printStackTrace();
        }
    }

    @Override
    public void loadData() {
        Map<UUID, PlayerData> playerMap = new HashMap<>();
        try (Connection connection = getConnection()) {
            PreparedStatement statement = connection.prepareStatement(
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
        try (Connection connection = getConnection()) {
            // players table
            String createPlayersTable = "CREATE TABLE IF NOT EXISTS playerkits_players (" +
                    "UUID varchar(36) NOT NULL, " +
                    "PLAYER_NAME varchar(50), " +
                    "PRIMARY KEY (UUID)" +
                    ")";

            PreparedStatement statement1 = connection.prepareStatement(createPlayersTable);
            statement1.executeUpdate();

            // players_kits table
            String createKitsTable = "CREATE TABLE IF NOT EXISTS playerkits_players_kits (" +
                    "ID int NOT NULL AUTO_INCREMENT, " +
                    "UUID varchar(36) NOT NULL, " +
                    "NAME varchar(100), " +
                    "COOLDOWN BIGINT, " +
                    "ONE_TIME BOOLEAN, " +
                    "BOUGHT BOOLEAN, " +
                    "PRIMARY KEY (ID), " +
                    "FOREIGN KEY (UUID) REFERENCES playerkits_players(UUID) ON DELETE CASCADE" +
                    ")";

            PreparedStatement statement2 = connection.prepareStatement(createKitsTable);
            statement2.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public java.sql.Connection getConnection() throws SQLException {
        if (hikari == null) {
            throw new SQLException("HikariDataSource is not initialized");
        }
        return hikari.getConnection();
    }

    @Override
    public void disable() {
        if (hikari != null && !hikari.isClosed()) {
            hikari.close();
        }
        connected = false;
    }
}