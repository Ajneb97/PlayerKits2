package pk.ajneb97.managers;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextReplacementConfig;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Color;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.components.CustomModelDataComponent;
import pk.ajneb97.PlayerKits2;
import pk.ajneb97.model.Kit;
import pk.ajneb97.model.internal.KitVariable;
import pk.ajneb97.model.item.*;
import pk.ajneb97.utils.ItemUtils;
import pk.ajneb97.utils.OtherUtils;
import pk.ajneb97.utils.ServerVersion;

import java.util.*;
import java.util.stream.Collectors;

public class KitItemManager {

    private PlayerKits2 plugin;
    public KitItemManager(PlayerKits2 plugin){
        this.plugin = plugin;
    }

    public KitItem createKitItemFromItemStack(ItemStack item,boolean exactItem){
        if(exactItem){
            return new KitItem(item.clone());
        }

        KitItem kitItem = new KitItem(item.getType().name());
        kitItem.setAmount(item.getAmount());

        if(item.getDurability() != 0) {
            kitItem.setDurability(item.getDurability());
        }

        ServerVersion serverVersion = PlayerKits2.serverVersion;
        boolean useMiniMessage = plugin.getConfigsManager().getMainConfigManager().isUseMiniMessage();

        if(item.hasItemMeta()) {
            ItemMeta meta = item.getItemMeta();
            if(meta.hasDisplayName()) {
                if(useMiniMessage){
                    kitItem.setName(MiniMessage.miniMessage().serialize(meta.displayName()));
                }else{
                    kitItem.setName(meta.getDisplayName().replace("§", "&"));
                }

            }
            if(meta.hasLore()) {
                List<String> lore = new ArrayList<>();
                if(useMiniMessage){
                    for (Component line : meta.lore()) {
                        lore.add(MiniMessage.miniMessage().serialize(line));
                    }
                }else{
                    for(String l : meta.getLore()) {
                        lore.add(l.replace("§", "&"));
                    }
                }
                kitItem.setLore(lore);
            }
            if(meta.hasEnchants()) {
                List<String> enchants = new ArrayList<String>();
                for(Map.Entry<Enchantment,Integer> entry : meta.getEnchants().entrySet()){
                    String enchant;
                    if(serverVersion.serverVersionGreaterEqualThan(serverVersion,ServerVersion.v1_21_R1)){
                        enchant = entry.getKey().getKey().getKey().toUpperCase();
                    }else{
                        enchant = entry.getKey().getName();
                    }
                    int level = entry.getValue();
                    enchants.add(enchant+";"+level);
                }
                kitItem.setEnchants(enchants);
            }
            Set<ItemFlag> flags = meta.getItemFlags();
            if(flags != null && !flags.isEmpty()) {
                List<String> flagsList = new ArrayList<String>();
                for(ItemFlag flag : flags) {
                    flagsList.add(flag.name());
                }
                kitItem.setFlags(flagsList);
            }

            if(OtherUtils.isNew() && meta.hasCustomModelData()) {
                if(!serverVersion.serverVersionGreaterEqualThan(serverVersion,ServerVersion.v1_21_R3)){
                    kitItem.setCustomModelData(meta.getCustomModelData());
                }
            }

            if(serverVersion.serverVersionGreaterEqualThan(serverVersion,ServerVersion.v1_21_R3)){
                if(meta.hasCustomModelData()){
                    CustomModelDataComponent customModelDataComponent = meta.getCustomModelDataComponent();

                    List<String> customModelDataComponentFlagsList = customModelDataComponent.getFlags()
                            .stream()
                            .map(String::valueOf)
                            .collect(Collectors.toList());

                    List<String> customModelDataComponentColorsList = customModelDataComponent.getColors()
                            .stream()
                            .map(color -> String.valueOf(color.asRGB()))
                            .collect(Collectors.toList());

                    List<String> customModelDataComponentFloatsList = customModelDataComponent.getFloats()
                            .stream()
                            .map(String::valueOf)
                            .collect(Collectors.toList());

                    List<String> customModelDataComponentStringsList = new ArrayList<>(customModelDataComponent.getStrings());

                    kitItem.setCustomModelComponentData(new KitItemCustomModelComponentData(
                            customModelDataComponentFlagsList,
                            customModelDataComponentColorsList,
                            customModelDataComponentFloatsList,
                            customModelDataComponentStringsList
                    ));
                }
            }

            if(serverVersion.serverVersionGreaterEqualThan(serverVersion,ServerVersion.v1_20_R4)){
                if(meta.isHideTooltip()){
                    kitItem.setHideTooltip(true);
                }
            }

            if(serverVersion.serverVersionGreaterEqualThan(serverVersion,ServerVersion.v1_21_R2)){
                if(meta.hasTooltipStyle()){
                    NamespacedKey key = meta.getTooltipStyle();
                    kitItem.setTooltipStyle(key.getNamespace()+":"+key.getKey());
                }
            }

            if(serverVersion.serverVersionGreaterEqualThan(serverVersion,ServerVersion.v1_21_R3)){
                if(meta.hasItemModel()){
                    NamespacedKey key = meta.getItemModel();
                    kitItem.setModel(key.getNamespace()+":"+key.getKey());
                }
            }

            if(meta instanceof LeatherArmorMeta) {
                LeatherArmorMeta meta2 = (LeatherArmorMeta) meta;
                kitItem.setColor(meta2.getColor().asRGB());
            }

            if(meta instanceof EnchantmentStorageMeta) {
                EnchantmentStorageMeta meta2 = (EnchantmentStorageMeta) meta;
                Map<Enchantment, Integer> enchants = meta2.getStoredEnchants();
                List<String> enchantsList = new ArrayList<String>();
                for (Map.Entry<Enchantment, Integer> entry : enchants.entrySet()) {
                    String enchant;
                    if(serverVersion.serverVersionGreaterEqualThan(serverVersion,ServerVersion.v1_21_R1)){
                        enchant = entry.getKey().getKey().getKey().toUpperCase();
                    }else{
                        enchant = entry.getKey().getName();
                    }
                    int level = entry.getValue();
                    enchantsList.add(enchant+";"+level);
                }
                if(!enchantsList.isEmpty()) {
                    kitItem.setBookEnchants(enchantsList);
                }
            }
        }

        if(!serverVersion.serverVersionGreaterEqualThan(serverVersion,ServerVersion.v1_20_R4)){
            List<String> nbtList = ItemUtils.getNBT(plugin,item);
            if(!nbtList.isEmpty()) {
                kitItem.setNbt(nbtList);
            }
        }


        kitItem.setAttributes(ItemUtils.getAttributes(plugin,item));
        kitItem.setSkullData(ItemUtils.getSkullData(item));
        kitItem.setPotionData(ItemUtils.getPotionData(item));
        kitItem.setFireworkData(ItemUtils.getFireworkData(item));
        kitItem.setBannerData(ItemUtils.getBannerData(item));
        kitItem.setBookData(ItemUtils.getBookData(item));
        kitItem.setTrimData(ItemUtils.getArmorTrimData(item));

        return kitItem;
    }

