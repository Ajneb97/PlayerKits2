package pk.ajneb97.managers.edit;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import pk.ajneb97.PlayerKits2;
import pk.ajneb97.managers.MessagesManager;
import pk.ajneb97.model.Kit;
import pk.ajneb97.model.KitRequirements;
import pk.ajneb97.model.inventory.InventoryPlayer;
import pk.ajneb97.utils.InventoryItem;

import java.util.ArrayList;
import java.util.List;

public class InventoryEditRequirementsManager {

    private PlayerKits2 plugin;
    private InventoryEditManager inventoryEditManager;
    public InventoryEditRequirementsManager(PlayerKits2 plugin, InventoryEditManager inventoryEditManager){
        this.plugin = plugin;
        this.inventoryEditManager = inventoryEditManager;
    }

    public void openInventory(InventoryPlayer inventoryPlayer) {
        inventoryPlayer.setInventoryName("edit_requirements");
        Inventory inv = Bukkit.createInventory(null, 27, MessagesManager.getColoredMessage("&9Editing Kit"));

        //Set Go Back
        new InventoryItem(inv, 18, Material.ARROW).name("&eGo Back").ready();

        Kit kit = plugin.getKitsManager().getKitByName(inventoryPlayer.getKitName());
        KitRequirements kitRequirements = kit.getRequirements();

        //Set Price
        List<String> lore = new ArrayList<String>();
        lore.add("&7Click to define the price of");
        lore.add("&7the kit. Requires Vault and an");
        lore.add("&7economy plugin.");
        lore.add("");
        double currentPrice = 0;
        if(kitRequirements != null){
            currentPrice = kitRequirements.getPrice();
        }
        lore.add("&7Current Price: &a$" + currentPrice);
        new InventoryItem(inv, 11, Material.EMERALD).name("&eSet &6&lPrice").lore(lore).ready();

        //Set Auto Armor
        lore = new ArrayList<String>();
        lore.add("&7Click to enable/disable whether players");
        lore.add("&7must accomplish the requirements of this");
        lore.add("&7kit only once.");
        lore.add("");
        String oneTimeRequirements = "&cNO";
        if (kitRequirements != null && kitRequirements.isOneTimeRequirements()) {
            oneTimeRequirements = "&aYES";
        }
        lore.add("&7Current Status: " + oneTimeRequirements);
        new InventoryItem(inv, 13, Material.GHAST_TEAR).name("&eSet &6&lOne Time Requirements").lore(lore).ready();

        //Set Extra Requirements
        lore = new ArrayList<String>();
        lore.add("&7Click to add/edit extra requirements.");
        lore.add("");
        lore.add("&cThis feature is not available yet on");
        lore.add("&cthis GUI! You can use the config.");
        new InventoryItem(inv, 15, Material.REDSTONE).name("&eSet &6&lExtra Requirements").lore(lore).ready();

        inventoryPlayer.getPlayer().openInventory(inv);
        inventoryEditManager.getPlayers().add(inventoryPlayer);
    }

    public void setOneTimeRequirements(InventoryPlayer inventoryPlayer){
        Kit kit = plugin.getKitsManager().getKitByName(inventoryPlayer.getKitName());
        KitRequirements kitRequirements = kit.getRequirements();
        if(kitRequirements == null){
            kitRequirements = new KitRequirements();
            kit.setRequirements(kitRequirements);
        }
        kitRequirements.setOneTimeRequirements(!kitRequirements.isOneTimeRequirements());
        openInventory(inventoryPlayer);
        plugin.getConfigsManager().getKitsConfigManager().saveConfig(kit);
    }

    public void clickPrice(InventoryPlayer inventoryPlayer){
        Player player = inventoryPlayer.getPlayer();
        player.sendMessage(MessagesManager.getColoredMessage(PlayerKits2.prefix+"&7Write the new price of the kit."));

        player.closeInventory();
        inventoryPlayer.setInventoryName("edit_chat_price");
        inventoryEditManager.getPlayers().add(inventoryPlayer);
    }

    public void setPrice(InventoryPlayer inventoryPlayer,String message){
        Kit kit = plugin.getKitsManager().getKitByName(inventoryPlayer.getKitName());
        Player player = inventoryPlayer.getPlayer();
        try{
            double price = Double.parseDouble(message);
            if(price >= 0){
                KitRequirements kitRequirements = kit.getRequirements();
                if(kitRequirements == null){
                    kitRequirements = new KitRequirements();
                    kit.setRequirements(kitRequirements);
                }
                kitRequirements.setPrice(price);
                inventoryEditManager.removeInventoryPlayer(inventoryPlayer.getPlayer());
                openInventory(inventoryPlayer);
                plugin.getConfigsManager().getKitsConfigManager().saveConfig(kit);
                return;
            }
        }catch(Exception e){}
        player.sendMessage(MessagesManager.getColoredMessage(PlayerKits2.prefix+"&cYou must use a valid number."));
    }

    public void clickInventory(InventoryPlayer inventoryPlayer, ItemStack item, int slot, ClickType clickType) {
        if (slot == 18) {
            inventoryEditManager.openInventory(inventoryPlayer);
        }else if(slot == 11){
            clickPrice(inventoryPlayer);
        }else if(slot == 13){
            setOneTimeRequirements(inventoryPlayer);
        }
    }
}
