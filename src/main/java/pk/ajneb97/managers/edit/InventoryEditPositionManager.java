package pk.ajneb97.managers.edit;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import pk.ajneb97.PlayerKits2;
import pk.ajneb97.configs.MainConfigManager;
import pk.ajneb97.managers.*;
import pk.ajneb97.model.Kit;
import pk.ajneb97.model.internal.KitVariable;
import pk.ajneb97.model.inventory.KitGuiHolder;
import pk.ajneb97.model.inventory.InventoryPlayer;
import pk.ajneb97.model.inventory.ItemKitInventory;
import pk.ajneb97.model.inventory.KitInventory;
import pk.ajneb97.model.item.KitItem;
import pk.ajneb97.utils.InventoryItem;
import pk.ajneb97.utils.InventoryUtils;
import pk.ajneb97.utils.ItemUtils;

import java.util.ArrayList;
import java.util.List;

public class InventoryEditPositionManager {

    private final PlayerKits2 plugin;
    private final InventoryEditManager inventoryEditManager;
    public InventoryEditPositionManager(PlayerKits2 plugin, InventoryEditManager inventoryEditManager){
        this.plugin = plugin;
        this.inventoryEditManager = inventoryEditManager;
    }

    public void openInventory(InventoryPlayer inventoryPlayer,String positionInventory) {
        KitInventory kitInventory = plugin.getInventoryManager().getInventory(positionInventory);

        MainConfigManager mainConfigManager = plugin.getConfigsManager().getMainConfigManager();
        KitGuiHolder holder = new KitGuiHolder(KitGuiHolder.Type.EDIT_POSITION, inventoryPlayer.getUuid());
        Inventory inv = InventoryUtils.createInventory(holder, kitInventory.slots(), kitInventory.title(),
                mainConfigManager.isUseMiniMessage());
        inventoryPlayer.setHolder(holder);

        List<ItemKitInventory> items = kitInventory.items();
        KitItemManager kitItemManager = plugin.getKitItemManager();
        KitsManager kitsManager = plugin.getKitsManager();

        for(ItemKitInventory itemInventory : items){
            for(int slot : itemInventory.getSlots()){
                String type = itemInventory.getType();
                if(type != null){
                    if(type.startsWith("kit: ")){
                        String kitName = type.replace("kit: ","");
                        Kit kit = kitsManager.getKitByName(kitName);
                        if(kit == null){
                            continue;
                        }

                        KitItem kitItem = kit.getDisplayItemDefault().copy();

                        if(kitName.equals(inventoryPlayer.getKitName())){
                            List<String> extraLore = new ArrayList<>();
                            List<String> currentLore = kitItem.getLore();
                            extraLore.add(" ");
                            if(mainConfigManager.isUseMiniMessage()){
                                extraLore.add(MessagesManager.getLegacyColoredMessage("<green><b>THIS IS THE CURRENT POSITION OF</b>"));
                                extraLore.add(MessagesManager.getLegacyColoredMessage("<green><b>THE KIT.</b>"));
                            }else{
                                extraLore.add(MessagesManager.getLegacyColoredMessage("&a&lTHIS IS THE CURRENT POSITION OF"));
                                extraLore.add(MessagesManager.getLegacyColoredMessage("&a&lTHE KIT."));
                            }

                            List<String> itemLore = new ArrayList<>();
                            if(currentLore != null){
                                itemLore = new ArrayList<>(currentLore);
                            }
                            itemLore.addAll(extraLore);
                            kitItem.setLore(itemLore);
                        }

                        ArrayList<KitVariable> variablesToReplace = new ArrayList<>();
                        variablesToReplace.add(new KitVariable("%kit_name%",kit.getName()));
                        kitItemManager.replaceVariables(kitItem,variablesToReplace,inventoryPlayer.getPlayer());

                        ItemStack item = kitItemManager.createItemFromKitItem(kitItem,null,kit);

                        inv.setItem(slot,item);
                    }
                    continue;
                }

                ItemStack item = kitItemManager.createItemFromKitItem(itemInventory.getItem(),inventoryPlayer.getPlayer(),null);
                String openInventory = itemInventory.getOpenInventory();
                if(openInventory != null) {
                    item = ItemUtils.setTagStringItem(plugin,item, "playerkits_open_inventory", openInventory);
                }
                inv.setItem(slot,item);
            }
        }

        inventoryPlayer.setInventoryName("edit_position;"+positionInventory+";false");
        inventoryPlayer.getPlayer().openInventory(inv);
        inventoryPlayer.setInventoryName("edit_position;"+positionInventory+";true");
        inventoryPlayer.saveInventoryContents();
        inventoryPlayer.getPlayer().getInventory().clear();

        List<String> lore = new ArrayList<>();
        lore.add("&7Just click on the position of the");
        lore.add("&7inventory, where you want the new kit");
        lore.add("&7item to be displayed.");
        lore.add("");
        lore.add("&7You can go back by closing this inventory.");
        new InventoryItem(inventoryPlayer.getPlayer().getInventory(), 22, Material.COMPASS).name("&6&lInfo").lore(lore).ready();

        inventoryEditManager.trackInventoryPlayer(inventoryPlayer);

    }

    public void setPosition(InventoryPlayer inventoryPlayer,int slot){
        String positionInventory = inventoryPlayer.getInventoryName().split(";")[1];
        InventoryManager inventoryManager = plugin.getInventoryManager();
        String kitName = inventoryPlayer.getKitName();

        inventoryManager.removeKitFromInventory(kitName);

        KitInventory kitInventory = inventoryManager.getInventory(positionInventory);
        kitInventory.addKitItemOnSlot(kitName,slot);
        plugin.getConfigsManager().getInventoryConfigManager().save();

        closeInventory(inventoryPlayer);
    }

    public void clickInventory(InventoryPlayer inventoryPlayer, ItemStack item, int slot, InventoryClickEvent event){
        event.setCancelled(true);
        if(event.getClickedInventory() == null){
            return;
        }
        if(event.getClickedInventory().equals(InventoryUtils.getTopInventory(inventoryPlayer.getPlayer()))){

            if(item == null || item.getType().equals(Material.AIR)){

                setPosition(inventoryPlayer,slot);
                inventoryPlayer.getPlayer().sendMessage(MessagesManager.getLegacyColoredMessage(PlayerKits2.prefix+"&aKit position updated."));
                return;
            }

            String openInventory = ItemUtils.getTagStringItem(plugin,item,"playerkits_open_inventory");
            if(openInventory != null){
                openInventory(inventoryPlayer,openInventory);
                return;
            }
            inventoryPlayer.getPlayer().sendMessage(MessagesManager.getLegacyColoredMessage(PlayerKits2.prefix+"&cThis position is occupied."));
        }
    }

    public void closeInventory(InventoryPlayer inventoryPlayer){
        String[] parts = inventoryPlayer.getInventoryName().split(";");
        boolean mustReturn = parts.length >= 3 && Boolean.parseBoolean(parts[2]);

        inventoryPlayer.restoreSavedInventoryContents();

        if(mustReturn){
            Player player = inventoryPlayer.getPlayer();
            plugin.getPlatformScheduler().runForEntityLater(player, () -> {
                if(player.isOnline()){
                    inventoryEditManager.openInventory(inventoryPlayer);
                }
            }, 1L);
        }
    }
}
