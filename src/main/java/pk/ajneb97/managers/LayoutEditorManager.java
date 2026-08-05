package pk.ajneb97.managers;

import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import pk.ajneb97.PlayerKits2;
import pk.ajneb97.model.Kit;
import pk.ajneb97.model.inventory.InventoryPlayer;
import pk.ajneb97.model.inventory.KitGuiHolder;
import pk.ajneb97.model.item.KitItem;
import pk.ajneb97.model.layout.LayoutEditSession;
import pk.ajneb97.model.layout.PlayerKitLayout;
import pk.ajneb97.utils.InventoryItem;
import pk.ajneb97.utils.InventoryUtils;
import pk.ajneb97.utils.OtherUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class LayoutEditorManager {

    public static final int REGION_SIZE = 36;
    private static final int GUI_SIZE = 54;
    private static final int SLOT_BACK = 45;
    private static final int SLOT_INFO = 49;
    private static final int SLOT_RESET = 51;
    private static final int SLOT_SAVE = 53;

    private final PlayerKits2 plugin;
    private final Map<UUID, LayoutEditSession> sessions;

    public LayoutEditorManager(PlayerKits2 plugin) {
        this.plugin = plugin;
        this.sessions = new ConcurrentHashMap<>();
    }

    public static int guiSlotToInventorySlot(int guiSlot) {
        if (guiSlot < 0 || guiSlot >= REGION_SIZE) {
            return -1;
        }
        return guiSlot < 27 ? guiSlot + 9 : guiSlot - 27;
    }

    public static int inventorySlotToGuiSlot(int inventorySlot) {
        if (inventorySlot < 0 || inventorySlot > PlayerKitLayout.MAX_SLOT) {
            return -1;
        }
        return inventorySlot < 9 ? inventorySlot + 27 : inventorySlot - 9;
    }

    public LayoutEditSession getSession(Player player) {
        return sessions.get(player.getUniqueId());
    }

    public void endSession(Player player) {
        sessions.remove(player.getUniqueId());
    }

    public void clearSessions() {
        sessions.clear();
    }

    public void openFromInventory(InventoryPlayer inventoryPlayer) {
        String kitName = inventoryPlayer.getKitName();
        if (kitName == null) {
            MessagesManager msgManager = plugin.getMessagesManager();
            FileConfiguration messagesConfig = plugin.getConfigsManager().getMessagesConfigManager().getConfig();
            msgManager.sendMessage(inventoryPlayer.getPlayer(), messagesConfig.getString("layoutNoKitSelected"), true);
            return;
        }
        open(inventoryPlayer, kitName);
    }

    public void open(InventoryPlayer inventoryPlayer, String kitName) {
        Player player = inventoryPlayer.getPlayer();
        FileConfiguration messagesConfig = plugin.getConfigsManager().getMessagesConfigManager().getConfig();
        MessagesManager msgManager = plugin.getMessagesManager();

        if (!plugin.getConfigsManager().getMainConfigManager().isLayoutEditor()) {
            msgManager.sendMessage(player, messagesConfig.getString("layoutDisabled"), true);
            return;
        }

        Kit kit = plugin.getKitsManager().getKitByName(kitName);
        if (kit == null) {
            msgManager.sendMessage(player, messagesConfig.getString("kitDoesNotExists").replace("%kit%", kitName), true);
            return;
        }

        if (!kit.playerHasPermission(player)) {
            msgManager.sendMessage(player, messagesConfig.getString("kitNoPermissions"), true);
            return;
        }

        List<KitItem> items = kit.getItems();
        if (positionableCount(items) == 0) {
            msgManager.sendMessage(player, messagesConfig.getString("layoutNoItems"), true);
            return;
        }

        PlayerKitLayout existing = plugin.getPlayerDataManager().getLayout(player, kitName);
        LayoutEditSession session = new LayoutEditSession(kitName, items.size(), existing, defaultSlots(items));
        sessions.put(player.getUniqueId(), session);

        inventoryPlayer.setPreviousInventoryName(inventoryPlayer.getInventoryName());
        inventoryPlayer.setKitName(kitName);

        plugin.getPlatformScheduler().runForEntityLater(player, () -> {
            if (player.isOnline() && sessions.get(player.getUniqueId()) == session) {
                render(inventoryPlayer, session, kit, items, true);
            }
        }, 1L);
    }

    private static boolean isPositionable(KitItem kitItem) {
        return kitItem != null && !kitItem.isOffhand();
    }

    private static int positionableCount(List<KitItem> items) {
        int count = 0;
        for (KitItem kitItem : items) {
            if (isPositionable(kitItem)) {
                count++;
            }
        }
        return count;
    }

    private int[] defaultSlots(List<KitItem> items) {
        int[] defaults = new int[items.size()];
        int next = 0;
        for (int i = 0; i < items.size(); i++) {
            if (!isPositionable(items.get(i)) || next > PlayerKitLayout.MAX_SLOT) {
                defaults[i] = PlayerKitLayout.UNSET;
                continue;
            }
            defaults[i] = next;
            next++;
        }
        return defaults;
    }

    private void render(InventoryPlayer inventoryPlayer, LayoutEditSession session, Kit kit,
                        List<KitItem> items, boolean openNow) {
        Player player = inventoryPlayer.getPlayer();
        boolean useMiniMessage = plugin.getConfigsManager().getMainConfigManager().isUseMiniMessage();
        String title = plugin.getConfigsManager().getMainConfigManager().getLayoutEditorTitle()
                .replace("%kit%", kit.getName());

        Inventory inv;
        KitGuiHolder holder;
        if (openNow) {
            holder = new KitGuiHolder(KitGuiHolder.Type.LAYOUT, player.getUniqueId());
            inv = InventoryUtils.createInventory(holder, GUI_SIZE, title, useMiniMessage);
        } else {
            inv = InventoryUtils.getTopInventory(player);
            holder = InventoryUtils.getKitGuiHolder(inv);
            if (holder == null || holder.getType() != KitGuiHolder.Type.LAYOUT) {
                return;
            }
            for (int i = 0; i < REGION_SIZE; i++) {
                inv.setItem(i, null);
            }
        }

        KitItemManager kitItemManager = plugin.getKitItemManager();
        for (int index = 0; index < items.size(); index++) {
            if (!isPositionable(items.get(index))) {
                continue;
            }
            int inventorySlot = session.getSlot(index);
            int guiSlot = inventorySlotToGuiSlot(inventorySlot);
            if (guiSlot < 0) {
                continue;
            }

            ItemStack item;
            try {
                item = kitItemManager.createItemFromKitItem(items.get(index), player, kit);
            } catch (Exception error) {
                continue;
            }
            if (item == null || item.getType() == Material.AIR) {
                continue;
            }

            decorate(item, index == session.getSelectedIndex());
            inv.setItem(guiSlot, item);
        }

        if (openNow) {
            fillControls(inv, session);
            inventoryPlayer.setHolder(holder);
            inventoryPlayer.setInventoryName("layout_editor");
            player.openInventory(inv);
            plugin.getInventoryManager().registerLayoutSession(inventoryPlayer);
        } else {
            player.updateInventory();
        }
    }

    @SuppressWarnings({"deprecation", "removal"})
    private void decorate(ItemStack item, boolean selected) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return;
        }
        List<String> lore = meta.hasLore() ? new ArrayList<>(meta.getLore()) : new ArrayList<>();
        lore.add(" ");
        if (selected) {
            lore.add(MessagesManager.getLegacyColoredMessage("&a&lSELECTED"));
            lore.add(MessagesManager.getLegacyColoredMessage("&7Click an empty slot to move it there."));
        } else {
            lore.add(MessagesManager.getLegacyColoredMessage("&eClick &7to pick this item up."));
        }
        meta.setLore(lore);
        item.setItemMeta(meta);
    }

    private void fillControls(Inventory inv, LayoutEditSession session) {
        Material filler = OtherUtils.isLegacy() ? Material.valueOf("STAINED_GLASS_PANE") : Material.BLACK_STAINED_GLASS_PANE;
        for (int i = REGION_SIZE; i < GUI_SIZE; i++) {
            InventoryItem item = new InventoryItem(inv, i, filler).name(" ");
            if (OtherUtils.isLegacy()) {
                item.dataValue((short) 15);
            }
            item.ready();
        }

        new InventoryItem(inv, SLOT_BACK, Material.ARROW).name("&eGo Back")
                .lore(mutableLore("&7Discards any unsaved changes.")).ready();

        List<String> infoLore = new ArrayList<>();
        infoLore.add("&7This is your personal layout for");
        infoLore.add("&a" + session.getKitName() + "&7.");
        infoLore.add("");
        infoLore.add("&7The grid above mirrors your inventory.");
        infoLore.add("&7Click an item to pick it up, then click");
        infoLore.add("&7another slot to place or swap it.");
        infoLore.add("");
        infoLore.add("&7The original kit is never modified and");
        infoLore.add("&7only you see this layout.");
        infoLore.add("");
        infoLore.add("&8Armor may still be auto-equipped and");
        infoLore.add("&8occupied slots fall back to the default.");
        new InventoryItem(inv, SLOT_INFO, Material.COMPASS).name("&6&lInfo").lore(infoLore).ready();

        new InventoryItem(inv, SLOT_RESET, Material.BARRIER).name("&c&lReset Layout")
                .lore(mutableLore("&7Restores the default kit layout.")).ready();

        new InventoryItem(inv, SLOT_SAVE, Material.EMERALD_BLOCK).name("&a&lSave Layout")
                .lore(mutableLore("&7Stores this layout for your account.")).ready();
    }

    private List<String> mutableLore(String... lines) {
        List<String> lore = new ArrayList<>(lines.length);
        for (String line : lines) {
            lore.add(line);
        }
        return lore;
    }

    public void clickInventory(InventoryPlayer inventoryPlayer, int slot, ClickType clickType) {
        Player player = inventoryPlayer.getPlayer();
        LayoutEditSession session = sessions.get(player.getUniqueId());
        if (session == null) {
            player.closeInventory();
            return;
        }

        Kit kit = plugin.getKitsManager().getKitByName(session.getKitName());
        if (kit == null) {
            player.closeInventory();
            return;
        }

        FileConfiguration messagesConfig = plugin.getConfigsManager().getMessagesConfigManager().getConfig();
        MessagesManager msgManager = plugin.getMessagesManager();
        List<KitItem> items = kit.getItems();

        if (slot >= 0 && slot < REGION_SIZE) {
            handleRegionClick(session, slot, clickType);
            render(inventoryPlayer, session, kit, items, false);
            return;
        }

        switch (slot) {
            case SLOT_SAVE:
                saveSession(player, session, items);
                msgManager.sendMessage(player, messagesConfig.getString("layoutSaved")
                        .replace("%kit%", session.getKitName()), true);
                closeToPrevious(inventoryPlayer);
                break;
            case SLOT_RESET:
                session.reset(defaultSlots(items));
                plugin.getPlayerDataManager().clearLayout(player, session.getKitName());
                msgManager.sendMessage(player, messagesConfig.getString("layoutReset")
                        .replace("%kit%", session.getKitName()), true);
                render(inventoryPlayer, session, kit, items, false);
                break;
            case SLOT_BACK:
                closeToPrevious(inventoryPlayer);
                break;
            default:
                break;
        }
    }

    private void handleRegionClick(LayoutEditSession session, int guiSlot, ClickType clickType) {
        int inventorySlot = guiSlotToInventorySlot(guiSlot);
        if (inventorySlot < 0) {
            return;
        }

        int occupant = session.getIndexAtSlot(inventorySlot);
        int selected = session.getSelectedIndex();

        if (clickType.isRightClick() && selected == -1 && occupant != -1) {
            session.setSelectedIndex(occupant);
            return;
        }

        if (selected == -1) {
            if (occupant != -1) {
                session.setSelectedIndex(occupant);
            }
            return;
        }

        if (occupant == selected) {
            session.setSelectedIndex(-1);
            return;
        }

        session.moveTo(selected, inventorySlot);
        session.setSelectedIndex(-1);
    }

    private void saveSession(Player player, LayoutEditSession session, List<KitItem> items) {
        if (session.matchesDefaults(defaultSlots(items))) {
            plugin.getPlayerDataManager().clearLayout(player, session.getKitName());
            return;
        }
        plugin.getPlayerDataManager().saveLayout(player, session.toLayout());
    }

    private void closeToPrevious(InventoryPlayer inventoryPlayer) {
        Player player = inventoryPlayer.getPlayer();
        endSession(player);

        String previous = inventoryPlayer.getPreviousInventoryName();
        if (previous == null || plugin.getInventoryManager().getInventory(previous) == null) {
            player.closeInventory();
            return;
        }

        inventoryPlayer.setInventoryName(previous);
        plugin.getPlatformScheduler().runForEntityLater(player, () -> {
            if (player.isOnline()) {
                plugin.getInventoryManager().openInventory(inventoryPlayer);
            }
        }, 1L);
    }
}
