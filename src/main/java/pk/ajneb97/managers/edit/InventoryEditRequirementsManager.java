package pk.ajneb97.managers.edit;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import pk.ajneb97.PlayerKits2;
import pk.ajneb97.managers.MessagesManager;
import pk.ajneb97.model.Kit;
import pk.ajneb97.model.KitRequirements;
import pk.ajneb97.model.inventory.KitGuiHolder;
import pk.ajneb97.model.inventory.InventoryPlayer;
import pk.ajneb97.utils.InventoryItem;

import java.util.ArrayList;
import java.util.List;

public class InventoryEditRequirementsManager {

    private final PlayerKits2 plugin;
    private final InventoryEditManager inventoryEditManager;
    public InventoryEditRequirementsManager(PlayerKits2 plugin, InventoryEditManager inventoryEditManager){
        this.plugin = plugin;
        this.inventoryEditManager = inventoryEditManager;
    }

    @SuppressWarnings("deprecation")
    public void openInventory(InventoryPlayer inventoryPlayer) {
        inventoryPlayer.setInventoryName("edit_requirements");
        KitGuiHolder holder = new KitGuiHolder(KitGuiHolder.Type.EDIT, inventoryPlayer.getUuid());
        Inventory inv = Bukkit.createInventory(holder, 27, MessagesManager.getLegacyColoredMessage("&9Editing Kit"));
        holder.setInventory(inv);
        inventoryPlayer.setHolder(holder);

        new InventoryItem(inv, 18, Material.ARROW).name("&eGo Back").ready();

        Kit kit = plugin.getKitsManager().getKitByName(inventoryPlayer.getKitName());
        KitRequirements kitRequirements = kit.getRequirements();

        List<String> lore = new ArrayList<>();
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

        lore = new ArrayList<>();
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

        lore = new ArrayList<>();
        lore.add("&7Click to add/edit extra requirements.");
        lore.add("");
        lore.add("&cThis feature is not available yet on");
        lore.add("&cthis GUI! You can use the config.");
        new InventoryItem(inv, 15, Material.REDSTONE).name("&eSet &6&lExtra Requirements").lore(lore).ready();

        inventoryPlayer.getPlayer().openInventory(inv);
        inventoryEditManager.trackInventoryPlayer(inventoryPlayer);
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
        player.sendMessage(MessagesManager.getLegacyColoredMessage(PlayerKits2.prefix+"&7Write the new price of the kit."));

        player.closeInventory();
        inventoryPlayer.setInventoryName("edit_chat_price");
        inventoryEditManager.trackInventoryPlayer(inventoryPlayer);
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
        }catch(NumberFormatException ignored){
        }
        player.sendMessage(MessagesManager.getLegacyColoredMessage(PlayerKits2.prefix+"&cYou must use a valid number."));
    }

    public void clickInventory(InventoryPlayer inventoryPlayer, int slot) {
        if (slot == 18) {
            inventoryEditManager.openInventory(inventoryPlayer);
        }else if(slot == 11){
            clickPrice(inventoryPlayer);
        }else if(slot == 13){
            setOneTimeRequirements(inventoryPlayer);
        }
    }
}
