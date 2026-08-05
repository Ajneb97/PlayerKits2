package pk.ajneb97.managers;

import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
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
import pk.ajneb97.model.layout.PlayerKitLayout;
import pk.ajneb97.utils.ActionUtils;
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
import java.util.regex.Pattern;

public class KitsManager {

    private static final Pattern VALID_KIT_NAME = Pattern.compile("[A-Za-z0-9_\\-]{1,48}");

    private PlayerKits2 plugin;
    private Map<String, Kit> kits;
    private final Set<UUID> claiming;

    public KitsManager(PlayerKits2 plugin) {
        this.plugin = plugin;
        this.kits = new ConcurrentHashMap<>();
        this.claiming = ConcurrentHashMap.newKeySet();
    }

    public PlayerKits2 getPlugin() {
        return plugin;
    }

    public void setPlugin(PlayerKits2 plugin) {
        this.plugin = plugin;
    }

    public ArrayList<Kit> getKits() {
        return new ArrayList<>(kits.values());
    }

    public Collection<Kit> getKitsView() {
        return kits.values();
    }

    public void setKits(ArrayList<Kit> newKits) {
        Map<String, Kit> replacement = new ConcurrentHashMap<>();
        for (Kit kit : newKits) {
            if (kit.getName() != null) {
                replacement.put(kit.getName(), kit);
            }
        }
        this.kits = replacement;
    }

    public Kit getKitByName(String name) {
        return name == null ? null : kits.get(name);
    }

    public void removeKit(String name) {
        kits.remove(name);
    }

    public static boolean isValidKitName(String name) {
        return name != null && VALID_KIT_NAME.matcher(name).matches();
    }

    public void createKit(String kitName, Player player, boolean saveOriginalItems) {
        FileConfiguration messagesFile = plugin.getConfigsManager().getMessagesConfigManager().getConfig();
        MainConfigManager mainConfigManager = plugin.getConfigsManager().getMainConfigManager();
        MessagesManager msgManager = plugin.getMessagesManager();

        if (!isValidKitName(kitName)) {
            msgManager.sendMessage(player, messagesFile.getString("invalidKitName"), true);
            return;
        }

        if (getKitByName(kitName) != null) {
            msgManager.sendMessage(player, messagesFile.getString("kitAlreadyExists").replace("%kit%", kitName), true);
            return;
        }

        ItemStack[] inventoryContents = PlayerUtils.getAllInventoryContents(player);

        KitItemManager kitItemManager = plugin.getKitItemManager();
        ArrayList<KitItem> items = new ArrayList<>();
        boolean hasArmor = false;
        for (int i = 0; i < inventoryContents.length; i++) {
            ItemStack item = inventoryContents[i];
            if (item == null || item.getType() == Material.AIR) {
                continue;
            }

            KitItem kitItem = kitItemManager.createKitItemFromItemStack(item, saveOriginalItems);

            if (i >= 36 && i <= 39) {
                hasArmor = true;
            }
            if (i == 40) {
                kitItem.setOffhand(true);
            }

            items.add(kitItem);
        }

        if (items.isEmpty()) {
            msgManager.sendMessage(player, messagesFile.getString("inventoryEmpty"), true);
            return;
        }

        Kit kit = new Kit(kitName);
        kit.setItems(items);
        kit.setDefaults(mainConfigManager.getNewKitDefault());
        kit.setAutoArmor(hasArmor);
        kit.setSaveOriginalItems(saveOriginalItems);

        kits.put(kitName, kit);
        plugin.getConfigsManager().getKitsConfigManager().saveConfig(kit);

        msgManager.sendMessage(player, messagesFile.getString("kitCreated").replace("%kit%", kitName), true);

        InventoryManager inventoryManager = plugin.getInventoryManager();
        String newKitDefaultInventory = mainConfigManager.getNewKitDefaultInventory();
        KitInventory inventory = inventoryManager.getInventory(newKitDefaultInventory);
        if (inventory != null) {
            int resultSlot = inventory.addKitItemOnFirstEmptySlot(kitName);
            if (resultSlot == -1) {
                msgManager.sendMessage(player, messagesFile.getString("kitNotAddedToInventory"), true);
            } else {
                msgManager.sendMessage(player, messagesFile.getString("kitAddedToInventory")
                        .replace("%inventory%", newKitDefaultInventory).replace("%slot%", String.valueOf(resultSlot)), true);
                plugin.getConfigsManager().getInventoryConfigManager().saveKitItemOnConfig(newKitDefaultInventory, resultSlot, kitName);
            }
        }
    }

