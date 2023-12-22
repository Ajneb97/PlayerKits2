package pk.ajneb97.model.item;

import java.util.ArrayList;
import java.util.List;

public class KitItemFireworkData {

    private List<String> fireworkRocketEffects;
    private String fireworkStarEffect;
    private int fireworkPower;
    public KitItemFireworkData(List<String> fireworkRocketEffects, String fireworkStarEffect, int fireworkPower) {
        super();
        this.fireworkRocketEffects = fireworkRocketEffects;
        this.fireworkStarEffect = fireworkStarEffect;
        this.fireworkPower = fireworkPower;
    }
    public List<String> getFireworkRocketEffects() {
        return fireworkRocketEffects;
    }
    public void setFireworkRocketEffects(List<String> fireworkRocketEffects) {
        this.fireworkRocketEffects = fireworkRocketEffects;
    }
    public String getFireworkStarEffect() {
        return fireworkStarEffect;
    }
    public void setFireworkStarEffect(String fireworkStarEffect) {
        this.fireworkStarEffect = fireworkStarEffect;
    }
    public int getFireworkPower() {
        return fireworkPower;
    }
    public void setFireworkPower(int fireworkPower) {
        this.fireworkPower = fireworkPower;
    }

    public KitItemFireworkData clone(){
        return new KitItemFireworkData(new ArrayList<>(fireworkRocketEffects),fireworkStarEffect,fireworkPower);
    }
}
