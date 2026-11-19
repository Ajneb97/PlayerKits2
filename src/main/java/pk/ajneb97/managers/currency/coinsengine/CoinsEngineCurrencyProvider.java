package pk.ajneb97.managers.currency.coinsengine;

import pk.ajneb97.managers.currency.Currency;
import pk.ajneb97.managers.currency.CurrencyProviderType;
import pk.ajneb97.managers.currency.MultiCurrencyProvider;
import su.nightexpress.coinsengine.api.CoinsEngineAPI;

public final class CoinsEngineCurrencyProvider implements MultiCurrencyProvider {

    @Override
    public Currency getCurrency(String currencyName) {
        if (!isEnabled()) {
            return null;
        }
        if (currencyName == null) {
            return null;
        }
        su.nightexpress.coinsengine.api.currency.Currency currency = CoinsEngineAPI.getCurrency(currencyName);
        return CoinsEngineCurrency.of(currency);
    }

    @Override
    public boolean isEnabled() {
        return CoinsEngineAPI.isLoaded();
    }

    @Override
    public CurrencyProviderType getType() {
        return CurrencyProviderType.COINS_ENGINE;
    }
}
