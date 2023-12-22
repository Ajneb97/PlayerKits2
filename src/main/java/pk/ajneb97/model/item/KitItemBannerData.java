package pk.ajneb97.model.item;

import java.util.ArrayList;
import java.util.List;

public class KitItemBannerData {

    private List<String> patterns;
    private String baseColor;
    public KitItemBannerData(List<String> patterns, String baseColor) {
        super();
        this.patterns = patterns;
        this.baseColor = baseColor;
    }
    public List<String> getPatterns() {
        return patterns;
    }
    public void setPatterns(List<String> patterns) {
        this.patterns = patterns;
    }
    public String getBaseColor() {
        return baseColor;
    }
    public void setBaseColor(String baseColor) {
        this.baseColor = baseColor;
    }

    public KitItemBannerData clone(){
        return new KitItemBannerData(new ArrayList<>(patterns),baseColor);
    }
}
