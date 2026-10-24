package pk.ajneb97.utils;

import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import pk.ajneb97.PlayerKits2;


public class PlayerUtils {

    public static ItemStack[] getAllInventoryContents(Player player){
        ServerVersion serverVersion = PlayerKits2.serverVersion;
        if(!serverVersion.serverVersionGreaterEqualThan(serverVersion,ServerVersion.v1_9_R1)) {
            ItemStack[] contents = new ItemStack[40];

            int slot = 0;
            ItemStack[] normalContents = player.getInventory().getContents();
            for(ItemStack item : normalContents){
                contents[slot] = item;
                slot++;
            }
            ItemStack[] armorContents = player.getInventory().getArmorContents();
            for(ItemStack item : armorContents){
                contents[slot] = item;
                slot++;
            }
            return contents;
        }else {
            return player.getInventory().getContents();
        }
    }

    public static int getUsedSlots(Player player){

        ServerVersion serverVersion = PlayerKits2.serverVersion;
        ItemStack[] contents = serverVersion.serverVersionGreaterEqualThan(serverVersion,ServerVersion.v1_9_R1)
                ? player.getInventory().getStorageContents() : player.getInventory().getContents();

        int usedSlots = 0;
        for(ItemStack content : contents) {
            if(content != null && !content.getType().equals(Material.AIR)) {
                usedSlots++;
            }
        }

        return usedSlots;
    }

    public static boolean isPlayerKitsAdmin(CommandSender sender){
        return sender.hasPermission("playerkits.admin");
    }

    public static boolean lacksCooldownBypassPermission(CommandSender sender){
        return !sender.hasPermission("playerkits.bypass.cooldown");
    }

    public static boolean lacksOneTimeBypassPermission(CommandSender sender){
        return !sender.hasPermission("playerkits.bypass.onetime");
    }

    public static boolean passCondition(Player player,String condition) {
        if(condition == null){
            return false;
        }

        String[] sep = condition.split(" ");
        if(sep.length < 3){
            return false;
        }

        String variable = PlaceholderAPI.setPlaceholders(player, sep[0]);
        String conditional = sep[1];

        String separator = " "+conditional+" ";
        int index = condition.indexOf(separator);
        if(index < 0){
            return false;
        }
        String value = condition.substring(index+separator.length());

        switch(conditional){
            case "==":
                return value.equals(variable);
            case "!=":
                return !value.equals(variable);
            case ">=":
            case "<=":
            case ">":
            case "<":
                break;
            default:
                return false;
        }

        double valueFinal;
        double valueFinalVariable;
        try {
            valueFinal = Double.parseDouble(value.trim());
            valueFinalVariable = Double.parseDouble(variable.trim());
        }catch(NumberFormatException error) {
            return false;
        }

        return switch(conditional){
            case ">=" -> valueFinalVariable >= valueFinal;
            case "<=" -> valueFinalVariable <= valueFinal;
            case ">" -> valueFinalVariable > valueFinal;
            case "<" -> valueFinalVariable < valueFinal;
            default -> false;
        };
    }

}
