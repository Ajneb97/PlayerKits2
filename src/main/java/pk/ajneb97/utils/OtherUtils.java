package pk.ajneb97.utils;

import org.bukkit.plugin.java.JavaPlugin;

import org.bukkit.configuration.ConfigurationSection;
import java.util.Collections;
import java.util.Set;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Color;
import org.bukkit.entity.Player;
import pk.ajneb97.PlayerKits2;
import pk.ajneb97.managers.MessagesManager;

import java.util.ArrayList;
import java.util.List;

public class OtherUtils {

    public static Set<String> getSectionKeys(ConfigurationSection config, String path){
        ConfigurationSection section = config.getConfigurationSection(path);
        return section == null ? Collections.emptySet() : section.getKeys(false);
    }

    public static List<String> wrapText(String text, int lineLength){
        List<String> lines = new ArrayList<>();
        int currentPos = 0;
        for(int i=0;i<text.length();i++) {
            if(currentPos >= lineLength && text.charAt(i) == ' ') {
                lines.add(text.substring(i-currentPos, i));
                currentPos = 0;
            }else {
                currentPos++;
            }
            if(i==text.length()-1) {
                lines.add(text.substring(i-currentPos+1));
            }
        }
        return lines;
    }

    public static boolean isNew() {
        ServerVersion serverVersion = PlayerKits2.serverVersion;
        return serverVersion.serverVersionGreaterEqualThan(serverVersion,ServerVersion.v1_16_R1);
    }

    public static boolean isLegacy() {
        ServerVersion serverVersion = PlayerKits2.serverVersion;
        return !serverVersion.serverVersionGreaterEqualThan(serverVersion,ServerVersion.v1_13_R1);
    }

    public static boolean isTrimUnsupported() {
        ServerVersion serverVersion = PlayerKits2.serverVersion;
        return !serverVersion.serverVersionGreaterEqualThan(serverVersion,ServerVersion.v1_20_R1);
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
        } catch (IllegalAccessException | NoSuchFieldException e) {
            JavaPlugin.getPlugin(PlayerKits2.class).getLogger().warning("Unknown firework color "+colorName+", using WHITE instead.");
            return Color.WHITE;
        }
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
