package pk.ajneb97.managers.edit;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import pk.ajneb97.PlayerKits2;
import pk.ajneb97.managers.MessagesManager;
import pk.ajneb97.model.Kit;
import pk.ajneb97.model.KitAction;
import pk.ajneb97.model.inventory.InventoryPlayer;
import pk.ajneb97.model.item.KitItem;
import pk.ajneb97.utils.InventoryItem;
import pk.ajneb97.utils.OtherUtils;

import java.util.ArrayList;
import java.util.List;

public class InventoryEditDisplayManager {

    private PlayerKits2 plugin;
    private InventoryEditManager inventoryEditManager;
    public InventoryEditDisplayManager(PlayerKits2 plugin, InventoryEditManager inventoryEditManager){
        this.plugin = plugin;
        this.inventoryEditManager = inventoryEditManager;
    }

    public void openInventory(InventoryPlayer inventoryPlayer, String type) {
        inventoryPlayer.setInventoryName("edit_display_" + type);
        Inventory inv = Bukkit.createInventory(null, 27, MessagesManager.getColoredMessage("&9Editing Kit"));

        //Decoration
        ArrayList<Integer> slots = new ArrayList<>();
        OtherUtils.addRangeToList(0,11,slots);
        OtherUtils.addRangeToList(15,26,slots);
        for(int i : slots){
            if(OtherUtils.isLegacy()){
                new InventoryItem(inv, i, Material.valueOf("STAINED_GLASS_PANE")).dataValue((short) 15).name("").ready();
            }else{
                new InventoryItem(inv, i, Material.BLACK_STAINED_GLASS_PANE).name("").ready();
            }
        }

        //Set Go Back
        new InventoryItem(inv, 18, Material.ARROW).name("&eGo Back").ready();

        //Info Heads
        Material headMaterial = null;
        if(OtherUtils.isLegacy()){
            headMaterial = Material.valueOf("SKULL_ITEM");
        }else{
            headMaterial = Material.PLAYER_HEAD;
        }
        List<String> lore = new ArrayList<>();
        lore.add("&7On this empty space you can place an");
        lore.add("&7already created custom item, or you can");
        lore.add("&7do so directly from the config of this kit.");
        new InventoryItem(inv, 12, headMaterial)
                .setSkull("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMTliZjMyOTJlMTI2YTEwNWI1NGViYTcxM2FhMWIxNTJkNTQxYTFkODkzODgyOWM1NjM2NGQxNzhlZDIyYmYifX19")
                .name("&6Place item here >>").lore(lore).ready();
        new InventoryItem(inv, 14, headMaterial)
                .setSkull("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYmQ2OWUwNmU1ZGFkZmQ4NGU1ZjNkMWMyMTA2M2YyNTUzYjJmYTk0NWVlMWQ0ZDcxNTJmZGM1NDI1YmMxMmE5In19fQ==")
                .name("&6<< Place item here").lore(lore).ready();

        Kit kit = plugin.getKitsManager().getKitByName(inventoryPlayer.getKitName());
        KitItem kitItem = getKitDisplayItemFromType(kit,type);

        //Set Info Item
        lore = new ArrayList<>();
        lore.add("&7You are currently editing the");
        if(type.equals("permission")){
            lore.add("&6&lNo Permissions Display Item");
        }else if(type.equals("onetime")){
            lore.add("&6&lOne Time Display Item");
        }else if(type.equals("cooldown")){
            lore.add("&eSet &6&lCooldown Display Item");
        }else if(type.equals("requirements")){
            lore.add("&6&lOne Time Requirements Display Item");
        }else if(type.startsWith("action")){
            String actionType = type.split("_")[1];
            int actionSlot = Integer.parseInt(type.split("_")[2]);
            lore.add("&6&l"+actionType.toUpperCase()+" Action "+actionSlot+" Display Item");
        }
        else{
            lore.add("&6&lDefault Display Item");
        }
        new InventoryItem(inv, 0, Material.COMPASS).name("&6&lInfo").lore(lore).ready();

        //Save Item
        lore = new ArrayList<>();
        lore.add("&7Click here if you made changes");
        lore.add("&7to the item, before leaving this");
        lore.add("&7inventory.");
        new InventoryItem(inv, 26, Material.EMERALD_BLOCK).name("&6&lSave Item").lore(lore).ready();

        if(kitItem != null && kitItem.getId() != null){
            inv.setItem(13,plugin.getKitItemManager().createItemFromKitItem(kitItem, inventoryPlayer.getPlayer()));
        }

        inventoryPlayer.getPlayer().openInventory(inv);
        inventoryEditManager.getPlayers().add(inventoryPlayer);
    }

