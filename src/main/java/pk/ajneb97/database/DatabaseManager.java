package pk.ajneb97.database;

import org.bukkit.configuration.file.FileConfiguration;
import pk.ajneb97.PlayerKits2;
import pk.ajneb97.model.PlayerData;
import pk.ajneb97.model.PlayerDataKit;

import java.sql.SQLException;

public class DatabaseManager {

    private PlayerKits2 plugin;
    private DatabaseConnection database;

    public DatabaseManager(PlayerKits2 plugin) {
        this.plugin = plugin;
    }

    public void setupDatabase() {
        FileConfiguration config = plugin.getConfigsManager().getMainConfigManager().getConfig();
        String databaseType = plugin.getConfigsManager().getMainConfigManager().getDatabaseType();

        switch (databaseType) {
            case "mysql":
                database = new MySQLConnection(plugin, "mysql");
                break;
            case "mariadb":
                database = new MySQLConnection(plugin, "mariadb");
                break;
            case "sqlite":
            default:
                database = new SQLiteConnection(plugin);
                break;
        }

        database.setup();
    }

    public void loadData() {
        if (database != null) {
            database.loadData();
        }
    }

    public void getPlayer(String uuid, PlayerCallback callback) {
        if (database != null) {
            database.getPlayer(uuid, callback);
        }
    }

    public void createPlayer(PlayerData player, SimpleCallback callback) {
        if (database != null) {
            database.createPlayer(player, callback);
        }
    }

    public void updatePlayerName(PlayerData player) {
        if (database != null) {
            database.updatePlayerName(player);
        }
    }

    public void updateKit(PlayerData player, PlayerDataKit kit, boolean mustCreate) {
        if (database != null) {
            database.updateKit(player, kit, mustCreate);
        }
    }

    public void resetKit(String uuid, String kitName, boolean all) {
        if (database != null) {
            database.resetKit(uuid, kitName, all);
        }
    }

    public void disable() {
        if (database != null) {
            database.disable();
        }
    }

    public java.sql.Connection getConnection() throws SQLException {
        if (database != null) {
            return database.getConnection();
        }
        throw new SQLException("Database connection is not initialized");
    }

    public boolean isConnected() {
        return database != null && ((BaseDatabaseConnection) database).isConnected();
    }
}