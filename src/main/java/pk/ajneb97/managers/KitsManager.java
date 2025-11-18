package pk.ajneb97.managers;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import pk.ajneb97.PlayerKits2;
import pk.ajneb97.configs.MainConfigManager;
import pk.ajneb97.model.Kit;
import pk.ajneb97.model.KitAction;
import pk.ajneb97.model.KitRequirements;
import pk.ajneb97.model.internal.GiveKitInstructions;
import pk.ajneb97.model.internal.PlayerKitsMessageResult;
import pk.ajneb97.model.inventory.KitInventory;
import pk.ajneb97.model.item.KitItem;
import pk.ajneb97.utils.ActionUtils;
import pk.ajneb97.utils.OtherUtils;
import pk.ajneb97.utils.PlayerUtils;
import java.util.ArrayList;
import java.util.List;

public class KitsManager {

    private PlayerKits2 plugin;
    private ArrayList<Kit> kits;
    public KitsManager(PlayerKits2 plugin){
        this.plugin = plugin;
    }

    public PlayerKits2 getPlugin() {
        return plugin;
    }

    public void setPlugin(PlayerKits2 plugin) {
        this.plugin = plugin;
    }

    public ArrayList<Kit> getKits() {
        return kits;
    }

    public void setKits(ArrayList<Kit> kits) {
        this.kits = kits;
    }

    public Kit getKitByName(String name){
        for(Kit kit : kits){
            if(kit.getName().equals(name)){
                return kit;
            }
        }
        return null;
    }

    public void removeKit(String name){
        for(int i=0;i<kits.size();i++){
            if(kits.get(i).getName().equals(name)){
                kits.remove(i);
                return;
            }
        }
    }

    public void createKit(String kitName,Player player,boolean saveOriginalItems){
        Kit kit = getKitByName(kitName);
        FileConfiguration messagesFile = plugin.getConfigsManager().getMessagesConfigManager().getConfig();
        MainConfigManager mainConfigManager = plugin.getConfigsManager().getMainConfigManager();
        MessagesManager msgManager = plugin.getMessagesManager();
        if(kit != null){
            msgManager.sendMessage(player,messagesFile.getString("kitAlreadyExists").replace("%kit%",kitName),true);
            return;
        }

        ItemStack[] inventoryContents = PlayerUtils.getAllInventoryContents(player);

        KitItemManager kitItemManager = plugin.getKitItemManager();
        ArrayList<KitItem> items = new ArrayList<>();
        boolean hasArmor = false;
        for(int i=0;i<inventoryContents.length;i++){
            ItemStack item = inventoryContents[i];
            if(item == null || item.getType().equals(Material.AIR)){
                continue;
            }

            KitItem kitItem = kitItemManager.createKitItemFromItemStack(item,saveOriginalItems);

            //Check for armor/offhand
            if(i >= 36 && i<=39){
                hasArmor = true;
            }
            if(i == 40){
                kitItem.setOffhand(true);
            }

            items.add(kitItem);
        }

        if(items.size() == 0){
            msgManager.sendMessage(player,messagesFile.getString("inventoryEmpty"),true);
            return;
        }

        //Set defaults
        kit = new Kit(kitName);
        kit.setItems(items);
        kit.setDefaults(mainConfigManager.getNewKitDefault());
        kit.setAutoArmor(hasArmor);
        kit.setSaveOriginalItems(saveOriginalItems);

        kits.add(kit);
        plugin.getConfigsManager().getKitsConfigManager().saveConfig(kit);

        msgManager.sendMessage(player,messagesFile.getString("kitCreated").replace("%kit%",kitName),true);

        //Add on inventory
        InventoryManager inventoryManager = plugin.getInventoryManager();
        String newKitDefaultInventory = mainConfigManager.getNewKitDefaultInventory();
        KitInventory inventory = inventoryManager.getInventory(newKitDefaultInventory);
        if(inventory != null){
            int resultSlot = inventory.addKitItemOnFirstEmptySlot(kitName);
            if(resultSlot == -1){
                msgManager.sendMessage(player,messagesFile.getString("kitNotAddedToInventory"),true);
            }else{
                msgManager.sendMessage(player,messagesFile.getString("kitAddedToInventory")
                        .replace("%inventory%",newKitDefaultInventory).replace("%slot%",resultSlot+""),true);
                //Update inventory file
                plugin.getConfigsManager().getInventoryConfigManager().saveKitItemOnConfig(newKitDefaultInventory,resultSlot,kitName);
            }
        }
    }

