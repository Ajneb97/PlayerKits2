package pk.ajneb97.model.verify;

import org.bukkit.entity.Player;
import pk.ajneb97.utils.JSONMessage;

import java.util.ArrayList;
import java.util.List;

public class PKInventoryInvalidKitError extends PKBaseError{

    private String kitName;
    private String inventoryName;
    private String slot;

    public PKInventoryInvalidKitError(String file, String errorText, boolean critical, String kitName, String inventoryName, String slot) {
        super(file, errorText, critical);
        this.kitName = kitName;
        this.inventoryName = inventoryName;
        this.slot = slot;
    }

    @Override
    public void sendMessage(Player player) {
        List<String> hover = new ArrayList<String>();

        JSONMessage jsonMessage = new JSONMessage(player,prefix+"&7Invalid kit named &c"+kitName+" &7on file &c"+file);
        hover.add("&eTHIS IS AN ERROR!");
        hover.add("&fA kit that doesn't exists is present on");
        hover.add("&finventory &c"+inventoryName+" &fand slot &c"+slot+"&f.");

        jsonMessage.hover(hover).send();
    }
}