    public ItemStack createItemFromKitItem(KitItem kitItem, Player player, Kit kit){
        if(kitItem.getOriginalItem() != null){
            ItemStack item = kitItem.getOriginalItem().clone();
            if(kit.isAllowPlaceholdersOnOriginalItems()){
                // Placeholders on original item
                ItemMeta meta = item.getItemMeta();
                if(meta.hasDisplayName()){
                    String name = OtherUtils.replaceGlobalVariables(meta.getDisplayName(),player,plugin);
                    meta.setDisplayName(name);
                }
                if(meta.hasLore()){
                    List<String> lore = meta.getLore();
                    lore.replaceAll(text -> OtherUtils.replaceGlobalVariables(text, player, plugin));
                    meta.setLore(lore);
                }
                item.setItemMeta(meta);
            }
            return item;
        }

        ItemStack item = ItemUtils.createItemFromID(kitItem.getId());
        item.setAmount(kitItem.getAmount());

        short durability = kitItem.getDurability();
        if(durability != 0) {
            item.setDurability(durability);
        }

        boolean useMiniMessage = plugin.getConfigsManager().getMainConfigManager().isUseMiniMessage();

        //MAIN META
        ItemMeta meta = item.getItemMeta();
        String name = kitItem.getName();
        if(name != null){
            name = OtherUtils.replaceGlobalVariables(name,player,plugin);
            if(useMiniMessage){
                meta.displayName(MiniMessage.miniMessage().deserialize(name).decoration(TextDecoration.ITALIC, false));
            }else{
                meta.setDisplayName(MessagesManager.getLegacyColoredMessage(name));
            }
        }

        List<String> lore = kitItem.getLore();
        if(lore != null) {
            List<String> loreCopy = new ArrayList<>(lore);
            if(useMiniMessage){
                List<Component> loreComponent = new ArrayList<>();
                for(int i=0;i<loreCopy.size();i++) {
                    String line = OtherUtils.replaceGlobalVariables(loreCopy.get(i),player,plugin);
                    loreComponent.add(MiniMessage.miniMessage().deserialize(line).decoration(TextDecoration.ITALIC, false));
                }
                meta.lore(loreComponent);
            }else{
                for(int i=0;i<loreCopy.size();i++) {
                    String line = OtherUtils.replaceGlobalVariables(loreCopy.get(i),player,plugin);
                    loreCopy.set(i, MessagesManager.getLegacyColoredMessage(line));
                }
                meta.setLore(loreCopy);
            }
        }

        int customModelData = kitItem.getCustomModelData();
        if(customModelData != 0) {
            meta.setCustomModelData(customModelData);
        }

        ServerVersion serverVersion = PlayerKits2.serverVersion;
        if(serverVersion.serverVersionGreaterEqualThan(serverVersion,ServerVersion.v1_21_R3)){
            KitItemCustomModelComponentData kitItemCustomModelComponentData = kitItem.getCustomModelComponentData();
            if(kitItemCustomModelComponentData != null){
                CustomModelDataComponent customModelDataComponent = meta.getCustomModelDataComponent();

                List<String> cFloats = kitItemCustomModelComponentData.getFloats();
                List<String> cColors = kitItemCustomModelComponentData.getColors();
                List<String> cFlags = kitItemCustomModelComponentData.getFlags();
                List<String> cStrings = kitItemCustomModelComponentData.getStrings();

                customModelDataComponent.setFlags(cFlags.stream()
                        .map(Boolean::parseBoolean)
                        .collect(Collectors.toList()));
                customModelDataComponent.setFloats(cFloats.stream()
                        .map(Float::parseFloat)
                        .collect(Collectors.toList()));
                customModelDataComponent.setColors(cColors.stream()
                        .map(rgb -> Color.fromRGB(Integer.parseInt(rgb)))
                        .collect(Collectors.toList()));
                customModelDataComponent.setStrings(new ArrayList<>(cStrings));
                meta.setCustomModelDataComponent(customModelDataComponent);
            }
        }

        List<String> enchants = kitItem.getEnchants();
        if(enchants != null) {
            for(int i=0;i<enchants.size();i++) {
                String[] sep = enchants.get(i).split(";");
                String enchantName = sep[0];
                int enchantLevel = Integer.parseInt(sep[1]);

                meta.addEnchant(Enchantment.getByName(enchantName), enchantLevel, true);
            }
        }

        if(serverVersion.serverVersionGreaterEqualThan(serverVersion,ServerVersion.v1_20_R4)){
            if(kitItem.isHideTooltip()){
                meta.setHideTooltip(true);
            }
        }

        if(serverVersion.serverVersionGreaterEqualThan(serverVersion,ServerVersion.v1_21_R2)){
            String tooltipStyle = kitItem.getTooltipStyle();
            if(tooltipStyle != null){
                String[] sep = tooltipStyle.split(":");
                meta.setTooltipStyle(new NamespacedKey(sep[0],sep[1]));
            }
        }

        if(serverVersion.serverVersionGreaterEqualThan(serverVersion,ServerVersion.v1_21_R3)){
            String model = kitItem.getModel();
            if(model != null){
                String[] sep = model.split(":");
                meta.setItemModel(new NamespacedKey(sep[0],sep[1]));
            }
        }

        item.setItemMeta(meta);

        //OTHER META
        int color = kitItem.getColor();
        if(color != 0) {
            LeatherArmorMeta meta2 = (LeatherArmorMeta) item.getItemMeta();
            meta2.setColor(Color.fromRGB(color));
            item.setItemMeta(meta2);
        }

        List<String> bookEnchants = kitItem.getBookEnchants();
        if(bookEnchants != null && !bookEnchants.isEmpty()) {
            EnchantmentStorageMeta meta2 = (EnchantmentStorageMeta) item.getItemMeta();
            for(int i=0;i<bookEnchants.size();i++) {
                String[] sep = bookEnchants.get(i).split(";");
                String enchantName = sep[0];
                int level = Integer.valueOf(sep[1]);
                meta2.addStoredEnchant(Enchantment.getByName(enchantName), level, true);
            }
            item.setItemMeta(meta2);
        }

        //ADVANCED DATA
        KitItemSkullData skullData = kitItem.getSkullData();
        ItemUtils.setSkullData(item, skullData, player);

        KitItemPotionData potionData = kitItem.getPotionData();
        ItemUtils.setPotionData(item, potionData);

        KitItemFireworkData fireworkData = kitItem.getFireworkData();
        ItemUtils.setFireworkData(item, fireworkData);

        KitItemBannerData bannerData = kitItem.getBannerData();
        ItemUtils.setBannerData(item, bannerData);

        KitItemBookData bookData = kitItem.getBookData();
        ItemUtils.setBookData(item, bookData);

        KitItemTrimData trimData = kitItem.getTrimData();
        ItemUtils.setArmorTrimData(item, trimData);

        List<String> attributes = kitItem.getAttributes();
        item = ItemUtils.setAttributes(plugin,item, attributes);

        //Item Flags
        meta = item.getItemMeta();
        List<String> flags = kitItem.getFlags();
        if(flags != null) {
            for(String flag : flags) {
                if (flag.equals("HIDE_ATTRIBUTES") && plugin.getDependencyManager().isPaper() &&
                        serverVersion.serverVersionGreaterEqualThan(serverVersion, ServerVersion.v1_21_R1)) {
                    //Fix PAPER HIDE_ATTRIBUTES
                    ItemUtils.addDummyAttribute(meta,plugin);
                }
                meta.addItemFlags(ItemFlag.valueOf(flag));
            }
        }
        item.setItemMeta(meta);

        if(!serverVersion.serverVersionGreaterEqualThan(serverVersion,ServerVersion.v1_20_R4)){
            List<String> nbtList = kitItem.getNbt();
            item = ItemUtils.setNBT(plugin,item, nbtList);
        }

        return item;
    }

