package pk.ajneb97.model.inventory;

import pk.ajneb97.model.item.KitItem;

import java.util.ArrayList;
import java.util.List;

public class ItemKitInventory {
    private List<Integer> slots;
    private String slotsString;
    private KitItem item;
    private String openInventory;
    private List<String> clickActions;
    private String type;
    private PackIcon packIcon;
    private boolean packHidden;
    private boolean packOnly;

    public ItemKitInventory(String slotsString, KitItem item, String openInventory, List<String> clickActions, String type) {
        slots = new ArrayList<>();
        this.slotsString = slotsString;
        String[] slotsSep = slotsString.split(";");
        for(int i=0;i<slotsSep.length;i++) {
            if(slotsSep[i].contains("-")) {
                String[] newSep = slotsSep[i].split("-");
                int sMin = Integer.valueOf(newSep[0]);
                int sMax = Integer.valueOf(newSep[1]);
                for(int c=sMin;c<=sMax;c++) {
                    slots.add(c);
                }
            }else {
                slots.add(Integer.valueOf(slotsSep[i]));
            }
        }

        this.item = item;
        this.openInventory = openInventory;
        this.clickActions = clickActions;
        this.type = type;
    }

    public List<Integer> getSlots() {
        return slots;
    }

    public void setSlots(List<Integer> slots) {
        this.slots = slots;
    }

    public KitItem getItem() {
        return item;
    }

    public void setItem(KitItem item) {
        this.item = item;
    }

    public String getOpenInventory() {
        return openInventory;
    }

    public void setOpenInventory(String openInventory) {
        this.openInventory = openInventory;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getSlotsString() {
        return slotsString;
    }

    public void setSlotsString(String slotsString) {
        this.slotsString = slotsString;
    }

    public List<String> getClickActions() {
        return clickActions;
    }

    public void setClickActions(List<String> clickActions) {
        this.clickActions = clickActions;
    }

    /** Icon for players with the resource pack ({@code pack_icon}), null for none. */
    public PackIcon getPackIcon() {
        return packIcon;
    }

    public void setPackIcon(PackIcon packIcon) {
        this.packIcon = packIcon;
    }

    /** {@code pack_hidden}: left out for players who see the textured inventory (glass filler). */
    public boolean isPackHidden() {
        return packHidden;
    }

    public void setPackHidden(boolean packHidden) {
        this.packHidden = packHidden;
    }

    /** {@code pack_only}: shown only to players who see the textured inventory. */
    public boolean isPackOnly() {
        return packOnly;
    }

    public void setPackOnly(boolean packOnly) {
        this.packOnly = packOnly;
    }
}
