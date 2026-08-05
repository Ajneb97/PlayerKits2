package pk.ajneb97.managers;

import org.bukkit.Material;
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
import pk.ajneb97.model.inventory.KitGuiHolder;
import pk.ajneb97.model.inventory.KitInventory;
import pk.ajneb97.model.item.KitItem;
import pk.ajneb97.utils.InventoryUtils;
import pk.ajneb97.utils.ItemUtils;
import pk.ajneb97.utils.MiniMessageUtils;
import pk.ajneb97.utils.OtherUtils;
import pk.ajneb97.utils.PlayerUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class InventoryManager {

    public static final String TAG_KIT = "playerkits_kit";
    public static final String TAG_KIT_STATUS = "playerkits_kit_status";
    public static final String TAG_OPEN_INVENTORY = "playerkits_open_inventory";
    public static final String TAG_ITEM_ACTIONS = "playerkits_item_actions";
    public static final String TAG_BUY = "playerkits_buy";
    public static final String TAG_LAYOUT_EDITOR = "playerkits_layout_editor";

    private static final String TYPE_LAYOUT_EDITOR = "layout_editor";
    private static final String KIT_TYPE_PREFIX = "kit: ";
    private static final long CLAIM_DEBOUNCE_MILLIS = 200L;

    private final PlayerKits2 plugin;
    private Map<String, KitInventory> inventories;
    private final Map<UUID, InventoryPlayer> players;
    private final Set<UUID> pendingClaims;
    private final Map<UUID, Long> lastClaimMillis;
    private final InventoryRequirementsManager inventoryRequirementsManager;

    public InventoryManager(PlayerKits2 plugin) {
        this.plugin = plugin;
        this.inventories = new ConcurrentHashMap<>();
        this.players = new ConcurrentHashMap<>();
        this.pendingClaims = ConcurrentHashMap.newKeySet();
        this.lastClaimMillis = new ConcurrentHashMap<>();
        this.inventoryRequirementsManager = new InventoryRequirementsManager(plugin, this);
    }

    public Collection<InventoryPlayer> getPlayers() {
        return players.values();
    }

    public ArrayList<KitInventory> getInventories() {
        return new ArrayList<>(inventories.values());
    }

    public InventoryRequirementsManager getInventoryRequirementsManager() {
        return inventoryRequirementsManager;
    }

    public void setInventories(ArrayList<KitInventory> newInventories) {
        Map<String, KitInventory> replacement = new ConcurrentHashMap<>();
        for (KitInventory inventory : newInventories) {
            if (inventory.getName() != null) {
                replacement.put(inventory.getName(), inventory);
            }
        }
        this.inventories = replacement;
    }

    public KitInventory getInventory(String name) {
        return name == null ? null : inventories.get(name);
    }

    public InventoryPlayer getInventoryPlayer(Player player) {
        return players.get(player.getUniqueId());
    }

    public void removeInventoryPlayer(Player player) {
        InventoryPlayer removed = players.remove(player.getUniqueId());
        if (removed != null) {
            removed.cancelUpdateTask();
        }
    }

    public void closeAll() {
        for (InventoryPlayer inventoryPlayer : new ArrayList<>(players.values())) {
            inventoryPlayer.cancelUpdateTask();
        }
        players.clear();
    }

    public void openInventory(InventoryPlayer inventoryPlayer) {
        Player player = inventoryPlayer.getPlayer();
        if (player == null || !player.isOnline()) {
            return;
        }

        KitInventory kitInventory = getInventory(inventoryPlayer.getInventoryName());
        if (kitInventory == null) {
            plugin.getMessagesManager().sendMessage(player,
                    plugin.getConfigsManager().getMessagesConfigManager().getConfig().getString("inventoryNotExists"), true);
            return;
        }

        MainConfigManager mainConfigManager = plugin.getConfigsManager().getMainConfigManager();

        String title = kitInventory.getTitle();
        String inventoryName = inventoryPlayer.getInventoryName();
        if (inventoryName.equals("buy_requirements_inventory") || inventoryName.equals("preview_inventory")) {
            title = title.replace("%kit%", inventoryPlayer.getKitName() == null ? "" : inventoryPlayer.getKitName());
        }

        KitGuiHolder holder = new KitGuiHolder(KitGuiHolder.Type.KITS, player.getUniqueId());
        Inventory inv = InventoryUtils.createInventory(holder, kitInventory.getSlots(), title,
                mainConfigManager.isUseMiniMessage());

        List<ItemKitInventory> items = kitInventory.getItems();
        KitItemManager kitItemManager = plugin.getKitItemManager();
        KitsManager kitsManager = plugin.getKitsManager();
        PlayerDataManager playerDataManager = plugin.getPlayerDataManager();
        int maxSlot = kitInventory.getSlots();

        for (ItemKitInventory itemInventory : items) {
            String type = itemInventory.getType();
            for (int slot : itemInventory.getSlots()) {
                if (slot < 0 || slot >= maxSlot) {
                    continue;
                }

                if (type != null && type.startsWith(KIT_TYPE_PREFIX)) {
                    setKit(type.substring(KIT_TYPE_PREFIX.length()), player, inv, slot, kitsManager,
                            playerDataManager, kitItemManager, null);
                    continue;
                }

                ItemStack item = kitItemManager.createItemFromKitItem(itemInventory.getItem(), player, null);
                if (item == null) {
                    continue;
                }

                if (inventoryName.equals("buy_requirements_inventory")) {
                    inventoryRequirementsManager.configureRequirementsItem(item, inventoryPlayer.getKitName(), player);
                    if (type != null) {
                        item = ItemUtils.setTagStringItem(plugin, item, TAG_BUY, type);
                    }
                }

                if (type != null && type.equals(TYPE_LAYOUT_EDITOR)) {
                    if (!mainConfigManager.isLayoutEditor()) {
                        continue;
                    }
                    item = ItemUtils.setTagStringItem(plugin, item, TAG_LAYOUT_EDITOR, "yes");
                }

                String openInventory = itemInventory.getOpenInventory();
                if (openInventory != null) {
                    item = ItemUtils.setTagStringItem(plugin, item, TAG_OPEN_INVENTORY, openInventory);
                }
                item = setItemActions(itemInventory, item);

                inv.setItem(slot, item);
            }
        }

        if (inventoryName.equals("preview_inventory")) {
            setKitPreviewItems(inv, inventoryPlayer, kitInventory);
        }

        inventoryPlayer.setHolder(holder);
        player.openInventory(inv);
        players.put(player.getUniqueId(), inventoryPlayer);
        startUpdateTask(inventoryPlayer);
    }

    public void openInventoryLater(InventoryPlayer inventoryPlayer) {
        Player player = inventoryPlayer.getPlayer();
        plugin.getPlatformScheduler().runForEntityLater(player, () -> {
            if (!player.isOnline()) {
                return;
            }
            if (players.get(player.getUniqueId()) != inventoryPlayer) {
                return;
            }
            openInventory(inventoryPlayer);
        }, 1L);
    }

    public void registerLayoutSession(InventoryPlayer inventoryPlayer) {
        Player player = inventoryPlayer.getPlayer();
        inventoryPlayer.cancelUpdateTask();
        players.put(player.getUniqueId(), inventoryPlayer);
    }

    private void startUpdateTask(InventoryPlayer inventoryPlayer) {
        Player player = inventoryPlayer.getPlayer();
        inventoryPlayer.setUpdateTask(plugin.getPlatformScheduler().runForEntityTimer(player,
                () -> updateOpenInventory(inventoryPlayer), 20L, 20L));
    }

    private void updateOpenInventory(InventoryPlayer inventoryPlayer) {
        Player player = inventoryPlayer.getPlayer();
        if (player == null || !player.isOnline()) {
            inventoryPlayer.cancelUpdateTask();
            return;
        }
        if (players.get(player.getUniqueId()) != inventoryPlayer) {
            inventoryPlayer.cancelUpdateTask();
            return;
        }

        Inventory inv = InventoryUtils.getTopInventory(player);
        KitGuiHolder holder = InventoryUtils.getKitGuiHolder(inv);
        if (holder == null || holder != inventoryPlayer.getHolder()) {
            return;
        }

        KitsManager kitsManager = plugin.getKitsManager();
        PlayerDataManager playerDataManager = plugin.getPlayerDataManager();
        KitItemManager kitItemManager = plugin.getKitItemManager();

        ItemStack[] contents = inv.getContents();
        for (int i = 0; i < contents.length; i++) {
            ItemStack item = contents[i];
            if (item == null || item.getType() == Material.AIR) {
                continue;
            }

            String kitName = ItemUtils.getTagStringItem(plugin, item, TAG_KIT);
            if (kitName != null) {
                setKit(kitName, player, inv, i, kitsManager, playerDataManager, kitItemManager, item);
            }
        }
    }

    private ItemStack setItemActions(ItemKitInventory itemInventory, ItemStack item) {
        List<String> clickActions = itemInventory.getClickActions();
        if (clickActions != null && !clickActions.isEmpty()) {
            item = ItemUtils.setTagStringItem(plugin, item, TAG_ITEM_ACTIONS, String.join("|", clickActions));
        }
        return item;
    }

    public void setKitPreviewItems(Inventory inv, InventoryPlayer inventoryPlayer, KitInventory kitInventory) {
        KitItemManager kitItemManager = plugin.getKitItemManager();
        Kit kit = plugin.getKitsManager().getKitByName(inventoryPlayer.getKitName());
        if (kit == null) {
            return;
        }

        List<KitItem> allItems = new ArrayList<>(kit.getItems());
        for (KitAction kitAction : kit.getClaimActions()) {
            KitItem kitItem = kitAction.getDisplayItem();
            if (kitItem != null) {
                allItems.add(kitItem);
            }
        }

        int maxSlot = kitInventory.getSlots();
        int slot = 0;
        for (KitItem kitItem : allItems) {
            ItemStack item = kitItemManager.createItemFromKitItem(kitItem, inventoryPlayer.getPlayer(), kit);
            int previewSlot = kitItem.getPreviewSlot();
            if (previewSlot != -1) {
                if (previewSlot >= 0 && previewSlot < maxSlot) {
                    inv.setItem(previewSlot, item);
                }
            } else if (slot < maxSlot) {
                inv.setItem(slot, item);
            }
            slot++;

            if (slot >= maxSlot) {
                break;
            }
        }
    }

    public void clickInventory(InventoryPlayer inventoryPlayer, ItemStack item, ClickType clickType) {
        String kitName = ItemUtils.getTagStringItem(plugin, item, TAG_KIT);
        if (kitName != null) {
            clickOnKitItem(inventoryPlayer, kitName, clickType);
            return;
        }

        if (ItemUtils.getTagStringItem(plugin, item, TAG_LAYOUT_EDITOR) != null) {
            plugin.getLayoutEditorManager().openFromInventory(inventoryPlayer);
            return;
        }

        String itemActions = ItemUtils.getTagStringItem(plugin, item, TAG_ITEM_ACTIONS);
        if (itemActions != null) {
            clickOnActionItem(inventoryPlayer, itemActions);
        }

        String openInventory = ItemUtils.getTagStringItem(plugin, item, TAG_OPEN_INVENTORY);
        if (openInventory != null) {
            clickOnOpenInventoryItem(inventoryPlayer, openInventory);
            return;
        }

        if (inventoryPlayer.getInventoryName().equals("buy_requirements_inventory")) {
            String buyTag = ItemUtils.getTagStringItem(plugin, item, TAG_BUY);
            if (buyTag == null) {
                return;
            }
            if (buyTag.equals("yes") || buyTag.equals("buy_yes")) {
                inventoryRequirementsManager.requirementsInventoryBuy(inventoryPlayer);
            } else {
                inventoryRequirementsManager.requirementsInventoryCancel(inventoryPlayer);
            }
        }
    }

    public void clickOnKitItem(InventoryPlayer inventoryPlayer, String kitName, ClickType clickType) {
        MainConfigManager mainConfigManager = plugin.getConfigsManager().getMainConfigManager();
        Player player = inventoryPlayer.getPlayer();
        FileConfiguration messagesConfig = plugin.getConfigsManager().getMessagesConfigManager().getConfig();
        MessagesManager msgManager = plugin.getMessagesManager();

        Kit kit = plugin.getKitsManager().getKitByName(kitName);
        if (kit == null) {
            msgManager.sendMessage(player, messagesConfig.getString("kitDoesNotExists").replace("%kit%", kitName), true);
            return;
        }

        if (mainConfigManager.isLayoutEditor() && mainConfigManager.isLayoutEditorShiftClick()
                && clickType.isShiftClick() && clickType.isLeftClick()) {
            plugin.getLayoutEditorManager().open(inventoryPlayer, kitName);
            return;
        }

        if (clickType == ClickType.RIGHT) {
            if (!mainConfigManager.isKitPreview()) {
                return;
            }

            if (kit.isPermissionRequired() && mainConfigManager.isKitPreviewRequiresKitPermission()
                    && !kit.playerHasPermission(player)) {
                msgManager.sendMessage(player, messagesConfig.getString("cantPreviewError"), true);
                return;
            }

            inventoryPlayer.setPreviousInventoryName(inventoryPlayer.getInventoryName());
            inventoryPlayer.setInventoryName("preview_inventory");
            inventoryPlayer.setKitName(kitName);
            openInventoryLater(inventoryPlayer);
            return;
        }

        if (!beginClaim(player)) {
            return;
        }

        plugin.getPlatformScheduler().runForEntityLater(player,
                () -> performClaim(inventoryPlayer, kitName),
                () -> pendingClaims.remove(player.getUniqueId()), 1L);
    }

    private void performClaim(InventoryPlayer inventoryPlayer, String kitName) {
        Player player = inventoryPlayer.getPlayer();
        try {
            if (player == null || !player.isOnline()) {
                return;
            }

            MainConfigManager mainConfigManager = plugin.getConfigsManager().getMainConfigManager();
            FileConfiguration messagesConfig = plugin.getConfigsManager().getMessagesConfigManager().getConfig();
            MessagesManager msgManager = plugin.getMessagesManager();

            PlayerKitsMessageResult result = plugin.getKitsManager().giveKit(player, kitName, new GiveKitInstructions());
            syncInventory(player);

            if (result.isError()) {
                msgManager.sendMessage(player, result.getMessage(), true);
                return;
            }

            if (result.isProceedToBuy()) {
                if (!isViewingOwnGui(inventoryPlayer)) {
                    return;
                }
                inventoryPlayer.setPreviousInventoryName(inventoryPlayer.getInventoryName());
                inventoryPlayer.setInventoryName("buy_requirements_inventory");
                inventoryPlayer.setKitName(kitName);
                openInventory(inventoryPlayer);
                return;
            }

            msgManager.sendMessage(player, messagesConfig.getString("kitReceived").replace("%kit%", kitName), true);

            if (mainConfigManager.isCloseInventoryOnClaim()) {
                player.closeInventory();
                return;
            }

            if (isViewingOwnGui(inventoryPlayer)) {
                openInventory(inventoryPlayer);
            }
        } finally {
            if (player != null) {
                pendingClaims.remove(player.getUniqueId());
            }
        }
    }

    public boolean beginClaim(Player player) {
        UUID uuid = player.getUniqueId();
        long now = System.currentTimeMillis();
        Long last = lastClaimMillis.get(uuid);
        if (last != null && now - last < CLAIM_DEBOUNCE_MILLIS) {
            return false;
        }
        if (!pendingClaims.add(uuid)) {
            return false;
        }
        lastClaimMillis.put(uuid, now);
        return true;
    }

    public void endClaim(Player player) {
        pendingClaims.remove(player.getUniqueId());
    }

    public void forgetPlayer(Player player) {
        UUID uuid = player.getUniqueId();
        pendingClaims.remove(uuid);
        lastClaimMillis.remove(uuid);
    }

    private boolean isViewingOwnGui(InventoryPlayer inventoryPlayer) {
        Player player = inventoryPlayer.getPlayer();
        if (player == null || !player.isOnline()) {
            return false;
        }
        if (players.get(player.getUniqueId()) != inventoryPlayer) {
            return false;
        }
        return InventoryUtils.getKitGuiHolder(InventoryUtils.getTopInventory(player)) == inventoryPlayer.getHolder();
    }

    @SuppressWarnings({"deprecation", "removal"})
    public void syncInventory(Player player) {
        if (player != null && player.isOnline()) {
            player.updateInventory();
        }
    }

    public void syncInventoryLater(Player player) {
        plugin.getPlatformScheduler().runForEntityLater(player, () -> syncInventory(player), 1L);
    }

    public void closeLater(Player player) {
        plugin.getPlatformScheduler().runForEntityLater(player, () -> {
            if (player.isOnline()) {
                player.closeInventory();
            }
        }, 1L);
    }

    public void clickOnOpenInventoryItem(InventoryPlayer inventoryPlayer, String openInventory) {
        if (openInventory.equals("previous")) {
            String previous = inventoryPlayer.getPreviousInventoryName();
            if (previous == null) {
                closeLater(inventoryPlayer.getPlayer());
                return;
            }
            inventoryPlayer.setInventoryName(previous);
        } else {
            inventoryPlayer.setPreviousInventoryName(inventoryPlayer.getInventoryName());
            inventoryPlayer.setInventoryName(openInventory);
        }
        openInventoryLater(inventoryPlayer);
    }

    public void clickOnActionItem(InventoryPlayer inventoryPlayer, String itemActions) {
        KitsManager kitsManager = plugin.getKitsManager();
        Player player = inventoryPlayer.getPlayer();
        for (String action : itemActions.split("\\|")) {
            kitsManager.executeAction(player, action);
        }
    }

    @SuppressWarnings({"deprecation", "removal"})
    public void setKit(String kitName, Player player, Inventory inv, int slot, KitsManager kitsManager,
                       PlayerDataManager playerDataManager, KitItemManager kitItemManager, ItemStack currentItem) {
        Kit kit = kitsManager.getKitByName(kitName);
        if (kit == null) {
            return;
        }

        String currentStatus = "default";
        if (currentItem != null) {
            String tagged = ItemUtils.getTagStringItem(plugin, currentItem, TAG_KIT_STATUS);
            if (tagged != null) {
                currentStatus = tagged;
            }
        }
        String newStatus = "default";

        ArrayList<KitVariable> variablesToReplace = new ArrayList<>(2);
        variablesToReplace.add(new KitVariable("%kit_name%", kit.getName()));

        KitItem kitItem = null;
        if (!kit.playerHasPermission(player)) {
            kitItem = kit.getDisplayItemNoPermission();
            newStatus = "no_permission";
        } else {
            boolean admin = PlayerUtils.isPlayerKitsAdmin(player);

            if (kit.isOneTime() && !admin && playerDataManager.isKitOneTime(player, kit.getName())) {
                kitItem = kit.getDisplayItemOneTime();
                newStatus = "one_time";
            }

            if (kit.getRequirements() != null && kit.getRequirements().isOneTimeRequirements()
                    && playerDataManager.isKitBought(player, kit.getName())) {
                kitItem = kit.getDisplayItemOneTimeRequirements();
                newStatus = "one_time_requirements";
            }

            if (kit.getCooldown() != 0 && !admin) {
                long playerCooldown = playerDataManager.getKitCooldown(player, kit.getName());
                String timeStringMillisDif = playerDataManager.getKitCooldownString(playerCooldown);
                if (!timeStringMillisDif.isEmpty()) {
                    kitItem = kit.getDisplayItemCooldown();
                    variablesToReplace.add(new KitVariable("%time%", timeStringMillisDif));
                    newStatus = "cooldown";
                }
            }
        }
        if (kitItem == null) {
            kitItem = kit.getDisplayItemDefault();
            newStatus = "default";
        }
        if (kitItem == null) {
            return;
        }

        kitItem = kitItem.clone();

        boolean useMiniMessage = plugin.getConfigsManager().getMainConfigManager().isUseMiniMessage();
        if (currentItem != null && newStatus.equals(currentStatus)) {
            kitItemManager.replaceVariables(kitItem, variablesToReplace, player);

            ItemMeta meta = currentItem.getItemMeta();
            if (meta == null) {
                return;
            }

            String name = kitItem.getName();
            if (name != null) {
                if (useMiniMessage) {
                    MiniMessageUtils.setItemName(meta, name);
                } else {
                    meta.setDisplayName(MessagesManager.getLegacyColoredMessage(name));
                }
            }

            List<String> lore = kitItem.getLore();
            if (lore != null) {
                if (useMiniMessage) {
                    MiniMessageUtils.setItemLore(meta, lore, player, plugin);
                } else {
                    List<String> loreCopy = new ArrayList<>(lore.size());
                    for (String line : lore) {
                        loreCopy.add(MessagesManager.getLegacyColoredMessage(
                                OtherUtils.replaceGlobalVariables(line, player, plugin)));
                    }
                    meta.setLore(loreCopy);
                }
            }

            currentItem.setItemMeta(meta);
        } else {
            kitItemManager.replaceVariables(kitItem, variablesToReplace, player);
            ItemStack item = kitItemManager.createItemFromKitItem(kitItem, player, kit);
            item = ItemUtils.setTagStringItem(plugin, item, TAG_KIT, kitName);
            item = ItemUtils.setTagStringItem(plugin, item, TAG_KIT_STATUS, newStatus);
            inv.setItem(slot, item);
        }
    }

    public KitPosition getKitPositionByKitName(String kitName) {
        String expected = KIT_TYPE_PREFIX + kitName;
        for (KitInventory kitInventory : inventories.values()) {
            for (ItemKitInventory itemKitInventory : kitInventory.getItems()) {
                if (expected.equals(itemKitInventory.getType()) && !itemKitInventory.getSlots().isEmpty()) {
                    return new KitPosition(itemKitInventory.getSlots().get(0), kitInventory.getName());
                }
            }
        }
        return null;
    }

    public void removeKitFromInventory(String kitName) {
        String expected = KIT_TYPE_PREFIX + kitName;
        for (KitInventory inventory : inventories.values()) {
            inventory.getItems().removeIf(item -> expected.equals(item.getType()));
        }
    }
}
