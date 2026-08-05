package pk.ajneb97.model.inventory;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

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

    public UUID getOwner() {
        return owner;
    }

    public boolean isOwner(UUID uuid) {
        return owner != null && owner.equals(uuid);
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }

    public void setInventory(Inventory inventory) {
        this.inventory = inventory;
    }
}
