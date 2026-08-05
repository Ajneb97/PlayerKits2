package pk.ajneb97.model.inventory;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import pk.ajneb97.platform.PlatformTask;

import java.util.UUID;

public class InventoryPlayer {

    private Player player;
    private final UUID uuid;
    private String inventoryName;

    private String previousInventoryName;
    private String kitName;
    private ItemStack[] savedInventoryContents;
    private PlatformTask updateTask;
    private KitGuiHolder holder;
    private int layoutSelectedIndex = -1;
    private int editSelectedSlot = -1;

    public InventoryPlayer(Player player, String inventoryName) {
        this.player = player;
        this.uuid = player.getUniqueId();
        this.inventoryName = inventoryName;
    }

    public Player getPlayer() {
        return player;
    }

    public void setPlayer(Player player) {
        this.player = player;
    }

    public UUID getUuid() {
        return uuid;
    }

    public String getInventoryName() {
        return inventoryName;
    }

    public void setInventoryName(String inventoryName) {
        this.inventoryName = inventoryName;
    }

    public String getPreviousInventoryName() {
        return previousInventoryName;
    }

    public void setPreviousInventoryName(String previousInventoryName) {
        this.previousInventoryName = previousInventoryName;
    }

    public String getKitName() {
        return kitName;
    }

    public void setKitName(String kitName) {
        this.kitName = kitName;
    }

    public KitGuiHolder getHolder() {
        return holder;
    }

    public void setHolder(KitGuiHolder holder) {
        this.holder = holder;
    }

    public int getEditSelectedSlot() {
        return editSelectedSlot;
    }

    public void setEditSelectedSlot(int editSelectedSlot) {
        this.editSelectedSlot = editSelectedSlot;
    }

    public int getLayoutSelectedIndex() {
        return layoutSelectedIndex;
    }

    public void setLayoutSelectedIndex(int layoutSelectedIndex) {
        this.layoutSelectedIndex = layoutSelectedIndex;
    }

    public boolean hasSavedInventoryContents() {
        return savedInventoryContents != null;
    }

    public void restoreSavedInventoryContents() {
        if (savedInventoryContents != null) {
            ItemStack[] contents = savedInventoryContents;
            savedInventoryContents = null;
            if (player != null && player.isOnline()) {
                player.getInventory().setContents(contents);
            }
        }
    }

    public void saveInventoryContents() {
        if (this.savedInventoryContents == null) {
            ItemStack[] contents = player.getInventory().getContents();
            ItemStack[] copy = new ItemStack[contents.length];
            for (int i = 0; i < contents.length; i++) {
                copy[i] = contents[i] == null ? null : contents[i].clone();
            }
            this.savedInventoryContents = copy;
        }
    }

    public void setUpdateTask(PlatformTask updateTask) {
        cancelUpdateTask();
        this.updateTask = updateTask;
    }

    public void cancelUpdateTask() {
        if (updateTask != null) {
            updateTask.cancel();
            updateTask = null;
        }
    }
}
