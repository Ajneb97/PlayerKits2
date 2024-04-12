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
import pk.ajneb97.model.KitAction;
import pk.ajneb97.model.internal.KitPosition;
import pk.ajneb97.model.inventory.InventoryPlayer;
import pk.ajneb97.model.item.KitItem;
import pk.ajneb97.utils.InventoryItem;
import pk.ajneb97.utils.OtherUtils;

import java.util.ArrayList;
import java.util.List;

public class InventoryEditManager {

    private PlayerKits2 plugin;
    private ArrayList<InventoryPlayer> players;
    private InventoryEditActionsManager inventoryEditActionsManager;
    private InventoryEditDisplayManager inventoryEditDisplayManager;
    private InventoryEditKitItemsManager inventoryEditKitItemsManager;
    private InventoryEditPositionManager inventoryEditPositionManager;
    private InventoryEditRequirementsManager inventoryEditRequirementsManager;
    public InventoryEditManager(PlayerKits2 plugin){
        this.plugin = plugin;
        this.players = new ArrayList<>();
        this.inventoryEditActionsManager = new InventoryEditActionsManager(plugin,this);
        this.inventoryEditDisplayManager = new InventoryEditDisplayManager(plugin,this);
        this.inventoryEditKitItemsManager = new InventoryEditKitItemsManager(plugin,this);
        this.inventoryEditPositionManager = new InventoryEditPositionManager(plugin,this);
        this.inventoryEditRequirementsManager = new InventoryEditRequirementsManager(plugin,this);
    }

    public InventoryPlayer getInventoryPlayer(Player player){
        for(InventoryPlayer inventoryPlayer : players){
            if(inventoryPlayer.getPlayer().equals(player)){
                return inventoryPlayer;
            }
        }
        return null;
    }

    public InventoryEditActionsManager getInventoryEditActionsManager() {
        return inventoryEditActionsManager;
    }

    public InventoryEditDisplayManager getInventoryEditDisplayManager() {
        return inventoryEditDisplayManager;
    }

    public InventoryEditKitItemsManager getInventoryEditKitItemsManager() {
        return inventoryEditKitItemsManager;
    }

    public InventoryEditPositionManager getInventoryEditPositionManager() {
        return inventoryEditPositionManager;
    }

    public ArrayList<InventoryPlayer> getPlayers() {
        return players;
    }

    public void removeInventoryPlayer(Player player){
        for(int i=0;i<players.size();i++){
            if(players.get(i).getPlayer().equals(player)){
                players.remove(i);
            }
        }
    }

