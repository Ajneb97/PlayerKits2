package pk.ajneb97.database;

import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;
import pk.ajneb97.PlayerKits2;
import pk.ajneb97.model.PlayerData;
import pk.ajneb97.model.PlayerDataKit;

import java.sql.*;
import java.util.UUID;

public abstract class BaseDatabaseConnection implements DatabaseConnection {

    protected PlayerKits2 plugin;
    protected boolean connected = false;

    public BaseDatabaseConnection(PlayerKits2 plugin) {
        this.plugin = plugin;
    }

    @Override
    public abstract java.sql.Connection getConnection() throws SQLException;

    @Override
    public abstract void loadData();

    @Override
    public abstract void setup();

    @Override
    public void getPlayer(String uuidStr, PlayerCallback callback) {
        new BukkitRunnable() {
            @Override
            public void run() {
                PlayerData player = null;
                try (Connection connection = getConnection()) {
                    PreparedStatement statement = connection.prepareStatement(
                            "SELECT playerkits_players.UUID, playerkits_players.PLAYER_NAME, " +
                                    "playerkits_players_kits.NAME, " +
                                    "playerkits_players_kits.COOLDOWN, " +
                                    "playerkits_players_kits.ONE_TIME, " +
                                    "playerkits_players_kits.BOUGHT " +
                                    "FROM playerkits_players LEFT JOIN playerkits_players_kits " +
                                    "ON playerkits_players.UUID = playerkits_players_kits.UUID " +
                                    "WHERE playerkits_players.UUID = ?");

                    statement.setString(1, uuidStr);
                    ResultSet result = statement.executeQuery();

                    while(result.next()) {
                        UUID uuid = UUID.fromString(result.getString("UUID"));
                        String playerName = result.getString("PLAYER_NAME");
                        String kitName = result.getString("NAME");
                        long cooldown = result.getLong("COOLDOWN");
                        boolean oneTime = result.getBoolean("ONE_TIME");
                        boolean bought = result.getBoolean("BOUGHT");

                        if(player == null) {
                            player = new PlayerData(uuid, playerName);
                        }

                        if(kitName != null) {
                            PlayerDataKit playerDataKit = new PlayerDataKit(kitName);
                            playerDataKit.setCooldown(cooldown);
                            playerDataKit.setOneTime(oneTime);
                            playerDataKit.setBought(bought);
                            player.addKit(playerDataKit);
                        }
                    }

                    PlayerData finalPlayer = player;
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            callback.onDone(finalPlayer);
                        }
                    }.runTask(plugin);
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }.runTaskAsynchronously(plugin);
    }

    @Override
    public void createPlayer(PlayerData player, SimpleCallback callback) {
        new BukkitRunnable() {
            @Override
            public void run() {
                try (Connection connection = getConnection()) {
                    PreparedStatement statement = connection.prepareStatement(
                            "INSERT INTO playerkits_players (UUID, PLAYER_NAME) VALUES (?,?)");

                    statement.setString(1, player.getUuid().toString());
                    statement.setString(2, player.getName());
                    statement.executeUpdate();

                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            callback.onDone();
                        }
                    }.runTask(plugin);
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }.runTaskAsynchronously(plugin);
    }

    @Override
    public void updatePlayerName(PlayerData player) {
        new BukkitRunnable() {
            @Override
            public void run() {
                try (Connection connection = getConnection()) {
                    PreparedStatement statement = connection.prepareStatement(
                            "UPDATE playerkits_players SET PLAYER_NAME=? WHERE UUID=?");

                    statement.setString(1, player.getName());
                    statement.setString(2, player.getUuid().toString());
                    statement.executeUpdate();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }.runTaskAsynchronously(plugin);
    }

    @Override
    public void updateKit(PlayerData player, PlayerDataKit kit, boolean mustCreate) {
        new BukkitRunnable() {
            @Override
            public void run() {
                try (Connection connection = getConnection()) {
                    PreparedStatement statement;
                    if(mustCreate) {
                        statement = connection.prepareStatement(
                                "INSERT INTO playerkits_players_kits (UUID, NAME, COOLDOWN, ONE_TIME, BOUGHT) VALUES (?,?,?,?,?)");

                        statement.setString(1, player.getUuid().toString());
                        statement.setString(2, kit.getName());
                        statement.setLong(3, kit.getCooldown());
                        statement.setBoolean(4, kit.isOneTime());
                        statement.setBoolean(5, kit.isBought());
                    } else {
                        statement = connection.prepareStatement(
                                "UPDATE playerkits_players_kits SET COOLDOWN=?, ONE_TIME=?, BOUGHT=? WHERE UUID=? AND NAME=?");

                        statement.setLong(1, kit.getCooldown());
                        statement.setBoolean(2, kit.isOneTime());
                        statement.setBoolean(3, kit.isBought());
                        statement.setString(4, player.getUuid().toString());
                        statement.setString(5, kit.getName());
                    }
                    statement.executeUpdate();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }.runTaskAsynchronously(plugin);
    }

    @Override
    public void resetKit(String uuid, String kitName, boolean all) {
        new BukkitRunnable() {
            @Override
            public void run() {
                try (Connection connection = getConnection()) {
                    PreparedStatement statement;
                    if(all) {
                        statement = connection.prepareStatement("DELETE FROM playerkits_players_kits WHERE NAME=?");
                        statement.setString(1, kitName);
                    } else {
                        statement = connection.prepareStatement("DELETE FROM playerkits_players_kits WHERE UUID=? AND NAME=?");
                        statement.setString(1, uuid);
                        statement.setString(2, kitName);
                    }
                    statement.executeUpdate();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }.runTaskAsynchronously(plugin);
    }

    @Override
    public abstract void disable();

    public boolean isConnected() {
        return connected;
    }
}