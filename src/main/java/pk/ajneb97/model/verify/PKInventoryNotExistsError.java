package pk.ajneb97.model.verify;

import org.bukkit.entity.Player;
import pk.ajneb97.api.PlayerKitsAPI;
import pk.ajneb97.utils.JSONMessage;
import pk.ajneb97.utils.JSONMessageAdventure;

import java.util.ArrayList;
import java.util.List;

public class PKInventoryNotExistsError extends PKBaseError{

    private final String inventoryName;
    private final String inventoryPath;
    private final String slot;

    public PKInventoryNotExistsError(String file, String errorText, boolean critical, String inventoryPath, String slot, String inventoryName) {
        super(file, errorText, critical);
        this.inventoryName = inventoryName;
        this.inventoryPath = inventoryPath;
        this.slot = slot;
    }

    @Override
    public void sendMessage(Player player) {
        List<String> hover = new ArrayList<>();

        boolean isPaper = PlayerKitsAPI.getPlugin().getDependencyManager().isPaper();
        if(isPaper){
            JSONMessageAdventure jsonMessage = new JSONMessageAdventure(player,prefix+"<gray>Inventory <red>"+inventoryName+" <gray>not valid");
            hover.add("<yellow>THIS IS AN ERROR!");
            hover.add("<white>The <red>"+inventoryName+" <white>opened inventory used");
            hover.add("<white>on slot <red>"+slot+" <white>on inventory <red>"+inventoryPath);
            hover.add("<white>is not valid. Create it on the inventory.yml file.");

            jsonMessage.hover(hover).send();
        }else{
            JSONMessage jsonMessage = new JSONMessage(player,prefix+"&7Inventory &c"+inventoryName+" &7not valid");
            hover.add("&eTHIS IS AN ERROR!");
            hover.add("&fThe &c"+inventoryName+" &fopened inventory used");
            hover.add("&fon slot &c"+slot+" &fon inventory &c"+inventoryPath);
            hover.add("&fis not valid. Create it on the inventory.yml file.");

            jsonMessage.hover(hover).send();
        }

    }
}
