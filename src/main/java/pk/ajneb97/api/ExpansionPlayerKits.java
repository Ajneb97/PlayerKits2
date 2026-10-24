package pk.ajneb97.api;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import pk.ajneb97.PlayerKits2;

public class ExpansionPlayerKits extends PlaceholderExpansion {

    private final PlayerKits2 plugin;

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
    public @NotNull String getAuthor(){
        return "Ajneb97 (fork maintained by Jules)";
    }

    @Override
    public @NotNull String getIdentifier(){
        return "playerkits";
    }

    @SuppressWarnings("deprecation")
    @Override
    public @NotNull String getVersion(){
        return plugin.getDescription().getVersion();
    }

    @Override
    public String onPlaceholderRequest(Player player, @NotNull String identifier){

        if(player == null){
            return "";
        }

        if(identifier.startsWith("cooldown_")){

        	String event = identifier.replace("cooldown_", "");
            return PlayerKitsAPI.getKitCooldown(player,event);
        }else if(identifier.startsWith("onetime_ready_")){

            String event = identifier.replace("onetime_ready_", "");
            return PlayerKitsAPI.getOneTimeReady(player,event);
        }

        return null;
    }
}
