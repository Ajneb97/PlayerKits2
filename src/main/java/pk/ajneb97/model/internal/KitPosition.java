package pk.ajneb97.model.internal;

public class KitPosition {

    private int slot;
    private String inventoryName;

    public KitPosition(int slot, String inventoryName) {
        this.slot = slot;
        this.inventoryName = inventoryName;
    }

    public int getSlot() {
        return slot;
    }

    public void setSlot(int slot) {
        this.slot = slot;
    }

    public String getInventoryName() {
        return inventoryName;
    }

    public void setInventoryName(String inventoryName) {
        this.inventoryName = inventoryName;
    }
}
