package pk.ajneb97.managers.currency.coinsengine;

import pk.ajneb97.managers.currency.Currency;
import su.nightexpress.coinsengine.api.CoinsEngineAPI;

import java.util.UUID;

public class CoinsEngineCurrency implements Currency {

    private final su.nightexpress.coinsengine.api.currency.Currency currency;

    private CoinsEngineCurrency(final su.nightexpress.coinsengine.api.currency.Currency currency) {
        this.currency = currency;
    }

    public static CoinsEngineCurrency of(final su.nightexpress.coinsengine.api.currency.Currency currency) {
        if (currency == null) {
            return null;
        }
        return new CoinsEngineCurrency(currency);
    }

    @Override
    public double getBalance(UUID playerId) {
        return CoinsEngineAPI.getBalance(playerId, currency);
    }

    @Override
    public boolean withdraw(UUID playerId, double amount) {
        return CoinsEngineAPI.removeBalance(playerId, currency, amount);
    }
}
