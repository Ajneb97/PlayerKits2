package pk.ajneb97.managers.currency;

public interface CurrencyProvider {

    boolean isEnabled();

    CurrencyProviderType getType();
}