    public void openInventory(InventoryPlayer inventoryPlayer) {
        inventoryPlayer.setInventoryName("edit_main_inventory");
        Inventory inv = Bukkit.createInventory(null, 45, MessagesManager.getColoredMessage("&9Editing Kit"));

        Kit kit = plugin.getKitsManager().getKitByName(inventoryPlayer.getKitName());

        //Set Position
        List<String> lore = new ArrayList<String>();
        lore.add("&7Click to define the position of the display");
        lore.add("&7item of this kit in the Inventory.");
        lore.add("");
        String slot = "none";
        String inventoryName = "none";
        KitPosition kitPosition = plugin.getInventoryManager().getKitPositionByKitName(kit.getName());
        if (kitPosition != null) {
            slot = kitPosition.getSlot() + "";
            inventoryName = kitPosition.getInventoryName();
        }
        lore.add("&7Current Slot: &a" + slot);
        lore.add("&7Current Inventory: &a" + inventoryName);
        new InventoryItem(inv, 10, Material.DROPPER).name("&eSet &6&lSlot").lore(lore).ready();

        //Set Cooldown
        lore = new ArrayList<String>();
        lore.add("&7Click to define the cooldown of");
        lore.add("&7the kit.");
        lore.add("");
        lore.add("&7Current Cooldown: &a" + kit.getCooldown() + "(s)");
        if (OtherUtils.isLegacy()) {
            new InventoryItem(inv, 11, Material.valueOf("WATCH")).name("&eSet &6&lCooldown").lore(lore).ready();
        } else {
            new InventoryItem(inv, 11, Material.CLOCK).name("&eSet &6&lCooldown").lore(lore).ready();
        }

        //Set Permission Required
        lore = new ArrayList<String>();
        lore.add("&7Click to enable/disable whether this kit");
        lore.add("&7needs permissions to be claimed.");
        lore.add("&7The permission will be:");
        lore.add("&eplayerkits.kit."+kit.getName());
        lore.add("");
        String permission = "&cNO";
        if (kit.isPermissionRequired()) {
            permission = "&aYES";
        }
        lore.add("&7Current Status: " + permission);
        new InventoryItem(inv, 12, Material.REDSTONE_BLOCK).name("&eSet &6&lPermission Required").lore(lore).ready();

        //Set One Time
        lore = new ArrayList<String>();
        lore.add("&7Click to enable/disable whether this kit");
        lore.add("&7should be claimed just one time.");
        lore.add("");
        String oneTime = "&cNO";
        if (kit.isOneTime()) {
            oneTime = "&aYES";
        }
        lore.add("&7Current Status: " + oneTime);
        new InventoryItem(inv, 20, Material.GHAST_TEAR).name("&eSet &6&lOne Time").lore(lore).ready();

        //Set Auto Armor
        lore = new ArrayList<String>();
        lore.add("&7Click to enable/disable whether armor");
        lore.add("&7should be equipped automatically when");
        lore.add("&7this kit is claimed.");
        lore.add("");
        String autoArmor = "&cNO";
        if (kit.isAutoArmor()) {
            autoArmor = "&aYES";
        }
        lore.add("&7Current Status: " + autoArmor);
        new InventoryItem(inv, 21, Material.IRON_CHESTPLATE).name("&eSet &6&lAuto Armor").lore(lore).ready();

        //Display Item DEFAULT
        Material headMaterial = null;
        if(OtherUtils.isLegacy()){
            headMaterial = Material.valueOf("SKULL_ITEM");
        }else{
            headMaterial = Material.PLAYER_HEAD;
        }
        lore = new ArrayList<String>();
        lore.add("&7Click to edit the default kit display");
        lore.add("&7item.");
        lore.add("");
        lore.add("&7Present: "+(kit.getDisplayItemDefault() != null ? "&aYES" : "&cNO"));
        new InventoryItem(inv, 38, headMaterial)
                .setSkull("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMWNkZTUxNGFmYTE5NGQ1Y2JkMDQ3N2MwNWI3Y2IxODVmZjFkZmZkMGMyZmFkZmFlMWE1YmI4MDY0ODU2Yzg5MiJ9fX0=")
                .name("&eSet &6&lDefault Display Item").lore(lore).ready();

        //Display Item NO PERMISSION
        lore = new ArrayList<String>();
        lore.add("&7Click to edit the kit display item when");
        lore.add("&7player doesn't have the permissions to");
        lore.add("&7claim it.");
        lore.add("");
        lore.add("&7Present: "+(kit.getDisplayItemNoPermission() != null ? "&aYES" : "&cNO"));
        new InventoryItem(inv, 39, headMaterial)
                .setSkull("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYzdkNDk5YTlhZjUyOTllZGE0Y2NkYWMyMDE5ZWZlN2YyNDk2MWYzZTFmY2U3Njk0Y2I2ODIwMjlkMjllOWVhMSJ9fX0=")
                .name("&eSet &6&lNo Permissions Display Item").lore(lore).ready();

        //Display Item ONE TIME
        lore = new ArrayList<String>();
        lore.add("&7Click to edit the kit display item when");
        lore.add("&7one time option is enabled and the player");
        lore.add("&7has already claimed it.");
        lore.add("");
        lore.add("&7Present: "+(kit.getDisplayItemOneTime() != null ? "&aYES" : "&cNO"));
        new InventoryItem(inv, 40, headMaterial)
                .setSkull("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMzU3NDc2NmUzZGVjNDkwNGZhNWFhMTU5MjA4ZGFlYzExYzYzYzVlMzI2MTU3YzM2NWViMTY5MDFiNjFmNjQ1YiJ9fX0=")
                .name("&eSet &6&lOne Time Display Item").lore(lore).ready();

        //Display Item COOLDOWN
        lore = new ArrayList<String>();
        lore.add("&7Click to edit the kit display item when");
        lore.add("&7the player is on cooldown.");
        lore.add("");
        lore.add("&7Present: "+(kit.getDisplayItemCooldown() != null ? "&aYES" : "&cNO"));
        new InventoryItem(inv, 41, headMaterial)
                .setSkull("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvODg3YTc1ZjZjYmNjOGUxN2I4ZmJmYWVlMzM3MGZlZjAyMWMyNWY3MDM1YjI1ZDRjNjU3OTZlZjMzODljZWYwMCJ9fX0=")
                .name("&eSet &6&lCooldown Display Item").lore(lore).ready();

        //Display Item ONE TIME REQUIREMENTS
        lore = new ArrayList<String>();
        lore.add("&7Click to edit the kit display item when");
        lore.add("&7one time requirements option is enabled and");
        lore.add("&7the player has already accomplished the");
        lore.add("&7requirements.");
        lore.add("");
        lore.add("&7Present: "+(kit.getDisplayItemOneTimeRequirements() != null ? "&aYES" : "&cNO"));
        new InventoryItem(inv, 42, headMaterial)
                .setSkull("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNGU4NmJlNTQ5OWViNDI0NWE0NjFiZGNiODZmZjE2M2M4NTVjMTgyODFmOTcxMDU0MmIxN2ZkMjhiYWU0MjQzYiJ9fX0=")
                .name("&eSet &6&lOne Time Requirements Display Item").lore(lore).ready();

        //Set Kit Items
        lore = new ArrayList<String>();
        lore.add("&7Click to edit the kit items.");
        lore.add("");
        lore.add("&7Current Items:");
        int max = 20;
        for(KitItem kitItem : kit.getItems()){
            if(kitItem.getOriginalItem() != null){
                ItemStack originalItem = kitItem.getOriginalItem();
                lore.add(MessagesManager.getColoredMessage("&8- &fx"+originalItem.getAmount()+" "+originalItem.getType()));
            }else{
                lore.add(MessagesManager.getColoredMessage("&8- &fx"+kitItem.getAmount()+" "+kitItem.getId()));
            }
            max--;
            if(max <= 0){
                break;
            }
        }
        new InventoryItem(inv, 14, Material.DIAMOND).name("&eSet &6&lKit Items").lore(lore).ready();

        //Set Requirements
        lore = new ArrayList<String>();
        lore.add("&7Click to edit the kit requirements.");
        new InventoryItem(inv, 15, Material.REDSTONE).name("&eSet &6&lRequirements").lore(lore).ready();

        //Set Claim Actions
        lore = new ArrayList<String>();
        lore.add("&7Click to edit the actions to execute");
        lore.add("&7when the player claims the kit.");
        lore.add("");
        lore.add("&7Current Actions:");
        lore = setActionItemLore(kit.getClaimActions(),lore);
        new InventoryItem(inv, 23, Material.IRON_INGOT).name("&eSet &6&lClaim Actions").lore(lore).ready();

        //Set Error Actions
        lore = new ArrayList<String>();
        lore.add("&7Click to edit the actions to execute");
        lore.add("&7when there is an error when claiming");
        lore.add("&7the kit.");
        lore.add("");
        lore.add("&7Current Actions:");
        lore = setActionItemLore(kit.getErrorActions(),lore);
        new InventoryItem(inv, 24, Material.NETHER_BRICK).name("&eSet &6&lError Actions").lore(lore).ready();

        inventoryPlayer.getPlayer().openInventory(inv);
        players.add(inventoryPlayer);
    }

