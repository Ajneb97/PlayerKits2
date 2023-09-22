package pk.ajneb97.utils;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.entity.Player;
import pk.ajneb97.PlayerKits2;
import pk.ajneb97.api.PlayerKitsAPI;
import pk.ajneb97.managers.MessagesManager;

import java.util.ArrayList;

public class OtherUtils {

    public static boolean isNew() {
        ServerVersion serverVersion = PlayerKits2.serverVersion;
        if(serverVersion.serverVersionGreaterEqualThan(serverVersion,ServerVersion.v1_16_R1)){
            return true;
        }
        return false;
    }

    public static boolean isLegacy() {
        ServerVersion serverVersion = PlayerKits2.serverVersion;
        if(serverVersion.serverVersionGreaterEqualThan(serverVersion,ServerVersion.v1_13_R1)) {
            return false;
        }
        return true;
    }

    // 1.20+
    public static boolean isTrimNew() {
        ServerVersion serverVersion = PlayerKits2.serverVersion;
        if(serverVersion.serverVersionGreaterEqualThan(serverVersion,ServerVersion.v1_20_R1)) {
            return true;
        }else {
            return false;
        }
    }

    public static String getTime(long seconds, MessagesManager msgManager) {
        long totalMinWait = seconds/60;
        long totalHourWait = totalMinWait/60;
        long totalDayWait = totalHourWait/24;
        String time = "";
        if(seconds > 59){
            seconds = seconds - 60*totalMinWait;
        }
        if(seconds > 0) {
            time = seconds+msgManager.getTimeSeconds();
        }
        if(totalMinWait > 59){
            totalMinWait = totalMinWait - 60*totalHourWait;
        }
        if(totalMinWait > 0){
            time = totalMinWait+msgManager.getTimeMinutes()+" "+time;
        }
        if(totalHourWait > 23) {
            totalHourWait = totalHourWait - 24*totalDayWait;
        }
        if(totalHourWait > 0){
            time = totalHourWait+msgManager.getTimeHours()+" " + time;
        }
        if(totalDayWait > 0) {
            time = totalDayWait+msgManager.getTimeDays()+" " + time;
        }

        if(time.endsWith(" ")) {
            time = time.substring(0, time.length()-1);
        }

        return time;
    }

    public static Color getFireworkColorFromName(String colorName) {
        try {
            return (Color) Color.class.getDeclaredField(colorName).get(Color.class);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String replaceGlobalVariables(String text, Player player, PlayerKits2 plugin) {
        if(player == null){
            return text;
        }
        text = text.replace("%player%",player.getName());
        if(plugin.getDependencyManager().isPlaceholderAPI()) {
            text = PlaceholderAPI.setPlaceholders(player, text);
        }

        return text;
    }

    public static void addRangeToList(int min,int max, ArrayList<Integer> list){
        for(int i=min;i<=max;i++){
            list.add(i);
        }
    }
}
