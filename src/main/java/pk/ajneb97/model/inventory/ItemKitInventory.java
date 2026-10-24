package pk.ajneb97.model.inventory;

import pk.ajneb97.model.item.KitItem;

import java.util.ArrayList;
import java.util.List;

public class ItemKitInventory {
    private final List<Integer> slots;
    private final String slotsString;
    private final KitItem item;
    private final String openInventory;
    private final List<String> clickActions;
    private final String type;

    public ItemKitInventory(String slotsString, KitItem item, String openInventory, List<String> clickActions, String type) {
        slots = new ArrayList<>();
        this.slotsString = slotsString;
        String[] slotsSep = slotsString.split(";");
        for(String slotPart : slotsSep) {
            if(slotPart.contains("-")) {
                String[] newSep = slotPart.split("-");
                int sMin = Integer.parseInt(newSep[0]);
                int sMax = Integer.parseInt(newSep[1]);
                for(int c=sMin;c<=sMax;c++) {
                    slots.add(c);
                }
            }else {
                slots.add(Integer.parseInt(slotPart));
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

    public KitItem getItem() {
        return item;
    }

    public String getOpenInventory() {
        return openInventory;
    }

    public String getType() {
        return type;
    }

    public String getSlotsString() {
        return slotsString;
    }

    public List<String> getClickActions() {
        return clickActions;
    }
}