    public void deleteKit(String kitName, CommandSender sender) {
        FileConfiguration messagesFile = plugin.getConfigsManager().getMessagesConfigManager().getConfig();
        MessagesManager msgManager = plugin.getMessagesManager();
        if (getKitByName(kitName) == null) {
            msgManager.sendMessage(sender, messagesFile.getString("kitDoesNotExists")
                    .replace("%kit%", kitName), true);
            return;
        }

        removeKit(kitName);
        plugin.getConfigsManager().getKitsConfigManager().removeKitFile(kitName);
        plugin.getInventoryManager().removeKitFromInventory(kitName);
        plugin.getConfigsManager().getInventoryConfigManager().save();

        msgManager.sendMessage(sender, messagesFile.getString("kitDeleted").replace("%kit%", kitName), true);
    }

    public PlayerKitsMessageResult giveKit(Player player, String kitName, GiveKitInstructions giveKitInstructions) {
        UUID uuid = player.getUniqueId();
        if (!claiming.add(uuid)) {
            FileConfiguration messagesFile = plugin.getConfigsManager().getMessagesConfigManager().getConfig();
            return PlayerKitsMessageResult.error(messagesFile.getString("kitClaimInProgress"));
        }
        try {
            return giveKitInternal(player, kitName, giveKitInstructions);
        } finally {
            claiming.remove(uuid);
        }
    }

