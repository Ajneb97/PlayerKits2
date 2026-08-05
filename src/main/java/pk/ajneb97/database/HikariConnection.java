package pk.ajneb97.database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

public class HikariConnection {

    private final HikariDataSource hikari;

    public HikariConnection(FileConfiguration config) {
        HikariConfig hikariConfig = new HikariConfig();
        String path = "mysql_database";

        String host = config.getString(path + ".host");
        int port = config.getInt(path + ".port");
        String database = config.getString(path + ".database");

        hikariConfig.setPoolName("BetterPlayerKits2");
        hikariConfig.setJdbcUrl("jdbc:mysql://" + host + ":" + port + "/" + database);
        hikariConfig.setUsername(config.getString(path + ".username"));
        hikariConfig.setPassword(config.getString(path + ".password"));

        if (config.contains(path + ".pool.connectionTimeout")) {
            hikariConfig.setConnectionTimeout(config.getLong(path + ".pool.connectionTimeout"));
        }
        if (config.contains(path + ".pool.maximumPoolSize")) {
            hikariConfig.setMaximumPoolSize(config.getInt(path + ".pool.maximumPoolSize"));
        }
        if (config.contains(path + ".pool.keepaliveTime")) {
            hikariConfig.setKeepaliveTime(config.getLong(path + ".pool.keepaliveTime"));
        }
        if (config.contains(path + ".pool.idleTimeout")) {
            hikariConfig.setIdleTimeout(config.getLong(path + ".pool.idleTimeout"));
        }
        if (config.contains(path + ".pool.maxLifetime")) {
            hikariConfig.setMaxLifetime(config.getLong(path + ".pool.maxLifetime"));
        }

        ConfigurationSection advanced = config.getConfigurationSection(path + ".advanced");
        if (advanced != null) {
            for (String key : advanced.getKeys(false)) {
                hikariConfig.addDataSourceProperty(key, advanced.get(key));
            }
        }

        hikari = new HikariDataSource(hikariConfig);
    }

    public HikariDataSource getHikari() {
        return this.hikari;
    }

    public void disable() {
        if (hikari != null && !hikari.isClosed()) {
            hikari.close();
        }
    }
}
