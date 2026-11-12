package pk.ajneb97.model.inventory;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import pk.ajneb97.model.item.KitItem;

/**
 * The icon of an inventory item for players with the resource pack ({@code pack_icon} of a slot in
 * {@code inventory.yml}): {@code id} replaces the material, {@code custom_model_data} and {@code model}
 * (item_model, 1.21.4+) replace the model. Name, lore, flags and click actions stay those of {@code item}.
 * Used for flat PAPER icons that only exist in the pack while players without it keep the vanilla item.
 */
public class PackIcon {

    public static final String KEY = "pack_icon";

    private final String id;
    private final int customModelData;
    private final String model;

    public PackIcon(String id, int customModelData, String model) {
        this.id = id;
        this.customModelData = customModelData;
        this.model = model;
    }

    /**
     * @param section the {@code pack_icon} section, may be null
     * @return the icon, or null if the section is missing or sets nothing
     */
    public static PackIcon fromConfig(ConfigurationSection section) {
        if (section == null) {
            return null;
        }
        String id = section.getString("id");
        int cmd = section.getInt("custom_model_data", 0);
        String model = section.getString("model");
        if ((id == null || id.trim().isEmpty()) && cmd == 0 && (model == null || model.trim().isEmpty())) {
            return null;
        }
        return new PackIcon(id, cmd, model);
    }

    public void save(FileConfiguration config, String path) {
        if (id != null) {
            config.set(path + ".id", id);
        }
        if (customModelData != 0) {
            config.set(path + ".custom_model_data", customModelData);
        }
        if (model != null) {
            config.set(path + ".model", model);
        }
    }

    /**
     * @param item the slot's normal item, not changed
     * @return a copy with this icon applied
     */
    public KitItem apply(KitItem item) {
        KitItem copy = item.clone();
        if (id != null && !id.trim().isEmpty()) {
            copy.setId(id);
        }
        if (customModelData != 0) {
            copy.setCustomModelData(customModelData);
        }
        if (model != null && !model.trim().isEmpty()) {
            copy.setModel(model);
        }
        return copy;
    }
}
