package pk.ajneb97.managers;

import org.bukkit.entity.Player;
import pk.ajneb97.PlayerKits2;
import pk.ajneb97.model.Kit;
import pk.ajneb97.model.KitAction;
import pk.ajneb97.model.inventory.ItemKitInventory;
import pk.ajneb97.model.inventory.KitInventory;
import pk.ajneb97.model.verify.PKBaseError;
import pk.ajneb97.model.verify.PKInventoryInvalidKitError;
import pk.ajneb97.model.verify.PKKitActionError;
import pk.ajneb97.model.verify.PKKitDisplayItemError;

import java.util.ArrayList;
import java.util.List;

public class VerifyManager {
    private PlayerKits2 plugin;
    private ArrayList<PKBaseError> errors;
    private boolean criticalErrors;
    public VerifyManager(PlayerKits2 plugin) {
        this.plugin = plugin;
        this.errors = new ArrayList<PKBaseError>();
        this.criticalErrors = false;
    }

    public void sendVerification(Player player) {
        player.sendMessage(MessagesManager.getColoredMessage("&f&l- - - - - - - - &b&lPLAYERKITS 2 VERIFY &f&l- - - - - - - -"));
        player.sendMessage(MessagesManager.getColoredMessage(""));
        if(errors.isEmpty()) {
            player.sendMessage(MessagesManager.getColoredMessage("&aThere are no errors in the plugin ;)"));
        }else {
            player.sendMessage(MessagesManager.getColoredMessage("&e&oHover on the errors to see more information."));
            for(PKBaseError error : errors) {
                error.sendMessage(player);
            }
        }
        player.sendMessage(MessagesManager.getColoredMessage(""));
        player.sendMessage(MessagesManager.getColoredMessage("&f&l- - - - - - - - &b&lPLAYERKITS 2 VERIFY &f&l- - - - - - - -"));
    }

    public void verify() {
        this.errors = new ArrayList<PKBaseError>();
        this.criticalErrors = false;

        //CHECK KITS
        ArrayList<Kit> kits = plugin.getKitsManager().getKits();
        for(Kit kit : kits) {
            verifyKit(kit);
        }

        //CHECK INVENTORIES
        ArrayList<KitInventory> inventories = plugin.getInventoryManager().getInventories();
        for(KitInventory inventory : inventories){
            verifyInventory(inventory);
        }
    }

    public void verifyKit(Kit kit) {
        String kitName = kit.getName();
        if(kit.getDisplayItemDefault() == null || kit.getDisplayItemDefault().getId() == null){
            errors.add(new PKKitDisplayItemError(kitName+".yml",null,true,kitName));
            criticalErrors = true;
        }
        verifyActions(kit.getClaimActions(),"claim",kitName);
        verifyActions(kit.getErrorActions(),"error",kitName);
    }

    public void verifyActions(ArrayList<KitAction> actions,String actionGroup,String kitName){
        for(int i=0;i<actions.size();i++){
            KitAction action = actions.get(i);
            String[] actionText = action.getAction().split(" ");
            String actionName = actionText[0];
            if(actionName.equals("console_command:") || actionName.equals("player_command:")
                    || actionName.equals("playsound:") || actionName.equals("actionbar:")
                    || actionName.equals("title:") || actionName.equals("firework:")){
                continue;
            }
            errors.add(new PKKitActionError(kitName+".yml",action.getAction(),false,kitName,actionGroup,(i+1)+""));
        }
    }

    public void verifyInventory(KitInventory inventory){
        //Invalid kit on slot
        KitsManager kitsManager = plugin.getKitsManager();
        List<ItemKitInventory> items = inventory.getItems();
        for(ItemKitInventory item : items){
           String type = item.getType();
           if(type != null && type.startsWith("kit: ")){
               String kitName = type.replace("kit: ","");
               if(kitsManager.getKitByName(kitName) == null){
                   errors.add(new PKInventoryInvalidKitError("inventory.yml",null,true,kitName,
                           inventory.getName(),item.getSlotsString()));
                   criticalErrors = true;
               }
           }
        }
    }

    public boolean isCriticalErrors() {
        return criticalErrors;
    }
}