    public void deleteKit(String kitName,CommandSender sender){
        FileConfiguration messagesFile = plugin.getConfigsManager().getMessagesConfigManager().getConfig();
        MessagesManager msgManager = plugin.getMessagesManager();
        if(getKitByName(kitName) == null){
            msgManager.sendMessage(sender,messagesFile.getString("kitDoesNotExists")
                    .replace("%kit%",kitName),true);
            return;
        }

        removeKit(kitName);
        plugin.getConfigsManager().getKitsConfigManager().removeKitFile(kitName);
        plugin.getInventoryManager().removeKitFromInventory(kitName);
        plugin.getConfigsManager().getInventoryConfigManager().save();

        msgManager.sendMessage(sender,messagesFile.getString("kitDeleted").replace("%kit%",kitName),true);
    }

    public PlayerKitsMessageResult giveKit(Player player, String kitName, GiveKitInstructions giveKitInstructions){
        Kit kit = getKitByName(kitName);
        FileConfiguration messagesFile = plugin.getConfigsManager().getMessagesConfigManager().getConfig();
        FileConfiguration configFile = plugin.getConfigsManager().getMainConfigManager().getConfig();
        PlayerDataManager playerDataManager = plugin.getPlayerDataManager();
        MessagesManager msgManager = plugin.getMessagesManager();

        if(kit == null){
            return PlayerKitsMessageResult.error(messagesFile.getString("kitDoesNotExists").replace("%kit%",kitName));
        }

        //Check properties
        if(!giveKitInstructions.isFromCommand()){
            //Permission
            if(!giveKitInstructions.isIgnorePermission() && !kit.playerHasPermission(player)){
                sendKitActions(kit.getErrorActions(),player,false);
                return PlayerKitsMessageResult.error(messagesFile.getString("kitNoPermissions"));
            }

            //One time
            if(kit.isOneTime() && !PlayerUtils.isPlayerKitsAdmin(player) && !PlayerUtils.hasOneTimeBypassPermission(player)
                    && playerDataManager.isKitOneTime(player,kit.getName())){
                sendKitActions(kit.getErrorActions(),player,false);
                return PlayerKitsMessageResult.error(messagesFile.getString("oneTimeError"));
            }

            //Cooldown
            long playerCooldown = playerDataManager.getKitCooldown(player,kit.getName());
            if(kit.getCooldown() != 0 && !PlayerUtils.isPlayerKitsAdmin(player) && !PlayerUtils.hasCooldownBypassPermission(player)){
                long currentMillis = System.currentTimeMillis();
                long millisDif = playerCooldown-currentMillis;
                String timeStringMillisDif = OtherUtils.getTime(millisDif/1000, msgManager);
                if(!timeStringMillisDif.isEmpty()) {
                    sendKitActions(kit.getErrorActions(),player,false);
                    return PlayerKitsMessageResult.error(messagesFile.getString("cooldownError")
                            .replace("%time%",timeStringMillisDif));
                }
            }

            //Requirements - Buy
            KitRequirements kitRequirements = kit.getRequirements();
            if(!giveKitInstructions.isIgnoreRequirements() && kitRequirements != null &&
                    (kitRequirements.getPrice() != 0 || !kitRequirements.getExtraRequirements().isEmpty())){
                if(!(kitRequirements.isOneTimeRequirements() && playerDataManager.isKitBought(player,kit.getName()))){
                    if(!giveKitInstructions.isRequirementsSatisfied()){
                        //Player must buy it first
                        PlayerKitsMessageResult result = PlayerKitsMessageResult.success();
                        result.setProceedToBuy(true);
                        return result;
                    }

                    //Check price
                    if(!passPrice(kitRequirements.getPrice(),player)){
                        sendKitActions(kit.getErrorActions(),player,false);
                        return PlayerKitsMessageResult.error(messagesFile.getString("requirementsError"));
                    }
                    //Check requirements
                    List<String> requirementsConditions = kitRequirements.getExtraRequirements();
                    if(plugin.getDependencyManager().isPlaceholderAPI()) {
                        for(String condition : requirementsConditions) {
                            boolean passCondition = PlayerUtils.passCondition(player, condition);
                            if(!passCondition) {
                                sendKitActions(kit.getErrorActions(),player,false);
                                return PlayerKitsMessageResult.error(messagesFile.getString("requirementsError"));
                            }
                        }
                    }
                }
            }
        }


        KitItemManager kitItemManager = plugin.getKitItemManager();
        ArrayList<KitItem> items = kit.getItems();

        //Check amount of free slots, including auto-armor
        int usedSlots = PlayerUtils.getUsedSlots(player); //storage contents, 36 slots
        int freeSlots = 36-usedSlots;
        int inventoryKitItems = 0; //Items that will be put in the player inventory (not equipment)

        KitItem itemHelmet = null;
        KitItem itemChestplate = null;
        KitItem itemLeggings = null;
        KitItem itemBoots = null;
        KitItem itemOffhand = null;

        boolean clearInventory = kit.isClearInventory();

        PlayerInventory playerInventory = player.getInventory();
        for(KitItem item : items){
            if(kit.isAutoArmor()){
                String id = item.getId();
                if(item.getOriginalItem() != null){
                    id = item.getOriginalItem().getType().name();
                }

                //Check if the item must be put in the player equipment
                if((id.contains("_HELMET") || id.contains("PLAYER_HEAD") || id.contains("SKULL_ITEM")) && itemHelmet == null){
                    if(playerInventory.getHelmet() == null || playerInventory.getHelmet().getType().equals(Material.AIR) || clearInventory){
                        itemHelmet = item;
                        freeSlots++;
                        continue;
                    }
                }else if((id.contains("_CHESTPLATE") || id.contains("ELYTRA")) && itemChestplate == null){
                    if(playerInventory.getChestplate() == null || playerInventory.getChestplate().getType().equals(Material.AIR) || clearInventory){
                        itemChestplate = item;
                        freeSlots++;
                        continue;
                    }
                }else if(id.contains("_LEGGINGS") && itemLeggings == null){
                    if(playerInventory.getLeggings() == null || playerInventory.getLeggings().getType().equals(Material.AIR) || clearInventory){
                        itemLeggings = item;
                        freeSlots++;
                        continue;
                    }
                }else if(id.contains("_BOOTS") && itemBoots == null){
                    if(playerInventory.getBoots() == null || playerInventory.getBoots().getType().equals(Material.AIR) || clearInventory){
                        itemBoots = item;
                        freeSlots++;
                        continue;
                    }
                }
            }

            if(item.isOffhand() && itemOffhand == null){
                if(playerInventory.getItemInOffHand() == null || playerInventory.getItemInOffHand().getType().equals(Material.AIR) || clearInventory){
                    itemOffhand = item;
                    freeSlots++;
                    continue;
                }
            }

            inventoryKitItems++;
        }
        ArrayList<KitAction> claimActions = kit.getClaimActions();
        for(KitAction action : claimActions){
            if(action.isCountAsItem()){
                inventoryKitItems++;
            }
        }

        boolean enoughSpace = freeSlots < inventoryKitItems;
        boolean dropItemsIfFullInventory = configFile.getBoolean("drop_items_if_full_inventory");

        if(enoughSpace && !dropItemsIfFullInventory && !clearInventory){
            sendKitActions(kit.getErrorActions(),player,false);
            return PlayerKitsMessageResult.error(messagesFile.getString("noSpaceError"));
        }

        if(clearInventory){
            player.getInventory().clear();
        }

        //Actions before
        sendKitActions(kit.getClaimActions(),player,true);

        //Give kit items
        for(KitItem kitItem : items){
            ItemStack item = kitItemManager.createItemFromKitItem(kitItem,player,kit);

            if(itemHelmet != null && kitItem.equals(itemHelmet)){
                playerInventory.setHelmet(item);
            }else if(itemChestplate != null && kitItem.equals(itemChestplate)){
                playerInventory.setChestplate(item);
            }else if(itemLeggings != null && kitItem.equals(itemLeggings)){
                playerInventory.setLeggings(item);
            }else if(itemBoots != null && kitItem.equals(itemBoots)){
                playerInventory.setBoots(item);
            }else if(itemOffhand != null && kitItem.equals(itemOffhand)){
                playerInventory.setItemInOffHand(item);
            }else{
                if(playerInventory.firstEmpty() == -1 && dropItemsIfFullInventory){
                    player.getWorld().dropItemNaturally(player.getLocation(), item);
                }else{
                    playerInventory.addItem(item);
                }
            }
        }

        //Actions after
        sendKitActions(kit.getClaimActions(),player,false);

        //Update properties
        if(!giveKitInstructions.isFromCommand()){
            //One time
            if(kit.isOneTime() && !PlayerUtils.isPlayerKitsAdmin(player) && !PlayerUtils.hasOneTimeBypassPermission(player)){
                playerDataManager.setKitOneTime(player,kit.getName());
            }

            //Cooldown
            if(kit.getCooldown() != 0 && !PlayerUtils.isPlayerKitsAdmin(player) && !PlayerUtils.hasCooldownBypassPermission(player)){
                long millisMax = System.currentTimeMillis()+(kit.getCooldown()* 1000L);
                playerDataManager.setKitCooldown(player,kit.getName(),millisMax);
            }

            //Requirements - Buy
            KitRequirements kitRequirements = kit.getRequirements();
            if(!giveKitInstructions.isIgnoreRequirements() && kitRequirements != null && giveKitInstructions.isRequirementsSatisfied()){
                //Check price and update balance
                double price = kitRequirements.getPrice();
                Economy economy = plugin.getDependencyManager().getVaultEconomy();
                if(price > 0 && economy != null){
                    economy.withdrawPlayer(player,price);
                }

                //Actions
                List<String> actions = kitRequirements.getActionsOnBuy();
                for(String action : actions){
                    executeAction(player,action);
                }

                //Data
                if(kitRequirements.isOneTimeRequirements()){
                    playerDataManager.setKitBought(player,kitName);
                }
            }
        }

        return PlayerKitsMessageResult.success();
    }

