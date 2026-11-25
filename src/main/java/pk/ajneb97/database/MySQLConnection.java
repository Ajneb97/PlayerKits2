package pk.ajneb97.database;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import pk.ajneb97.PlayerKits2;
import pk.ajneb97.managers.MessagesManager;
import pk.ajneb97.model.PlayerData;
import pk.ajneb97.model.PlayerDataKit;
import pk.ajneb97.model.internal.GenericCallback;
import pk.ajneb97.model.internal.SimpleCallback;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;

public class MySQLConnection {

    private PlayerKits2 plugin;
    private HikariConnection connection;
    private final Queue<Runnable> writeQueue = new ConcurrentLinkedQueue<>();
    private final Object writeLock = new Object();
    private final AtomicBoolean draining = new AtomicBoolean(false);
    private BukkitTask writeTask;
    private boolean shuttingDown;

    public MySQLConnection(PlayerKits2 plugin){
        this.plugin = plugin;
    }

    public void setupMySql(){
        FileConfiguration config = plugin.getConfigsManager().getMainConfigManager().getConfig();
        try {
            connection = new HikariConnection(config);
            createTables();
            startWriteWorker();
            Bukkit.getConsoleSender().sendMessage(MessagesManager.getLegacyColoredMessage(plugin.prefix+" &aSuccessfully connected to the Database."));
        }catch(Exception e) {
            Bukkit.getConsoleSender().sendMessage(MessagesManager.getLegacyColoredMessage(plugin.prefix+" &cError while connecting to the Database."));
        }
    }

    public Connection getConnection() throws SQLException {
        if(connection == null){
            throw new SQLException("MySQL connection is not initialized");
        }
        return connection.getHikari().getConnection();
    }

    private void enqueue(Runnable task){
        synchronized(writeLock){
            if(shuttingDown){
                return;
            }
            writeQueue.add(task);
            if(writeTask == null || writeTask.isCancelled()){
                startWriteWorkerLocked();
            }
        }
    }

    private void startWriteWorker(){
        synchronized(writeLock){
            startWriteWorkerLocked();
        }
    }

    private void startWriteWorkerLocked(){
        if(shuttingDown || (writeTask != null && !writeTask.isCancelled())){
            return;
        }
        writeTask = new BukkitRunnable(){
            @Override
            public void run(){
                if(shuttingDown || !draining.compareAndSet(false,true)){
                    return;
                }
                try{
                    Runnable queuedTask;
                    while((queuedTask = writeQueue.poll()) != null){
                        runWriteTask(queuedTask);
                    }
                }finally{
                    draining.set(false);
                }
            }
        }.runTaskTimerAsynchronously(plugin,1L,1L);
    }

    private void runWriteTask(Runnable task){
        try{
            task.run();
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    public void shutdown(){
        synchronized(writeLock){
            shuttingDown = true;
            if(writeTask != null){
                writeTask.cancel();
                writeTask = null;
            }
        }

        while(draining.get()){
            try{
                Thread.sleep(1);
            }catch(InterruptedException e){
                Thread.currentThread().interrupt();
                break;
            }
        }

        Runnable queuedTask;
        while((queuedTask = writeQueue.poll()) != null){
            runWriteTask(queuedTask);
        }

        synchronized(writeLock){
            if(connection != null){
                connection.disable();
                connection = null;
            }
        }
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

    public void getPlayer(String uuid, GenericCallback<PlayerData> callback){
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
                        UUID uuid = UUID.fromString(result.getString("UUID"));
                        String playerName = result.getString("PLAYER_NAME");
                        String kitName = result.getString("NAME");
                        long cooldown = result.getLong("COOLDOWN");
                        boolean oneTime = result.getBoolean("ONE_TIME");
                        boolean bought = result.getBoolean("BOUGHT");
                        if(player == null){
                            player = new PlayerData(uuid,playerName);
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

    public void createPlayer(PlayerData player, SimpleCallback callback){
        final String uuid = player.getUuid().toString();
        final String playerName = player.getName();
        final SimpleCallback finalCallback = callback;
        enqueue(() -> {
            try(Connection connection = getConnection()){
                try(PreparedStatement statement = connection.prepareStatement(
                        "INSERT INTO playerkits_players " +
                                "(UUID, PLAYER_NAME) VALUE (?,?)")){

                    statement.setString(1, uuid);
                    statement.setString(2, playerName);
                    statement.executeUpdate();

                    new BukkitRunnable(){
                        @Override
                        public void run() {
                            finalCallback.onDone();
                        }
                    }.runTask(plugin);
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }

    public void updatePlayerName(PlayerData player){
        final String uuid = player.getUuid().toString();
        final String playerName = player.getName();
        enqueue(() -> {
            try(Connection connection = getConnection()){
                try(PreparedStatement statement = connection.prepareStatement(
                        "UPDATE playerkits_players SET " +
                                "PLAYER_NAME=? WHERE UUID=?")){

                    statement.setString(1, playerName);
                    statement.setString(2, uuid);
                    statement.executeUpdate();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }

    public void updateKit(PlayerData player,PlayerDataKit kit,boolean mustCreate){
        final String uuid = player.getUuid().toString();
        final String kitName = kit.getName();
        final long cooldown = kit.getCooldown();
        final boolean oneTime = kit.isOneTime();
        final boolean bought = kit.isBought();
        final boolean create = mustCreate;
        enqueue(() -> {
            try(Connection connection = getConnection()){
                if(create){
                    try(PreparedStatement statement = connection.prepareStatement(
                            "INSERT INTO playerkits_players_kits " +
                                    "(UUID, NAME, COOLDOWN, ONE_TIME, BOUGHT) VALUE (?,?,?,?,?)")){

                        statement.setString(1, uuid);
                        statement.setString(2, kitName);
                        statement.setLong(3, cooldown);
                        statement.setBoolean(4, oneTime);
                        statement.setBoolean(5, bought);
                        statement.executeUpdate();
                    }
                }else{
                    try(PreparedStatement statement = connection.prepareStatement(
                            "UPDATE playerkits_players_kits SET " +
                                    "COOLDOWN=?, ONE_TIME=?, BOUGHT=? WHERE UUID=? AND NAME=?")){

                        statement.setLong(1, cooldown);
                        statement.setBoolean(2, oneTime);
                        statement.setBoolean(3, bought);
                        statement.setString(4, uuid);
                        statement.setString(5, kitName);
                        statement.executeUpdate();
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }

    public void resetKit(String uuid,String kitName,boolean all){
        enqueue(() -> {
            try(Connection connection = getConnection()){
                if(all){
                    try(PreparedStatement statement = connection.prepareStatement(
                            "DELETE FROM playerkits_players_kits " +
                                    "WHERE NAME=?")){
                        statement.setString(1, kitName);
                        statement.executeUpdate();
                    }
                }else{
                    try(PreparedStatement statement = connection.prepareStatement(
                            "DELETE FROM playerkits_players_kits " +
                                    "WHERE UUID=? AND NAME=?")){

                        statement.setString(1, uuid);
                        statement.setString(2, kitName);
                        statement.executeUpdate();
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }
}
