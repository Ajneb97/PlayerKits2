package pk.ajneb97.managers;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.plugin.RegisteredServiceProvider;
import pk.ajneb97.PlayerKits2;
import pk.ajneb97.managers.currency.CurrencyProvider;
import pk.ajneb97.managers.currency.CurrencyProviderType;
import pk.ajneb97.managers.currency.coinsengine.CoinsEngineCurrencyProvider;
import pk.ajneb97.managers.currency.vault.VaultCurrencyProvider;

import java.util.HashMap;
import java.util.Map;

public class DependencyManager {

    private PlayerKits2 plugin;

    private boolean isPlaceholderAPI;
    private Economy vaultEconomy;
    private final Map<CurrencyProviderType, CurrencyProvider> currencyProviders;
    private boolean isPaper;

    public DependencyManager(PlayerKits2 plugin){
        this.plugin = plugin;
        this.currencyProviders = new HashMap<>();

        if(Bukkit.getServer().getPluginManager().getPlugin("PlaceholderAPI") != null){
            isPlaceholderAPI = true;
        }
//        if(Bukkit.getServer().getPluginManager().getPlugin("Vault") != null){
//            RegisteredServiceProvider<Economy> rsp = Bukkit.getServer().getServicesManager().getRegistration(Economy.class);
//            if(rsp != null){
//                vaultEconomy = rsp.getProvider();
//            }
//        }
        loadCurrencyProviders();
        try{
            Class.forName("com.destroystokyo.paper.ParticleBuilder");
            isPaper = true;
        }catch(Exception e){

        }
    }

    public void reloadCurrencyProviders() {
        currencyProviders.clear();
        loadCurrencyProviders();
    }

    public void loadCurrencyProviders(){
        if (Bukkit.getServer().getPluginManager().getPlugin("Vault") != null) {
            RegisteredServiceProvider<Economy> rsp = Bukkit.getServer().getServicesManager().getRegistration(Economy.class);
            if (rsp != null) {
                vaultEconomy = rsp.getProvider();
                currencyProviders.put(CurrencyProviderType.VAULT, new VaultCurrencyProvider(vaultEconomy));
            }
        } else {
            vaultEconomy = null;
        }
        if (Bukkit.getServer().getPluginManager().getPlugin("CoinsEngine") != null) {
            currencyProviders.put(CurrencyProviderType.COINS_ENGINE, new CoinsEngineCurrencyProvider());
        }
    }

    public CurrencyProvider getCurrencyProvider(CurrencyProviderType type) {
        return currencyProviders.get(type);
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
