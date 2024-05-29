package pk.ajneb97.utils;

import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;

public class PlayerUtils {

    public static ItemStack[] getAllInventoryContents(Player player){
        if(Bukkit.getVersion().contains("1.8")) {
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
        //ItemStack[] contents = getAllInventoryContents(player);
        ItemStack[] contents = null;
        if(Bukkit.getVersion().contains("1.8")) {
            contents = player.getInventory().getContents();
        }else{
            contents = player.getInventory().getStorageContents();
        }

        int usedSlots = 0;
        for(int i=0;i<contents.length;i++) {
            if(contents[i] != null && !contents[i].getType().equals(Material.AIR)) {
                usedSlots++;
            }
        }

        return usedSlots;
    }

    public static boolean isPlayerKitsAdmin(CommandSender sender){
        return sender.hasPermission("playerkits.admin");
    }

    public static boolean hasCooldownBypassPermission(CommandSender sender){
        return sender.hasPermission("playerkits.bypass.cooldown");
    }

    public static boolean hasOneTimeBypassPermission(CommandSender sender){
        return sender.hasPermission("playerkits.bypass.onetime");
    }

    public static boolean passCondition(Player player,String condition) {
        String[] sep = condition.split(" ");
        String variable = sep[0];
        variable = PlaceholderAPI.setPlaceholders(player, variable);
        String conditional = sep[1];

        if(conditional.equals(">=")) {
            String[] conditionMiniSep = condition.split(" >= ");
            String value = conditionMiniSep[1];
            try {
                double valueFinal = Double.valueOf(value);
                double valueFinalVariable = Double.valueOf(variable);
                if(valueFinalVariable >= valueFinal) {
                    return true;
                }
            }catch(NumberFormatException e) {
                return true;
            }
        }else if(conditional.equals("<=")) {
            String[] conditionMiniSep = condition.split(" <= ");
            String value = conditionMiniSep[1];
            try {
                double valueFinal = Double.valueOf(value);
                double valueFinalVariable = Double.valueOf(variable);
                if(valueFinalVariable <= valueFinal) {
                    return true;
                }
            }catch(NumberFormatException e) {
                return true;
            }
        }else if(conditional.equals("==")) {
            String[] conditionMiniSep = condition.split(" == ");
            String value = conditionMiniSep[1];
            if(value.equals(variable)) {
                return true;
            }
        }else if(conditional.equals("!=")) {
            String[] conditionMiniSep = condition.split(" != ");
            String value = conditionMiniSep[1];
            if(!value.equals(variable)) {
                return true;
            }
        }else if(conditional.equals(">")) {
            String[] conditionMiniSep = condition.split(" > ");
            String value = conditionMiniSep[1];
            try {
                double valueFinal = Double.valueOf(value);
                double valueFinalVariable = Double.valueOf(variable);
                if(valueFinalVariable > valueFinal) {
                    return true;
                }
            }catch(NumberFormatException e) {
                return true;
            }
        }else if(conditional.equals("<")) {
            String[] conditionMiniSep = condition.split(" < ");
            String value = conditionMiniSep[1];
            try {
                double valueFinal = Double.valueOf(value);
                double valueFinalVariable = Double.valueOf(variable);
                if(valueFinalVariable < valueFinal) {
                    return true;
                }
            }catch(NumberFormatException e) {
                return true;
            }
        }

        return false;
    }

}