    public void giveFirstJoinKit(Player player){
        // Will ignore:
        // - requirements
        // - permissions
        String firstJoinKit = plugin.getConfigsManager().getMainConfigManager().getFirstJoinKit();
        if(firstJoinKit.equals("none")){
            return;
        }
        giveKit(player,firstJoinKit,new GiveKitInstructions(false,false,true,true));
    }

    public void executeAction(Player player,String actionText){
        if(actionText.equals("close_inventory")){
            ActionUtils.closeInventory(player);
            return;
        }
        int indexFirst = actionText.indexOf(" ");
        String actionType = actionText.substring(0,indexFirst).replace(":","");
        String actionLine = actionText.substring(indexFirst+1);
        actionLine = OtherUtils.replaceGlobalVariables(actionLine,player,plugin);

        switch(actionType){
            case "message":
                ActionUtils.message(player,actionLine);
                break;
            case "console_command":
                ActionUtils.consoleCommand(plugin, actionLine);
                break;
            case "player_command":
                ActionUtils.playerCommand(plugin, player,actionLine);
                break;
            case "playsound":
                ActionUtils.playSound(player,actionLine);
                break;
            case "actionbar":
                ActionUtils.actionbar(player,actionLine,plugin);
                break;
            case "title":
                ActionUtils.title(player,actionLine);
                break;
            case "firework":
                ActionUtils.firework(player,actionLine,plugin);
                break;
        }
    }

    public void sendKitActions(ArrayList<KitAction> actions,Player player,boolean beforeItems){
        for(KitAction action : actions){
            if(action.isExecuteBeforeItems() == beforeItems){
                String actionText = action.getAction();
                executeAction(player,actionText);
            }
        }
    }

    public boolean passPrice(double price,Player player){
        if(price != 0){
            Economy economy = plugin.getDependencyManager().getVaultEconomy();
            if(economy != null){
                if(economy.getBalance(player) < price){
                    return false;
                }
            }
        }
        return true;
    }

}
