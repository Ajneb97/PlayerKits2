package pk.ajneb97.model;

import org.bukkit.command.CommandSender;

import lombok.Getter;
import lombok.Setter;
import pk.ajneb97.model.item.KitItem;
import pk.ajneb97.utils.PlayerUtils;

import java.util.ArrayList;

@Getter
@Setter
public class Kit {
    private String name;
    private int cooldown;
    private boolean permissionRequired;
    private String customPermission;
    private boolean oneTime;
    private ArrayList<KitItem> items;
    private ArrayList<KitAction> claimActions;
    private ArrayList<KitAction> errorActions;
    private boolean saveOriginalItems;

    private KitItem displayItemDefault;
    private KitItem displayItemNoPermission;
    private KitItem displayItemCooldown;
    private KitItem displayItemOneTime;
    private KitItem displayItemOneTimeRequirements;
    private KitRequirements requirements;

    private boolean autoArmor;

    public Kit(String name){
        this.name = name;
        this.cooldown = 0;
        this.autoArmor = false;
        this.oneTime = false;
        this.saveOriginalItems = false;
    }

    public boolean playerHasPermission(CommandSender player){
        if(permissionRequired){
            if(customPermission != null){
                return PlayerUtils.isPlayerKitsAdmin(player) || player.hasPermission(customPermission);
            }else{
                return PlayerUtils.isPlayerKitsAdmin(player) || player.hasPermission("playerkits.kit."+name);
            }
        }
        return true;
    }

    public void setDefaults(Kit defaultKit){
        cooldown = defaultKit.getCooldown();
        oneTime = defaultKit.isOneTime();
        permissionRequired = defaultKit.isPermissionRequired();
        if(defaultKit.getClaimActions() != null){
            ArrayList<KitAction> actions = new ArrayList<>();
            for(KitAction action : defaultKit.getClaimActions()){
                actions.add(action.clone());
            }
            claimActions = actions;
        }
        if(defaultKit.getErrorActions() != null){
            ArrayList<KitAction> actions = new ArrayList<>();
            for(KitAction action : defaultKit.getErrorActions()){
                actions.add(action.clone());
            }
            errorActions = actions;
        }
        displayItemDefault = defaultKit.getDisplayItemDefault() != null ? defaultKit.getDisplayItemDefault().clone() : null;
        displayItemNoPermission = defaultKit.getDisplayItemNoPermission() != null ? defaultKit.getDisplayItemNoPermission().clone() : null;
        displayItemCooldown = defaultKit.getDisplayItemCooldown() != null ? defaultKit.getDisplayItemCooldown().clone() : null;
        displayItemOneTime = defaultKit.getDisplayItemOneTime() != null ? defaultKit.getDisplayItemOneTime().clone() : null;
        displayItemOneTimeRequirements = defaultKit.getDisplayItemOneTimeRequirements() != null ? defaultKit.getDisplayItemOneTimeRequirements().clone() : null;
        autoArmor = defaultKit.isAutoArmor();
    }

    @Override
    public boolean equals(Object obj) {
        return obj == this || (obj instanceof Kit && ((Kit)obj).name.equals(this.name));
    }
}
