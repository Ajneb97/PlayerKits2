package pk.ajneb97.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextReplacementConfig;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.meta.ItemMeta;
import pk.ajneb97.PlayerKits2;
import pk.ajneb97.managers.InventoryRequirementsManager;
import pk.ajneb97.managers.MessagesManager;
import pk.ajneb97.model.internal.KitVariable;
import pk.ajneb97.model.item.KitItem;

import java.util.ArrayList;
import java.util.List;

public class MiniMessageUtils {

    public static void messagePrefix(CommandSender sender, String message, boolean isPrefix, String prefix){
        if(isPrefix){
            sender.sendMessage(MiniMessage.miniMessage().deserialize(prefix+message));
        }else{
            sender.sendMessage(MiniMessage.miniMessage().deserialize(message));
        }
    }

    public static void title(Player player, String title, String subtitle){
        player.showTitle(Title.title(
                MiniMessage.miniMessage().deserialize(title),MiniMessage.miniMessage().deserialize(subtitle)
        ));
    }

    public static void actionbar(Player player, String message){
        player.sendActionBar(MiniMessage.miniMessage().deserialize(message));
    }

    public static void message(Player player,String message){
        player.sendMessage(MiniMessage.miniMessage().deserialize(message));
    }

    public static Inventory createInventory(int slots, String title){
        return Bukkit.createInventory(null,slots, MiniMessage.miniMessage().deserialize(title));
    }

    public static void setCommonItemName(KitItem commonItem, ItemMeta meta){
        commonItem.setName(MiniMessage.miniMessage().serialize(meta.displayName()));
    }

    public static void setCommonItemLore(List<String> lore, ItemMeta meta){
        for (Component line : meta.lore()) {
            lore.add(MiniMessage.miniMessage().serialize(line));
        }
    }

    public static void setCommonItemNameLegacy(KitItem commonItem, ItemMeta meta){
        commonItem.setName(LegacyComponentSerializer.legacyAmpersand().serialize(meta.displayName()));
    }

    public static void setCommonItemLoreLegacy(List<String> lore, ItemMeta meta){
        for (Component line : meta.lore()) {
            lore.add(LegacyComponentSerializer.legacyAmpersand().serialize(line));
        }
    }

    public static void setItemName(ItemMeta meta,String name){
        meta.displayName(MiniMessage.miniMessage().deserialize(name).decoration(TextDecoration.ITALIC, false));
    }

    public static void setItemLore(ItemMeta meta, List<String> lore, Player player, PlayerKits2 plugin){
        List<Component> loreComponent = new ArrayList<>();
        for(int i=0;i<lore.size();i++) {
            String line = OtherUtils.replaceGlobalVariables(lore.get(i),player,plugin);
            loreComponent.add(MiniMessage.miniMessage().deserialize(line).decoration(TextDecoration.ITALIC, false));
        }
        meta.lore(loreComponent);
    }

    public static void replaceVariablesItemName(ItemMeta meta, ArrayList<KitVariable> variables){
        Component name = meta.displayName();
        Component newName = name;
        for(KitVariable variable : variables){
            newName = newName.replaceText(TextReplacementConfig.builder()
                    .matchLiteral(variable.getVariable())
                    .replacement(MiniMessage.miniMessage().deserialize(variable.getValue()))
                    .build());
        }
        meta.displayName(newName);
    }

    public static void replaceVariablesItemLore(ItemMeta meta,ArrayList<KitVariable> variables){
        List<Component> lore = meta.lore();
        List<Component> newLore = new ArrayList<>();
        for(Component c : lore){
            Component newComponent = c;
            for(KitVariable variable : variables){
                newComponent = newComponent.replaceText(TextReplacementConfig.builder()
                        .matchLiteral(variable.getVariable())
                        .replacement(MiniMessage.miniMessage().deserialize(variable.getValue()))
                        .build());
            }
            newLore.add(newComponent);
        }
        meta.lore(newLore);
    }

    public static void setRequirementsMessage(ItemMeta meta, String kitName, Player player, InventoryRequirementsManager inventoryRequirementsManager){
        List<Component> newLore = new ArrayList<>();
        PlainTextComponentSerializer plainSerializer = PlainTextComponentSerializer.plainText();
        for(Component line : meta.lore()){
            String plainText = plainSerializer.serialize(line);
            if(plainText.contains("%kit_requirements_message%")){
                List<String> message = inventoryRequirementsManager.replaceRequirementsMessageVariable(kitName,player);
                for(String m : message){
                    newLore.add(MiniMessage.miniMessage().deserialize(m).decoration(TextDecoration.ITALIC, false));
                }
            }else{
                newLore.add(line);
            }
        }
        meta.lore(newLore);
    }
}
