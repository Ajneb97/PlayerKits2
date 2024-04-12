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
    //Format:
    //<name>;<operation>;<amount>;<uuid>;<slot>
    private List<String> attributes;

    //For Paper only
    private List<String> canDestroy;
    private List<String> canPlace;

    private KitItemSkullData skullData;
    private KitItemPotionData potionData;
    private KitItemFireworkData fireworkData;
    private KitItemBannerData bannerData;
    private KitItemBookData bookData;
    private KitItemTrimData trimData;

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

    public void setId(String id) {
        this.id = id;
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

    public void removeOffHandFromEditInventory(PlayerKits2 plugin){
        //Assumes that has the lore and the nbt
        if(originalItem != null){
            ItemMeta meta = originalItem.getItemMeta();
            List<String> lore = meta.getLore();
            lore.remove(lore.size()-1);
            lore.remove(lore.size()-1);
            meta.setLore(lore);
            originalItem.setItemMeta(meta);
            originalItem = ItemUtils.removeTagItem(plugin,originalItem,"playerkits_offhand");
            return;
        }
        lore.remove(lore.size()-1);
        lore.remove(lore.size()-1);
        for(int i=0;i<nbt.size();i++){
            if(nbt.get(i).startsWith("playerkits_offhand")){
                nbt.remove(i);
                return;
            }
        }
    }

    public KitItem clone(){
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
        kitItem.setCanDestroy(canPlace != null ? new ArrayList<>(canPlace) : null);

        kitItem.setSkullData(skullData != null ? skullData.clone() : null);
        kitItem.setPotionData(potionData != null ? potionData.clone() : null);
        kitItem.setFireworkData(fireworkData != null ? fireworkData.clone() : null);
        kitItem.setBannerData(bannerData != null ? bannerData.clone() : null);
        kitItem.setBookData(bookData != null ? bookData.clone() : null);
        kitItem.setTrimData(trimData != null ? trimData.clone() : null);

        kitItem.setPreviewSlot(previewSlot);
        kitItem.setOffhand(offhand);

        return kitItem;
    }
}
