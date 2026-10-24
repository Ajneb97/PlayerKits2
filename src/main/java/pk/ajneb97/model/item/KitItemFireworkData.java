package pk.ajneb97.model.item;

import java.util.ArrayList;
import java.util.List;

public record KitItemFireworkData(List<String> fireworkRocketEffects, String fireworkStarEffect, int fireworkPower) {
    public KitItemFireworkData copy(){
        return new KitItemFireworkData(new ArrayList<>(fireworkRocketEffects),fireworkStarEffect,fireworkPower);
    }
}
