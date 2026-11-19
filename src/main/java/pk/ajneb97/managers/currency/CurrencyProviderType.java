package pk.ajneb97.managers.currency;

import com.google.common.base.Preconditions;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public enum CurrencyProviderType {

    VAULT("Vault"),
    COINS_ENGINE("CoinsEngine", "coins_engine");

    private final String name;
    private final List<String> aliases = new ArrayList<>();

    CurrencyProviderType(final String name) {
        this.name = name;
    }

    CurrencyProviderType(final String name, final List<String> aliases) {
        this.name = name;
        this.aliases.addAll(aliases);
    }

    CurrencyProviderType(final String name, final String... aliases) {
        this.name = name;
        Collections.addAll(this.aliases, aliases);
    }

    public static @Nullable CurrencyProviderType getByName(final @Nullable String name) {
        if (name == null) {
            return null;
        }
        for (final CurrencyProviderType type : values()) {
            if (type.name.equalsIgnoreCase(name) || type.aliases.stream().anyMatch(alias -> alias.equalsIgnoreCase(name))) {
                return type;
            }
        }
        return null;
    }

    public static @NotNull CurrencyProviderType getByNameOrDefault(final @Nullable String name, final @NotNull CurrencyProviderType defaultType) {
        Preconditions.checkNotNull(defaultType, "Default type cannot be null");
        CurrencyProviderType type = getByName(name);
        return type != null ? type : defaultType;
    }
}
