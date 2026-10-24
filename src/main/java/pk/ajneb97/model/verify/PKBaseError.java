package pk.ajneb97.model.verify;

import org.bukkit.entity.Player;
import pk.ajneb97.api.PlayerKitsAPI;
import pk.ajneb97.utils.OtherUtils;

import java.util.List;

public abstract class PKBaseError {

    protected final String file;
    protected final String errorText;
    protected final boolean critical;
    protected final String prefix;

    public PKBaseError(String file, String errorText, boolean critical){
        this.file = file;
        this.errorText = errorText;
        this.critical = critical;

        boolean isPaper = PlayerKitsAPI.getPlugin().getDependencyManager().isPaper();
        if(isPaper){
            this.prefix = this.critical ? "<red>⚠ " : "<yellow>⚠ ";
        }else{
            this.prefix = this.critical ? "&c⚠ " : "&e⚠ ";
        }
    }

    public List<String> getFixedErrorText(){
        return OtherUtils.wrapText(errorText,35);
    }

    public abstract void sendMessage(Player player);
}
