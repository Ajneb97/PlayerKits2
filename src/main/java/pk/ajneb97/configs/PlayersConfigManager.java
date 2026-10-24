package pk.ajneb97.configs;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import pk.ajneb97.PlayerKits2;
import pk.ajneb97.configs.model.CommonConfig;
import pk.ajneb97.model.PlayerData;
import pk.ajneb97.model.PlayerDataKit;
import pk.ajneb97.model.internal.GenericCallback;
import pk.ajneb97.model.layout.PlayerKitLayout;

import java.util.ArrayList;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.locks.ReentrantLock;

public class PlayersConfigManager extends DataFolderConfigManager {

    private final ReentrantLock ioLock = new ReentrantLock();

    public PlayersConfigManager(PlayerKits2 plugin, String folderName) {
        super(plugin, folderName);
    }

    @Override
    public void createFiles() {

    }

    @Override
    public void loadConfigs() {

    }

    public void loadConfig(UUID uuid, GenericCallback<PlayerData> callback) {
        plugin.getPlatformScheduler().runAsync(() -> {
            PlayerData playerData = readPlayerData(uuid);
            plugin.getPlatformScheduler().runGlobal(() -> callback.onDone(playerData));
        });
    }

    private PlayerData readPlayerData(UUID uuid) {
        ioLock.lock();
        try {
            CommonConfig playerConfig = getConfigFile(uuid + ".yml", false);
            if (playerConfig == null) {
                return null;
            }

            FileConfiguration config = playerConfig.getConfig();
            PlayerData playerData = new PlayerData(uuid, config.getString("name"));

            ConfigurationSection kitsSection = config.getConfigurationSection("kits");
            if (kitsSection != null) {
                for (String key : kitsSection.getKeys(false)) {
                    ConfigurationSection kitSection = kitsSection.getConfigurationSection(key);
                    if (kitSection == null) {
                        continue;
                    }
                    PlayerDataKit playerDataKit = new PlayerDataKit(key);
                    playerDataKit.setCooldown(kitSection.getLong("cooldown"));
                    playerDataKit.setOneTime(kitSection.getBoolean("one_time"));
                    playerDataKit.setBought(kitSection.getBoolean("bought"));
                    playerData.addKit(playerDataKit);
                }
            }

            ConfigurationSection layoutsSection = config.getConfigurationSection("layouts");
            if (layoutsSection != null) {
                for (String key : layoutsSection.getKeys(false)) {
                    PlayerKitLayout layout = PlayerKitLayout.deserialize(key, layoutsSection.getString(key));
                    if (layout != null) {
                        playerData.putLayout(layout);
                    }
                }
            }

            playerData.setModified(false);
            return playerData;
        } catch (Exception error) {
            plugin.getLogger().severe("Could not read player data for " + uuid + ": " + error);
            return null;
        } finally {
            ioLock.unlock();
        }
    }

    public void saveConfig(PlayerData playerData) {
        ioLock.lock();
        try {
            CommonConfig playerConfig = getConfigFile(playerData.getUuid() + ".yml", true);
            if (playerConfig == null) {
                return;
            }
            FileConfiguration config = playerConfig.getConfig();

            config.set("name", playerData.getName());
            config.set("kits", null);
            config.set("layouts", null);

            for (PlayerDataKit playerDataKit : playerData.getKits()) {
                String path = "kits." + playerDataKit.getName();
                config.set(path + ".cooldown", playerDataKit.getCooldown());
                config.set(path + ".one_time", playerDataKit.isOneTime());
                config.set(path + ".bought", playerDataKit.isBought());
            }

            for (PlayerKitLayout layout : playerData.getLayouts()) {
                config.set("layouts." + layout.getKitName(), layout.serialize());
            }

            playerConfig.saveConfig();
        } catch (Exception error) {
            plugin.getLogger().severe("Could not save player data for " + playerData.getUuid() + ": " + error);
        } finally {
            ioLock.unlock();
        }
    }

    public void saveConfigs() {
        if (plugin.getConfigsManager().getMainConfigManager().isMySQL()) {
            return;
        }
        Map<UUID, PlayerData> players = plugin.getPlayerDataManager().getPlayers();
        for (PlayerData playerData : new ArrayList<>(players.values())) {
            if (playerData.isModified()) {
                playerData.setModified(false);
                saveConfig(playerData);
            }
        }
    }

    public void resetKitForAllPlayers(String kitName) {
        ioLock.lock();
        try {
            for (CommonConfig commonConfig : getConfigs()) {
                FileConfiguration config = commonConfig.getConfig();
                if (!config.contains("kits." + kitName)) {
                    continue;
                }
                config.set("kits." + kitName, null);
                commonConfig.saveConfig();
            }
        } finally {
            ioLock.unlock();
        }
    }
}
