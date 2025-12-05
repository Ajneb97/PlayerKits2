package pk.ajneb97.database;

import pk.ajneb97.model.PlayerData;
import pk.ajneb97.model.PlayerDataKit;

import java.sql.SQLException;

public interface DatabaseConnection {
    void setup();
    void loadData();
    void getPlayer(String uuid, PlayerCallback callback);
    void createPlayer(PlayerData player, SimpleCallback callback);
    void updatePlayerName(PlayerData player);
    void updateKit(PlayerData player, PlayerDataKit kit, boolean mustCreate);
    void resetKit(String uuid, String kitName, boolean all);
    void disable();
    java.sql.Connection getConnection() throws SQLException; // این خط رو تغییر بده
}