package pk.ajneb97.managers.edit;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import pk.ajneb97.PlayerKits2;
import pk.ajneb97.managers.MessagesManager;
import pk.ajneb97.model.Kit;
import pk.ajneb97.model.KitAction;
import pk.ajneb97.model.inventory.InventoryPlayer;
import pk.ajneb97.utils.InventoryItem;
import pk.ajneb97.utils.OtherUtils;

import java.util.ArrayList;
import java.util.List;

public class InventoryEditActionsManager {

    private PlayerKits2 plugin;
    private InventoryEditManager inventoryEditManager;
    private InventoryEditActionsEditManager inventoryEditActionsEditManager;
    public InventoryEditActionsManager(PlayerKits2 plugin, InventoryEditManager inventoryEditManager){
        this.plugin = plugin;
        this.inventoryEditManager = inventoryEditManager;
        this.inventoryEditActionsEditManager = new InventoryEditActionsEditManager(plugin,this);
    }

    public void openInventory(InventoryPlayer inventoryPlayer,String type) {
        inventoryPlayer.setInventoryName("edit_actions_"+type);
        Inventory inv = Bukkit.createInventory(null, 54, MessagesManager.getColoredMessage("&9Editing Kit"));

        //Decoration
        for(int i=45;i<=52;i++){
            if(OtherUtils.isLegacy()){
                new InventoryItem(inv, i, Material.valueOf("STAINED_GLASS_PANE")).dataValue((short) 15).name("").ready();
            }else{
                new InventoryItem(inv, i, Material.BLACK_STAINED_GLASS_PANE).name("").ready();
            }
        }

        //Set Go Back
        new InventoryItem(inv, 45, Material.ARROW).name("&eGo Back").ready();

        //Set Add Action
        new InventoryItem(inv, 53, Material.EMERALD_BLOCK).name("&6&lAdd Action").ready();

        //Set Info Item
        List<String> lore = new ArrayList<String>();
        lore.add("&7You are currently editing the actions");
        if(type.equals("claim")){
            lore.add("&7when the player claims the kit.");
        }else{
            lore.add("&7when there is an error when claiming");
            lore.add("&7the kit.");
        }
        new InventoryItem(inv, 49, Material.COMPASS).name("&6&lInfo").lore(lore).ready();

        //Set Actions
        Kit kit = plugin.getKitsManager().getKitByName(inventoryPlayer.getKitName());
        ArrayList<KitAction> actions = getKitActionsFromType(kit,type);
        int slot = 0;
        for(KitAction kitAction : actions){
            String executeBeforeItems = kitAction.isExecuteBeforeItems() ? "&aYES" : "&cNO";
            String countAsItem = kitAction.isCountAsItem() ? "&aYES" : "&cNO";
            String displayItem = kitAction.getDisplayItem() != null ? "&aYES" : "&cNO";

            lore = new ArrayList<>();
            lore.add(MessagesManager.getColoredMessage("&f")+kitAction.getAction());
            lore.add("");
            lore.add(MessagesManager.getColoredMessage("&7Execute before giving kit items? "+executeBeforeItems));
            lore.add(MessagesManager.getColoredMessage("&7Count as item? "+countAsItem));
            lore.add(MessagesManager.getColoredMessage("&7Has display item? "+displayItem));
            lore.add("");
            lore.add(MessagesManager.getColoredMessage("&a&lLEFT CLICK &ato edit"));
            lore.add(MessagesManager.getColoredMessage("&c&lRIGHT CLICK &cto remove"));

            ItemStack item = new ItemStack(Material.PAPER);
            ItemMeta meta = item.getItemMeta();
            meta.setDisplayName(MessagesManager.getColoredMessage("&7Action &e#"+(slot+1)));
            meta.setLore(lore);
            item.setItemMeta(meta);
            inv.setItem(slot,item);

            slot++;
            if(slot >= 44){
                break;
            }
        }

        inventoryPlayer.getPlayer().openInventory(inv);
        inventoryEditManager.getPlayers().add(inventoryPlayer);
    }

    public void removeAction(InventoryPlayer inventoryPlayer,int slot){
        Kit kit = plugin.getKitsManager().getKitByName(inventoryPlayer.getKitName());
        String type = inventoryPlayer.getInventoryName().replace("edit_actions_","");
        ArrayList<KitAction> actions = getKitActionsFromType(kit,type);
        actions.remove(slot);

        openInventory(inventoryPlayer,type);

        plugin.getConfigsManager().getKitsConfigManager().saveConfig(kit);
    }


    public void clickAddAction(InventoryPlayer inventoryPlayer){
        Player player = inventoryPlayer.getPlayer();
        player.sendMessage(MessagesManager.getColoredMessage(PlayerKits2.prefix+"&7Write the new action to add."));
        player.sendMessage(MessagesManager.getColoredMessage(PlayerKits2.prefix+"&fCheck all actions on the wiki: &bhttps://ajneb97.gitbook.io/playerkits-2/actions"));

        player.closeInventory();
        String type = inventoryPlayer.getInventoryName().replace("edit_actions_","");
        inventoryPlayer.setInventoryName("edit_chat_add_action_"+type);
        inventoryEditManager.getPlayers().add(inventoryPlayer);
    }

    public void addAction(InventoryPlayer inventoryPlayer,String message){
        Kit kit = plugin.getKitsManager().getKitByName(inventoryPlayer.getKitName());
        String type = inventoryPlayer.getInventoryName().replace("edit_chat_add_action_","");
        ArrayList<KitAction> actions = getKitActionsFromType(kit,type);
        actions.add(new KitAction(message,null,false,false));

        inventoryEditManager.removeInventoryPlayer(inventoryPlayer.getPlayer());
        openInventory(inventoryPlayer,type);
        plugin.getConfigsManager().getKitsConfigManager().saveConfig(kit);
    }


    public void clickInventory(InventoryPlayer inventoryPlayer, ItemStack item, int slot, ClickType clickType){
        if(slot == 45){
            inventoryEditManager.openInventory(inventoryPlayer);
        }else if(slot >= 0 && slot <= 44 && item != null && !item.getType().equals(Material.AIR)){
            if(clickType.isLeftClick()){
                String type = inventoryPlayer.getInventoryName().replace("edit_actions_","");
                inventoryEditActionsEditManager.openInventory(inventoryPlayer,type,slot);
            }else if(clickType.isRightClick()){
                removeAction(inventoryPlayer,slot);
            }
        }else if(slot == 53){
            clickAddAction(inventoryPlayer);
        }
    }

    public ArrayList<KitAction> getKitActionsFromType(Kit kit,String type){
        ArrayList<KitAction> actions = null;
        if(type.equals("claim")){
            actions = kit.getClaimActions();
        }else{
            actions = kit.getErrorActions();
        }
        return actions;
    }

    public InventoryEditManager getInventoryEditManager() {
        return inventoryEditManager;
    }

    public InventoryEditActionsEditManager getInventoryEditActionsEditManager() {
        return inventoryEditActionsEditManager;
    }
}
