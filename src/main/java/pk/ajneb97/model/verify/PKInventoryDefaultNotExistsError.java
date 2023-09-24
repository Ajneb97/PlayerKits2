package pk.ajneb97.model.verify;

import org.bukkit.entity.Player;
import pk.ajneb97.utils.JSONMessage;

import java.util.ArrayList;
import java.util.List;

public class PKInventoryDefaultNotExistsError extends PKBaseError{

    private String inventoryName;

    public PKInventoryDefaultNotExistsError(String file, String errorText, boolean critical, String inventoryName) {
        super(file, errorText, critical);
        this.inventoryName = inventoryName;
    }

    @Override
    public void sendMessage(Player player) {
        List<String> hover = new ArrayList<String>();

        JSONMessage jsonMessage = new JSONMessage(player,prefix+"&7Inventory &c"+inventoryName+" &7not found");
        hover.add("&eTHIS IS AN ERROR!");
        hover.add("&fThe &c"+inventoryName+" &fis a needed inventory for");
        hover.add("&fthe plugin to work. You MUST NOT delete it");
        hover.add("&ffrom the inventory.yml file. You can find the default");
        hover.add("&fconfig for this inventory on the wiki:");
        hover.add("&ahttps://ajneb97.gitbook.io/playerkits-2/default-files/inventory.yml");

        jsonMessage.hover(hover).send();
    }
}
