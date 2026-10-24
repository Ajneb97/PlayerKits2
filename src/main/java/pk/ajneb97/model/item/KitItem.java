package pk.ajneb97.model.item;

import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import pk.ajneb97.PlayerKits2;
import pk.ajneb97.utils.ItemUtils;

import java.util.ArrayList;
import java.util.List;

public class KitItem{

    private String id;
    private int amount;
    private String name;
    private List<String> lore;
    private short durability;
    private int customModelData;
    private List<String> enchants;
    private List<String> flags;
    private List<String> bookEnchants;
    private int color;
    private List<String> nbt;

    private List<String> attributes;

    private List<String> canDestroy;
    private List<String> canPlace;

    private KitItemSkullData skullData;
    private KitItemPotionData potionData;
    private KitItemFireworkData fireworkData;
    private KitItemBannerData bannerData;
    private KitItemBookData bookData;
    private KitItemTrimData trimData;
    private KitItemCustomModelComponentData customModelComponentData;

    private boolean hideTooltip;
    private String tooltipStyle;
    private String model;

    private boolean offhand;
    private int previewSlot;

    private ItemStack originalItem;

    public KitItem(String id) {
        this.id = id;
        this.amount = 1;
        this.durability = 0;
        this.customModelData = 0;
        this.color = 0;
        this.previewSlot = -1;
    }

    public KitItem(ItemStack item){
        this.previewSlot = -1;
        this.originalItem = item;
    }

    public ItemStack getOriginalItem() {
        return originalItem;
    }

    public void setOriginalItem(ItemStack originalItem) {
        this.originalItem = originalItem;
    }

    public String getId() {
        return id;
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<String> getLore() {
        return lore;
    }

    public void setLore(List<String> lore) {
        this.lore = lore;
    }

    public short getDurability() {
        return durability;
    }

    public void setDurability(short durability) {
        this.durability = durability;
    }

    public int getCustomModelData() {
        return customModelData;
    }

    public void setCustomModelData(int customModelData) {
        this.customModelData = customModelData;
    }

    public List<String> getEnchants() {
        return enchants;
    }

    public void setEnchants(List<String> enchants) {
        this.enchants = enchants;
    }

    public List<String> getFlags() {
        return flags;
    }

    public void setFlags(List<String> flags) {
        this.flags = flags;
    }

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
    }

    public KitItemSkullData getSkullData() {
        return skullData;
    }

    public void setSkullData(KitItemSkullData skullData) {
        this.skullData = skullData;
    }

    public KitItemPotionData getPotionData() {
        return potionData;
    }

    public void setPotionData(KitItemPotionData potionData) {
        this.potionData = potionData;
    }

    public List<String> getBookEnchants() {
        return bookEnchants;
    }

    public void setBookEnchants(List<String> bookEnchants) {
        this.bookEnchants = bookEnchants;
    }

    public List<String> getNbt() {
        return nbt;
    }

    public void setNbt(List<String> nbt) {
        this.nbt = nbt;
    }

    public List<String> getAttributes() {
        return attributes;
    }

    public void setAttributes(List<String> attributes) {
        this.attributes = attributes;
    }

    public List<String> getCanDestroy() {
        return canDestroy;
    }

    public void setCanDestroy(List<String> canDestroy) {
        this.canDestroy = canDestroy;
    }

    public List<String> getCanPlace() {
        return canPlace;
    }

    public void setCanPlace(List<String> canPlace) {
        this.canPlace = canPlace;
    }

    public KitItemFireworkData getFireworkData() {
        return fireworkData;
    }

    public void setFireworkData(KitItemFireworkData fireworkData) {
        this.fireworkData = fireworkData;
    }

    public KitItemBannerData getBannerData() {
        return bannerData;
    }

    public void setBannerData(KitItemBannerData bannerData) {
        this.bannerData = bannerData;
    }

    public KitItemBookData getBookData() {
        return bookData;
    }

    public void setBookData(KitItemBookData bookData) {
        this.bookData = bookData;
    }

