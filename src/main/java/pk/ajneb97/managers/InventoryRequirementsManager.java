package pk.ajneb97.managers;

import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import pk.ajneb97.PlayerKits2;
import pk.ajneb97.model.Kit;
import pk.ajneb97.model.KitRequirements;
import pk.ajneb97.model.internal.GiveKitInstructions;
import pk.ajneb97.model.internal.PlayerKitsMessageResult;
import pk.ajneb97.model.inventory.InventoryPlayer;
import pk.ajneb97.utils.MiniMessageUtils;
import pk.ajneb97.utils.PlayerUtils;

import java.util.ArrayList;
import java.util.List;

public class InventoryRequirementsManager {

    private final PlayerKits2 plugin;
    private final InventoryManager inventoryManager;
    public InventoryRequirementsManager(PlayerKits2 plugin, InventoryManager inventoryManager){
        this.plugin = plugin;
        this.inventoryManager = inventoryManager;
    }

    @SuppressWarnings("deprecation")
    public void configureRequirementsItem(ItemStack item, String kitName, Player player){
        if(item.hasItemMeta()) {
            ItemMeta meta = item.getItemMeta();
            if(!meta.hasLore()){
                return;
            }

            if(plugin.getConfigsManager().getMainConfigManager().isUseMiniMessage()){
                MiniMessageUtils.setRequirementsMessage(meta,kitName,player,this);
            }else{
                List<String> lore = new ArrayList<>();
                List<String> metaLore = meta.getLore();
                for(String line : metaLore != null ? metaLore : new ArrayList<String>()){
                    if(line.equals("%kit_requirements_message%")){
                        List<String> message = replaceRequirementsMessageVariable(kitName,player);
                        for(String m : message){
                            lore.add(MessagesManager.getLegacyColoredMessage(m));
                        }
                    }else{
                        lore.add(line);
                    }
                }
                meta.setLore(lore);
            }
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
                int posNumber = Integer.parseInt(variable.replace("%status_symbol_requirement_","").replace("%", ""));
                boolean passCondition = requirementsBooleans.get(posNumber-1);
                if(passCondition) {
                    line = line.replace(variable, msgManager.getRequirementsMessageStatusSymbolTrue());
                }else {
                    line = line.replace(variable, msgManager.getRequirementsMessageStatusSymbolFalse());
                }
            }

            line = line.replace("%status_symbol_price%",priceRequirementStatusSymbol);

            if(isPlaceholderAPI) {
                line = PlaceholderAPI.setPlaceholders(player, line);
            }

            requirementsMessage.set(i, line);
        }

        return requirementsMessage;
    }

    public void requirementsInventoryBuy(InventoryPlayer inventoryPlayer){
        Player player = inventoryPlayer.getPlayer();
        String kitName = inventoryPlayer.getKitName();
        if(player == null || kitName == null){
            return;
        }

        if(inventoryManager.cannotBeginClaim(player)){
            return;
        }

        plugin.getPlatformScheduler().runForEntityLater(player,
                () -> performPurchase(inventoryPlayer, kitName),
                () -> inventoryManager.endClaim(player), 1L);
    }

    private void performPurchase(InventoryPlayer inventoryPlayer, String kitName){
        Player player = inventoryPlayer.getPlayer();
        try{
            performPurchaseInternal(inventoryPlayer, kitName);
        }finally{
            if(player != null){
                inventoryManager.endClaim(player);
            }
        }
    }

    private void performPurchaseInternal(InventoryPlayer inventoryPlayer, String kitName){
        Player player = inventoryPlayer.getPlayer();
        if(player == null || !player.isOnline()){
            return;
        }

        MessagesManager msgManager = plugin.getMessagesManager();
        PlayerKitsMessageResult result = plugin.getKitsManager().giveKit(player, kitName,
                new GiveKitInstructions(false,true,false,false));
        inventoryManager.syncInventory(player);

        if(result.isError()){
            msgManager.sendMessage(player, result.getMessage(), true);
            return;
        }

        if(plugin.getConfigsManager().getMainConfigManager().isCloseInventoryOnClaim()){
            player.closeInventory();
            return;
        }

        requirementsInventoryCancel(inventoryPlayer);
    }

    public void requirementsInventoryCancel(InventoryPlayer inventoryPlayer){
        String previous = inventoryPlayer.getPreviousInventoryName();
        if(previous == null || inventoryManager.getInventory(previous) == null){
            inventoryManager.closeLater(inventoryPlayer.getPlayer());
            return;
        }
        inventoryPlayer.setInventoryName(previous);
        inventoryPlayer.setKitName(null);
        inventoryManager.openInventoryLater(inventoryPlayer);
    }

}
