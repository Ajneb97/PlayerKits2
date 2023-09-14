package pk.ajneb97.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import pk.ajneb97.PlayerKits2;
import pk.ajneb97.managers.InventoryManager;
import pk.ajneb97.managers.MessagesManager;
import pk.ajneb97.model.inventory.InventoryPlayer;

public class PlayerListener implements Listener {

    private PlayerKits2 plugin;
    public PlayerListener(PlayerKits2 plugin){
        this.plugin = plugin;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event){
        Player player = event.getPlayer();
        plugin.getPlayerDataManager().manageJoin(player);

        //Update notification
        String latestVersion = plugin.getUpdateCheckerManager().getLatestVersion();
        if(player.isOp() && plugin.getConfigsManager().getMainConfigManager().isUpdateNotify() && !plugin.version.equals(latestVersion)){
            player.sendMessage(MessagesManager.getColoredMessage(plugin.prefix+"&cThere is a new version available. &e(&7"+latestVersion+"&e)"));
            player.sendMessage(MessagesManager.getColoredMessage("&cYou can download it at: &ahttps://modrinth.com/plugin/playerkits-2"));
        }
    }

    @EventHandler
    public void closeInventory(InventoryCloseEvent event){
        Player player = (Player) event.getPlayer();
        plugin.getInventoryManager().removeInventoryPlayer(player);
    }

    @EventHandler
    public void clickInventory(InventoryClickEvent event){
        Player player = (Player) event.getWhoClicked();
        InventoryManager invManager = plugin.getInventoryManager();
        InventoryPlayer inventoryPlayer = invManager.getInventoryPlayer(player);
        if(inventoryPlayer != null) {
            event.setCancelled(true);
            if(event.getCurrentItem() == null || event.getSlotType() == null){
                return;
            }

            if(event.getClickedInventory().equals(player.getOpenInventory().getTopInventory())) {
                ClickType clickType = event.getClick();
                invManager.clickInventory(inventoryPlayer,event.getCurrentItem(),clickType);
            }
        }
    }
}
