package pk.ajneb97.model.verify;

import org.bukkit.entity.Player;
import pk.ajneb97.utils.JSONMessage;

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

        JSONMessage jsonMessage = new JSONMessage(player,prefix+"&7Item material &c"+material+" &7on file &c"+file+" &7is not valid.");
        hover.add("&eTHIS IS AN ERROR!");
        hover.add("&fThe material &c"+material+" &fdefined on");
        hover.add("&ffile &c"+file+" &fdoesn't exists for your");
        hover.add("&fminecraft version.");

        jsonMessage.hover(hover).send();
    }
}
