package pk.ajneb97.managers;

import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import pk.ajneb97.PlayerKits2;
import pk.ajneb97.model.Kit;
import pk.ajneb97.model.KitRequirements;
import pk.ajneb97.model.internal.GiveKitInstructions;
import pk.ajneb97.model.internal.PlayerKitsMessageResult;
import pk.ajneb97.model.inventory.InventoryPlayer;
import pk.ajneb97.utils.PlayerUtils;

import java.util.ArrayList;
import java.util.List;

public class InventoryRequirementsManager {

    private PlayerKits2 plugin;
    private InventoryManager inventoryManager;
    public InventoryRequirementsManager(PlayerKits2 plugin, InventoryManager inventoryManager){
        this.plugin = plugin;
        this.inventoryManager = inventoryManager;
    }

    public void configureRequirementsItem(ItemStack item, String kitName, Player player){
        if(item.hasItemMeta()) {
            ItemMeta meta = item.getItemMeta();
            if(!meta.hasLore()){
                return;
            }
            List<String> lore = new ArrayList<>();
            for(String line : meta.getLore()){
                if(line.equals("%kit_requirements_message%")){
                    lore.addAll(replaceRequirementsMessageVariable(kitName,player));
                }else{
                    lore.add(line);
                }
            }
            meta.setLore(lore);
            item.setItemMeta(meta);
        }
    }

    public List<String> replaceRequirementsMessageVariable(String kitName, Player player){
        Kit kit = plugin.getKitsManager().getKitByName(kitName);
        KitRequirements requirements = kit.getRequirements();

        MessagesManager msgManager = plugin.getMessagesManager();
        List<String> requirementsMessage = new ArrayList<>(requirements.getMessage());
        List<String> extraRequirements = requirements.getExtraRequirements();
        List<Boolean> requirementsBooleans = new ArrayList<>();

        boolean passPrice = plugin.getKitsManager().passPrice(requirements.getPrice(),player);
        String priceRequirementStatusSymbol =
                passPrice ? msgManager.getRequirementsMessageStatusSymbolTrue()
                        : msgManager.getRequirementsMessageStatusSymbolFalse();

        boolean isPlaceholderAPI = plugin.getDependencyManager().isPlaceholderAPI();
        for(String condition : extraRequirements) {
            boolean pass = false;
            if(isPlaceholderAPI) {
                pass = PlayerUtils.passCondition(player, condition);
            }
            requirementsBooleans.add(pass);
        }

        for(int i=0;i<requirementsMessage.size();i++) {
            String line = requirementsMessage.get(i);
            if(line.contains("%status_symbol_requirement_")) {
                int pos = line.indexOf("%status_symbol_requirement_");
                int lastPos = line.indexOf("%", pos+1)+1;
                String variable = line.substring(pos, lastPos);
                int posNumber = Integer.valueOf(variable.replace("%status_symbol_requirement_","").replace("%", ""));
                boolean passCondition = requirementsBooleans.get(posNumber-1);
                if(passCondition) {
                    line = line.replace(variable, msgManager.getRequirementsMessageStatusSymbolTrue());
                }else {
                    line = line.replace(variable, msgManager.getRequirementsMessageStatusSymbolFalse());
                }
            }

            //Replace price variable
            line = line.replace("%status_symbol_price%",priceRequirementStatusSymbol);

            if(isPlaceholderAPI) {
                line = PlaceholderAPI.setPlaceholders(player, line);
            }

            line = MessagesManager.getColoredMessage(line);
            requirementsMessage.set(i, line);
        }

        return requirementsMessage;
    }

    public void requirementsInventoryBuy(InventoryPlayer inventoryPlayer){
        Kit kit = plugin.getKitsManager().getKitByName(inventoryPlayer.getKitName());
        KitRequirements requirements = kit.getRequirements();
        DependencyManager dependencyManager = plugin.getDependencyManager();
        boolean isPlaceholderAPI = dependencyManager.isPlaceholderAPI();
        Player player = inventoryPlayer.getPlayer();
        FileConfiguration messages = plugin.getConfigsManager().getMessagesConfigManager().getConfig();
        MessagesManager msgManager = plugin.getMessagesManager();

        //Buy - Unlock
        KitsManager kitsManager = plugin.getKitsManager();
        PlayerKitsMessageResult result = kitsManager.giveKit(player,inventoryPlayer.getKitName(),
                new GiveKitInstructions(false,true,false,false));
        if(!result.isError()){
            //Everything is good, open previous inventory.
            requirementsInventoryCancel(inventoryPlayer);
        }else{
            msgManager.sendMessage(player, result.getMessage(), true);
        }
    }

    public void requirementsInventoryCancel(InventoryPlayer inventoryPlayer){
        inventoryPlayer.setInventoryName(inventoryPlayer.getPreviousInventoryName());
        inventoryPlayer.setKitName(null);
        inventoryManager.openInventory(inventoryPlayer);
    }


}