    private PlayerKitsMessageResult giveKitInternal(Player player, String kitName, GiveKitInstructions giveKitInstructions) {
        Kit kit = getKitByName(kitName);
        FileConfiguration messagesFile = plugin.getConfigsManager().getMessagesConfigManager().getConfig();
        FileConfiguration configFile = plugin.getConfigsManager().getMainConfigManager().getConfig();
        PlayerDataManager playerDataManager = plugin.getPlayerDataManager();
        MessagesManager msgManager = plugin.getMessagesManager();

        if (kit == null) {
            return PlayerKitsMessageResult.error(messagesFile.getString("kitDoesNotExists").replace("%kit%", kitName));
        }

        if (!player.isOnline()) {
            return PlayerKitsMessageResult.error(messagesFile.getString("playerNotOnline")
                    .replace("%player%", player.getName()));
        }

        boolean fromCommand = giveKitInstructions.isFromCommand();

        if (!fromCommand && !playerDataManager.isDataReady(player)) {
            return PlayerKitsMessageResult.error(messagesFile.getString("playerDataNotLoaded"));
        }

        if (!fromCommand) {
            if (!giveKitInstructions.isIgnorePermission() && !kit.playerHasPermission(player)) {
                sendKitActions(kit.getErrorActions(), player, false);
                return PlayerKitsMessageResult.error(messagesFile.getString("kitNoPermissions"));
            }

            if (kit.isOneTime() && !PlayerUtils.isPlayerKitsAdmin(player) && !PlayerUtils.hasOneTimeBypassPermission(player)
                    && playerDataManager.isKitOneTime(player, kit.getName())) {
                sendKitActions(kit.getErrorActions(), player, false);
                return PlayerKitsMessageResult.error(messagesFile.getString("oneTimeError"));
            }

            if (kit.getCooldown() != 0 && !PlayerUtils.isPlayerKitsAdmin(player) && !PlayerUtils.hasCooldownBypassPermission(player)) {
                long playerCooldown = playerDataManager.getKitCooldown(player, kit.getName());
                long millisDif = playerCooldown - System.currentTimeMillis();
                String timeStringMillisDif = OtherUtils.getTime(millisDif / 1000, msgManager);
                if (!timeStringMillisDif.isEmpty()) {
                    sendKitActions(kit.getErrorActions(), player, false);
                    return PlayerKitsMessageResult.error(messagesFile.getString("cooldownError")
                            .replace("%time%", timeStringMillisDif));
                }
            }

            KitRequirements kitRequirements = kit.getRequirements();
            if (!giveKitInstructions.isIgnoreRequirements() && kitRequirements != null &&
                    (kitRequirements.getPrice() != 0 || !kitRequirements.getExtraRequirements().isEmpty())) {
                if (!(kitRequirements.isOneTimeRequirements() && playerDataManager.isKitBought(player, kit.getName()))) {
                    if (!giveKitInstructions.isRequirementsSatisfied()) {
                        PlayerKitsMessageResult result = PlayerKitsMessageResult.success();
                        result.setProceedToBuy(true);
                        return result;
                    }

                    if (!passPrice(kitRequirements.getPrice(), player)) {
                        sendKitActions(kit.getErrorActions(), player, false);
                        return PlayerKitsMessageResult.error(messagesFile.getString("requirementsError"));
                    }

                    if (plugin.getDependencyManager().isPlaceholderAPI()) {
                        for (String condition : kitRequirements.getExtraRequirements()) {
                            if (!PlayerUtils.passCondition(player, condition)) {
                                sendKitActions(kit.getErrorActions(), player, false);
                                return PlayerKitsMessageResult.error(messagesFile.getString("requirementsError"));
                            }
                        }
                    }
                }
            }
        }

        KitItemManager kitItemManager = plugin.getKitItemManager();
        ArrayList<KitItem> items = kit.getItems();

        int usedSlots = PlayerUtils.getUsedSlots(player);
        int freeSlots = 36 - usedSlots;
        int inventoryKitItems = 0;

        KitItem itemHelmet = null;
        KitItem itemChestplate = null;
        KitItem itemLeggings = null;
        KitItem itemBoots = null;
        KitItem itemOffhand = null;

        boolean clearInventory = kit.isClearInventory();
        boolean autoArmor = kit.isAutoArmor();

        PlayerInventory playerInventory = player.getInventory();
        for (KitItem item : items) {
            if (autoArmor) {
                String id = item.getId();
                if (item.getOriginalItem() != null) {
                    id = item.getOriginalItem().getType().name();
                }
                if (id == null) {
                    id = "";
                }

                if ((id.contains("_HELMET") || id.contains("PLAYER_HEAD") || id.contains("SKULL_ITEM")) && itemHelmet == null) {
                    if (isEmptySlot(playerInventory.getHelmet()) || clearInventory) {
                        itemHelmet = item;
                        freeSlots++;
                        continue;
                    }
                } else if ((id.contains("_CHESTPLATE") || id.contains("ELYTRA")) && itemChestplate == null) {
                    if (isEmptySlot(playerInventory.getChestplate()) || clearInventory) {
                        itemChestplate = item;
                        freeSlots++;
                        continue;
                    }
                } else if (id.contains("_LEGGINGS") && itemLeggings == null) {
                    if (isEmptySlot(playerInventory.getLeggings()) || clearInventory) {
                        itemLeggings = item;
                        freeSlots++;
                        continue;
                    }
                } else if (id.contains("_BOOTS") && itemBoots == null) {
                    if (isEmptySlot(playerInventory.getBoots()) || clearInventory) {
                        itemBoots = item;
                        freeSlots++;
                        continue;
                    }
                }
            }

            if (item.isOffhand() && itemOffhand == null) {
                if (isEmptySlot(getOffhand(playerInventory)) || clearInventory) {
                    itemOffhand = item;
                    freeSlots++;
                    continue;
                }
            }

            inventoryKitItems++;
        }

        ArrayList<KitAction> claimActions = kit.getClaimActions();
        for (KitAction action : claimActions) {
            if (action.isCountAsItem()) {
                inventoryKitItems++;
            }
        }

        boolean notEnoughSpace = freeSlots < inventoryKitItems;
        boolean dropItemsIfFullInventory = configFile.getBoolean("drop_items_if_full_inventory");

        if (notEnoughSpace && !dropItemsIfFullInventory && !clearInventory) {
            sendKitActions(kit.getErrorActions(), player, false);
            return PlayerKitsMessageResult.error(messagesFile.getString("noSpaceError"));
        }

        if (!fromCommand) {
            KitRequirements kitRequirements = kit.getRequirements();
            boolean chargeRequirements = !giveKitInstructions.isIgnoreRequirements()
                    && kitRequirements != null && giveKitInstructions.isRequirementsSatisfied();

            if (chargeRequirements) {
                double price = kitRequirements.getPrice();
                Economy economy = plugin.getDependencyManager().getVaultEconomy();
                if (price > 0 && economy != null) {
                    EconomyResponse response = economy.withdrawPlayer(player, price);
                    if (response == null || !response.transactionSuccess()) {
                        sendKitActions(kit.getErrorActions(), player, false);
                        return PlayerKitsMessageResult.error(messagesFile.getString("requirementsError"));
                    }
                }
            }

            if (kit.isOneTime() && !PlayerUtils.isPlayerKitsAdmin(player) && !PlayerUtils.hasOneTimeBypassPermission(player)) {
                playerDataManager.setKitOneTime(player, kit.getName());
            }

            if (kit.getCooldown() != 0 && !PlayerUtils.isPlayerKitsAdmin(player) && !PlayerUtils.hasCooldownBypassPermission(player)) {
                long millisMax = System.currentTimeMillis() + (kit.getCooldown() * 1000L);
                playerDataManager.setKitCooldown(player, kit.getName(), millisMax);
            }

            if (chargeRequirements) {
                for (String action : kitRequirements.getActionsOnBuy()) {
                    executeAction(player, action);
                }
                if (kitRequirements.isOneTimeRequirements()) {
                    playerDataManager.setKitBought(player, kitName);
                }
            }
        }

        if (clearInventory) {
            playerInventory.clear();
        }

        sendKitActions(claimActions, player, true);

        PlayerKitLayout layout = plugin.getConfigsManager().getMainConfigManager().isLayoutEditor()
                ? playerDataManager.getLayout(player, kit.getName()) : null;
        boolean[] layoutSlotUsed = layout != null ? new boolean[PlayerKitLayout.MAX_SLOT + 1] : null;

        for (int index = 0; index < items.size(); index++) {
            KitItem kitItem = items.get(index);
            ItemStack item;
            try {
                item = kitItemManager.createItemFromKitItem(kitItem, player, kit);
            } catch (Exception error) {
                plugin.getLogger().severe("Could not build item " + index + " of kit " + kit.getName() + ": " + error);
                continue;
            }
            if (item == null || item.getType() == Material.AIR) {
                continue;
            }

            if (itemHelmet == kitItem) {
                playerInventory.setHelmet(item);
            } else if (itemChestplate == kitItem) {
                playerInventory.setChestplate(item);
            } else if (itemLeggings == kitItem) {
                playerInventory.setLeggings(item);
            } else if (itemBoots == kitItem) {
                playerInventory.setBoots(item);
            } else if (itemOffhand == kitItem) {
                setOffhand(playerInventory, item);
            } else {
                giveInventoryItem(player, playerInventory, item, layout, layoutSlotUsed, index, dropItemsIfFullInventory);
            }
        }

        sendKitActions(claimActions, player, false);

        return PlayerKitsMessageResult.success();
    }

