package pk.ajneb97.managers;

import org.bukkit.entity.Player;
import pk.ajneb97.PlayerKits2;
import pk.ajneb97.model.Kit;
import pk.ajneb97.model.KitAction;
import pk.ajneb97.model.inventory.InventoryPlayer;
import pk.ajneb97.model.inventory.ItemKitInventory;
import pk.ajneb97.model.inventory.KitInventory;
import pk.ajneb97.model.item.KitItem;
import pk.ajneb97.model.verify.*;
import pk.ajneb97.utils.ItemUtils;

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
        InventoryManager inventoryManager = plugin.getInventoryManager();
        ArrayList<KitInventory> inventories = inventoryManager.getInventories();
        for(KitInventory inventory : inventories){
            verifyInventory(inventory);
        }
        if(inventoryManager.getInventory("main_inventory") == null){
            errors.add(new PKInventoryDefaultNotExistsError("inventory.yml",null,true,"main_inventory"));
            criticalErrors = true;
        }
        if(inventoryManager.getInventory("preview_inventory") == null){
            errors.add(new PKInventoryDefaultNotExistsError("inventory.yml",null,true,"preview_inventory"));
            criticalErrors = true;
        }
        if(inventoryManager.getInventory("buy_requirements_inventory") == null){
            errors.add(new PKInventoryDefaultNotExistsError("inventory.yml",null,true,"buy_requirements_inventory"));
            criticalErrors = true;
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

        //Items
        ArrayList<KitItem> allKitItems = new ArrayList<KitItem>();
        allKitItems.add(kit.getDisplayItemDefault());
        allKitItems.add(kit.getDisplayItemCooldown());
        allKitItems.add(kit.getDisplayItemNoPermission());
        allKitItems.add(kit.getDisplayItemOneTime());
        allKitItems.add(kit.getDisplayItemOneTimeRequirements());
        allKitItems.addAll(kit.getItems());
        for(KitAction kitAction : kit.getClaimActions()){
            allKitItems.add(kitAction.getDisplayItem());
        }
        for(KitAction kitAction : kit.getErrorActions()){
            allKitItems.add(kitAction.getDisplayItem());
        }
        for(KitItem kitItem : allKitItems){
            if(kitItem != null){
                if(kitItem.getOriginalItem() != null){
                    continue;
                }
                if(!verifyItem(kitItem.getId())){
                    errors.add(new PKInvalidItem(kit.getName()+".yml",null,true,kitItem.getId()));
                    criticalErrors = true;
                }
            }
        }
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
        KitsManager kitsManager = plugin.getKitsManager();
        List<ItemKitInventory> items = inventory.getItems();
        InventoryManager inventoryManager = plugin.getInventoryManager();
        int maxSlots = inventory.getSlots();
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

           String openInventory = item.getOpenInventory();
           if(openInventory != null){
               if(!openInventory.equals("previous") && inventoryManager.getInventory(openInventory) == null){
                   errors.add(new PKInventoryNotExistsError("inventory.yml",null,true,inventory.getName(),
                           item.getSlotsString(),openInventory));
                   criticalErrors = true;
               }
           }

           //Items
           KitItem kitItem = item.getItem();
           if(kitItem != null){
               if(!verifyItem(kitItem.getId())){
                   errors.add(new PKInvalidItem("inventory.yml",null,true,kitItem.getId()));
                   criticalErrors = true;
               }
           }

           //Valid slots
           for(int slot : item.getSlots()){
               if(slot >= maxSlots){
                   errors.add(new PKInventoryInvalidSlotError("inventory.yml",null,true,slot,
                           inventory.getName(),maxSlots));
                   criticalErrors = true;
               }
           }
        }
    }

    public boolean isCriticalErrors() {
        return criticalErrors;
    }

    public boolean verifyItem(String material){
        try{
            ItemUtils.createItemFromID(material);
            return true;
        }catch(Exception e){
            return false;
        }
    }
}
