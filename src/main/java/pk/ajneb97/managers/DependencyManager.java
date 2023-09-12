package pk.ajneb97.managers;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.plugin.RegisteredServiceProvider;
import pk.ajneb97.PlayerKits2;

public class DependencyManager {

    private PlayerKits2 plugin;

    private boolean isPlaceholderAPI;
    private Economy vaultEconomy;

    public DependencyManager(PlayerKits2 plugin){
        this.plugin = plugin;

        if(Bukkit.getServer().getPluginManager().getPlugin("PlaceholderAPI") != null){
            isPlaceholderAPI = true;
        }
        if(Bukkit.getServer().getPluginManager().getPlugin("Vault") != null){
            RegisteredServiceProvider<Economy> rsp = Bukkit.getServer().getServicesManager().getRegistration(Economy.class);
            if(rsp != null){
                vaultEconomy = rsp.getProvider();
            }
        }
    }

    public boolean isPlaceholderAPI() {
        return isPlaceholderAPI;
    }

    public Economy getVaultEconomy() {
        return vaultEconomy;
    }
}
