package pk.ajneb97.model.item;

import java.util.ArrayList;
import java.util.List;

public record KitItemPotionData(boolean upgraded, boolean extended, String potionType, int potionColor, List<String> potionEffects) {
    public KitItemPotionData copy(){
        return new KitItemPotionData(upgraded,extended,potionType,potionColor,new ArrayList<>(potionEffects));
    }
}
