package pk.ajneb97.listeners;

import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import pk.ajneb97.PlayerKits2;
import pk.ajneb97.managers.InventoryManager;
import pk.ajneb97.managers.MessagesManager;
import pk.ajneb97.model.inventory.InventoryPlayer;
import pk.ajneb97.model.inventory.KitGuiHolder;
import pk.ajneb97.utils.InventoryUtils;

public class PlayerListener implements Listener {

    private final PlayerKits2 plugin;

    public PlayerListener(PlayerKits2 plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        plugin.getPlayerDataManager().manageJoin(player);

        if (!player.isOp() || !plugin.getConfigsManager().getMainConfigManager().isUpdateNotify()) {
            return;
        }
        String latestVersion = plugin.getUpdateCheckerManager().getLatestVersion();
        if (latestVersion != null && !plugin.version.equals(latestVersion)) {
            player.sendMessage(MessagesManager.getLegacyColoredMessage(PlayerKits2.prefix
                    + "&cThere is a new version available. &e(&7" + latestVersion + "&e)"));
            player.sendMessage(MessagesManager.getLegacyColoredMessage(
                    "&cYou can download it at: &ahttps://modrinth.com/plugin/playerkits-2"));
        }
    }

    @EventHandler
    public void onLeave(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        plugin.getInventoryManager().removeInventoryPlayer(player);
        plugin.getLayoutEditorManager().endSession(player);
        plugin.getPlayerDataManager().manageLeave(player);
    }

    @EventHandler
    public void closeInventory(InventoryCloseEvent event) {
        HumanEntity human = event.getPlayer();
        if (!(human instanceof Player)) {
            return;
        }
        Player player = (Player) human;
        if (InventoryUtils.isKitGui(event.getInventory(), KitGuiHolder.Type.LAYOUT)) {
            plugin.getLayoutEditorManager().endSession(player);
        }
        plugin.getInventoryManager().removeInventoryPlayer(player);
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
        if (holder == null) {
            return;
        }
        if (holder.getType() != KitGuiHolder.Type.KITS && holder.getType() != KitGuiHolder.Type.LAYOUT) {
            return;
        }

        event.setCancelled(true);
        event.setResult(org.bukkit.event.Event.Result.DENY);

        if (!holder.isOwner(player.getUniqueId())) {
            plugin.getInventoryManager().closeLater(player);
            return;
        }

        InventoryManager invManager = plugin.getInventoryManager();
        InventoryPlayer inventoryPlayer = invManager.getInventoryPlayer(player);
        if (inventoryPlayer == null || inventoryPlayer.getHolder() != holder) {
            return;
        }

        Inventory clicked = event.getClickedInventory();
        if (clicked == null || !clicked.equals(topInventory)) {
            return;
        }

        int slot = event.getSlot();
        if (slot < 0 || slot >= topInventory.getSize()) {
            return;
        }

        ClickType clickType = event.getClick();

        if (holder.getType() == KitGuiHolder.Type.LAYOUT) {
            plugin.getLayoutEditorManager().clickInventory(inventoryPlayer, slot, clickType);
            return;
        }

        ItemStack current = event.getCurrentItem();
        if (current == null || current.getType() == Material.AIR) {
            return;
        }

        invManager.clickInventory(inventoryPlayer, current, clickType);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
    public void dragInventory(InventoryDragEvent event) {
        Inventory topInventory = InventoryUtils.getTopInventory(event);
        KitGuiHolder holder = InventoryUtils.getKitGuiHolder(topInventory);
        if (holder == null) {
            return;
        }
        if (holder.getType() != KitGuiHolder.Type.KITS && holder.getType() != KitGuiHolder.Type.LAYOUT) {
            return;
        }

        event.setCancelled(true);
        event.setResult(org.bukkit.event.Event.Result.DENY);
    }
}