    public void saveKitItemOnConfig(KitItem item,FileConfiguration config,String path){
        if(item.getOriginalItem() != null){
            config.set(path+".original",item.getOriginalItem().clone());
        }else{
            config.set(path+".id", item.getId());
            config.set(path+".name", item.getName());
            config.set(path+".amount", item.getAmount());
            if(item.getDurability() != 0) {
                config.set(path+".durability", item.getDurability());
            }
            if(item.getLore() != null && !item.getLore().isEmpty()) {
                config.set(path+".lore", item.getLore());
            }
            if(item.getEnchants() != null && !item.getEnchants().isEmpty()) {
                config.set(path+".enchants", item.getEnchants());
            }
            if(item.getFlags() != null && !item.getFlags().isEmpty()) {
                config.set(path+".item_flags", item.getFlags());
            }

            if(item.getCustomModelData() != 0) {
                config.set(path+".custom_model_data", item.getCustomModelData());
            }

            KitItemCustomModelComponentData customModelComponentData = item.getCustomModelComponentData();
            if(customModelComponentData != null){
                if(!customModelComponentData.getFlags().isEmpty()) config.set(path+".custom_model_component_data.flags",customModelComponentData.getFlags());
                if(!customModelComponentData.getFloats().isEmpty()) config.set(path+".custom_model_component_data.floats",customModelComponentData.getFloats());
                if(!customModelComponentData.getColors().isEmpty()) config.set(path+".custom_model_component_data.colors",customModelComponentData.getColors());
                if(!customModelComponentData.getStrings().isEmpty()) config.set(path+".custom_model_component_data.strings",customModelComponentData.getStrings());
            }

            if(item.isHideTooltip()){
                config.set(path+".hide_tooltip", true);
            }
            if(item.getTooltipStyle() != null){
                config.set(path+".tooltip_style",item.getTooltipStyle());
            }
            if(item.getModel() != null){
                config.set(path+".model",item.getModel());
            }

            if(item.getColor() != 0) {
                config.set(path+".color", item.getColor());
            }
            if(item.getNbt() != null && !item.getNbt().isEmpty()) {
                config.set(path+".nbt", item.getNbt());
            }
            if(item.getAttributes() != null && !item.getAttributes().isEmpty()) {
                config.set(path+".attributes", item.getAttributes());
            }
            if(item.getBookEnchants() != null && !item.getBookEnchants().isEmpty()) {
                config.set(path+".book_enchants", item.getBookEnchants());
            }
            if(item.getCanPlace() != null && !item.getCanPlace().isEmpty()) {
                config.set(path+".can_place", item.getCanPlace());
            }
            if(item.getCanDestroy() != null && !item.getCanDestroy().isEmpty()) {
                config.set(path+".can_destroy", item.getCanDestroy());
            }

            KitItemSkullData skullData = item.getSkullData();
            if(skullData != null) {
                config.set(path+".skull_data.texture", skullData.getTexture());
                config.set(path+".skull_data.id", skullData.getId());
                config.set(path+".skull_data.owner", skullData.getOwner());
            }

            KitItemPotionData potionData = item.getPotionData();
            if(potionData != null) {
                if(potionData.getPotionEffects() != null && !potionData.getPotionEffects().isEmpty()) {
                    config.set(path+".potion_data.effects", potionData.getPotionEffects());
                }
                config.set(path+".potion_data.extended", potionData.isExtended());
                config.set(path+".potion_data.upgraded", potionData.isUpgraded());
                config.set(path+".potion_data.type", potionData.getPotionType());
                if(potionData.getPotionColor() != 0) {
                    config.set(path+".potion_data.color", potionData.getPotionColor());
                }
            }

            KitItemFireworkData fireworkData = item.getFireworkData();
            if(fireworkData != null) {
                if(fireworkData.getFireworkRocketEffects() != null && !fireworkData.getFireworkRocketEffects().isEmpty()) {
                    config.set(path+".firework_data.rocket_effects", fireworkData.getFireworkRocketEffects());
                }
                config.set(path+".firework_data.star_effect", fireworkData.getFireworkStarEffect());
                if(fireworkData.getFireworkPower() != 0) {
                    config.set(path+".firework_data.power", fireworkData.getFireworkPower());
                }
            }

            KitItemBannerData bannerData = item.getBannerData();
            if(bannerData != null) {
                if(bannerData.getPatterns() != null && !bannerData.getPatterns().isEmpty()) {
                    config.set(path+".banner_data.patterns", bannerData.getPatterns());
                }
                config.set(path+".banner_data.base_color", bannerData.getBaseColor());
            }

            KitItemBookData bookData = item.getBookData();
            if(bookData != null) {
                config.set(path+".book_data.author", bookData.getAuthor());
                config.set(path+".book_data.title", bookData.getTitle());
                config.set(path+".book_data.pages", bookData.getPages());
                config.set(path+".book_data.generation", bookData.getGeneration());
            }

            KitItemTrimData trimData = item.getTrimData();
            if(trimData != null){
                config.set(path+".trim_data.pattern", trimData.getPattern());
                config.set(path+".trim_data.material", trimData.getMaterial());
            }
        }

        if(item.isOffhand()){
            config.set(path+".offhand", item.isOffhand());
        }
        if(item.getPreviewSlot() != -1){
            config.set(path+".preview_slot", item.getPreviewSlot());
        }
    }

