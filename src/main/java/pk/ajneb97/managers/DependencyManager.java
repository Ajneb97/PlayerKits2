package pk.ajneb97.managers;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.plugin.RegisteredServiceProvider;

public class DependencyManager {

    private boolean isPlaceholderAPI;
    private Economy vaultEconomy;
    private boolean isPaper;

    public DependencyManager(){
        if(Bukkit.getServer().getPluginManager().getPlugin("PlaceholderAPI") != null){
            isPlaceholderAPI = true;
        }
        if(Bukkit.getServer().getPluginManager().getPlugin("Vault") != null){
            RegisteredServiceProvider<Economy> rsp = Bukkit.getServer().getServicesManager().getRegistration(Economy.class);
            if(rsp != null){
                vaultEconomy = rsp.getProvider();
            }
        }
        try{
            Class.forName("com.destroystokyo.paper.ParticleBuilder");
            isPaper = true;
        }catch(ClassNotFoundException e){
            isPaper = false;
        }
    }

    public boolean isPlaceholderAPI() {
        return isPlaceholderAPI;
    }

    public Economy getVaultEconomy() {
        return vaultEconomy;
    }

    public boolean isPaper() {
        return isPaper;
    }
}