    public void saveKitItem(InventoryPlayer inventoryPlayer){
        Inventory inv = inventoryPlayer.getPlayer().getOpenInventory().getTopInventory();
        if(inv != null) {
            ItemStack item = inv.getItem(13);

            KitItem kitItem = null;
            if(item != null && !item.getType().equals(Material.AIR)){
                kitItem = plugin.getKitItemManager().createKitItemFromItemStack(item,false);
            }

            Kit kit = plugin.getKitsManager().getKitByName(inventoryPlayer.getKitName());
            String type = inventoryPlayer.getInventoryName().replace("edit_display_","");
            if(type.equals("permission")){
                kit.setDisplayItemNoPermission(kitItem);
            }else if(type.equals("onetime")){
                kit.setDisplayItemOneTime(kitItem);
            }else if(type.equals("cooldown")){
                kit.setDisplayItemCooldown(kitItem);
            }else if(type.equals("requirements")){
                kit.setDisplayItemOneTimeRequirements(kitItem);
            }else if(type.startsWith("action")){
                String actionType = type.split("_")[1];
                int actionSlot = Integer.parseInt(type.split("_")[2]);
                KitAction kitAction = getKitActionsFromType(kit,actionType).get(actionSlot);
                kitAction.setDisplayItem(kitItem);
            }else{
                kit.setDisplayItemDefault(kitItem);
            }
            plugin.getConfigsManager().getKitsConfigManager().saveConfig(kit);
        }
    }

    public void clickInventory(InventoryPlayer inventoryPlayer, ItemStack item, int slot, ClickType clickType,InventoryClickEvent event){
        if(event.getSlotType() == null || event.getClickedInventory() == null){
            return;
        }
        if(event.getClickedInventory().equals(inventoryPlayer.getPlayer().getOpenInventory().getTopInventory())){
            //Top inventory
            //Cancel unless is slot 13
            if(slot != 13){
                event.setCancelled(true);
                if(slot == 18){
                    if(inventoryPlayer.getInventoryName().startsWith("edit_display_action")){
                        String type = inventoryPlayer.getInventoryName().replace("edit_display_action_","");
                        String actionType = type.split("_")[0];
                        int actionSlot = Integer.parseInt(type.split("_")[1]);
                        inventoryEditManager.getInventoryEditActionsManager().getInventoryEditActionsEditManager()
                                .openInventory(inventoryPlayer,actionType,actionSlot);
                    }else{
                        inventoryEditManager.openInventory(inventoryPlayer);
                    }
                }else if(slot == 26){
                    saveKitItem(inventoryPlayer);
                    inventoryPlayer.getPlayer().sendMessage(MessagesManager.getColoredMessage(PlayerKits2.prefix+"&aDisplay item saved."));
                }
            }
        }
    }

    public KitItem getKitDisplayItemFromType(Kit kit, String type){
        if(type.startsWith("action")){
            String actionType = type.split("_")[1];
            int actionSlot = Integer.parseInt(type.split("_")[2]);
            KitAction kitAction = getKitActionsFromType(kit,actionType).get(actionSlot);
            return kitAction.getDisplayItem();
        }else{
            KitItem kitItem = kit.getDisplayItemDefault();
            switch(type){
                case "permission":
                    return kit.getDisplayItemNoPermission();
                case "onetime":
                    return kit.getDisplayItemOneTime();
                case "cooldown":
                    return kit.getDisplayItemCooldown();
                case "requirements":
                    return kit.getDisplayItemOneTimeRequirements();
            }
            return kitItem;
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
}
