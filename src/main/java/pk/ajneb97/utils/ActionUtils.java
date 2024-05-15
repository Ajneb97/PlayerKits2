package pk.ajneb97.utils;

import org.bukkit.*;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.metadata.FixedMetadataValue;
import pk.ajneb97.PlayerKits2;
import pk.ajneb97.libs.actionbar.ActionBarAPI;
import pk.ajneb97.libs.titles.TitleAPI;
import pk.ajneb97.managers.MessagesManager;

import java.util.ArrayList;

public class ActionUtils {

    public static void consoleCommand(String actionLine){
        ConsoleCommandSender sender = Bukkit.getConsoleSender();
        Bukkit.dispatchCommand(sender, actionLine);
    }

    public static void playerCommand(Player player, String actionLine){
        player.performCommand(actionLine);
    }

    public static void playSound(Player player,String actionLine){
        String[] sep = actionLine.split(";");
        Sound sound = null;
        int volume = 0;
        float pitch = 0;
        try {
            sound = Sound.valueOf(sep[0]);
            volume = Integer.valueOf(sep[1]);
            pitch = Float.valueOf(sep[2]);
        }catch(Exception e ) {
            Bukkit.getConsoleSender().sendMessage(PlayerKits2.prefix+
                    MessagesManager.getColoredMessage("&7Sound Name: &c"+sep[0]+" &7is not valid. Change it in the config!"));
            return;
        }

        player.playSound(player.getLocation(), sound, volume, pitch);
    }

    public static void actionbar(Player player, String actionLine, PlayerKits2 plugin){
        String[] sep = actionLine.split(";");
        String text = sep[0];
        int duration = Integer.valueOf(sep[1]);
        ActionBarAPI.sendActionBar(player,text,duration,plugin);
    }

    public static void title(Player player,String actionLine){
        String[] sep = actionLine.split(";");
        int fadeIn = Integer.valueOf(sep[0]);
        int stay = Integer.valueOf(sep[1]);
        int fadeOut = Integer.valueOf(sep[2]);

        String title = sep[3];
        String subtitle = sep[4];
        if(title.equals("none")) {
            title = "";
        }
        if(subtitle.equals("none")) {
            subtitle = "";
        }
        TitleAPI.sendTitle(player,fadeIn,stay,fadeOut,title,subtitle);
    }

    public static void firework(Player player,String actionLine,PlayerKits2 plugin){
        ArrayList<Color> colors = new ArrayList<Color>();
        FireworkEffect.Type type = null;
        ArrayList<Color> fadeColors = new ArrayList<Color>();
        int power = 0;

        String[] sep = actionLine.split(" ");
        for(String s : sep) {
            if(s.startsWith("colors:")) {
                s = s.replace("colors:", "");
                String[] colorsSep = s.split(",");
                for(String colorSep : colorsSep) {
                    colors.add(OtherUtils.getFireworkColorFromName(colorSep));
                }
            }else if(s.startsWith("type:")) {
                s = s.replace("type:", "");
                type = FireworkEffect.Type.valueOf(s);
            }else if(s.startsWith("fade:")) {
                s = s.replace("fade:", "");
                String[] colorsSep = s.split(",");
                for(String colorSep : colorsSep) {
                    fadeColors.add(OtherUtils.getFireworkColorFromName(colorSep));
                }
            }else if(s.startsWith("power:")) {
                s = s.replace("power:", "");
                power = Integer.valueOf(s);
            }
        }

        Location location = player.getLocation();

        ServerVersion serverVersion = PlayerKits2.serverVersion;
        EntityType entityType = null;
        if(serverVersion.serverVersionGreaterEqualThan(serverVersion,ServerVersion.v1_20_R4)){
            entityType = EntityType.FIREWORK_ROCKET;
        }else{
            entityType = EntityType.valueOf("FIREWORK");
        }
        Firework firework = (Firework) location.getWorld().spawnEntity(location, entityType);
        FireworkMeta fireworkMeta = firework.getFireworkMeta();
        FireworkEffect effect = FireworkEffect.builder().flicker(false)
                .withColor(colors)
                .with(type)
                .withFade(fadeColors)
                .build();
        fireworkMeta.addEffect(effect);
        fireworkMeta.setPower(power);
        firework.setFireworkMeta(fireworkMeta);
        firework.setMetadata("playerkits", new FixedMetadataValue(plugin, "no_damage"));
    }
}
