package pk.ajneb97.configs;

import org.bukkit.configuration.file.FileConfiguration;
import pk.ajneb97.PlayerKits2;
import pk.ajneb97.managers.KitItemManager;
import pk.ajneb97.model.inventory.ItemKitInventory;
import pk.ajneb97.model.inventory.KitInventory;
import pk.ajneb97.model.item.KitItem;
import pk.ajneb97.utils.OtherUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class InventoryConfigManager {

    private PlayerKits2 plugin;
    private CustomConfig configFile;


    public InventoryConfigManager(PlayerKits2 plugin){
        this.plugin = plugin;
        this.configFile = new CustomConfig("inventory.yml",plugin,null, false);
        this.configFile.registerConfig();
        if(this.configFile.isFirstTime() && OtherUtils.isLegacy()){
            checkAndFix();
        }
    }

    public void checkAndFix(){
        FileConfiguration config = configFile.getConfig();
        config.set("inventories.main_inventory.0;1;7;8;9;17;36;44;45;46;52;53.item.id","STAINED_GLASS_PANE:15");
        config.set("inventories.premium_kits_inventory.0;1;7;8;9;17;36;44;45;46;52;53.item.id","STAINED_GLASS_PANE:4");
        config.set("inventories.buy_requirements_inventory.10.item.id","STAINED_GLASS_PANE:14");
        config.set("inventories.buy_requirements_inventory.16.item.id","STAINED_GLASS_PANE:5");
        configFile.saveConfig();
    }

    public void configure(){
        FileConfiguration config = configFile.getConfig();

        ArrayList<KitInventory> inventories = new ArrayList<KitInventory>();
        KitItemManager kitItemManager = plugin.getKitItemManager();
        if(config.contains("inventories")) {
            for(String key : config.getConfigurationSection("inventories").getKeys(false)) {
                int slots = Integer.valueOf(config.getString("inventories."+key+".slots"));
                String title = config.getString("inventories."+key+".title");

                List<ItemKitInventory> items = new ArrayList<>();
                for(String slotString : config.getConfigurationSection("inventories."+key).getKeys(false)) {
                    if(!slotString.equals("slots") && !slotString.equals("title")) {
                        String path = "inventories."+key+"."+slotString;
                        KitItem item = null;
                        if(config.contains(path+".item")){
                            item = kitItemManager.getKitItemFromConfig(config, path+".item");
                        }

                        String openInventory = config.contains(path+".open_inventory") ?
                                config.getString(path+".open_inventory") : null;

                        List<String> commands = config.contains(path+".click_commands") ?
                                config.getStringList(path+".click_commands") : null;

                        String type = config.contains(path+".type") ?
                                config.getString(path+".type") : null;

                        ItemKitInventory itemCraft = new ItemKitInventory(slotString,item,openInventory,commands,type);
                        items.add(itemCraft);
                    }
                }

                KitInventory inv = new KitInventory(key,slots,title,items);
                inventories.add(inv);
            }
        }

        plugin.getInventoryManager().setInventories(inventories);
    }

    public void saveKitItemOnConfig(String inventoryName,int slot,String kitName){
        FileConfiguration config = configFile.getConfig();
        config.set("inventories."+inventoryName+"."+slot+".type","kit: "+kitName);

        configFile.saveConfig();
    }

    public void save(){
        FileConfiguration config = configFile.getConfig();
        config.set("inventories",null);

        KitItemManager kitItemManager = plugin.getKitItemManager();
        for(KitInventory kitInventory : plugin.getInventoryManager().getInventories()){
            String name = kitInventory.getName();
            config.set("inventories."+name+".slots",kitInventory.getSlots());
            config.set("inventories."+name+".title",kitInventory.getTitle());
            for(ItemKitInventory item : kitInventory.getItems()){
                String slotsString = item.getSlotsString();
                String path = "inventories."+name+"."+slotsString;
                if(item.getType() != null){
                    config.set(path+".type",item.getType());
                }
                if(item.getItem() != null){
                    kitItemManager.saveKitItemOnConfig(item.getItem(),config,path+".item");
                }
                if(item.getOpenInventory() != null){
                    config.set(path+".open_inventory",item.getOpenInventory());
                }
                if(item.getCommands() != null && !item.getCommands().isEmpty()){
                    config.set(path+".click_commands",item.getCommands());
                }
            }
        }

        configFile.saveConfig();
    }

    public boolean reloadConfig(){
        if(!configFile.reloadConfig()){
            return false;
        }
        configure();
        return true;
    }

    public FileConfiguration getConfig(){
        return configFile.getConfig();
    }

    public void checkUpdate(){
        Path pathConfig = Paths.get(configFile.getRoute());
        try{
            String text = new String(Files.readAllBytes(pathConfig));
            /*
            if(!text.contains("register_commands:")){
                List<String> commands = new ArrayList<>();
                getConfig().set("Config.register_commands", commands);
                saveConfig();
            }
            */
        }catch(IOException e){
            e.printStackTrace();
        }
    }

}
