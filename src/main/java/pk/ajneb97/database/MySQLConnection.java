package pk.ajneb97.database;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.scheduler.BukkitRunnable;
import pk.ajneb97.PlayerKits2;
import pk.ajneb97.managers.MessagesManager;
import pk.ajneb97.model.PlayerData;
import pk.ajneb97.model.PlayerDataKit;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class MySQLConnection {

    private PlayerKits2 plugin;
    private HikariConnection connection;
    private String host;
    private String database;
    private String username;
    private String password;
    private int port;

    public MySQLConnection(PlayerKits2 plugin){
        this.plugin = plugin;
    }

    public void setupMySql(){
        FileConfiguration config = plugin.getConfigsManager().getMainConfigManager().getConfig();
        try {
            host = config.getString("mysql_database.host");
            port = Integer.valueOf(config.getString("mysql_database.port"));
            database = config.getString("mysql_database.database");
            username = config.getString("mysql_database.username");
            password = config.getString("mysql_database.password");
            connection = new HikariConnection(host,port,database,username,password);
            connection.getHikari().getConnection();
            createTables();
            loadData();
            Bukkit.getConsoleSender().sendMessage(MessagesManager.getColoredMessage(plugin.prefix+" &aSuccessfully connected to the Database."));
        }catch(Exception e) {
            Bukkit.getConsoleSender().sendMessage(MessagesManager.getColoredMessage(plugin.prefix+" &cError while connecting to the Database."));
        }
    }


    public String getDatabase() {
        return this.database;
    }

    public Connection getConnection() {
        try {
            return connection.getHikari().getConnection();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public void loadData(){
        ArrayList<PlayerData> players = new ArrayList<>();
        try(Connection connection = getConnection()){
            PreparedStatement statement = connection.prepareStatement(
                    "SELECT playerkits_players.UUID, playerkits_players.PLAYER_NAME, " +
                            "playerkits_players_kits.NAME, " +
                            "playerkits_players_kits.COOLDOWN, " +
                            "playerkits_players_kits.ONE_TIME, " +
                            "playerkits_players_kits.BOUGHT " +
                            "FROM playerkits_players LEFT JOIN playerkits_players_kits " +
                            "ON playerkits_players.UUID = playerkits_players_kits.UUID");

            ResultSet result = statement.executeQuery();

            Map<String, PlayerData> playerMap = new HashMap<>();
            while(result.next()){
                String uuid = result.getString("UUID");
                String playerName = result.getString("PLAYER_NAME");
                String kitName = result.getString("NAME");
                long cooldown = result.getLong("COOLDOWN");
                boolean oneTime = result.getBoolean("ONE_TIME");
                boolean bought = result.getBoolean("BOUGHT");

                PlayerData player = playerMap.get(uuid);

                if(player == null){
                    //Create and add it
                    player = new PlayerData(playerName,uuid);
                    players.add(player);
                    playerMap.put(uuid, player);
                }

                if(kitName != null){
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

        plugin.getPlayerDataManager().setPlayers(players);
    }

    public void createTables() {
        try(Connection connection = getConnection()){
            PreparedStatement statement1 = connection.prepareStatement(
                    "CREATE TABLE IF NOT EXISTS playerkits_players" +
                    " (UUID varchar(200) NOT NULL, " +
                    " PLAYER_NAME varchar(50), " +
                    " PRIMARY KEY ( UUID ))"
            );
            statement1.executeUpdate();
            PreparedStatement statement2 = connection.prepareStatement(
                    "CREATE TABLE IF NOT EXISTS playerkits_players_kits" +
                    " (ID int NOT NULL AUTO_INCREMENT, " +
                    " UUID varchar(200) NOT NULL, " +
                    " NAME varchar(100), " +
                    " COOLDOWN BIGINT, " +
                    " ONE_TIME BOOLEAN, " +
                    " BOUGHT BOOLEAN, " +
                    " PRIMARY KEY ( ID ), " +
                    " FOREIGN KEY (UUID) REFERENCES playerkits_players(UUID))");
            statement2.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void getPlayer(String uuid,PlayerCallback callback){
        new BukkitRunnable(){
            @Override
            public void run() {
                PlayerData player = null;
                try(Connection connection = getConnection()){
                    PreparedStatement statement = connection.prepareStatement(
                            "SELECT playerkits_players.UUID, playerkits_players.PLAYER_NAME, " +
                                    "playerkits_players_kits.NAME, " +
                                    "playerkits_players_kits.COOLDOWN, " +
                                    "playerkits_players_kits.ONE_TIME, " +
                                    "playerkits_players_kits.BOUGHT " +
                                    "FROM playerkits_players LEFT JOIN playerkits_players_kits " +
                                    "ON playerkits_players.UUID = playerkits_players_kits.UUID " +
                                    "WHERE playerkits_players.UUID = ?");

                    statement.setString(1, uuid);
                    ResultSet result = statement.executeQuery();

                    boolean firstFind = true;
                    while(result.next()){
                        String playerName = result.getString("PLAYER_NAME");
                        String kitName = result.getString("NAME");
                        long cooldown = result.getLong("COOLDOWN");
                        boolean oneTime = result.getBoolean("ONE_TIME");
                        boolean bought = result.getBoolean("BOUGHT");
                        if(firstFind){
                            firstFind = false;
                            player = new PlayerData(playerName,uuid);
                        }
                        if(kitName != null){
                            PlayerDataKit playerDataKit = new PlayerDataKit(kitName);
                            playerDataKit.setCooldown(cooldown);
                            playerDataKit.setOneTime(oneTime);
                            playerDataKit.setBought(bought);
                            player.addKit(playerDataKit);
                        }
                    }

                    PlayerData finalPlayer = player;
                    new BukkitRunnable(){
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

    public void createPlayer(PlayerData player,SimpleCallback callback){
        new BukkitRunnable(){
            @Override
            public void run() {
                try(Connection connection = getConnection()){
                    PreparedStatement statement = connection.prepareStatement(
                            "INSERT INTO playerkits_players " +
                                    "(UUID, PLAYER_NAME) VALUE (?,?)");

                    statement.setString(1, player.getUuid());
                    statement.setString(2, player.getName());
                    statement.executeUpdate();

                    new BukkitRunnable(){
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

    public void updatePlayerName(PlayerData player){
        new BukkitRunnable(){
            @Override
            public void run() {
                try(Connection connection = getConnection()){
                    PreparedStatement statement = connection.prepareStatement(
                            "UPDATE playerkits_players SET " +
                                    "PLAYER_NAME=? WHERE UUID=?");

                    statement.setString(1, player.getName());
                    statement.setString(2, player.getUuid());
                    statement.executeUpdate();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }.runTaskAsynchronously(plugin);
    }

    public void updateKit(PlayerData player,PlayerDataKit kit,boolean mustCreate){
        new BukkitRunnable(){
            @Override
            public void run() {
                try(Connection connection = getConnection()){
                    PreparedStatement statement = null;
                    if(mustCreate){
                        // Insert
                        statement = connection.prepareStatement(
                                "INSERT INTO playerkits_players_kits " +
                                        "(UUID, NAME, COOLDOWN, ONE_TIME, BOUGHT) VALUE (?,?,?,?,?)");

                        statement.setString(1, player.getUuid());
                        statement.setString(2, kit.getName());
                        statement.setLong(3, kit.getCooldown());
                        statement.setBoolean(4, kit.isOneTime());
                        statement.setBoolean(5, kit.isBought());
                    }else{
                        // Update
                        statement = connection.prepareStatement(
                                "UPDATE playerkits_players_kits SET " +
                                        "COOLDOWN=?, ONE_TIME=?, BOUGHT=? WHERE UUID=? AND NAME=?");

                        statement.setLong(1, kit.getCooldown());
                        statement.setBoolean(2, kit.isOneTime());
                        statement.setBoolean(3, kit.isBought());
                        statement.setString(4, player.getUuid());
                        statement.setString(5, kit.getName());
                    }
                    statement.executeUpdate();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }.runTaskAsynchronously(plugin);
    }

    public void resetKit(String uuid,String kitName){
        new BukkitRunnable(){
            @Override
            public void run() {
                try(Connection connection = getConnection()){
                    PreparedStatement statement = connection.prepareStatement(
                            "DELETE FROM playerkits_players_kits " +
                                    "WHERE UUID=? AND NAME=?");

                    statement.setString(1, uuid);
                    statement.setString(2, kitName);
                    statement.executeUpdate();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }.runTaskAsynchronously(plugin);
    }
}
