package pk.ajneb97.model.verify;

import org.bukkit.entity.Player;
import pk.ajneb97.api.PlayerKitsAPI;
import pk.ajneb97.utils.JSONMessage;
import pk.ajneb97.utils.JSONMessageAdventure;

import java.util.ArrayList;
import java.util.List;

public class PKInventoryInvalidKitError extends PKBaseError{

    private final String kitName;
    private final String inventoryName;
    private final String slot;

    public PKInventoryInvalidKitError(String file, String errorText, boolean critical, String kitName, String inventoryName, String slot) {
        super(file, errorText, critical);
        this.kitName = kitName;
        this.inventoryName = inventoryName;
        this.slot = slot;
    }

    @Override
    public void sendMessage(Player player) {
        List<String> hover = new ArrayList<>();

        boolean isPaper = PlayerKitsAPI.getPlugin().getDependencyManager().isPaper();
        if(isPaper){
            JSONMessageAdventure jsonMessage = new JSONMessageAdventure(player,prefix+"<gray>Invalid kit named <red>"+kitName+" <gray>on file <red>"+file);
            hover.add("<yellow>THIS IS AN ERROR!");
            hover.add("<white>A kit that doesn't exist is present on");
            hover.add("<white>inventory <red>"+inventoryName+" <white>and slot <red>"+slot+"<white>.");

            jsonMessage.hover(hover).send();
        }else{
            JSONMessage jsonMessage = new JSONMessage(player,prefix+"&7Invalid kit named &c"+kitName+" &7on file &c"+file);
            hover.add("&eTHIS IS AN ERROR!");
            hover.add("&fA kit that doesn't exist is present on");
            hover.add("&finventory &c"+inventoryName+" &fand slot &c"+slot+"&f.");

            jsonMessage.hover(hover).send();
        }

    }
}
