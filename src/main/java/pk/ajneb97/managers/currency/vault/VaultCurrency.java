package pk.ajneb97.managers.currency.vault;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import pk.ajneb97.managers.currency.Currency;

import java.util.UUID;

public class VaultCurrency implements Currency {

    private final Economy economy;

    private VaultCurrency(final Economy economy) {
        this.economy = economy;
    }

    public static VaultCurrency of(final Economy economy) {
        if (economy == null) {
            return null;
        }
        return new VaultCurrency(economy);
    }

    @Override
    public double getBalance(UUID playerId) {
        return economy.getBalance(Bukkit.getOfflinePlayer(playerId));
    }

    @Override
    public boolean withdraw(UUID playerId, double amount) {
        return economy.withdrawPlayer(Bukkit.getOfflinePlayer(playerId), amount).transactionSuccess();
    }
}
