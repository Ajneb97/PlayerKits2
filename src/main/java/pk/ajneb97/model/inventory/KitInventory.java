package pk.ajneb97.model.inventory;

import java.util.ArrayList;
import java.util.List;

public record KitInventory(String name, int slots, String title, List<ItemKitInventory> items) {
    public int addKitItemOnFirstEmptySlot(String kitName){
        List<Integer> occupiedSlots = new ArrayList<>();
        for(ItemKitInventory item : items){
            occupiedSlots.addAll(item.getSlots());
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
        items.add(new ItemKitInventory(slot+"",null,null,null,"kit: "+kitName));
    }
}