    private void giveInventoryItem(Player player, PlayerInventory playerInventory, ItemStack item,
                                   PlayerKitLayout layout, boolean[] layoutSlotUsed, int index,
                                   boolean dropItemsIfFullInventory) {
        if (layout != null) {
            int slot = layout.getSlot(index);
            if (slot >= 0 && slot <= PlayerKitLayout.MAX_SLOT && !layoutSlotUsed[slot]
                    && isEmptySlot(playerInventory.getItem(slot))) {
                playerInventory.setItem(slot, item);
                layoutSlotUsed[slot] = true;
                return;
            }
        }

        if (playerInventory.firstEmpty() == -1 && dropItemsIfFullInventory) {
            dropItem(player, item);
            return;
        }

        Map<Integer, ItemStack> leftovers = playerInventory.addItem(item);
        if (leftovers == null || leftovers.isEmpty()) {
            return;
        }
        for (ItemStack leftover : leftovers.values()) {
            if (leftover != null && leftover.getType() != Material.AIR) {
                dropItem(player, leftover);
            }
        }
    }

    private void dropItem(Player player, ItemStack item) {
        try {
            player.getWorld().dropItemNaturally(player.getLocation(), item);
        } catch (Exception error) {
            plugin.getLogger().severe("Could not drop overflow kit item: " + error);
        }
    }

