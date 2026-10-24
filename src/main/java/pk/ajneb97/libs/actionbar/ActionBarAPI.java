package pk.ajneb97.libs.actionbar;

import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Level;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import pk.ajneb97.PlayerKits2;
import pk.ajneb97.api.PlayerKitsAPI;
import pk.ajneb97.managers.MessagesManager;
import pk.ajneb97.utils.MiniMessageUtils;
import pk.ajneb97.utils.OtherUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class ActionBarAPI
{

    @SuppressWarnings("deprecation")
    public static void sendActionBar(Player player, String message) {
	  if(OtherUtils.isNew()) {
          if(PlayerKitsAPI.getPlugin().getConfigsManager().getMainConfigManager().isUseMiniMessage()){
              MiniMessageUtils.actionbar(player,message);
          }else{
              player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(MessagesManager.getLegacyColoredMessage(message)));
          }
		  return;
	  }
      message = MessagesManager.getLegacyColoredMessage(message);
	  boolean useOldMethods = false;
	  String nmsVersion = Bukkit.getServer().getClass().getPackage().getName();
      nmsVersion = nmsVersion.substring(nmsVersion.lastIndexOf(".") + 1);
      if ((nmsVersion.equalsIgnoreCase("v1_8_R1")) || (nmsVersion.startsWith("v1_7_"))) {
          useOldMethods = true;
      }
      if (!player.isOnline()) {
          return;
      }

      try {
          Class<?> craftPlayerClass = Class.forName("org.bukkit.craftbukkit." + nmsVersion + ".entity.CraftPlayer");
          Object craftPlayer = craftPlayerClass.cast(player);
          Object packet;
          Class<?> packetPlayOutChatClass = Class.forName("net.minecraft.server." + nmsVersion + ".PacketPlayOutChat");
          Class<?> packetClass = Class.forName("net.minecraft.server." + nmsVersion + ".Packet");
          if (useOldMethods) {
              Class<?> chatSerializerClass = Class.forName("net.minecraft.server." + nmsVersion + ".ChatSerializer");
              Class<?> iChatBaseComponentClass = Class.forName("net.minecraft.server." + nmsVersion + ".IChatBaseComponent");
              Method m3 = chatSerializerClass.getDeclaredMethod("a", String.class);
              Object cbc = iChatBaseComponentClass.cast(m3.invoke(chatSerializerClass, "{\"text\": \"" + message + "\"}"));
              packet = packetPlayOutChatClass.getConstructor(iChatBaseComponentClass, byte.class).newInstance(cbc, (byte) 2);
          } else {
              Class<?> chatComponentTextClass = Class.forName("net.minecraft.server." + nmsVersion + ".ChatComponentText");
              Class<?> iChatBaseComponentClass = Class.forName("net.minecraft.server." + nmsVersion + ".IChatBaseComponent");
              try {

                  Class<?> chatMessageTypeClass = Class.forName("net.minecraft.server." + nmsVersion + ".ChatMessageType");
                  Object[] chatMessageTypes = chatMessageTypeClass.getEnumConstants();
                  Object chatMessageType = null;
                  for (Object obj : chatMessageTypes) {
                      if (obj.toString().equals("GAME_INFO")) {
                          chatMessageType = obj;
                      }
                  }
                  Object chatComponentText = chatComponentTextClass.getConstructor(String.class).newInstance(message);
                  packet = packetPlayOutChatClass.getConstructor(iChatBaseComponentClass, chatMessageTypeClass).newInstance(chatComponentText, chatMessageType);
              } catch (ClassNotFoundException classNotFound) {
                  Object chatComponentText = chatComponentTextClass.getConstructor(String.class).newInstance(message);
                  packet = packetPlayOutChatClass.getConstructor(iChatBaseComponentClass, byte.class).newInstance(chatComponentText, (byte) 2);
              }
          }
          Method craftPlayerHandleMethod = craftPlayerClass.getDeclaredMethod("getHandle");
          Object craftPlayerHandle = craftPlayerHandleMethod.invoke(craftPlayer);
          Field playerConnectionField = craftPlayerHandle.getClass().getDeclaredField("playerConnection");
          Object playerConnection = playerConnectionField.get(craftPlayerHandle);
          Method sendPacketMethod = playerConnection.getClass().getDeclaredMethod("sendPacket", packetClass);
          sendPacketMethod.invoke(playerConnection, packet);
      } catch (Exception e) {
          JavaPlugin.getPlugin(PlayerKits2.class).getLogger().log(Level.WARNING, "Could not send action bar", e);
      }
    }

  public static void sendActionBar(final Player player, final String message, int duration, PlayerKits2 plugin) {
      sendActionBar(player, message);

      if (duration > 0) {
          plugin.getPlatformScheduler().runForEntityLater(player, () -> sendActionBar(player, ""), duration + 1L);
      }

      while (duration > 40) {
          duration -= 40;
          final long delay = duration;
          plugin.getPlatformScheduler().runForEntityLater(player, () -> sendActionBar(player, message), delay);
      }
  }

}
