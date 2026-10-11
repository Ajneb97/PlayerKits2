package pk.ajneb97.managers.currency;

import org.bukkit.OfflinePlayer;

import java.util.UUID;

public interface Currency {

    double getBalance(UUID playerId);

    default boolean has(UUID playerId, double amount) {
        return getBalance(playerId) >= amount;
    }

    boolean withdraw(UUID playerId, double amount);
}
