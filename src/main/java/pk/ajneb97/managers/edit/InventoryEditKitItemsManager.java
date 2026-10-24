package pk.ajneb97.managers.edit;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import pk.ajneb97.PlayerKits2;
import pk.ajneb97.managers.KitItemManager;
import pk.ajneb97.managers.MessagesManager;
import pk.ajneb97.model.Kit;
import pk.ajneb97.model.inventory.KitGuiHolder;
import pk.ajneb97.model.inventory.InventoryPlayer;
import pk.ajneb97.model.item.KitItem;
import pk.ajneb97.utils.*;

import java.util.ArrayList;
import java.util.List;

public class InventoryEditKitItemsManager {

    private final PlayerKits2 plugin;
    private final InventoryEditManager inventoryEditManager;
    public InventoryEditKitItemsManager(PlayerKits2 plugin, InventoryEditManager inventoryEditManager){
        this.plugin = plugin;
        this.inventoryEditManager = inventoryEditManager;
    }

    @SuppressWarnings("deprecation")
    public void openInventory(InventoryPlayer inventoryPlayer){
        inventoryPlayer.setInventoryName("edit_items");
        inventoryPlayer.setEditSelectedSlot(-1);
        KitGuiHolder holder = new KitGuiHolder(KitGuiHolder.Type.EDIT_ITEMS, inventoryPlayer.getUuid());
        Inventory inv = Bukkit.createInventory(holder, 54, MessagesManager.getLegacyColoredMessage("&9Editing Kit"));
        holder.setInventory(inv);
        inventoryPlayer.setHolder(holder);

        for(int i=46;i<=52;i++){
            if(OtherUtils.isLegacy()){
                new InventoryItem(inv, i, Material.valueOf("STAINED_GLASS_PANE")).dataValue((short) 15).name("").ready();
            }else{
                new InventoryItem(inv, i, Material.BLACK_STAINED_GLASS_PANE).name("").ready();
            }
        }

        new InventoryItem(inv, 45, Material.ARROW).name("&eGo Back").ready();

        List<String> lore = new ArrayList<>();
        lore.add("&7If you make any changes in this inventory");
        lore.add("&7it is very important to click this item");
        lore.add("&7before closing it or going back.");
        new InventoryItem(inv, 53, Material.EMERALD_BLOCK).name("&6&lSave Kit Items").lore(lore).ready();

        lore = new ArrayList<>();
        lore.add("&7Here you can edit the items of the kit.");
        lore.add("");
        lore.add("&eClick an item in your inventory &7to copy");
        lore.add("&7it in. You always keep your own item.");
        lore.add("");
        lore.add("&eClick an item here &7to pick it up, then");
        lore.add("&7click another slot to move or swap it.");
        lore.add("&7This sets its PREVIEW inventory position.");
        lore.add("");
        lore.add("&eSHIFT CLICK &7to remove an item.");
        lore.add("&eRIGHT CLICK &7to toggle offhand.");
        lore.add("");
        lore.add("&8Nothing here is a real item, so your own");
        lore.add("&8inventory is never changed.");
        new InventoryItem(inv, 49, Material.COMPASS).name("&6&lInfo").lore(lore).ready();

        Kit kit = plugin.getKitsManager().getKitByName(inventoryPlayer.getKitName());
        ArrayList<KitItem> items = kit.getItems();
        KitItemManager kitItemManager = plugin.getKitItemManager();
        int slot = 0;
        for(KitItem kitItem : items){
            ItemStack item = kitItemManager.createItemFromKitItem(kitItem, null, kit);

            int finalSlot = kitItem.getPreviewSlot() != -1 ? kitItem.getPreviewSlot() : slot;

            if(kitItem.isOffhand()){
                setItemOffHand(inv,item,finalSlot);
            }else{
                inv.setItem(finalSlot,item);
            }
            slot++;
        }

        inventoryPlayer.getPlayer().openInventory(inv);
        inventoryEditManager.trackInventoryPlayer(inventoryPlayer);
    }

