package pk.ajneb97.database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.apache.logging.log4j.util.Strings;
import org.bukkit.configuration.ConfigurationSection;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;

public class HikariConnection {

    private final HikariDataSource hikari;
    private final String database;

    public HikariConnection(ConfigurationSection configSection) throws URISyntaxException {
        HikariConfig config = new HikariConfig();
        this.database = configSection.getString("database", "database");
        URI databaseUri = buildDatabaseUri(configSection);
        config.setJdbcUrl(databaseUri.toString());
        config.setUsername(configSection.getString("username", "root"));
        config.setPassword(configSection.getString("password", "root"));
        ConfigurationSection driverSection = configSection.getConfigurationSection("driver_properties");
        if (driverSection != null) {
            setAdditionalProperties(config, driverSection);
        }
        ConfigurationSection hikariSection = configSection.getConfigurationSection("driver_properties");
        if (hikariSection != null) {
            setHikariProperties(hikariSection, config);
        }
        hikari = new HikariDataSource(config);
    }

    private void setAdditionalProperties(HikariConfig config, ConfigurationSection section) {
        config.addDataSourceProperty("autoReconnect", "true");
        config.addDataSourceProperty("leakDetectionThreshold", "true");
        config.addDataSourceProperty("verifyServerCertificate", "false");
        config.addDataSourceProperty("useSSL", "false");
        for (String key : section.getKeys(false)) {
            String value = section.getString(key);
            if (Strings.isBlank(value)) continue;
            config.addDataSourceProperty(key, value);
        }
    }

    private void setHikariProperties(ConfigurationSection section, HikariConfig config) {
        setLongValue(section, config, "connection_timeout", HikariConfig::setConnectionTimeout, 5000L);
        setLongValue(section, config, "keepalive_time", HikariConfig::setKeepaliveTime, null);
        setLongValue(section, config, "max_lifetime", HikariConfig::setMaxLifetime, null);
        setLongValue(section, config, "idle_timeout", HikariConfig::setIdleTimeout, null);
        setLongValue(section, config, "leak_detection_threshold", HikariConfig::setLeakDetectionThreshold, null);
        setIntValue(section, config, "maximum_pool_size", HikariConfig::setMaximumPoolSize, null);
        setIntValue(section, config, "minimum_idle", HikariConfig::setMinimumIdle, null);
        setLongValue(section, config, "validation_timeout", HikariConfig::setValidationTimeout, null);
        setLongValue(section, config, "initialization_fail_timeout", HikariConfig::setInitializationFailTimeout, null);

    }

    private static void setLongValue(
            ConfigurationSection section, HikariConfig config, String path,
            BiConsumer<HikariConfig, Long> apply, Long def
    ) {
        setIfExist(section, config, path ,apply, ConfigurationSection::getLong, def);
    }

    private static void setIntValue(
            ConfigurationSection section, HikariConfig config, String path,
            BiConsumer<HikariConfig, Integer> apply, Integer def
    ) {
        setIfExist(section, config, path ,apply, ConfigurationSection::getInt, def);
    }

    private static <T> void setIfExist(
            ConfigurationSection section, HikariConfig config, String path,
            BiConsumer<HikariConfig, T> apply,
            BiFunction<ConfigurationSection, String, T> getter,
            T def) {
        T value = getter.apply(section, path);
        if (value != null) {
            apply.accept(config, value);
        } else {
            apply.accept(config, def);
        }
    }

    private URI buildDatabaseUri(ConfigurationSection configSection) throws URISyntaxException {
        String host = configSection.getString("host", "localhost");
        int port = configSection.getInt("port", 3306);
        String database = configSection.getString("database", "database");
        if (!database.startsWith("/")) database = "/" + database;
        return  new URI("jdbc:mysql", null, host, port, database, null, null);
    }

    public String getDatabase() {
        return database;
    }

    public HikariDataSource getHikari() {
        return this.hikari;
    }

    public void disable() {
        if(hikari != null) {
            hikari.close();
        }
    }
}
