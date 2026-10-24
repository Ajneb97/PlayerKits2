package pk.ajneb97.libs.titles;

import pk.ajneb97.PlayerKits2;

import org.bukkit.plugin.java.JavaPlugin;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import pk.ajneb97.api.PlayerKitsAPI;
import pk.ajneb97.managers.MessagesManager;
import pk.ajneb97.utils.MiniMessageUtils;
import pk.ajneb97.utils.OtherUtils;

import java.lang.reflect.Constructor;
import java.util.logging.Level;

public class TitleAPI implements Listener {

    public static void sendPacket(Player player, Object packet) {
        try {
            Object handle = player.getClass().getMethod("getHandle").invoke(player);
            Object playerConnection = handle.getClass().getField("playerConnection").get(handle);
            playerConnection.getClass().getMethod("sendPacket", getNMSClass("Packet")).invoke(playerConnection, packet);
        } catch (Exception e) {
            JavaPlugin.getPlugin(PlayerKits2.class).getLogger().log(Level.WARNING, "Could not send title packet", e);
        }
    }

    public static Class<?> getNMSClass(String name) throws ClassNotFoundException {
        String version = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];
        return Class.forName("net.minecraft.server." + version + "." + name);
    }

    @SuppressWarnings("deprecation")
    public static void sendTitle(Player player, Integer fadeIn, Integer stay, Integer fadeOut, String title, String subtitle) {
    	if(OtherUtils.isNew()) {
    		if(title.isEmpty()) {
        		title = " ";
        	}
        	if(subtitle.isEmpty()) {
        		subtitle = " ";
        	}

            if(PlayerKitsAPI.getPlugin().getConfigsManager().getMainConfigManager().isUseMiniMessage()){
                MiniMessageUtils.title(player,title,subtitle,fadeIn,stay,fadeOut);
            }else{
                player.sendTitle(MessagesManager.getLegacyColoredMessage(title), MessagesManager.getLegacyColoredMessage(subtitle), fadeIn, stay, fadeOut);
            }
    		return;
    	}
    	try {
            if (title != null) {
                String text = ChatColor.translateAlternateColorCodes('&', title).replace("%player%", player.getDisplayName());
                sendPacket(player, createTimedPacket("TIMES", text, fadeIn, stay, fadeOut));
                sendPacket(player, createTitlePacket(text));
            }

            if (subtitle != null) {
                String text = ChatColor.translateAlternateColorCodes('&', subtitle).replace("%player%", player.getDisplayName());
                sendPacket(player, createTimedPacket("TIMES", text, fadeIn, stay, fadeOut));
                sendPacket(player, createTimedPacket("SUBTITLE", text, fadeIn, stay, fadeOut));
            }
        } catch (Exception e) {
            JavaPlugin.getPlugin(PlayerKits2.class).getLogger().log(Level.WARNING, "Could not send title", e);
        }
    }

    private static Object getTitleAction(String action) throws ReflectiveOperationException {
        return getNMSClass("PacketPlayOutTitle").getDeclaredClasses()[0].getField(action).get(null);
    }

    private static Object getChatComponent(String text) throws ReflectiveOperationException {
        return getNMSClass("IChatBaseComponent").getDeclaredClasses()[0].getMethod("a", String.class)
                .invoke(null, "{\"text\":\"" + text + "\"}");
    }

    private static Object createTimedPacket(String action, String text, int fadeIn, int stay, int fadeOut) throws ReflectiveOperationException {
        Class<?> packetClass = getNMSClass("PacketPlayOutTitle");
        Constructor<?> constructor = packetClass.getConstructor(packetClass.getDeclaredClasses()[0], getNMSClass("IChatBaseComponent"),
                Integer.TYPE, Integer.TYPE, Integer.TYPE);
        return constructor.newInstance(getTitleAction(action), getChatComponent(text), fadeIn, stay, fadeOut);
    }

    private static Object createTitlePacket(String text) throws ReflectiveOperationException {
        Class<?> packetClass = getNMSClass("PacketPlayOutTitle");
        Constructor<?> constructor = packetClass.getConstructor(packetClass.getDeclaredClasses()[0], getNMSClass("IChatBaseComponent"));
        return constructor.newInstance(getTitleAction("TITLE"), getChatComponent(text));
    }
}