    public List<String> setActionItemLore(ArrayList<KitAction> actions,List<String> lore){
        int max = 20;
        if(actions.isEmpty()){
            lore.add(MessagesManager.getColoredMessage("&cNONE"));
        }else{
            for(KitAction kitAction : actions){
                String actionLine = kitAction.getAction();
                List<String> separatedActionLine = new ArrayList<String>();
                int currentPos = 0;
                for(int i=0;i<actionLine.length();i++) {
                    if(currentPos >= 35 && actionLine.charAt(i) == ' ') {
                        String m = actionLine.substring(i-currentPos, i);
                        currentPos = 0;
                        separatedActionLine.add(m);
                    }else {
                        currentPos++;
                    }
                    if(i==actionLine.length()-1) {
                        String m = actionLine.substring(i-currentPos+1, actionLine.length());
                        separatedActionLine.add(m);
                    }
                }

                for(int i=0;i<separatedActionLine.size();i++){
                    if(i == 0){
                        lore.add(MessagesManager.getColoredMessage("&8- &f")+separatedActionLine.get(i));
                    }else{
                        lore.add(MessagesManager.getColoredMessage("&f")+separatedActionLine.get(i));
                    }
                }

                max--;
                if(max <= 0){
                    break;
                }
            }
        }
        return lore;
    }

    public void setPermissionRequired(InventoryPlayer inventoryPlayer){
        Kit kit = plugin.getKitsManager().getKitByName(inventoryPlayer.getKitName());
        kit.setPermissionRequired(!kit.isPermissionRequired());
        openInventory(inventoryPlayer);
        plugin.getConfigsManager().getKitsConfigManager().saveConfig(kit);
    }

    public void setOneTime(InventoryPlayer inventoryPlayer){
        Kit kit = plugin.getKitsManager().getKitByName(inventoryPlayer.getKitName());
        kit.setOneTime(!kit.isOneTime());
        openInventory(inventoryPlayer);
        plugin.getConfigsManager().getKitsConfigManager().saveConfig(kit);
    }

