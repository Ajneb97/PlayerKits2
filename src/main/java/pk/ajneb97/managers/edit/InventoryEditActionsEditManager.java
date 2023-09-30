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

public class InventoryEditActionsEditManager {

    private PlayerKits2 plugin;
    private InventoryEditActionsManager inventoryEditActionsManager;

    public InventoryEditActionsEditManager(PlayerKits2 plugin, InventoryEditActionsManager inventoryEditActionsManager) {
        this.plugin = plugin;
        this.inventoryEditActionsManager = inventoryEditActionsManager;
    }

    public void openInventory(InventoryPlayer inventoryPlayer, String type, int actionSlot) {
        inventoryPlayer.setInventoryName("edit_action_slot_" + type + "_" + actionSlot);
        Inventory inv = Bukkit.createInventory(null, 27, MessagesManager.getColoredMessage("&9Editing Kit"));

        //Set Go Back
        new InventoryItem(inv, 18, Material.ARROW).name("&eGo Back").ready();

        Kit kit = plugin.getKitsManager().getKitByName(inventoryPlayer.getKitName());
        KitAction kitAction = inventoryEditActionsManager.getKitActionsFromType(kit, type).get(actionSlot);

        //Set Edit Action
        List<String> lore = new ArrayList<>();
        lore.add(MessagesManager.getColoredMessage("&7Click to change the action."));
        lore.add("");
        lore.add(MessagesManager.getColoredMessage("&7Current:"));
        lore.add(MessagesManager.getColoredMessage("&f") + kitAction.getAction());
        ItemStack item = new ItemStack(Material.PAPER);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(MessagesManager.getColoredMessage("&6&lEdit Action"));
        meta.setLore(lore);
        item.setItemMeta(meta);
        inv.setItem(10, item);

        //Set Execute before giving items
        lore = new ArrayList<>();
        lore.add("&7Click to enable whether the action should");
        lore.add("&7be executed before giving the kit items");
        lore.add("&7to the player.");
        lore.add("");
        String executeBeforeGivingItems = "&cNO";
        if (kitAction.isExecuteBeforeItems()) {
            executeBeforeGivingItems = "&aYES";
        }
        lore.add("&7Current Status: " + executeBeforeGivingItems);
        new InventoryItem(inv, 12, Material.BEACON).name("&eSet &6&lExecute Before Giving Items").lore(lore).ready();

        //Set Count As Item
        lore = new ArrayList<>();
        lore.add("&7Click to enable whether this action should");
        lore.add("&7count as an item, and therefore will be used");
        lore.add("&7to count empty slots on the player inventory");
        lore.add("&7when claiming the kit. Useful when you want");
        lore.add("&7to give items through a command");
        lore.add("");
        String countAsItem = "&cNO";
        if (kitAction.isCountAsItem()) {
            countAsItem = "&aYES";
        }
        lore.add("&7Current Status: " + countAsItem);
        new InventoryItem(inv, 14, Material.LADDER).name("&eSet &6&lCount As Item").lore(lore).ready();

        //Set Display Item
        Material headMaterial = null;
        if (OtherUtils.isLegacy()) {
            headMaterial = Material.valueOf("SKULL_ITEM");
        } else {
            headMaterial = Material.PLAYER_HEAD;
        }
        lore = new ArrayList<String>();
        lore.add("&7Click to edit the display item for");
        lore.add("&7this action.");
        lore.add("");
        lore.add("&7Present: " + (kitAction.getDisplayItem() != null ? "&aYES" : "&cNO"));
        new InventoryItem(inv, 16, headMaterial)
                .setSkull("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMWNkZTUxNGFmYTE5NGQ1Y2JkMDQ3N2MwNWI3Y2IxODVmZjFkZmZkMGMyZmFkZmFlMWE1YmI4MDY0ODU2Yzg5MiJ9fX0=")
                .name("&eSet &6&lDisplay Item").lore(lore).ready();


        inventoryPlayer.getPlayer().openInventory(inv);
        inventoryEditActionsManager.getInventoryEditManager().getPlayers().add(inventoryPlayer);
    }

