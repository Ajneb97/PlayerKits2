package pk.ajneb97.managers.currency;

public interface MultiCurrencyProvider extends CurrencyProvider {

    Currency getCurrency(String currencyName);
}
