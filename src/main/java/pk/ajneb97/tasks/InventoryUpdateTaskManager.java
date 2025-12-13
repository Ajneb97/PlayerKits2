package pk.ajneb97.tasks;

import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import pk.ajneb97.PlayerKits2;
import pk.ajneb97.managers.*;
import pk.ajneb97.model.inventory.InventoryPlayer;
import pk.ajneb97.utils.InventoryUtils;
import pk.ajneb97.utils.ItemUtils;

import java.util.ArrayList;

public class InventoryUpdateTaskManager {

    private PlayerKits2 plugin;
    private BukkitRunnable task; // Store reference to the task
    private boolean running; // Track if task is running

    public InventoryUpdateTaskManager(PlayerKits2 plugin){
        this.plugin = plugin;
        this.running = false;
        this.task = null;
    }

    public void start(){
        if (running) {
            plugin.getLogger().warning("InventoryUpdateTaskManager is already running!");
            return;
        }

        task = new BukkitRunnable(){
            @Override
            public void run() {
                execute();
            }
        };

        task.runTaskTimer(plugin, 0L, 20L);
        running = true;
        plugin.getLogger().info("InventoryUpdateTaskManager started.");
    }

    public void stop(){
        if (!running || task == null) {
            plugin.getLogger().warning("InventoryUpdateTaskManager is not running!");
            return;
        }

        try {
            task.cancel();
            running = false;
            task = null;
            plugin.getLogger().info("InventoryUpdateTaskManager stopped.");
        } catch (IllegalStateException e) {
            plugin.getLogger().warning("Failed to stop InventoryUpdateTaskManager: " + e.getMessage());
        }
    }

    public boolean isRunning() {
        return running;
    }

    public void execute(){
        InventoryManager inventoryManager = plugin.getInventoryManager();
        KitsManager kitsManager = plugin.getKitsManager();
        PlayerDataManager playerDataManager = plugin.getPlayerDataManager();
        KitItemManager kitItemManager = plugin.getKitItemManager();

        ArrayList<InventoryPlayer> players = inventoryManager.getPlayers();
        for(InventoryPlayer player : players){
            Inventory inv = InventoryUtils.getTopInventory(player.getPlayer());
            if(inv == null){
                continue;
            }
            ItemStack[] contents = inv.getContents();
            for(int i=0;i<contents.length;i++){
                ItemStack item = contents[i];
                if(item == null || item.getType().equals(Material.AIR)){
                    continue;
                }

                String kitName = ItemUtils.getTagStringItem(plugin,item,"playerkits_kit");
                if(kitName != null){
                    inventoryManager.setKit(kitName,player.getPlayer(),inv,i,kitsManager,
                            playerDataManager,kitItemManager,item);
                }
            }
        }
    }
}