    public void setAutoArmor(InventoryPlayer inventoryPlayer){
        Kit kit = plugin.getKitsManager().getKitByName(inventoryPlayer.getKitName());
        kit.setAutoArmor(!kit.isAutoArmor());
        openInventory(inventoryPlayer);
        plugin.getConfigsManager().getKitsConfigManager().saveConfig(kit);
    }

    public void clickCooldown(InventoryPlayer inventoryPlayer){
        Player player = inventoryPlayer.getPlayer();
        player.sendMessage(MessagesManager.getColoredMessage(PlayerKits2.prefix+"&7Write the new cooldown of the kit (in seconds)."));

        player.closeInventory();
        inventoryPlayer.setInventoryName("edit_chat_cooldown");
        players.add(inventoryPlayer);
    }

    public void setCooldown(InventoryPlayer inventoryPlayer,String message){
        Kit kit = plugin.getKitsManager().getKitByName(inventoryPlayer.getKitName());
        Player player = inventoryPlayer.getPlayer();
        try{
            int cooldown = Integer.parseInt(message);
            if(cooldown >= 0){
                kit.setCooldown(cooldown);
                removeInventoryPlayer(inventoryPlayer.getPlayer());
                openInventory(inventoryPlayer);
                plugin.getConfigsManager().getKitsConfigManager().saveConfig(kit);
                return;
            }
        }catch(Exception e){}
        player.sendMessage(MessagesManager.getColoredMessage(PlayerKits2.prefix+"&cYou must use a valid number."));
    }

    public void clickInventory(InventoryPlayer inventoryPlayer, ItemStack item, int slot, ClickType clickType) {
        String inventory = inventoryPlayer.getInventoryName();

        if(inventory.equals("edit_main_inventory")){
            switch(slot){
                case 10:
                    inventoryEditPositionManager.openInventory(inventoryPlayer,"main_inventory");
                    break;
                case 11:
                    clickCooldown(inventoryPlayer);
                    break;
                case 12:
                    setPermissionRequired(inventoryPlayer);
                    break;
                case 14:
                    inventoryEditKitItemsManager.openInventory(inventoryPlayer);
                    break;
                case 15:
                    inventoryEditRequirementsManager.openInventory(inventoryPlayer);
                    break;
                case 20:
                    setOneTime(inventoryPlayer);
                    break;
                case 21:
                    setAutoArmor(inventoryPlayer);
                    break;
                case 23:
                    inventoryEditActionsManager.openInventory(inventoryPlayer,"claim");
                    break;
                case 24:
                    inventoryEditActionsManager.openInventory(inventoryPlayer,"error");
                    break;
                case 38:
                    inventoryEditDisplayManager.openInventory(inventoryPlayer,"default");
                    break;
                case 39:
                    inventoryEditDisplayManager.openInventory(inventoryPlayer,"permission");
                    break;
                case 40:
                    inventoryEditDisplayManager.openInventory(inventoryPlayer,"onetime");
                    break;
                case 41:
                    inventoryEditDisplayManager.openInventory(inventoryPlayer,"cooldown");
                    break;
                case 42:
                    inventoryEditDisplayManager.openInventory(inventoryPlayer,"requirements");
                    break;
            }
        }else if(inventory.startsWith("edit_actions_")){
            inventoryEditActionsManager.clickInventory(inventoryPlayer,item,slot,clickType);
        }else if(inventory.startsWith("edit_action_slot_")){
            inventoryEditActionsManager.getInventoryEditActionsEditManager().clickInventory(inventoryPlayer,item,slot,clickType);
        }else if(inventory.equals("edit_requirements")){
            inventoryEditRequirementsManager.clickInventory(inventoryPlayer,item,slot,clickType);
        }
    }

    public void writeChat(InventoryPlayer inventoryPlayer,String message){
        String inventory = inventoryPlayer.getInventoryName();
        if(inventory.equals("edit_chat_cooldown")){
            setCooldown(inventoryPlayer,message);
        }else if(inventory.startsWith("edit_chat_add_action_")){
            inventoryEditActionsManager.addAction(inventoryPlayer,message);
        }else if(inventory.startsWith("edit_chat_action_slot_")){
            inventoryEditActionsManager.getInventoryEditActionsEditManager().editAction(inventoryPlayer,message);
        }else if(inventory.equals("edit_chat_price")){
            inventoryEditRequirementsManager.setPrice(inventoryPlayer,message);
        }
    }
}
