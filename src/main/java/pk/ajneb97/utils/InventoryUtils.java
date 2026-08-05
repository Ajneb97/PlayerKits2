package pk.ajneb97.utils;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import pk.ajneb97.managers.MessagesManager;
import pk.ajneb97.model.inventory.KitGuiHolder;

import java.lang.reflect.Method;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

public class InventoryUtils {

    private static final Map<Class<?>, Method> TOP_INVENTORY_CACHE = new ConcurrentHashMap<>();

    public static Inventory getTopInventory(InventoryEvent event) {
        return resolveTopInventory(event.getView());
    }

    public static Inventory getTopInventory(Player player) {
        return resolveTopInventory(player.getOpenInventory());
    }

    private static Inventory resolveTopInventory(Object view) {
        if (view == null) {
            return null;
        }
        try {
            Method method = TOP_INVENTORY_CACHE.computeIfAbsent(view.getClass(), type -> {
                try {
                    Method resolved = type.getMethod("getTopInventory");
                    resolved.setAccessible(true);
                    return resolved;
                } catch (NoSuchMethodException error) {
                    throw new IllegalStateException(error);
                }
            });
            return (Inventory) method.invoke(view);
        } catch (Exception error) {
            return null;
        }
    }

    @SuppressWarnings({"deprecation", "removal"})
    public static Inventory createInventory(KitGuiHolder holder, int slots, String title, boolean useMiniMessage) {
        Inventory inventory;
        if (useMiniMessage) {
            inventory = MiniMessageUtils.createInventory(holder, slots, title);
        } else {
            inventory = Bukkit.createInventory(holder, slots, MessagesManager.getLegacyColoredMessage(title));
        }
        holder.setInventory(inventory);
        return inventory;
    }

    public static KitGuiHolder getKitGuiHolder(Inventory inventory) {
        if (inventory == null) {
            return null;
        }
        InventoryHolder holder = inventory.getHolder();
        return holder instanceof KitGuiHolder ? (KitGuiHolder) holder : null;
    }

    public static boolean isKitGui(Inventory inventory, KitGuiHolder.Type type) {
        KitGuiHolder holder = getKitGuiHolder(inventory);
        return holder != null && holder.getType() == type;
    }
}