    public KitItemTrimData getTrimData() {
        return trimData;
    }

    public void setTrimData(KitItemTrimData trimData) {
        this.trimData = trimData;
    }

    public KitItemCustomModelComponentData getCustomModelComponentData() {
        return customModelComponentData;
    }

    public void setCustomModelComponentData(KitItemCustomModelComponentData customModelComponentData) {
        this.customModelComponentData = customModelComponentData;
    }
    public boolean isHideTooltip() {
        return hideTooltip;
    }

    public void setHideTooltip(boolean hideTooltip) {
        this.hideTooltip = hideTooltip;
    }

    public String getTooltipStyle() {
        return tooltipStyle;
    }

    public void setTooltipStyle(String tooltipStyle) {
        this.tooltipStyle = tooltipStyle;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public boolean isOffhand() {
        return offhand;
    }

    public void setOffhand(boolean offhand) {
        this.offhand = offhand;
    }

    public int getPreviewSlot() {
        return previewSlot;
    }

    public void setPreviewSlot(int previewSlot) {
        this.previewSlot = previewSlot;
    }

    private static void removeOffHandLoreLines(List<String> lines){
        for(int i=0;i<2 && !lines.isEmpty();i++){
            lines.removeLast();
        }
    }

    @SuppressWarnings("deprecation")
    public void removeOffHandFromEditInventory(PlayerKits2 plugin){

        if(originalItem != null){
            ItemMeta meta = originalItem.getItemMeta();
            List<String> originalLore = meta != null ? meta.getLore() : null;
            if(originalLore != null){
                removeOffHandLoreLines(originalLore);
                meta.setLore(originalLore);
                originalItem.setItemMeta(meta);
            }
            originalItem = ItemUtils.removeTagItem(plugin,originalItem,"playerkits_offhand");
            return;
        }
        if(lore != null){
            removeOffHandLoreLines(lore);
        }
        if(nbt != null){
            for(int i=0;i<nbt.size();i++){
                if(nbt.get(i).startsWith("playerkits_offhand")){
                    nbt.remove(i);
                    return;
                }
            }
        }
    }

    public KitItem copy(){
        KitItem kitItem = new KitItem(id);
        kitItem.setAmount(amount);
        kitItem.setName(name);
        kitItem.setLore(lore != null ? new ArrayList<>(lore) : null);
        kitItem.setDurability(durability);
        kitItem.setCustomModelData(customModelData);
        kitItem.setEnchants(enchants != null ? new ArrayList<>(enchants) : null);
        kitItem.setFlags(flags != null ? new ArrayList<>(flags) : null);
        kitItem.setBookEnchants(bookEnchants != null ? new ArrayList<>(bookEnchants) : null);
        kitItem.setColor(color);
        kitItem.setNbt(nbt != null ? new ArrayList<>(nbt) : null);
        kitItem.setAttributes(attributes != null ? new ArrayList<>(attributes) : null);
        kitItem.setCanDestroy(canDestroy != null ? new ArrayList<>(canDestroy) : null);
        kitItem.setCanPlace(canPlace != null ? new ArrayList<>(canPlace) : null);
        kitItem.setOriginalItem(originalItem != null ? originalItem.clone() : null);

        kitItem.setSkullData(skullData != null ? skullData.copy() : null);
        kitItem.setPotionData(potionData != null ? potionData.copy() : null);
        kitItem.setFireworkData(fireworkData != null ? fireworkData.copy() : null);
        kitItem.setBannerData(bannerData != null ? bannerData.copy() : null);
        kitItem.setBookData(bookData != null ? bookData.copy() : null);
        kitItem.setTrimData(trimData != null ? trimData.copy() : null);
        kitItem.setCustomModelComponentData(customModelComponentData != null ? customModelComponentData.copy() : null);

        kitItem.setHideTooltip(hideTooltip);
        kitItem.setTooltipStyle(tooltipStyle);

        kitItem.setPreviewSlot(previewSlot);
        kitItem.setOffhand(offhand);
        kitItem.setModel(model);

        return kitItem;
    }
}
