package pk.ajneb97.model.item;

import java.util.ArrayList;
import java.util.List;

public record KitItemBannerData(List<String> patterns, String baseColor) {
    public KitItemBannerData copy(){
        return new KitItemBannerData(new ArrayList<>(patterns),baseColor);
    }
}