    public void clickInventory(InventoryPlayer inventoryPlayer, ItemStack item, int slot, ClickType clickType){
        if(slot == 18){
            inventoryEditActionsManager.openInventory(inventoryPlayer,getType(inventoryPlayer));
        }else if(slot == 10){
            clickEditAction(inventoryPlayer);
        }else if(slot == 12){
            alternateExecutionBeforeGivingItems(inventoryPlayer);
        }else if(slot == 14){
            alternateCountAsItem(inventoryPlayer);
        }else if(slot == 16) {
            String type = getType(inventoryPlayer);
            int actionSlot = getActionSlot(inventoryPlayer);
            inventoryEditActionsManager.getInventoryEditManager().getInventoryEditDisplayManager()
                    .openInventory(inventoryPlayer,"action_"+type+"_"+actionSlot);
        }
    }

    public void alternateExecutionBeforeGivingItems(InventoryPlayer inventoryPlayer){
        Kit kit = plugin.getKitsManager().getKitByName(inventoryPlayer.getKitName());
        String type = getType(inventoryPlayer);
        int slot = getActionSlot(inventoryPlayer);
        KitAction kitAction = inventoryEditActionsManager.getKitActionsFromType(kit, type).get(slot);
        kitAction.setExecuteBeforeItems(!kitAction.isExecuteBeforeItems());

        openInventory(inventoryPlayer,type,slot);

        plugin.getConfigsManager().getKitsConfigManager().saveConfig(kit);
    }

    public void alternateCountAsItem(InventoryPlayer inventoryPlayer){
        Kit kit = plugin.getKitsManager().getKitByName(inventoryPlayer.getKitName());
        String type = getType(inventoryPlayer);
        int slot = getActionSlot(inventoryPlayer);
        KitAction kitAction = inventoryEditActionsManager.getKitActionsFromType(kit, type).get(slot);
        kitAction.setCountAsItem(!kitAction.isCountAsItem());

        openInventory(inventoryPlayer,type,slot);

        plugin.getConfigsManager().getKitsConfigManager().saveConfig(kit);
    }

    public void clickEditAction(InventoryPlayer inventoryPlayer){
        Player player = inventoryPlayer.getPlayer();
        player.sendMessage(MessagesManager.getColoredMessage(PlayerKits2.prefix+"&7Write the new action to set."));
        player.sendMessage(MessagesManager.getColoredMessage(PlayerKits2.prefix+"&fCheck all actions on the wiki: &bhttps://ajneb97.gitbook.io/playerkits-2/actions"));

        player.closeInventory();
        inventoryPlayer.setInventoryName("edit_chat_action_slot_" + getType(inventoryPlayer) + "_" + getActionSlot(inventoryPlayer));
        inventoryEditActionsManager.getInventoryEditManager().getPlayers().add(inventoryPlayer);
    }

    public void editAction(InventoryPlayer inventoryPlayer,String message){
        Kit kit = plugin.getKitsManager().getKitByName(inventoryPlayer.getKitName());
        String[] sep = inventoryPlayer.getInventoryName().replace("edit_chat_action_slot_","").split("_");
        String type = sep[0];
        int slot = Integer.parseInt(sep[1]);
        KitAction kitAction = inventoryEditActionsManager.getKitActionsFromType(kit, type).get(slot);
        kitAction.setAction(message);

        inventoryEditActionsManager.getInventoryEditManager().removeInventoryPlayer(inventoryPlayer.getPlayer());
        openInventory(inventoryPlayer,type,slot);
        plugin.getConfigsManager().getKitsConfigManager().saveConfig(kit);
    }

    public String getType(InventoryPlayer inventoryPlayer){
        return inventoryPlayer.getInventoryName().replace("edit_action_slot_","").split("_")[0];
    }

    public int getActionSlot(InventoryPlayer inventoryPlayer){
        return Integer.parseInt(inventoryPlayer.getInventoryName().replace("edit_action_slot_","").split("_")[1]);
    }
}
