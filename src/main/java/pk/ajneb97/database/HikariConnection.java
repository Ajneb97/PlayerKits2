package pk.ajneb97.database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import pk.ajneb97.PlayerKits2;

import java.util.Objects;

public class HikariConnection {

    private HikariDataSource hikari;

    public HikariConnection(String ip, int port, String database, String username, String password, PlayerKits2 plugin) {
        HikariConfig config = new HikariConfig();
        if (Objects.requireNonNull(
                plugin.getConfig().getString("mysql_database.driver")
        ).equalsIgnoreCase("mysql")) {
            config.setDriverClassName("com.mysql.jdbc.Driver");
            config.setJdbcUrl("jdbc:mysql://" + ip + ":" + port + "/" + database);
        } else if (Objects.requireNonNull(
                plugin.getConfig().getString("mysql_database.driver")
        ).equalsIgnoreCase("mariadb")) {
            config.setDriverClassName("org.mariadb.jdbc.Driver");
            config.setJdbcUrl("jdbc:mariadb://" + ip + ":" + port + "/" + database);
        }
        config.setUsername(username);
        config.setPassword(password);
        config.addDataSourceProperty("autoReconnect", "true");
        config.addDataSourceProperty("leakDetectionThreshold", "true");
        config.addDataSourceProperty("verifyServerCertificate", "false");
        config.addDataSourceProperty("useSSL", "false");
        config.setConnectionTimeout(5000);
        hikari = new HikariDataSource(config);
    }

    public HikariDataSource getHikari() {
        return this.hikari;
    }

    public void disable() {
        if (hikari != null) {
            hikari.close();
        }
    }
}