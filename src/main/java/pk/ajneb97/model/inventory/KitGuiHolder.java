package pk.ajneb97.model.inventory;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.UUID;

public class KitGuiHolder implements InventoryHolder {

    public enum Type {
        KITS,
        EDIT,
        EDIT_ITEMS,
        EDIT_POSITION,
        LAYOUT
    }

    private final Type type;
    private final UUID owner;
    private Inventory inventory;

    public KitGuiHolder(Type type, UUID owner) {
        this.type = type;
        this.owner = owner;
    }

    public Type getType() {
        return type;
    }

    public boolean isNotOwner(UUID uuid) {
        return owner == null || !owner.equals(uuid);
    }

    @Override
    public @NotNull Inventory getInventory() {
        return Objects.requireNonNull(inventory, "Kit GUI inventory has not been created yet");
    }

    public void setInventory(Inventory inventory) {
        this.inventory = inventory;
    }
}