    private static boolean isEmptySlot(ItemStack item) {
        return item == null || item.getType() == Material.AIR;
    }

    private static ItemStack getOffhand(PlayerInventory inventory) {
        try {
            return inventory.getItemInOffHand();
        } catch (Throwable ignored) {
            return null;
        }
    }

    private static void setOffhand(PlayerInventory inventory, ItemStack item) {
        try {
            inventory.setItemInOffHand(item);
        } catch (Throwable ignored) {
            inventory.addItem(item);
        }
    }

    public void giveFirstJoinKit(Player player) {
        String firstJoinKit = plugin.getConfigsManager().getMainConfigManager().getFirstJoinKit();
        if (firstJoinKit == null || firstJoinKit.equals("none")) {
            return;
        }
        giveKit(player, firstJoinKit, new GiveKitInstructions(false, false, true, true));
    }

    public void executeAction(Player player, String actionText) {
        if (actionText == null || actionText.isEmpty()) {
            return;
        }
        try {
            if (actionText.equals("close_inventory")) {
                ActionUtils.closeInventory(player);
                return;
            }

            int indexFirst = actionText.indexOf(' ');
            if (indexFirst <= 0) {
                plugin.getLogger().warning("Ignoring malformed kit action: " + actionText);
                return;
            }

            String actionType = actionText.substring(0, indexFirst).replace(":", "");
            String actionLine = OtherUtils.replaceGlobalVariables(actionText.substring(indexFirst + 1), player, plugin);

            switch (actionType) {
                case "message":
                    ActionUtils.message(player, actionLine);
                    break;
                case "console_command":
                    ActionUtils.consoleCommand(plugin, actionLine);
                    break;
                case "player_command":
                    ActionUtils.playerCommand(player, actionLine);
                    break;
                case "playsound":
                    ActionUtils.playSound(player, actionLine);
                    break;
                case "playsound_resource_pack":
                    ActionUtils.playSoundResourcePack(player, actionLine);
                    break;
                case "actionbar":
                    ActionUtils.actionbar(player, actionLine, plugin);
                    break;
                case "title":
                    ActionUtils.title(player, actionLine);
                    break;
                case "firework":
                    ActionUtils.firework(player, actionLine, plugin);
                    break;
                default:
                    plugin.getLogger().warning("Ignoring unknown kit action type: " + actionType);
                    break;
            }
        } catch (Exception error) {
            plugin.getLogger().severe("Error while executing kit action '" + actionText + "': " + error);
        }
    }

    public void sendKitActions(ArrayList<KitAction> actions, Player player, boolean beforeItems) {
        if (actions == null) {
            return;
        }
        for (KitAction action : actions) {
            if (action.isExecuteBeforeItems() == beforeItems) {
                executeAction(player, action.getAction());
            }
        }
    }

    public boolean passPrice(double price, Player player) {
        if (price != 0) {
            Economy economy = plugin.getDependencyManager().getVaultEconomy();
            if (economy != null) {
                return economy.getBalance(player) >= price;
            }
        }
        return true;
    }
}
