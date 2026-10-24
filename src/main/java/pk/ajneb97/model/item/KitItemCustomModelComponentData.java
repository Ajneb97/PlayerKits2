package pk.ajneb97.model.item;

import java.util.ArrayList;
import java.util.List;

public record KitItemCustomModelComponentData(List<String> flags, List<String> colors, List<String> floats, List<String> strings) {
    public KitItemCustomModelComponentData copy(){
        return new KitItemCustomModelComponentData(new ArrayList<>(flags),new ArrayList<>(colors),new ArrayList<>(floats),new ArrayList<>(strings));
    }
}