    public void clickOffHand(InventoryPlayer inventoryPlayer, Inventory inv, ItemStack clickedItem, int clickedSlot){
        ServerVersion serverVersion = PlayerKits2.serverVersion;
        if(!serverVersion.serverVersionGreaterEqualThan(serverVersion,ServerVersion.v1_9_R1)) {
            inventoryPlayer.getPlayer().sendMessage(MessagesManager.getLegacyColoredMessage(PlayerKits2.prefix+"&cOffhand only works on 1.9+."));
            return;
        }
        String offhand = ItemUtils.getTagStringItem(plugin,clickedItem,"playerkits_offhand");
        if(offhand != null){

            removeItemOffHand(inv,clickedItem,clickedSlot);
        }else {

            setItemOffHand(inv,clickedItem,clickedSlot);
        }

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

    @SuppressWarnings("deprecation")
    public void setItemOffHand(Inventory inv,ItemStack item,int slot){
        List<String> offHandLore = new ArrayList<>();
        offHandLore.add(" ");
        offHandLore.add(MessagesManager.getLegacyColoredMessage("&c&lRIGHT CLICK &cto remove from offhand"));
        ItemMeta meta = item.getItemMeta();
        if(meta == null){
            return;
        }
        List<String> currentLore = meta.getLore();
        List<String> itemLore = currentLore != null ? currentLore : new ArrayList<>();
        itemLore.addAll(offHandLore);
        meta.setLore(itemLore);
        item.setItemMeta(meta);
        item = ItemUtils.setTagStringItem(plugin,item,"playerkits_offhand","yes");
        inv.setItem(slot,item);
    }

    private static void removeOffHandLoreLines(List<String> lines){
        for(int i=0;i<2 && !lines.isEmpty();i++){
            lines.removeLast();
        }
    }

    @SuppressWarnings("deprecation")
    public void removeItemOffHand(Inventory inv,ItemStack item,int slot){
        ItemMeta meta = item.getItemMeta();
        if(meta == null){
            return;
        }
        List<String> itemLore = meta.getLore();
        if(itemLore != null){
            removeOffHandLoreLines(itemLore);
            meta.setLore(itemLore);
        }
        item.setItemMeta(meta);
        item = ItemUtils.removeTagItem(plugin,item,"playerkits_offhand");
        inv.setItem(slot,item);
    }

    public void saveKitItems(InventoryPlayer inventoryPlayer){
        Inventory inv = InventoryUtils.getTopInventory(inventoryPlayer.getPlayer());
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

                String offhand = ItemUtils.getTagStringItem(plugin,item,"playerkits_offhand");
                if(offhand != null){
                    kitItem.setOffhand(true);
                    kitItem.removeOffHandFromEditInventory(plugin);
                }

                kitItem.setPreviewSlot(i);

                kitItems.add(kitItem);
            }

            kit.setItems(kitItems);
            plugin.getConfigsManager().getKitsConfigManager().saveConfig(kit);
        }
    }

    public void clickInventory(InventoryPlayer inventoryPlayer, ItemStack item, int slot, ClickType clickType, InventoryClickEvent event){
        event.setCancelled(true);
        event.setResult(org.bukkit.event.Event.Result.DENY);

        Player player = inventoryPlayer.getPlayer();
        Inventory top = InventoryUtils.getTopInventory(player);
        Inventory clicked = event.getClickedInventory();
        if(top == null || clicked == null){
            return;
        }

        plugin.getInventoryManager().syncInventoryLater(player);

        if(!clicked.equals(top)){
            ItemStack source = event.getCurrentItem();
            if(source != null && source.getType() != Material.AIR){
                copyIntoEditor(inventoryPlayer, top, source);
            }
            return;
        }

        if(slot >= 45 && slot <= 53){
            inventoryPlayer.setEditSelectedSlot(-1);
            if(slot == 45){
                inventoryEditManager.openInventory(inventoryPlayer);
            }else if(slot == 53){
                saveKitItems(inventoryPlayer);
                player.sendMessage(MessagesManager.getLegacyColoredMessage(PlayerKits2.prefix+"&aKit Items saved."));
            }
            return;
        }

        if(slot < 0 || slot > 44){
            return;
        }

        if(clickType.isRightClick()){
            inventoryPlayer.setEditSelectedSlot(-1);
            if(item != null && item.getType() != Material.AIR){
                clickOffHand(inventoryPlayer, top, item, slot);
            }
            return;
        }

        if(clickType.isShiftClick()){
            inventoryPlayer.setEditSelectedSlot(-1);
            if(item != null && item.getType() != Material.AIR){
                top.setItem(slot, null);
                player.sendMessage(MessagesManager.getLegacyColoredMessage(PlayerKits2.prefix+"&eItem removed from the kit editor."));
            }
            return;
        }

        int selected = inventoryPlayer.getEditSelectedSlot();
        if(selected == -1){
            if(item != null && item.getType() != Material.AIR){
                inventoryPlayer.setEditSelectedSlot(slot);
                player.sendMessage(MessagesManager.getLegacyColoredMessage(PlayerKits2.prefix+"&eItem picked up. Click another slot to move it."));
            }
            return;
        }

        if(selected == slot){
            inventoryPlayer.setEditSelectedSlot(-1);
            return;
        }

        ItemStack moving = top.getItem(selected);
        top.setItem(selected, top.getItem(slot));
        top.setItem(slot, moving);
        inventoryPlayer.setEditSelectedSlot(-1);
    }

    private void copyIntoEditor(InventoryPlayer inventoryPlayer, Inventory top, ItemStack source){
        for(int i=0;i<=44;i++){
            ItemStack existing = top.getItem(i);
            if(existing == null || existing.getType() == Material.AIR){
                top.setItem(i, source.clone());
                inventoryPlayer.getPlayer().sendMessage(MessagesManager.getLegacyColoredMessage(
                        PlayerKits2.prefix+"&aItem copied into the kit editor. &7You keep your original."));
                return;
            }
        }
        inventoryPlayer.getPlayer().sendMessage(MessagesManager.getLegacyColoredMessage(
                PlayerKits2.prefix+"&cThe kit editor is full."));
    }
}
