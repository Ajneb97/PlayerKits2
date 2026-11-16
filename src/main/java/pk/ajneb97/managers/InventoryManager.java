package pk.ajneb97.managers;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import pk.ajneb97.PlayerKits2;
import pk.ajneb97.configs.MainConfigManager;
import pk.ajneb97.model.Kit;
import pk.ajneb97.model.KitAction;
import pk.ajneb97.model.internal.GiveKitInstructions;
import pk.ajneb97.model.internal.KitPosition;
import pk.ajneb97.model.internal.KitVariable;
import pk.ajneb97.model.internal.PlayerKitsMessageResult;
import pk.ajneb97.model.inventory.InventoryPlayer;
import pk.ajneb97.model.inventory.ItemKitInventory;
import pk.ajneb97.model.inventory.KitInventory;
import pk.ajneb97.model.item.KitItem;
import pk.ajneb97.utils.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class InventoryManager {

    private static final String PREVIEW_INVENTORY = "preview_inventory";
    private static final String REQUIREMENTS_INVENTORY = "buy_requirements_inventory";
    private static final String PREVIEW_CLAIM_TYPE = "preview_claim";
    private static final String PREVIEW_CLAIM_TAG = "playerkits_preview_claim";

    private PlayerKits2 plugin;
    private ArrayList<KitInventory> inventories;
    private ArrayList<InventoryPlayer> players;
    private InventoryRequirementsManager inventoryRequirementsManager;
    public InventoryManager(PlayerKits2 plugin){
        this.plugin = plugin;
        this.players = new ArrayList<>();
        this.inventoryRequirementsManager = new InventoryRequirementsManager(plugin,this);
    }

    public ArrayList<InventoryPlayer> getPlayers() {
        return players;
    }

    public ArrayList<KitInventory> getInventories() {
        return inventories;
    }

    public InventoryRequirementsManager getInventoryRequirementsManager() {
        return inventoryRequirementsManager;
    }

    public void setInventories(ArrayList<KitInventory> inventories) {
        this.inventories = inventories;
    }

    public KitInventory getInventory(String name){
        for(KitInventory kitInventory : inventories){
            if(kitInventory.getName().equals(name)){
                return kitInventory;
            }
        }
        return null;
    }

    public InventoryPlayer getInventoryPlayer(Player player){
        for(InventoryPlayer inventoryPlayer : players){
            if(inventoryPlayer.getPlayer().equals(player)){
                return inventoryPlayer;
            }
        }
        return null;
    }

    public void removeInventoryPlayer(Player player){
        for(int i=0;i<players.size();i++){
            if(players.get(i).getPlayer().equals(player)){
                players.remove(i);
            }
        }
    }

    public void openInventory(InventoryPlayer inventoryPlayer){
        String inventoryName = inventoryPlayer.getInventoryName();
        String kitName = inventoryPlayer.getKitName();
        boolean preview = PREVIEW_INVENTORY.equals(inventoryName) && kitName != null;
        boolean requirements = REQUIREMENTS_INVENTORY.equals(inventoryName);
        KitInventory kitInventory = getInventory(inventoryName);
        KitsManager kitsManager = plugin.getKitsManager();
        if (preview) {
            Kit kit = kitsManager.getKitByName(kitName);
            KitInventory layout = kit == null ? null : getInventory(kit.getPreviewInventory());
            if (layout != null && !REQUIREMENTS_INVENTORY.equals(layout.getName())
                    && layout.getTitle() != null && layout.getSlots() >= 9
                    && layout.getSlots() <= 54 && layout.getSlots() % 9 == 0) {
                kitInventory = layout;
            }
        }
        MainConfigManager mainConfigManager = plugin.getConfigsManager().getMainConfigManager();

        String title = kitInventory.getTitle();
        if (kitName != null && (preview || requirements)) {
            title = title.replace("%kit%", kitName);
        }
        Inventory inv;
        if(mainConfigManager.isUseMiniMessage()){
            inv = MiniMessageUtils.createInventory(kitInventory.getSlots(),title);
        }else{
            inv = Bukkit.createInventory(null,kitInventory.getSlots(), MessagesManager.getLegacyColoredMessage(title));
        }

        List<ItemKitInventory> items = kitInventory.getItems();
        KitItemManager kitItemManager = plugin.getKitItemManager();
        PlayerDataManager playerDataManager = plugin.getPlayerDataManager();

        //Add items for all inventories
        for(ItemKitInventory itemInventory : items){
            String type = itemInventory.getType();
            boolean previewClaim = PREVIEW_CLAIM_TYPE.equals(type);
            if (previewClaim && (!preview || itemInventory.getItem() == null)) {
                continue;
            }
            for(int slot : itemInventory.getSlots()){
                if (preview && (slot < 0 || slot >= inv.getSize())) {
                    continue;
                }
                if(type != null && type.startsWith("kit: ")){
                    setKit(type.replace("kit: ",""),inventoryPlayer.getPlayer(),inv,slot,kitsManager
                            ,playerDataManager,kitItemManager,null);
                    continue;
                }

                ItemStack item = kitItemManager.createItemFromKitItem(itemInventory.getItem(),inventoryPlayer.getPlayer(),null);

                if (previewClaim) {
                    inv.setItem(slot, ItemUtils.setTagStringItem(plugin, item, PREVIEW_CLAIM_TAG, kitName));
                    continue;
                }

                if(requirements){
                    inventoryRequirementsManager.configureRequirementsItem(item,kitName,inventoryPlayer.getPlayer());
                    if(type != null){
                        item = ItemUtils.setTagStringItem(plugin,item, "playerkits_buy", type);
                    }
                }

                String openInventory = itemInventory.getOpenInventory();
                if(openInventory != null) {
                    item = ItemUtils.setTagStringItem(plugin,item, "playerkits_open_inventory", openInventory);
                }
                item = setItemActions(itemInventory,item);

                inv.setItem(slot,item);
            }
        }

        //Special items for some inventories
        if(preview){
            setKitPreviewItems(inv,inventoryPlayer,kitInventory);
        }


        inventoryPlayer.getPlayer().openInventory(inv);
        players.add(inventoryPlayer);
    }

    private ItemStack setItemActions(ItemKitInventory itemInventory, ItemStack item) {
        List<String> clickActions = itemInventory.getClickActions();
        if(clickActions != null && !clickActions.isEmpty()) {
            String actionsList = "";
            for(int i=0;i<clickActions.size();i++) {
                if(i==clickActions.size()-1) {
                    actionsList=actionsList+clickActions.get(i);
                }else {
                    actionsList=actionsList+clickActions.get(i)+"|";
                }
            }
            item = ItemUtils.setTagStringItem(plugin, item, "playerkits_item_actions", actionsList);
        }
        return item;
    }

    public void setKitPreviewItems(Inventory inv,InventoryPlayer inventoryPlayer,KitInventory kitInventory){
        KitItemManager kitItemManager = plugin.getKitItemManager();
        KitsManager kitsManager = plugin.getKitsManager();

        Kit kit = kitsManager.getKitByName(inventoryPlayer.getKitName());
        if(kit == null){
            return;
        }

        // Create a list with all items including actions display items
        ArrayList<KitItem> allItems = new ArrayList<>();
        allItems.addAll(kit.getItems());
        for(KitAction kitAction : kit.getClaimActions()){
            KitItem kitItem = kitAction.getDisplayItem();
            if(kitItem != null){
                allItems.add(kitItem);
            }
        }

        boolean[] reservedSlots = new boolean[inv.getSize()];
        for (ItemKitInventory entry : kitInventory.getItems()) {
            List<String> actions = entry.getClickActions();
            if (entry.getType() == null && entry.getOpenInventory() == null
                    && (actions == null || actions.isEmpty())) {
                continue;
            }
            for (int slot : entry.getSlots()) {
                if (slot >= 0 && slot < reservedSlots.length) {
                    reservedSlots[slot] = true;
                }
            }
        }

        int[] preferredSlots = allItems.stream().mapToInt(KitItem::getPreviewSlot).toArray();
        int[] assignedSlots = PreviewLayout.assignSlots(preferredSlots, reservedSlots);
        for (int index = 0; index < assignedSlots.length; index++) {
            int slot = assignedSlots[index];
            if (slot != -1) {
                ItemStack item = kitItemManager.createItemFromKitItem(allItems.get(index), inventoryPlayer.getPlayer(), kit);
                inv.setItem(slot, item);
            }
        }
    }

    public void clickInventory(InventoryPlayer inventoryPlayer, ItemStack item, ClickType clickType){
        if (PREVIEW_INVENTORY.equals(inventoryPlayer.getInventoryName())) {
            String previewKit = ItemUtils.getTagStringItem(plugin, item, PREVIEW_CLAIM_TAG);
            if (previewKit != null && previewKit.equals(inventoryPlayer.getKitName())) {
                if (clickType == ClickType.LEFT || clickType == ClickType.RIGHT) {
                    clickOnKitItem(inventoryPlayer, previewKit, ClickType.LEFT);
                }
                return;
            }
        }

        String kitName = ItemUtils.getTagStringItem(plugin,item,"playerkits_kit");
        if(kitName != null){
            clickOnKitItem(inventoryPlayer,kitName,clickType);
            return;
        }

        String itemActions = ItemUtils.getTagStringItem(plugin,item,"playerkits_item_actions");
        if(itemActions != null){
            clickOnActionItem(inventoryPlayer,itemActions);
        }

        String openInventory = ItemUtils.getTagStringItem(plugin,item,"playerkits_open_inventory");
        if(openInventory != null){
            clickOnOpenInventoryItem(inventoryPlayer,openInventory);
            return;
        }

        //Requirements inventory
        if(REQUIREMENTS_INVENTORY.equals(inventoryPlayer.getInventoryName())){
            String buyTag = ItemUtils.getTagStringItem(plugin,item,"playerkits_buy");
            if(buyTag == null){
                return;
            }
            if(buyTag.equals("yes") || buyTag.equals("buy_yes")){
                inventoryRequirementsManager.requirementsInventoryBuy(inventoryPlayer);
            }else{
                inventoryRequirementsManager.requirementsInventoryCancel(inventoryPlayer);
            }
        }
    }

    public void clickOnKitItem(InventoryPlayer inventoryPlayer,String kitName,ClickType clickType){
        MainConfigManager mainConfigManager = plugin.getConfigsManager().getMainConfigManager();
        Player player = inventoryPlayer.getPlayer();
        FileConfiguration messagesConfig = plugin.getConfigsManager().getMessagesConfigManager().getConfig();
        MessagesManager msgManager = plugin.getMessagesManager();

        if(clickType.equals(ClickType.RIGHT)){
            //Preview
            if(!mainConfigManager.isKitPreview()){
                return;
            }

            Kit kit = plugin.getKitsManager().getKitByName(kitName);
            if(kit.isPermissionRequired()){
                if(mainConfigManager.isKitPreviewRequiresKitPermission() && !kit.playerHasPermission(player)){
                    msgManager.sendMessage(player,messagesConfig.getString("cantPreviewError"),true);
                    return;
                }
            }
            if (!PREVIEW_INVENTORY.equals(inventoryPlayer.getInventoryName())) {
                inventoryPlayer.setPreviousInventoryName(inventoryPlayer.getInventoryName());
            }
            inventoryPlayer.setInventoryName(PREVIEW_INVENTORY);
            inventoryPlayer.setKitName(kitName);
            openInventory(inventoryPlayer);
            return;
        }


        PlayerKitsMessageResult result = plugin.getKitsManager().giveKit(player,kitName,
                new GiveKitInstructions());
        if(result.isError()){
            msgManager.sendMessage(player,result.getMessage(),true);
            return;
        }else{
            if(result.isProceedToBuy()){
                // A purchase started in a preview returns to the menu that opened it.
                if (!PREVIEW_INVENTORY.equals(inventoryPlayer.getInventoryName())) {
                    inventoryPlayer.setPreviousInventoryName(inventoryPlayer.getInventoryName());
                }
                inventoryPlayer.setInventoryName(REQUIREMENTS_INVENTORY);
                inventoryPlayer.setKitName(kitName);
                openInventory(inventoryPlayer);
                return;
            }
            msgManager.sendMessage(player,messagesConfig.getString("kitReceived").replace("%kit%",kitName),true);
        }

        if(mainConfigManager.isCloseInventoryOnClaim()){
            player.closeInventory();
            return;
        }

        openInventory(inventoryPlayer);
    }

    public void clickOnOpenInventoryItem(InventoryPlayer inventoryPlayer,String openInventory){
        if(openInventory.equals("previous")){
            String previous = inventoryPlayer.getPreviousInventoryName();
            inventoryPlayer.setInventoryName(previous == null ? "main_inventory" : previous);
        }else{
            inventoryPlayer.setPreviousInventoryName(inventoryPlayer.getInventoryName());
            inventoryPlayer.setInventoryName(openInventory);
        }
        openInventory(inventoryPlayer);
    }

    public void clickOnActionItem(InventoryPlayer inventoryPlayer,String itemActions) {
        String[] sep = itemActions.split("\\|");

        KitsManager kitsManager = plugin.getKitsManager();
        Player player = inventoryPlayer.getPlayer();
        for(String action : sep){
            kitsManager.executeAction(player,action);
        }
    }

    public void setKit(String kitName, Player player, Inventory inv, int slot, KitsManager kitsManager, PlayerDataManager playerDataManager
        ,KitItemManager kitItemManager,ItemStack currentItem){
        Kit kit = kitsManager.getKitByName(kitName);
        if(kit == null){
            return;
        }

        // Check status before
        String currentStatus = "default";
        if(currentItem != null){
            currentStatus = ItemUtils.getTagStringItem(plugin,currentItem,"playerkits_kit_status");
            if(currentStatus == null){
                currentStatus = "default";
            }
        }
        String newStatus = "default";

        ArrayList<KitVariable> variablesToReplace = new ArrayList<>();
        variablesToReplace.add(new KitVariable("%kit_name%",kit.getName()));

        KitItem kitItem = null;
        if(!kit.playerHasPermission(player)){
            kitItem = kit.getDisplayItemNoPermission();
            newStatus = "no_permission";
        }else{
            //One time
            if(kit.isOneTime() && !PlayerUtils.isPlayerKitsAdmin(player) && playerDataManager.isKitOneTime(player,kit.getName())){
                kitItem = kit.getDisplayItemOneTime();
                newStatus = "one_time";
            }

            //One time requirements
            if(kit.getRequirements() != null && kit.getRequirements().isOneTimeRequirements()
                && playerDataManager.isKitBought(player,kit.getName())){
                kitItem = kit.getDisplayItemOneTimeRequirements();
                newStatus = "one_time_requirements";
            }

            //Cooldown
            long playerCooldown = playerDataManager.getKitCooldown(player,kit.getName());
            if(kit.getCooldown() != 0 && !PlayerUtils.isPlayerKitsAdmin(player)){
                String timeStringMillisDif = playerDataManager.getKitCooldownString(playerCooldown);
                if(!timeStringMillisDif.isEmpty()) {
                    kitItem = kit.getDisplayItemCooldown();
                    variablesToReplace.add(new KitVariable("%time%",timeStringMillisDif));
                    newStatus = "cooldown";
                }
            }
        }
        if(kitItem == null){
            kitItem = kit.getDisplayItemDefault();
            newStatus = "default";
        }

        kitItem = kitItem.clone();

        boolean useMiniMessage = plugin.getConfigsManager().getMainConfigManager().isUseMiniMessage();
        if(newStatus.equals(currentStatus) && currentItem != null){
            // Name and Lore update
            kitItemManager.replaceVariables(kitItem,variablesToReplace,player);

            ItemMeta meta = currentItem.getItemMeta();

            String name = kitItem.getName();
            if(name != null){
                if(useMiniMessage){
                    MiniMessageUtils.setItemName(meta,name);
                }else{
                    meta.setDisplayName(MessagesManager.getLegacyColoredMessage(name));
                }
            }

            List<String> lore = kitItem.getLore();
            if(lore != null) {
                List<String> loreCopy = new ArrayList<>(lore);
                if(useMiniMessage){
                    MiniMessageUtils.setItemLore(meta,lore,player,plugin);
                }else{
                    for(int i=0;i<loreCopy.size();i++) {
                        String line = OtherUtils.replaceGlobalVariables(loreCopy.get(i),player,plugin);
                        loreCopy.set(i, MessagesManager.getLegacyColoredMessage(line));
                    }
                    meta.setLore(loreCopy);
                }
            }

            currentItem.setItemMeta(meta);
        }else{
            // Full update
            kitItemManager.replaceVariables(kitItem,variablesToReplace,player);
            ItemStack item = kitItemManager.createItemFromKitItem(kitItem,player,kit);
            item = ItemUtils.setTagStringItem(plugin,item, "playerkits_kit", kitName);
            item = ItemUtils.setTagStringItem(plugin,item, "playerkits_kit_status", newStatus);
            inv.setItem(slot,item);
        }
    }



    public KitPosition getKitPositionByKitName(String kitName){
        for(KitInventory kitInventory : inventories){
            for(ItemKitInventory itemKitInventory : kitInventory.getItems()){
                if(itemKitInventory.getType() != null && itemKitInventory.getType().equals("kit: "+kitName)){
                    return new KitPosition(itemKitInventory.getSlots().get(0), kitInventory.getName());
                }
            }
        }
        return null;
    }

    public void removeKitFromInventory(String kitName){
        for(KitInventory inventory : inventories){
            List<ItemKitInventory> items = inventory.getItems();
            for(int i=0;i<items.size();i++){
                ItemKitInventory item = items.get(i);
                if(item.getType() != null && item.getType().equals("kit: "+kitName)){
                    items.remove(i);
                    i--;
                }
            }
        }
    }
}
