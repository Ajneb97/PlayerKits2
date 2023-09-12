package pk.ajneb97.api;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;
import pk.ajneb97.PlayerKits2;

public class ExpansionPlayerKits extends PlaceholderExpansion {

    // We get an instance of the plugin later.
    private PlayerKits2 plugin;

    public ExpansionPlayerKits(PlayerKits2 plugin) {
    	this.plugin = plugin;
    }

    @Override
    public boolean persist(){
        return true;
    }

    @Override
    public boolean canRegister(){
        return true;
    }

    @Override
    public String getAuthor(){
        return "Ajneb97";
    }

    @Override
    public String getIdentifier(){
        return "playerkits";
    }

    @Override
    public String getVersion(){
        return plugin.getDescription().getVersion();
    }

    @Override
    public String onPlaceholderRequest(Player player, String identifier){

        if(player == null){
            return "";
        }

        if(identifier.startsWith("cooldown_")){
        	// %playerkits_cooldown_<kit>%
        	String event = identifier.replace("cooldown_", "");
            return PlayerKitsAPI.getKitCooldown(player,event);
        }else if(identifier.startsWith("onetime_ready_")){
            // %conditionalevents_onetime_ready_<kit>%
            String event = identifier.replace("onetime_ready_", "");
            return PlayerKitsAPI.getOneTimeReady(player,event);
        }

        return null;
    }
}
