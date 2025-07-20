package pk.ajneb97.configs;

import org.bukkit.configuration.file.FileConfiguration;
import pk.ajneb97.PlayerKits2;
import pk.ajneb97.configs.model.CommonConfig;
import pk.ajneb97.managers.KitItemManager;
import pk.ajneb97.model.Kit;
import pk.ajneb97.model.KitAction;
import pk.ajneb97.model.KitRequirements;
import pk.ajneb97.model.item.KitItem;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class KitsConfigManager extends DataFolderConfigManager{

    public KitsConfigManager(PlayerKits2 plugin, String folderName){
        super(plugin, folderName);
    }

    @Override
    public void createFiles() {
        new CommonConfig("diamond.yml",plugin,folderName,false).registerConfig();
        new CommonConfig("iron.yml",plugin,folderName,false).registerConfig();
        new CommonConfig("food.yml",plugin,folderName,false).registerConfig();
    }

    @Override
    public void loadConfigs(){
        ArrayList<Kit> kits = new ArrayList<>();

        ArrayList<CommonConfig> configFiles = getConfigs();
        for(CommonConfig configFile : configFiles){
            FileConfiguration config = configFile.getConfig();

            String name = configFile.getPath().replace(".yml","");
            Kit kit = getKitFromConfig(config,plugin,name,"");
            kits.add(kit);
        }

        plugin.getKitsManager().setKits(kits);
    }

    @Override
    public void saveConfigs(){

    }

    public void saveConfig(Kit kit){
        String kitName = kit.getName();
        CommonConfig kitConfig = getConfigFile(kitName+".yml");

        FileConfiguration config = kitConfig.getConfig();

        config.set("cooldown",kit.getCooldown());
        config.set("one_time",kit.isOneTime());
        config.set("auto_armor",kit.isAutoArmor());
        config.set("permission_required",kit.isPermissionRequired());
        config.set("clear_inventory",kit.isClearInventory());
        config.set("custom_permission",kit.getCustomPermission());
        config.set("save_original_items",kit.isSaveOriginalItems());
        config.set("allow_placeholders_on_original_items",kit.isAllowPlaceholdersOnOriginalItems());

        KitItemManager kitItemManager = plugin.getKitItemManager();
        int currentPos = 1;
        config.set("items",null);
        for(KitItem kitItem : kit.getItems()){
            kitItemManager.saveKitItemOnConfig(kitItem,config,"items."+currentPos);
            currentPos++;
        }


        config.set("actions",null);
        saveActions(kit.getClaimActions(),"claim",config,kitItemManager);
        saveActions(kit.getErrorActions(),"error",config,kitItemManager);

        config.set("display.default",null);
        if(kit.getDisplayItemDefault() != null){
            kitItemManager.saveKitItemOnConfig(kit.getDisplayItemDefault(),config,"display.default");
        }
        config.set("display.no_permission",null);
        if(kit.getDisplayItemNoPermission() != null){
            kitItemManager.saveKitItemOnConfig(kit.getDisplayItemNoPermission(),config,"display.no_permission");
        }
        config.set("display.cooldown",null);
        if(kit.getDisplayItemCooldown() != null){
            kitItemManager.saveKitItemOnConfig(kit.getDisplayItemCooldown(),config,"display.cooldown");
        }
        config.set("display.one_time",null);
        if(kit.getDisplayItemOneTime() != null){
            kitItemManager.saveKitItemOnConfig(kit.getDisplayItemOneTime(),config,"display.one_time");
        }
        config.set("display.one_time_requirements",null);
        if(kit.getDisplayItemOneTimeRequirements() != null){
            kitItemManager.saveKitItemOnConfig(kit.getDisplayItemOneTimeRequirements(),config,"display.one_time_requirements");
        }

        KitRequirements requirements = kit.getRequirements();
        if(requirements != null){
            config.set("requirements.one_time_requirements",requirements.isOneTimeRequirements());
            if(requirements.getExtraRequirements() != null){
                config.set("requirements.extra_requirements",requirements.getExtraRequirements());
            }
            config.set("requirements.message",requirements.getMessage());
            if(requirements.getActionsOnBuy() != null){
                config.set("requirements.actions_on_buy",requirements.getActionsOnBuy());
            }
            if(requirements.getPrice() != 0){
                config.set("requirements.price",requirements.getPrice());
            }
        }

        kitConfig.saveConfig();
    }

    public void saveActions(ArrayList<KitAction> kitActions,String actionType,FileConfiguration config,KitItemManager kitItemManager){
        int currentPos = 1;
        for(KitAction kitAction : kitActions){
            String path = "actions."+actionType+"."+currentPos;
            config.set(path+".action",kitAction.getAction());
            config.set(path+".execute_before_items",kitAction.isExecuteBeforeItems());
            config.set(path+".count_as_item",kitAction.isCountAsItem());
            if(kitAction.getDisplayItem() != null){
                kitItemManager.saveKitItemOnConfig(kitAction.getDisplayItem(),config,path+".display_item");
            }

            currentPos++;
        }
    }

    public void removeKitFile(String kitName){
        File file = new File(plugin.getDataFolder()+File.separator+folderName,kitName+".yml");
        file.delete();
    }

    // mainPath must include a "." at the end
    public static Kit getKitFromConfig(FileConfiguration config, PlayerKits2 plugin, String name, String mainPath){
        KitItemManager kitItemManager = plugin.getKitItemManager();
        int cooldown = config.contains(mainPath+"cooldown") ? config.getInt(mainPath+"cooldown") : 0;
        boolean permissionRequired = config.contains(mainPath+"permission_required") ? config.getBoolean(mainPath+"permission_required") : false;
        String customPermission = config.contains(mainPath+"custom_permission") ? config.getString(mainPath+"custom_permission") : null;
        boolean autoArmor = config.contains(mainPath+"auto_armor") ? config.getBoolean(mainPath+"auto_armor") : false;
        boolean oneTime = config.contains(mainPath+"one_time") ? config.getBoolean(mainPath+"one_time") : false;
        boolean clearInventory = config.contains(mainPath+"clear_inventory") ? config.getBoolean(mainPath+"clear_inventory") : false;
        boolean saveOriginalItems = config.contains(mainPath+"save_original_items") ? config.getBoolean(mainPath+"save_original_items") : false;
        boolean allowPlaceholdersOnOriginalItems = config.contains(mainPath+"allow_placeholders_on_original_items") ? config.getBoolean(mainPath+"allow_placeholders_on_original_items") : false;

        ArrayList<KitItem> items = new ArrayList<>();
        if(config.contains(mainPath+"items")){
            for(String key : config.getConfigurationSection(mainPath+"items").getKeys(false)){
                KitItem item = kitItemManager.getKitItemFromConfig(config,mainPath+"items."+key);
                items.add(item);
            }
        }
        ArrayList<KitAction> claimActions = getActions(config,"claim",mainPath,kitItemManager);
        ArrayList<KitAction> errorActions = getActions(config,"error",mainPath,kitItemManager);

        KitItem displayItemDefault = kitItemManager.getKitItemFromConfig(config,mainPath+"display.default");
        KitItem displayItemNoPermission = config.contains(mainPath+"display.no_permission") ?
                kitItemManager.getKitItemFromConfig(config,mainPath+"display.no_permission") : null;
        KitItem displayItemCooldown = config.contains(mainPath+"display.cooldown") ?
                kitItemManager.getKitItemFromConfig(config,mainPath+"display.cooldown") : null;
        KitItem displayItemOneTime = config.contains(mainPath+"display.one_time") ?
                kitItemManager.getKitItemFromConfig(config,mainPath+"display.one_time") : null;
        KitItem displayItemOneTimeRequirements = config.contains(mainPath+"display.one_time_requirements") ?
                kitItemManager.getKitItemFromConfig(config,mainPath+"display.one_time_requirements") : null;

        KitRequirements kitRequirements = null;
        if(config.contains("requirements")){
            boolean oneTimeRequirements = config.getBoolean("requirements.one_time_requirements");
            List<String> extraRequirements = config.getStringList("requirements.extra_requirements");
            List<String> message = config.getStringList("requirements.message");
            List<String> actionsOnBuy = config.getStringList("requirements.actions_on_buy");
            double price = config.contains("requirements.price") ? config.getDouble("requirements.price") : 0;
            kitRequirements = new KitRequirements(oneTimeRequirements,extraRequirements,message,actionsOnBuy,price);
        }


        Kit kit = new Kit(name);
        kit.setCooldown(cooldown);

        kit.setAutoArmor(autoArmor);
        kit.setOneTime(oneTime);
        kit.setPermissionRequired(permissionRequired);
        kit.setClearInventory(clearInventory);
        kit.setCustomPermission(customPermission);
        kit.setItems(items);
        kit.setClaimActions(claimActions);
        kit.setErrorActions(errorActions);
        kit.setDisplayItemDefault(displayItemDefault);
        kit.setDisplayItemNoPermission(displayItemNoPermission);
        kit.setDisplayItemCooldown(displayItemCooldown);
        kit.setDisplayItemOneTime(displayItemOneTime);
        kit.setDisplayItemOneTimeRequirements(displayItemOneTimeRequirements);
        kit.setRequirements(kitRequirements);
        kit.setSaveOriginalItems(saveOriginalItems);
        kit.setAllowPlaceholdersOnOriginalItems(allowPlaceholdersOnOriginalItems);

        return kit;
    }

    public static ArrayList<KitAction> getActions(FileConfiguration config,String actionType,String mainPath,KitItemManager kitItemManager){
        ArrayList<KitAction> actions = new ArrayList<>();
        if(config.contains(mainPath+"actions."+actionType)){
            for(String key : config.getConfigurationSection(mainPath+"actions."+actionType).getKeys(false)){
                String path = mainPath+"actions."+actionType+"."+key;
                String action = config.getString(path+".action");
                boolean executeBeforeItem = config.contains(path+".execute_before_items") ? config.getBoolean(path+".execute_before_items") : false;
                boolean countAsItem = config.contains(path+".count_as_item") ? config.getBoolean(path+".count_as_item") : false;
                KitItem item = config.contains(path+".display_item") ? kitItemManager.getKitItemFromConfig(config,path+".display_item") : null;

                actions.add(new KitAction(action,item,executeBeforeItem,countAsItem));
            }
        }
        return actions;
    }
}
