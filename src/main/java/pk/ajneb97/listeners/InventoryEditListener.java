package pk.ajneb97.listeners;

import org.bukkit.ChatColor;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.inventory.Inventory;
import pk.ajneb97.PlayerKits2;
import pk.ajneb97.managers.edit.InventoryEditManager;
import pk.ajneb97.model.inventory.InventoryPlayer;
import pk.ajneb97.model.inventory.KitGuiHolder;
import pk.ajneb97.utils.InventoryUtils;
import pk.ajneb97.utils.PlayerUtils;

public class InventoryEditListener implements Listener {

    private final PlayerKits2 plugin;

    public InventoryEditListener(PlayerKits2 plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void closeInventory(InventoryCloseEvent event) {
        HumanEntity human = event.getPlayer();
        if (!(human instanceof Player)) {
            return;
        }
        Player player = (Player) human;
        InventoryEditManager invManager = plugin.getInventoryEditManager();
        InventoryPlayer inventoryPlayer = invManager.getInventoryPlayer(player);
        if (inventoryPlayer != null) {
            String inventoryName = inventoryPlayer.getInventoryName();
            if (inventoryName != null && inventoryName.startsWith("edit_position")) {
                invManager.getInventoryEditPositionManager().closeInventory(inventoryPlayer);
            }
            if (plugin.getVerifyManager() != null) {
                plugin.getVerifyManager().verify();
            }
        }
        invManager.removeInventoryPlayer(player);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
    public void clickInventory(InventoryClickEvent event) {
        HumanEntity human = event.getWhoClicked();
        if (!(human instanceof Player)) {
            return;
        }
        Player player = (Player) human;

        Inventory topInventory = InventoryUtils.getTopInventory(event);
        KitGuiHolder holder = InventoryUtils.getKitGuiHolder(topInventory);
        if (holder == null || !isEditHolder(holder)) {
            return;
        }

        if (!holder.isOwner(player.getUniqueId()) || !PlayerUtils.isPlayerKitsAdmin(player)) {
            event.setCancelled(true);
            event.setResult(org.bukkit.event.Event.Result.DENY);
            plugin.getInventoryManager().closeLater(player);
            return;
        }

        InventoryEditManager invManager = plugin.getInventoryEditManager();
        InventoryPlayer inventoryPlayer = invManager.getInventoryPlayer(player);
        if (inventoryPlayer == null || inventoryPlayer.getHolder() != holder) {
            event.setCancelled(true);
            return;
        }

        ClickType clickType = event.getClick();
        String inventoryName = inventoryPlayer.getInventoryName();
        if (inventoryName == null) {
            event.setCancelled(true);
            return;
        }

        if (inventoryName.startsWith("edit_display_")) {
            invManager.getInventoryEditDisplayManager().clickInventory(inventoryPlayer,
                    event.getCurrentItem(), event.getSlot(), clickType, event);
            return;
        }
        if (inventoryName.equals("edit_items")) {
            invManager.getInventoryEditKitItemsManager().clickInventory(inventoryPlayer,
                    event.getCurrentItem(), event.getSlot(), clickType, event);
            return;
        }
        if (inventoryName.startsWith("edit_position")) {
            invManager.getInventoryEditPositionManager().clickInventory(inventoryPlayer,
                    event.getCurrentItem(), event.getSlot(), clickType, event);
            return;
        }

        event.setCancelled(true);
        event.setResult(org.bukkit.event.Event.Result.DENY);

        if (event.getCurrentItem() == null) {
            return;
        }

        Inventory clicked = event.getClickedInventory();
        if (clicked != null && clicked.equals(topInventory)) {
            invManager.clickInventory(inventoryPlayer, event.getCurrentItem(), event.getSlot(), clickType);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
    public void dragInventory(InventoryDragEvent event) {
        Inventory topInventory = InventoryUtils.getTopInventory(event);
        KitGuiHolder holder = InventoryUtils.getKitGuiHolder(topInventory);
        if (holder == null || !isEditHolder(holder)) {
            return;
        }

        if (holder.getType() == KitGuiHolder.Type.EDIT_ITEMS) {
            HumanEntity human = event.getWhoClicked();
            if (human instanceof Player && holder.isOwner(human.getUniqueId())
                    && PlayerUtils.isPlayerKitsAdmin(human)) {
                for (int rawSlot : event.getRawSlots()) {
                    if (rawSlot >= 45 && rawSlot <= 53) {
                        event.setCancelled(true);
                        return;
                    }
                }
                return;
            }
        }

        event.setCancelled(true);
        event.setResult(org.bukkit.event.Event.Result.DENY);
    }

    @SuppressWarnings({"deprecation", "removal"})
    @EventHandler(priority = EventPriority.LOWEST)
    public void onChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        InventoryEditManager invManager = plugin.getInventoryEditManager();
        InventoryPlayer inventoryPlayer = invManager.getInventoryPlayer(player);
        if (inventoryPlayer == null) {
            return;
        }
        if (!PlayerUtils.isPlayerKitsAdmin(player)) {
            invManager.removeInventoryPlayer(player);
            return;
        }

        event.setCancelled(true);
        String message = ChatColor.stripColor(event.getMessage());
        plugin.getPlatformScheduler().runForEntityLater(player,
                () -> invManager.writeChat(inventoryPlayer, message), 1L);
    }

    private static boolean isEditHolder(KitGuiHolder holder) {
        KitGuiHolder.Type type = holder.getType();
        return type == KitGuiHolder.Type.EDIT
                || type == KitGuiHolder.Type.EDIT_ITEMS
                || type == KitGuiHolder.Type.EDIT_POSITION;
    }
}