    public KitItem getKitItemFromConfig(FileConfiguration config, String path){
        boolean offhand = config.contains(path+".offhand") ? config.getBoolean(path+".offhand") : false;
        int previewSlot = config.contains(path+".preview_slot") ? config.getInt(path+".preview_slot") : -1;
        KitItem kitItem = null;
        if(config.contains(path+".original")){
            kitItem = new KitItem(config.getItemStack(path+".original"));
        }else{
            String id = config.getString(path+".id");
            String name = config.contains(path+".name") ? config.getString(path+".name") : null;
            List<String> lore = config.contains(path+".lore") ? config.getStringList(path+".lore") : null;
            int amount = config.contains(path+".amount") ? config.getInt(path+".amount") : 1;
            short durability = config.contains(path+".durability") ? (short) config.getInt(path+".durability") : 0;
            int customModelData = config.contains(path+".custom_model_data") ? config.getInt(path+".custom_model_data") : 0;
            int color = config.contains(path+".color") ? config.getInt(path+".color") : 0;
            List<String> enchants = config.contains(path+".enchants") ? config.getStringList(path+".enchants") : null;
            List<String> flags = config.contains(path+".item_flags") ? config.getStringList(path+".item_flags") : null;
            List<String> bookEnchants = config.contains(path+".book_enchants") ? config.getStringList(path+".book_enchants") : null;
            List<String> nbtList = config.contains(path+".nbt") ? config.getStringList(path+".nbt") : null;
            List<String> attributes = config.contains(path+".attributes") ? config.getStringList(path+".attributes") : null;
            List<String> canPlace = config.contains(path+".can_place") ? config.getStringList(path+".can_place") : null;
            List<String> canDestroy = config.contains(path+".can_destroy") ? config.getStringList(path+".can_destroy") : null;

            KitItemCustomModelComponentData customModelComponentData = null;
            if(config.contains(path+".custom_model_component_data")) {
                List<String> cFlags = new ArrayList<>();
                List<String> cFloats = new ArrayList<>();
                List<String> cColors = new ArrayList<>();
                List<String> cStrings = new ArrayList<>();

                if(config.contains(path+".custom_model_component_data.flags")) {
                    cFlags = config.getStringList(path+".custom_model_component_data.flags");
                }
                if(config.contains(path+".custom_model_component_data.floats")) {
                    cFloats = config.getStringList(path+".custom_model_component_data.floats");
                }
                if(config.contains(path+".custom_model_component_data.colors")) {
                    cColors = config.getStringList(path+".custom_model_component_data.colors");
                }
                if(config.contains(path+".custom_model_component_data.strings")) {
                    cStrings = config.getStringList(path+".custom_model_component_data.strings");
                }

                customModelComponentData = new KitItemCustomModelComponentData(cFlags,cColors,cFloats,cStrings);
            }

            boolean hideTooltip = config.getBoolean(path+".hide_tooltip");
            String tooltipStyle = config.contains(path+".tooltip_style") ? config.getString(path+".tooltip_style") : null;
            String model = config.contains(path+".model") ? config.getString(path+".model") : null;

            KitItemSkullData skullData = null;
            if(config.contains(path+".skull_data")) {
                String skullTexture = null;
                String skullId = null;
                String skullOwner = null;
                if(config.contains(path+".skull_data.texture")) {
                    skullTexture = config.getString(path+".skull_data.texture");
                }
                if(config.contains(path+".skull_data.id")) {
                    skullId = config.getString(path+".skull_data.id");
                }
                if(config.contains(path+".skull_data.owner")) {
                    skullOwner = config.getString(path+".skull_data.owner");
                }
                skullData = new KitItemSkullData(skullOwner,skullTexture,skullId);
            }
            KitItemPotionData potionData = null;
            if(config.contains(path+".potion_data")) {
                List<String> potionEffects = null;
                boolean extended = false;
                boolean upgraded = false;
                String potionType = null;
                int potionColor = 0;
                if(config.contains(path+".potion_data.effects")) {
                    potionEffects = config.getStringList(path+".potion_data.effects");
                }
                if(config.contains(path+".potion_data.extended")) {
                    extended = config.getBoolean(path+".potion_data.extended");
                }
                if(config.contains(path+".potion_data.upgraded")) {
                    upgraded = config.getBoolean(path+".potion_data.upgraded");
                }
                if(config.contains(path+".potion_data.type")) {
                    potionType = config.getString(path+".potion_data.type");
                }
                if(config.contains(path+".potion_data.color")) {
                    potionColor = config.getInt(path+".potion_data.color");
                }

                potionData = new KitItemPotionData(upgraded,extended,potionType,potionColor,potionEffects);
            }
            KitItemFireworkData fireworkData = null;
            if(config.contains(path+".firework_data")) {
                List<String> rocketEffects = null;
                String starEffect = null;
                int power = 0;
                if(config.contains(path+".firework_data.rocket_effects")) {
                    rocketEffects = config.getStringList(path+".firework_data.rocket_effects");
                }
                if(config.contains(path+".firework_data.star_effect")) {
                    starEffect = config.getString(path+".firework_data.star_effect");
                }
                if(config.contains(path+".firework_data.power")) {
                    power = config.getInt(path+".firework_data.power");
                }

                fireworkData = new KitItemFireworkData(rocketEffects,starEffect,power);
            }
            KitItemBannerData bannerData = null;
            if(config.contains(path+".banner_data")) {
                List<String> patterns = null;
                String baseColor = null;
                if(config.contains(path+".banner_data.patterns")) {
                    patterns = config.getStringList(path+".banner_data.patterns");
                }
                if(config.contains(path+".banner_data.base_color")) {
                    baseColor = config.getString(path+".banner_data.base_color");
                }

                bannerData = new KitItemBannerData(patterns,baseColor);
            }
            KitItemBookData bookData = null;
            if(config.contains(path+".book_data")) {
                List<String> pages = config.getStringList(path+".book_data.pages");
                String author = null;
                String generation = null;
                String title = null;
                if(config.contains(path+".book_data.author")) {
                    author = config.getString(path+".book_data.author");
                }
                if(config.contains(path+".book_data.generation")) {
                    generation = config.getString(path+".book_data.generation");
                }
                if(config.contains(path+".book_data.title")) {
                    title = config.getString(path+".book_data.title");
                }

                bookData = new KitItemBookData(pages,author,generation,title);
            }
            KitItemTrimData trimData = null;
            if(config.contains(path+".trim_data")){
                String material = config.getString(path+".trim_data.material");
                String pattern = config.getString(path+".trim_data.pattern");
                trimData = new KitItemTrimData(pattern,material);
            }

            kitItem = new KitItem(id);
            kitItem.setName(name);
            kitItem.setLore(lore);
            kitItem.setAmount(amount);
            kitItem.setDurability(durability);
            kitItem.setCustomModelData(customModelData);
            kitItem.setColor(color);
            kitItem.setEnchants(enchants);
            kitItem.setFlags(flags);
            kitItem.setBookEnchants(bookEnchants);
            kitItem.setNbt(nbtList);
            kitItem.setAttributes(attributes);
            kitItem.setCanPlace(canPlace);
            kitItem.setCanDestroy(canDestroy);
            kitItem.setSkullData(skullData);
            kitItem.setPotionData(potionData);
            kitItem.setFireworkData(fireworkData);
            kitItem.setBannerData(bannerData);
            kitItem.setBookData(bookData);
            kitItem.setTrimData(trimData);
            kitItem.setCustomModelComponentData(customModelComponentData);
            kitItem.setHideTooltip(hideTooltip);
            kitItem.setTooltipStyle(tooltipStyle);
            kitItem.setModel(model);
        }

        kitItem.setOffhand(offhand);
        kitItem.setPreviewSlot(previewSlot);

        return kitItem;
    }

