package pk.ajneb97.utils;

import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import pk.ajneb97.managers.MessagesManager;
import pk.ajneb97.model.item.KitItemSkullData;

import java.util.List;

public class InventoryItem {

	private final Inventory inventory;
	private final int slot;
	private final ItemStack item;
	private final ItemMeta meta;
	private String headTexture;

	public InventoryItem(Inventory inventory, int slot, Material material) {
		this.inventory = inventory;
		this.item = new ItemStack(material);
		this.meta = item.getItemMeta();
		this.slot = slot;
	}

	@SuppressWarnings("deprecation")
	public InventoryItem dataValue(short dataValue) {
		item.setDurability(dataValue);
		return this;
	}

	@SuppressWarnings("deprecation")
	public InventoryItem name(String name) {
		meta.setDisplayName(MessagesManager.getLegacyColoredMessage(name));
		return this;
	}

	@SuppressWarnings("deprecation")
	public InventoryItem lore(List<String> lore) {
		lore.replaceAll(MessagesManager::getLegacyColoredMessage);
		meta.setLore(lore);
		return this;
	}

	@SuppressWarnings("deprecation")
	public InventoryItem setSkull(String texture){
		if(OtherUtils.isLegacy()){
			item.setDurability((short) 3);
		}
		headTexture = texture;
		return this;
	}

	public void ready() {
		item.setItemMeta(meta);
		if(headTexture != null){
			ItemUtils.setSkullData(item,new KitItemSkullData(null,headTexture,null),null);
		}
		inventory.setItem(slot, item);
	}
}
