package pk.ajneb97.managers.edit;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import pk.ajneb97.PlayerKits2;
import pk.ajneb97.managers.KitItemManager;
import pk.ajneb97.managers.MessagesManager;
import pk.ajneb97.model.Kit;
import pk.ajneb97.model.inventory.InventoryPlayer;
import pk.ajneb97.model.item.KitItem;
import pk.ajneb97.utils.InventoryItem;
import pk.ajneb97.utils.ItemUtils;
import pk.ajneb97.utils.OtherUtils;

import java.util.ArrayList;
import java.util.List;

public class InventoryEditKitItemsManager {

    private PlayerKits2 plugin;
    private InventoryEditManager inventoryEditManager;
    public InventoryEditKitItemsManager(PlayerKits2 plugin, InventoryEditManager inventoryEditManager){
        this.plugin = plugin;
        this.inventoryEditManager = inventoryEditManager;
    }

    public void openInventory(InventoryPlayer inventoryPlayer){
        inventoryPlayer.setInventoryName("edit_items");
        Inventory inv = Bukkit.createInventory(null, 54, MessagesManager.getColoredMessage("&9Editing Kit"));

        //Decoration
        for(int i=46;i<=52;i++){
            if(OtherUtils.isLegacy()){
                new InventoryItem(inv, i, Material.valueOf("STAINED_GLASS_PANE")).dataValue((short) 15).name("").ready();
            }else{
                new InventoryItem(inv, i, Material.BLACK_STAINED_GLASS_PANE).name("").ready();
            }
        }

        //Set Go Back
        new InventoryItem(inv, 45, Material.ARROW).name("&eGo Back").ready();

        //Save Item
        List<String> lore = new ArrayList<>();
        lore.add("&7If you make any changes in this inventory");
        lore.add("&7it is very important to click this item");
        lore.add("&7before closing it or going back.");
        new InventoryItem(inv, 53, Material.EMERALD_BLOCK).name("&6&lSave Kit Items").lore(lore).ready();

        //Info
        lore = new ArrayList<>();
        lore.add("&7Here you can edit the items of the kit.");
        lore.add("");
        lore.add("&7If you want to set an item on the offhand");
        lore.add("&7just RIGHT CLICK it.");
        lore.add("");
        lore.add("&7You can move the items to define a custom");
        lore.add("&7position on the PREVIEW inventory.");
        new InventoryItem(inv, 49, Material.COMPASS).name("&6&lInfo").lore(lore).ready();

        //Kit Items
        Kit kit = plugin.getKitsManager().getKitByName(inventoryPlayer.getKitName());
        ArrayList<KitItem> items = kit.getItems();
        KitItemManager kitItemManager = plugin.getKitItemManager();
        int slot = 0;
        for(KitItem kitItem : items){
            ItemStack item = kitItemManager.createItemFromKitItem(kitItem, null); //Player null means variables will be showed

            int finalSlot = kitItem.getPreviewSlot() != -1 ? kitItem.getPreviewSlot() : slot;

            if(kitItem.isOffhand()){
                setItemOffHand(inv,item,finalSlot);
            }else{
                inv.setItem(finalSlot,item);
            }
            slot++;
        }

        inventoryPlayer.getPlayer().openInventory(inv);
        inventoryEditManager.getPlayers().add(inventoryPlayer);
    }

    public void clickOffHand(InventoryPlayer inventoryPlayer, Inventory inv, ItemStack clickedItem, int clickedSlot){
        if(Bukkit.getVersion().contains("1.8")){
            inventoryPlayer.getPlayer().sendMessage(MessagesManager.getColoredMessage(PlayerKits2.prefix+"&cOffhand only works on 1.9+."));
            return;
        }
        String offhand = ItemUtils.getTagStringItem(plugin,clickedItem,"playerkits_offhand");
        if(offhand != null){
            //On offhand, remove
            removeItemOffHand(inv,clickedItem,clickedSlot);
        }else {
            //Not on offhand, add
            setItemOffHand(inv,clickedItem,clickedSlot);
        }

        //Clear offhand for other items
        ItemStack[] contents = inv.getContents();
        for(int i=0;i<contents.length;i++){
            ItemStack item = contents[i];
            if(item == null || item.getType().equals(Material.AIR)){
                continue;
            }
            if(clickedSlot == i){
                continue;
            }

            String offhandCurrentItem = ItemUtils.getTagStringItem(plugin,item,"playerkits_offhand");
            if(offhandCurrentItem != null){
                removeItemOffHand(inv,item,i);
            }
        }
    }