    public KitItem getKitItemFromV1Config(FileConfiguration config, String path){
        String id = config.getString(path+".id");
        String name = config.contains(path+".name") ? config.getString(path+".name") : null;
        List<String> lore = config.contains(path+".lore") ? config.getStringList(path+".lore") : null;
        int amount = config.contains(path+".amount") ? Integer.parseInt(config.getString(path+".amount")) : 1;
        short durability = config.contains(path+".durability") ? Short.parseShort(config.getString(path+".durability")) : 0;
        int customModelData = config.contains(path+".custom_model_data") ? Integer.parseInt(config.getString(path+".custom_model_data")) : 0;
        int color = config.contains(path+".color") ? Integer.parseInt(config.getString(path+".color")) : 0;
        List<String> enchants = config.contains(path+".enchants") ? config.getStringList(path+".enchants") : null;
        List<String> flags = config.contains(path+".hide-flags") ? config.getStringList(path+".hide-flags") : null;
        List<String> bookEnchants = config.contains(path+".book-enchants") ? config.getStringList(path+".book-enchants") : null;

        KitItemPotionData potionData = null;
        if(config.contains(path+".potion-type")) {
            List<String> potionEffects = null;
            boolean extended = false;
            boolean upgraded = false;
            String potionType = null;
            int potionColor = 0;
            if(config.contains(path+".potion-effects")) {
                potionEffects = config.getStringList(path+".potion-effects");
            }
            if(config.contains(path+".potion-extended")) {
                extended = Boolean.parseBoolean(config.getString(path+".potion-extended"));
            }
            if(config.contains(path+".potion-upgraded")) {
                upgraded = Boolean.parseBoolean(config.getString(path+".potion-upgraded"));
            }
            potionType = config.getString(path+".potion-type");
            if(config.contains(path+".potion-color")) {
                potionColor = config.getInt(path+".potion-color");
            }

            potionData = new KitItemPotionData(upgraded,extended,potionType,potionColor,potionEffects);
        }
        KitItemBookData bookData = null;
        if(config.contains(path+".book-title")) {
            List<String> pages = config.getStringList(path+".book-pages");
            String author = null;
            String generation = null;
            String title = null;
            if(config.contains(path+".book-author")) {
                author = config.getString(path+".book-author");
            }
            if(config.contains(path+".book-generation")) {
                generation = config.getString(path+".book-generation");
            }
            title = config.getString(path+".book-title");

            bookData = new KitItemBookData(pages,author,generation,title);
        }
        KitItemFireworkData fireworkData = null;
        if(config.contains(path+".firework-effects")) {
            List<String> rocketEffects = null;
            int power = 0;
            rocketEffects = config.getStringList(path+".firework-effects");
            if(config.contains(path+".firework-power")) {
                power = Integer.parseInt(config.getString(path+".firework-power"));
            }

            fireworkData = new KitItemFireworkData(rocketEffects,null,power);
        }
        KitItemBannerData bannerData = null;
        if(config.contains(path+".banner-pattern")) {
            List<String> patterns = null;
            String baseColor = null;
            String[] pattern = config.getString(path+".banner-pattern").split(";");
            patterns = new ArrayList<>();
            for(String p : pattern){
                String[] pSep = p.split(":");
                patterns.add(pSep[0]+";"+pSep[1]);
            }
            if(config.contains(path+".banner-color")) {
                baseColor = config.getString(path+".banner-color");
            }

            bannerData = new KitItemBannerData(patterns,baseColor);
        }
        KitItemSkullData skullData = null;
        if(config.contains(path+".skull-texture")) {
            String skullTexture = null;
            String skullId = null;
            skullTexture = config.getString(path+".skull-texture");
            skullId = UUID.randomUUID().toString();
            skullData = new KitItemSkullData(null,skullTexture,skullId);
        }

        List<String> nbtList = new ArrayList<>();
        if(config.contains(path+".nbt")){
            nbtList = config.getStringList(path+".nbt");
            for(int i=0;i<nbtList.size();i++){
                nbtList.set(i,nbtList.get(i).replace(";","|"));
            }
        }

        List<String> attributes = new ArrayList<>();
        if(config.contains(path+".attributes")){
            for(String attributeName : config.getConfigurationSection(path+".attributes").getKeys(false)){
                String attribute = config.getString(path+".attributes."+attributeName+".modifiers");
                attributes.add(attribute);
            }
        }

        boolean offhand = config.contains(path+".offhand") ? config.getBoolean(path+".offhand") : false;

        KitItem kitItem = new KitItem(id);
        kitItem.setName(name);
        kitItem.setLore(lore);
        kitItem.setAmount(amount);
        kitItem.setDurability(durability);
        kitItem.setCustomModelData(customModelData);
        kitItem.setColor(color);
        kitItem.setEnchants(enchants);
        kitItem.setFlags(flags);
        kitItem.setBookEnchants(bookEnchants);
        kitItem.setNbt(nbtList);
        kitItem.setAttributes(attributes);
        kitItem.setSkullData(skullData);
        kitItem.setPotionData(potionData);
        kitItem.setFireworkData(fireworkData);
        kitItem.setBannerData(bannerData);
        kitItem.setBookData(bookData);

        kitItem.setOffhand(offhand);

        return kitItem;
    }

