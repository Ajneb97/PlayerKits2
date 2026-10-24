package pk.ajneb97.model.verify;

import org.bukkit.entity.Player;
import pk.ajneb97.api.PlayerKitsAPI;
import pk.ajneb97.utils.JSONMessage;
import pk.ajneb97.utils.JSONMessageAdventure;

import java.util.ArrayList;
import java.util.List;

public class PKKitActionError extends PKBaseError{

    private final String kitName;
    private final String actionGroup;
    private final String actionId;

    public PKKitActionError(String file, String errorText, boolean critical, String kitName, String actionGroup, String actionId) {
        super(file, errorText, critical);
        this.kitName = kitName;
        this.actionId = actionId;
        this.actionGroup = actionGroup;
    }

    @Override
    public void sendMessage(Player player) {
        List<String> hover = new ArrayList<>();

        boolean isPaper = PlayerKitsAPI.getPlugin().getDependencyManager().isPaper();
        if(isPaper){
            JSONMessageAdventure jsonMessage = new JSONMessageAdventure(player,prefix+"<gray>Action (<red>"+actionGroup+"<gray>,<red>"+actionId+"<gray>)" +
                    " <gray>on kit <red>"+kitName+" <gray>is not valid.");
            hover.add("<yellow>THIS IS A WARNING!");
            hover.add("<white>The action defined for this event");
            hover.add("<white>is probably not formatted correctly:");
            for(String m : getFixedErrorText()) {
                hover.add("<red>"+m);
            }
            hover.add(" ");
            hover.add("<white>Remember to use a valid action from this list:");
            hover.add("<green>https://ajneb97.gitbook.io/playerkits-2/actions");

            jsonMessage.hover(hover).send();
        }else{
            JSONMessage jsonMessage = new JSONMessage(player,prefix+"&7Action (&c"+actionGroup+"&7,&c"+actionId+"&7)" +
                    " &7on kit &c"+kitName+" &7is not valid.");
            hover.add("&eTHIS IS A WARNING!");
            hover.add("&fThe action defined for this event");
            hover.add("&fis probably not formatted correctly:");
            for(String m : getFixedErrorText()) {
                hover.add("&c"+m);
            }
            hover.add(" ");
            hover.add("&fRemember to use a valid action from this list:");
            hover.add("&ahttps://ajneb97.gitbook.io/playerkits-2/actions");

            jsonMessage.hover(hover).send();
        }

    }
}
