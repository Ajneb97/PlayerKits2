package pk.ajneb97.listeners;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.scheduler.BukkitRunnable;
import pk.ajneb97.PlayerKits2;
import pk.ajneb97.managers.edit.InventoryEditManager;
import pk.ajneb97.model.inventory.InventoryPlayer;

public class InventoryEditListener implements Listener {

    private PlayerKits2 plugin;
    public InventoryEditListener(PlayerKits2 plugin){
        this.plugin = plugin;
    }
    @EventHandler
    public void closeInventory(InventoryCloseEvent event){
        Player player = (Player) event.getPlayer();
        InventoryEditManager invManager = plugin.getInventoryEditManager();
        InventoryPlayer inventoryPlayer = invManager.getInventoryPlayer(player);
        if(inventoryPlayer != null) {
            if(inventoryPlayer.getInventoryName().startsWith("edit_position")){
                invManager.getInventoryEditPositionManager().closeInventory(inventoryPlayer);
            }
            plugin.getVerifyManager().verify();
        }
        plugin.getInventoryEditManager().removeInventoryPlayer(player);
    }

    @EventHandler
    public void clickInventory(InventoryClickEvent event){
        Player player = (Player) event.getWhoClicked();
        InventoryEditManager invManager = plugin.getInventoryEditManager();
        InventoryPlayer inventoryPlayer = invManager.getInventoryPlayer(player);
        if(inventoryPlayer != null) {
            ClickType clickType = event.getClick();

            //Special cases before
            if(inventoryPlayer.getInventoryName().startsWith("edit_display_")){
                invManager.getInventoryEditDisplayManager().clickInventory(inventoryPlayer,
                        event.getCurrentItem(),event.getSlot(),clickType,event);
                return;
            }else if(inventoryPlayer.getInventoryName().equals("edit_items")){
                invManager.getInventoryEditKitItemsManager().clickInventory(inventoryPlayer,
                        event.getCurrentItem(),event.getSlot(),clickType,event);
                return;
            }else if(inventoryPlayer.getInventoryName().startsWith("edit_position")){
                invManager.getInventoryEditPositionManager().clickInventory(inventoryPlayer,
                        event.getCurrentItem(),event.getSlot(),clickType,event);
                return;
            }

            event.setCancelled(true);
            if(event.getCurrentItem() == null || event.getSlotType() == null){
                return;
            }

            if(event.getClickedInventory().equals(player.getOpenInventory().getTopInventory())) {
                invManager.clickInventory(inventoryPlayer,event.getCurrentItem(),event.getSlot(),clickType);
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onChat(AsyncPlayerChatEvent event){
        Player player = event.getPlayer();
        InventoryEditManager invManager = plugin.getInventoryEditManager();
        InventoryPlayer inventoryPlayer = invManager.getInventoryPlayer(player);
        if(inventoryPlayer != null) {
            event.setCancelled(true);
            new BukkitRunnable(){
                @Override
                public void run() {
                    invManager.writeChat(inventoryPlayer, ChatColor.stripColor(event.getMessage()));
                }
            }.runTaskLater(plugin,1L);
        }
    }
}