    public void setItemOffHand(Inventory inv,ItemStack item,int slot){
        List<String> offHandLore = new ArrayList<>();
        offHandLore.add(" ");
        offHandLore.add(MessagesManager.getColoredMessage("&c&lRIGHT CLICK &cto remove from offhand"));
        ItemMeta meta = item.getItemMeta();
        List<String> itemLore = new ArrayList<>();
        if(meta.hasLore()){
            itemLore = meta.getLore();
        }
        itemLore.addAll(offHandLore);
        meta.setLore(itemLore);
        item.setItemMeta(meta);
        item = ItemUtils.setTagStringItem(plugin,item,"playerkits_offhand","yes");
        inv.setItem(slot,item);
    }

    public void removeItemOffHand(Inventory inv,ItemStack item,int slot){
        ItemMeta meta = item.getItemMeta();
        if(meta.hasLore()){
            List<String> itemLore = meta.getLore();
            itemLore.remove(itemLore.size()-1);
            itemLore.remove(itemLore.size()-1);
            meta.setLore(itemLore);
        }
        item.setItemMeta(meta);
        item = ItemUtils.removeTagItem(plugin,item,"playerkits_offhand");
        inv.setItem(slot,item);
    }

    public void saveKitItems(InventoryPlayer inventoryPlayer){
        Inventory inv = inventoryPlayer.getPlayer().getOpenInventory().getTopInventory();
        if(inv != null) {
            Kit kit = plugin.getKitsManager().getKitByName(inventoryPlayer.getKitName());
            KitItemManager kitItemManager = plugin.getKitItemManager();
            ArrayList<KitItem> kitItems = new ArrayList<>();
            ItemStack[] contents = inv.getContents();
            for(int i=0;i<=44;i++){
                ItemStack item = contents[i];
                if(item == null || item.getType().equals(Material.AIR)){
                    continue;
                }

                KitItem kitItem = kitItemManager.createKitItemFromItemStack(item,kit.isSaveOriginalItems());
                //Check offhand
                String offhand = ItemUtils.getTagStringItem(plugin,item,"playerkits_offhand");
                if(offhand != null){
                    kitItem.setOffhand(true);
                    kitItem.removeOffHandFromEditInventory(plugin);
                }

                //Preview position
                kitItem.setPreviewSlot(i);

                kitItems.add(kitItem);
            }

            kit.setItems(kitItems);
            plugin.getConfigsManager().getKitsConfigManager().saveConfig(kit);
        }
    }

    public void clickInventory(InventoryPlayer inventoryPlayer, ItemStack item, int slot, ClickType clickType, InventoryClickEvent event){
        if(event.getSlotType() == null || event.getClickedInventory() == null){
            return;
        }
        if(event.getClickedInventory().equals(inventoryPlayer.getPlayer().getOpenInventory().getTopInventory())){
            //Top inventory
            //Cancel between 45 and 53
            //Cancel if right click
            if(slot >= 45 && slot <= 53){
                event.setCancelled(true);
                if(slot == 45){
                    inventoryEditManager.openInventory(inventoryPlayer);
                }else if(slot == 53){
                    saveKitItems(inventoryPlayer);
                    inventoryPlayer.getPlayer().sendMessage(MessagesManager.getColoredMessage(PlayerKits2.prefix+"&aKit Items saved."));
                }
            }else if(clickType.isRightClick()){
                event.setCancelled(true);
                if(item != null){
                    //Is kit item
                    clickOffHand(inventoryPlayer,event.getClickedInventory(),item,slot);
                }
            }
        }
    }
}
