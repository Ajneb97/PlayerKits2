package pk.ajneb97.model.inventory;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import pk.ajneb97.PlayerKits2;
import pk.ajneb97.utils.OtherUtils;

import java.lang.reflect.Method;
import java.util.regex.Pattern;

/**
 * The look of a kit inventory for players who have the server resource pack, read from the optional
 * {@code pack} section of the inventory in {@code inventory.yml}:
 * <pre>
 * main_inventory:
 *   slots: 54
 *   title: '&amp;8Kitler'
 *   pack:
 *     title: '&amp;f%img_offset_-48%%img_my_frame%%img_offset_-167%&amp;f[title]'
 *     max_title_length: 26
 *   0-53:
 *     item: {id: BLACK_STAINED_GLASS_PANE, name: ' '}
 *     pack_hidden: true          # filler: the texture is the frame
 *   49:
 *     item: {id: BARRIER, name: '&amp;cKapat'}
 *     pack_icon: {id: PAPER, custom_model_data: 84001}   # flat icon for pack players only
 *   50:
 *     item: {...}
 *     pack_only: true            # a button that only the textured menu has
 * </pre>
 * {@code [title]} is the inventory's normal title (after {@code %kit%} / {@code %kit_display%}) without
 * colours, cut to {@code max_title_length} characters. PlaceholderAPI placeholders are replaced afterwards,
 * so ItemsAdder's {@code %img_...%} glyphs work. The pack title is always read with legacy {@code &amp;}
 * codes. Players without the pack, or an inventory without a {@code pack} section, keep the inventory
 * exactly as it is: no slot is hidden, no icon is swapped.
 */
public class PackStyle {

    public static final String SECTION = "pack";
    private static final int DEFAULT_MAX_TITLE = 26;
    /** Legacy colour and format codes, also hex ones in every form PlayerKits2 reads. */
    private static final Pattern COLOUR = Pattern
            .compile("(?i)(?:[§&](?:#[0-9A-F]{6}|x(?:[§&][0-9A-F]){6}|[0-9A-FK-ORX])|#[0-9A-F]{6})");
    private static final Method HAS_RESOURCE_PACK = method("hasResourcePack");
    private static final Method CLIENT_BRAND = method("getClientBrandName");

    private final String title;
    private final int maxTitleLength;

    public PackStyle(String title, int maxTitleLength) {
        this.title = title;
        this.maxTitleLength = Math.max(4, maxTitleLength);
    }

    private static Method method(String name) {
        try {
            return Player.class.getMethod(name);
        } catch (Throwable t) {
            return null;
        }
    }

    /**
     * @param section the inventory's {@code pack} section, may be null
     * @return the pack look, or null when there is no section or no {@code title}
     */
    public static PackStyle fromConfig(ConfigurationSection section) {
        if (section == null) {
            return null;
        }
        String title = section.getString("title", "");
        if (title == null || title.trim().isEmpty()) {
            return null;
        }
        return new PackStyle(title, section.getInt("max_title_length", DEFAULT_MAX_TITLE));
    }

    public void save(FileConfiguration config, String path) {
        config.set(path + ".title", title);
        if (maxTitleLength != DEFAULT_MAX_TITLE) {
            config.set(path + ".max_title_length", maxTitleLength);
        }
    }

    /**
     * Whether a player sees resource pack art: Paper's {@code Player#hasResourcePack()}, and never for
     * Bedrock players (Geyser client brand, or a Floodgate name starting with a dot). Same rule as the
     * servers' Skript {@code hasPack}. Without these methods: no pack.
     */
    public static boolean hasPack(Player player) {
        if (player == null || player.getName().startsWith(".") || HAS_RESOURCE_PACK == null) {
            return false;
        }
        try {
            if (CLIENT_BRAND != null && "Geyser".equalsIgnoreCase(String.valueOf(CLIENT_BRAND.invoke(player)))) {
                return false;
            }
            return Boolean.TRUE.equals(HAS_RESOURCE_PACK.invoke(player));
        } catch (Throwable t) {
            return false;
        }
    }

    /**
     * @param text a title, may carry colour codes
     * @return the title without colours, cut to the longest that fits the frame's band
     */
    public String fitTitle(String text) {
        String plain = text == null ? "" : COLOUR.matcher(text).replaceAll("").trim();
        if (plain.length() > maxTitleLength) {
            plain = plain.substring(0, maxTitleLength - 1).trim() + "…";
        }
        return plain;
    }

    /**
     * @param normalTitle the inventory's normal title, placeholders of the kit already replaced
     * @return the pack title with {@code [title]} and PlaceholderAPI placeholders resolved, still with
     * {@code &} codes
     */
    public String title(String normalTitle, Player player, PlayerKits2 plugin) {
        String raw = title.replace("[title]", fitTitle(normalTitle));
        if (raw.indexOf('%') >= 0) {
            raw = OtherUtils.replaceGlobalVariables(raw, player, plugin);
        }
        return raw;
    }

    public String getTitle() {
        return title;
    }

    public int getMaxTitleLength() {
        return maxTitleLength;
    }
}
