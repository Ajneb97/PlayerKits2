package pk.ajneb97.managers.currency.vault;

import net.milkbowl.vault.economy.Economy;
import pk.ajneb97.managers.currency.Currency;
import pk.ajneb97.managers.currency.CurrencyProviderType;
import pk.ajneb97.managers.currency.SingleCurrencyProvider;

public class VaultCurrencyProvider implements SingleCurrencyProvider {

    private final Economy economy;

    public VaultCurrencyProvider(final Economy economy) {
        this.economy = economy;
    }

    @Override
    public Currency getCurrency() {
        if (!isEnabled()) {
            return null;
        }
        return VaultCurrency.of(economy);
    }

    @Override
    public boolean isEnabled() {
        return economy != null && economy.isEnabled();
    }

    @Override
    public CurrencyProviderType getType() {
        return CurrencyProviderType.VAULT;
    }
}
