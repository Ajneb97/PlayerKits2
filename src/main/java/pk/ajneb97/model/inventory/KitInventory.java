package pk.ajneb97.model.inventory;

import java.util.ArrayList;
import java.util.List;

public class KitInventory {

    private String name;
    private int slots;
    private String title;
    private List<ItemKitInventory> items;

    public KitInventory(String name, int slots, String title, List<ItemKitInventory> items) {
        this.name = name;
        this.slots = slots;
        this.title = title;
        this.items = items;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getSlots() {
        return slots;
    }

    public void setSlots(int slots) {
        this.slots = slots;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public List<ItemKitInventory> getItems() {
        return items;
    }

    public void setItems(List<ItemKitInventory> items) {
        this.items = items;
    }

    public int addKitItemOnFirstEmptySlot(String kitName){
        List<Integer> occupiedSlots = new ArrayList<>();
        for(ItemKitInventory item : items){
            for(int slot : item.getSlots()){
                occupiedSlots.add(slot);
            }
        }

        for(int i=0;i<slots;i++){
            if(!occupiedSlots.contains(i)){
                items.add(new ItemKitInventory(i+"",null,null,null,"kit: "+kitName));
                return i;
            }
        }
        return -1;
    }

    public void addKitItemOnSlot(String kitName,int slot){
        List<Integer> newSlotList = new ArrayList<>();
        newSlotList.add(slot);
        items.add(new ItemKitInventory(slot+"",null,null,null,"kit: "+kitName));
    }
}
