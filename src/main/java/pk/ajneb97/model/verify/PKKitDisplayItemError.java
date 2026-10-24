package pk.ajneb97.model.verify;

import org.bukkit.entity.Player;
import pk.ajneb97.api.PlayerKitsAPI;
import pk.ajneb97.utils.JSONMessage;
import pk.ajneb97.utils.JSONMessageAdventure;

import java.util.ArrayList;
import java.util.List;

public class PKKitDisplayItemError extends PKBaseError{

    private final String kitName;

    public PKKitDisplayItemError(String file, String errorText, boolean critical, String kitName) {
        super(file, errorText, critical);
        this.kitName = kitName;
    }

    @Override
    public void sendMessage(Player player) {
        List<String> hover = new ArrayList<>();

        boolean isPaper = PlayerKitsAPI.getPlugin().getDependencyManager().isPaper();
        if(isPaper){
            JSONMessageAdventure jsonMessage = new JSONMessageAdventure(player,prefix+"<gray>Kit <red>"+kitName+" <gray>doesn't have a default display item.");
            hover.add("<yellow>THIS IS AN ERROR!");
            hover.add("<white>All kits must have a default display");
            hover.add("<white>item. Set one using /kit edit "+kitName);

            jsonMessage.hover(hover).send();
        }else{
            JSONMessage jsonMessage = new JSONMessage(player,prefix+"&7Kit &c"+kitName+" &7doesn't have a default display item.");
            hover.add("&eTHIS IS AN ERROR!");
            hover.add("&fAll kits must have a default display");
            hover.add("&fitem. Set one using /kit edit "+kitName);

            jsonMessage.hover(hover).send();
        }

    }
}
