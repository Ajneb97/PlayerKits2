package pk.ajneb97.model.verify;

import org.bukkit.entity.Player;
import pk.ajneb97.PlayerKits2;
import pk.ajneb97.api.PlayerKitsAPI;
import pk.ajneb97.utils.JSONMessage;
import pk.ajneb97.utils.JSONMessageAdventure;

import java.util.ArrayList;
import java.util.List;

public class PKInvalidItem extends PKBaseError{

    private String material;

    public PKInvalidItem(String file, String errorText, boolean critical, String material) {
        super(file, errorText, critical);
        this.material = material;
    }

    @Override
    public void sendMessage(Player player) {
        List<String> hover = new ArrayList<String>();

        boolean isPaper = PlayerKitsAPI.getPlugin().getDependencyManager().isPaper();
        if(isPaper){
            JSONMessageAdventure jsonMessage = new JSONMessageAdventure(player,prefix+"<gray>Item material <red>"+material+" <gray>on file <red>"+file+" <gray>is not valid.");
            hover.add("<yellow>THIS IS AN ERROR!");
            hover.add("<white>The material <red>"+material+" <white>defined on");
            hover.add("<white>file <red>"+file+" <white>doesn't exists for your");
            hover.add("<white>minecraft version.");

            jsonMessage.hover(hover).send();
        }else{
            JSONMessage jsonMessage = new JSONMessage(player,prefix+"&7Item material &c"+material+" &7on file &c"+file+" &7is not valid.");
            hover.add("&eTHIS IS AN ERROR!");
            hover.add("&fThe material &c"+material+" &fdefined on");
            hover.add("&ffile &c"+file+" &fdoesn't exists for your");
            hover.add("&fminecraft version.");

            jsonMessage.hover(hover).send();
        }

    }
}
