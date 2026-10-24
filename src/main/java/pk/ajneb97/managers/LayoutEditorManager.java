package pk.ajneb97.managers;

import org.bukkit.Bukkit;
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
import pk.ajneb97.model.inventory.InventoryPlayer;
import pk.ajneb97.model.item.KitItem;
import pk.ajneb97.model.layout.LayoutEditSession;
import pk.ajneb97.model.layout.PlayerKitLayout;
import pk.ajneb97.utils.InventoryItem;
import pk.ajneb97.utils.InventoryUtils;
import pk.ajneb97.utils.MiniMessageUtils;
import pk.ajneb97.utils.OtherUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class LayoutEditorManager {

    public static final String INVENTORY_NAME = "layout_editor";
    public static final int REGION_SIZE = 36;
    private static final int GUI_SIZE = 54;
    private static final int SLOT_BACK = 45;
    private static final int SLOT_INFO = 49;
    private static final int SLOT_RESET = 51;
    private static final int SLOT_SAVE = 53;

    private PlayerKits2 plugin;
    private Map<UUID, LayoutEditSession> sessions;

    public LayoutEditorManager(PlayerKits2 plugin){
        this.plugin = plugin;
        this.sessions = new HashMap<>();
    }

    public static int guiSlotToInventorySlot(int guiSlot){
        if(guiSlot < 0 || guiSlot >= REGION_SIZE){
            return -1;
        }
        return guiSlot < 27 ? guiSlot+9 : guiSlot-27;
    }

    public static int inventorySlotToGuiSlot(int inventorySlot){
        if(inventorySlot < 0 || inventorySlot > PlayerKitLayout.MAX_SLOT){
            return -1;
        }
        return inventorySlot < 9 ? inventorySlot+27 : inventorySlot-9;
    }

    public boolean isEditing(InventoryPlayer inventoryPlayer){
        return INVENTORY_NAME.equals(inventoryPlayer.getInventoryName());
    }

    public void endSession(Player player){
        sessions.remove(player.getUniqueId());
    }

    public void clearSessions(){
        sessions.clear();
    }

    public void openFromInventory(InventoryPlayer inventoryPlayer){
        String kitName = inventoryPlayer.getKitName();
        if(kitName == null){
            FileConfiguration messagesConfig = plugin.getConfigsManager().getMessagesConfigManager().getConfig();
            plugin.getMessagesManager().sendMessage(inventoryPlayer.getPlayer(),messagesConfig.getString("layoutNoKitSelected"),true);
            return;
        }
        open(inventoryPlayer,kitName);
    }

    public void open(InventoryPlayer inventoryPlayer, String kitName){
        Player player = inventoryPlayer.getPlayer();
        FileConfiguration messagesConfig = plugin.getConfigsManager().getMessagesConfigManager().getConfig();
        MessagesManager msgManager = plugin.getMessagesManager();

        if(!plugin.getConfigsManager().getMainConfigManager().isLayoutEditor()){
            msgManager.sendMessage(player,messagesConfig.getString("layoutDisabled"),true);
            return;
        }

        Kit kit = plugin.getKitsManager().getKitByName(kitName);
        if(kit == null){
            msgManager.sendMessage(player,messagesConfig.getString("kitDoesNotExists").replace("%kit%",kitName),true);
            return;
        }

        if(!kit.playerHasPermission(player)){
            msgManager.sendMessage(player,messagesConfig.getString("kitNoPermissions"),true);
            return;
        }

        List<KitItem> items = kit.getItems();
        if(positionableCount(items) == 0){
            msgManager.sendMessage(player,messagesConfig.getString("layoutNoItems"),true);
            return;
        }

        PlayerKitLayout existing = plugin.getPlayerDataManager().getLayout(player,kit.getName());
        LayoutEditSession session = new LayoutEditSession(kit.getName(),inventoryPlayer.getInventoryName(),
                items.size(),existing,defaultSlots(items));
        sessions.put(player.getUniqueId(),session);
        inventoryPlayer.setKitName(kit.getName());

        render(inventoryPlayer,session,kit,items,true);
    }

    private static boolean isPositionable(KitItem kitItem){
        return kitItem != null && !kitItem.isOffhand();
    }

    private static int positionableCount(List<KitItem> items){
        int count = 0;
        for(KitItem kitItem : items){
            if(isPositionable(kitItem)){
                count++;
            }
        }
        return count;
    }

    private int[] defaultSlots(List<KitItem> items){
        int[] defaults = new int[items.size()];
        int next = 0;
        for(int i=0;i<items.size();i++){
            if(!isPositionable(items.get(i)) || next > PlayerKitLayout.MAX_SLOT){
                defaults[i] = PlayerKitLayout.UNSET;
                continue;
            }
            defaults[i] = next;
            next++;
        }
        return defaults;
    }

    private void render(InventoryPlayer inventoryPlayer, LayoutEditSession session, Kit kit, List<KitItem> items, boolean openNow){
        Player player = inventoryPlayer.getPlayer();
        MainConfigManager mainConfigManager = plugin.getConfigsManager().getMainConfigManager();

        Inventory inv;
        if(openNow){
            String title = mainConfigManager.getLayoutEditorTitle().replace("%kit%",kit.getName());
            if(mainConfigManager.isUseMiniMessage()){
                inv = MiniMessageUtils.createInventory(GUI_SIZE,title);
            }else{
                inv = Bukkit.createInventory(null,GUI_SIZE,MessagesManager.getLegacyColoredMessage(title));
            }
            fillControls(inv,session);
        }else{
            inv = InventoryUtils.getTopInventory(player);
            if(inv == null || inv.getSize() != GUI_SIZE){
                return;
            }
            for(int i=0;i<REGION_SIZE;i++){
                inv.setItem(i,null);
            }
        }

        KitItemManager kitItemManager = plugin.getKitItemManager();
        for(int index=0;index<items.size();index++){
            if(!isPositionable(items.get(index))){
                continue;
            }
            int guiSlot = inventorySlotToGuiSlot(session.getSlot(index));
            if(guiSlot < 0){
                continue;
            }

            ItemStack item = kitItemManager.createItemFromKitItem(items.get(index),player,kit);
            if(item == null || item.getType() == Material.AIR){
                continue;
            }

            decorate(item,index == session.getSelectedIndex());
            inv.setItem(guiSlot,item);
        }

        if(openNow){
            player.openInventory(inv);
            inventoryPlayer.setInventoryName(INVENTORY_NAME);
            plugin.getInventoryManager().getPlayers().add(inventoryPlayer);
        }
    }

    private void decorate(ItemStack item, boolean selected){
        ItemMeta meta = item.getItemMeta();
        if(meta == null){
            return;
        }
        List<String> lore = meta.hasLore() ? new ArrayList<>(meta.getLore()) : new ArrayList<>();
        lore.add(" ");
        if(selected){
            lore.add(MessagesManager.getLegacyColoredMessage("&a&lSELECTED"));
            lore.add(MessagesManager.getLegacyColoredMessage("&7Click another slot to move it there."));
        }else{
            lore.add(MessagesManager.getLegacyColoredMessage("&eClick &7to pick this item up."));
        }
        meta.setLore(lore);
        item.setItemMeta(meta);
    }

    private void fillControls(Inventory inv, LayoutEditSession session){
        boolean legacy = OtherUtils.isLegacy();
        Material filler = legacy ? Material.valueOf("STAINED_GLASS_PANE") : Material.BLACK_STAINED_GLASS_PANE;
        for(int i=REGION_SIZE;i<GUI_SIZE;i++){
            InventoryItem item = new InventoryItem(inv,i,filler).name(" ");
            if(legacy){
                item.dataValue((short) 15);
            }
            item.ready();
        }

        new InventoryItem(inv,SLOT_BACK,Material.ARROW).name("&eGo Back")
                .lore(new ArrayList<>(Arrays.asList("&7Discards any unsaved changes."))).ready();

        List<String> infoLore = new ArrayList<>();
        infoLore.add("&7This is your personal layout for");
        infoLore.add("&a"+session.getKitName()+"&7.");
        infoLore.add("");
        infoLore.add("&7The grid above mirrors your inventory,");
        infoLore.add("&7with the hotbar on the bottom row.");
        infoLore.add("&7Click an item to pick it up, then click");
        infoLore.add("&7another slot to place or swap it.");
        infoLore.add("");
        infoLore.add("&7The original kit is never modified and");
        infoLore.add("&7only you see this layout.");
        infoLore.add("");
        infoLore.add("&8Armor may still be auto-equipped and");
        infoLore.add("&8occupied slots fall back to the default.");
        new InventoryItem(inv,SLOT_INFO,Material.COMPASS).name("&6&lInfo").lore(infoLore).ready();

        new InventoryItem(inv,SLOT_RESET,Material.BARRIER).name("&c&lReset Layout")
                .lore(new ArrayList<>(Arrays.asList("&7Restores the default kit layout."))).ready();

        new InventoryItem(inv,SLOT_SAVE,Material.EMERALD_BLOCK).name("&a&lSave Layout")
                .lore(new ArrayList<>(Arrays.asList("&7Stores this layout for your account."))).ready();
    }

    public void clickInventory(InventoryPlayer inventoryPlayer, int slot, ClickType clickType){
        Player player = inventoryPlayer.getPlayer();
        LayoutEditSession session = sessions.get(player.getUniqueId());
        if(session == null){
            player.closeInventory();
            return;
        }

        Kit kit = plugin.getKitsManager().getKitByName(session.getKitName());
        if(kit == null){
            endSession(player);
            player.closeInventory();
            return;
        }

        FileConfiguration messagesConfig = plugin.getConfigsManager().getMessagesConfigManager().getConfig();
        MessagesManager msgManager = plugin.getMessagesManager();
        List<KitItem> items = kit.getItems();

        if(slot >= 0 && slot < REGION_SIZE){
            handleRegionClick(session,slot);
            render(inventoryPlayer,session,kit,items,false);
            return;
        }

        switch(slot){
            case SLOT_SAVE:
                saveSession(player,session,items);
                msgManager.sendMessage(player,messagesConfig.getString("layoutSaved").replace("%kit%",session.getKitName()),true);
                closeToPrevious(inventoryPlayer,session);
                break;
            case SLOT_RESET:
                session.reset(defaultSlots(items));
                plugin.getPlayerDataManager().clearLayout(player,session.getKitName());
                msgManager.sendMessage(player,messagesConfig.getString("layoutReset").replace("%kit%",session.getKitName()),true);
                render(inventoryPlayer,session,kit,items,false);
                break;
            case SLOT_BACK:
                closeToPrevious(inventoryPlayer,session);
                break;
            default:
                break;
        }
    }

    private void handleRegionClick(LayoutEditSession session, int guiSlot){
        int inventorySlot = guiSlotToInventorySlot(guiSlot);
        if(inventorySlot < 0){
            return;
        }

        int occupant = session.getIndexAtSlot(inventorySlot);
        int selected = session.getSelectedIndex();

        if(selected == -1){
            if(occupant != -1){
                session.setSelectedIndex(occupant);
            }
            return;
        }

        if(occupant != selected){
            session.moveTo(selected,inventorySlot);
        }
        session.setSelectedIndex(-1);
    }

    private void saveSession(Player player, LayoutEditSession session, List<KitItem> items){
        if(session.matchesDefaults(defaultSlots(items))){
            plugin.getPlayerDataManager().clearLayout(player,session.getKitName());
            return;
        }
        plugin.getPlayerDataManager().saveLayout(player,session.toLayout());
    }

    private void closeToPrevious(InventoryPlayer inventoryPlayer, LayoutEditSession session){
        Player player = inventoryPlayer.getPlayer();
        endSession(player);

        String previous = session.getReturnInventoryName();
        if(previous == null || plugin.getInventoryManager().getInventory(previous) == null){
            player.closeInventory();
            return;
        }

        inventoryPlayer.setInventoryName(previous);
        plugin.getInventoryManager().openInventory(inventoryPlayer);
    }
}