    public void replaceVariables(ItemStack item, ArrayList<KitVariable> variables){
        boolean useMiniMessage = plugin.getConfigsManager().getMainConfigManager().isUseMiniMessage();
        if(item.hasItemMeta()){
            ItemMeta meta = item.getItemMeta();
            if(meta.hasDisplayName()){
                if(useMiniMessage){
                    Component name = meta.displayName();
                    Component newName = name;
                    for(KitVariable variable : variables){
                        newName = newName.replaceText(TextReplacementConfig.builder()
                                .matchLiteral(variable.getVariable())
                                .replacement(MiniMessage.miniMessage().deserialize(variable.getValue()))
                                .build());
                    }
                    meta.displayName(newName);
                }else{
                    String newName = meta.getDisplayName();
                    for(KitVariable variable : variables){
                        newName = newName.replace(variable.getVariable(),variable.getValue());
                    }
                    meta.setDisplayName(MessagesManager.getLegacyColoredMessage(newName));
                }
            }

            if(meta.hasLore()){
                if(useMiniMessage){
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
                }else{
                    List<String> lore = meta.getLore();
                    for(int i=0;i<lore.size();i++){
                        for(KitVariable variable : variables){
                            String line = lore.get(i).replace(variable.getVariable(),variable.getValue());
                            lore.set(i,MessagesManager.getLegacyColoredMessage(line));
                        }
                    }
                    meta.setLore(lore);
                }
            }
            item.setItemMeta(meta);
        }
    }


}
