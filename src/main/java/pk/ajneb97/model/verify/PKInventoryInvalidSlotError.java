package pk.ajneb97.model.verify;

import org.bukkit.entity.Player;
import pk.ajneb97.api.PlayerKitsAPI;
import pk.ajneb97.utils.JSONMessage;
import pk.ajneb97.utils.JSONMessageAdventure;

import java.util.ArrayList;
import java.util.List;

public class PKInventoryInvalidSlotError extends PKBaseError{

    private final String inventoryName;
    private final int slot;
    private final int maxSlots;

    public PKInventoryInvalidSlotError(String file, String errorText, boolean critical, int slot, String inventoryName, int maxSlots) {
        super(file, errorText, critical);
        this.inventoryName = inventoryName;
        this.slot = slot;
        this.maxSlots = maxSlots;
    }

    @Override
    public void sendMessage(Player player) {
        List<String> hover = new ArrayList<>();

        boolean isPaper = PlayerKitsAPI.getPlugin().getDependencyManager().isPaper();
        if(isPaper){
            JSONMessageAdventure jsonMessage = new JSONMessageAdventure(player,prefix+"<gray>Inventory <red>"+inventoryName+" <gray>has an item on an invalid slot");
            hover.add("<yellow>THIS IS AN ERROR!");
            hover.add("<white>Slot <red>"+slot+" <white>on inventory <red>"+inventoryName);
            hover.add("<white>is out of range. Use a range");
            hover.add("<white>between 0 and "+(maxSlots-1)+".");

            jsonMessage.hover(hover).send();
        }else{
            JSONMessage jsonMessage = new JSONMessage(player,prefix+"&7Inventory &c"+inventoryName+" &7has an item on an invalid slot");
            hover.add("&eTHIS IS AN ERROR!");
            hover.add("&fSlot &c"+slot+" &fon inventory &c"+inventoryName);
            hover.add("&fis out of range. Use a range");
            hover.add("&fbetween 0 and "+(maxSlots-1)+".");

            jsonMessage.hover(hover).send();
        }

    }
}
