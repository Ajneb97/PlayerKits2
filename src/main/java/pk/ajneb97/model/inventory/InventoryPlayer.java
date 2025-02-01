package pk.ajneb97.model.inventory;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class InventoryPlayer {
    private final Player player;
    private String inventoryName;

    private String previousInventoryName;
    private String kitName;
    private ItemStack[] savedInventoryContents;

    public InventoryPlayer(Player player, String inventoryName) {
        this.player = player;
        this.inventoryName = inventoryName;
    }

    public void restoreSavedInventoryContents() {
        if(savedInventoryContents != null){
            player.getInventory().setContents(savedInventoryContents);
            //player.updateInventory();
            savedInventoryContents = null;
        }
    }

    public void saveInventoryContents() {
        if(this.savedInventoryContents == null){
            this.savedInventoryContents = player.getInventory().getContents();
        }
    }
}
