package pk.ajneb97.model;

import java.util.ArrayList;
import java.util.List;

public class KitRequirements {

    private boolean oneTimeRequirements;
    private double price;
    private List<String> extraRequirements;
    private List<String> message;
    private List<String> actionsOnBuy;

    public KitRequirements(boolean oneTimeRequirements, List<String> extraRequirements, List<String> message,
                           List<String> actionsOnBuy,double price) {
        this.oneTimeRequirements = oneTimeRequirements;
        this.extraRequirements = extraRequirements;
        this.message = message;
        this.actionsOnBuy = actionsOnBuy;
        this.price = price;
    }

    public KitRequirements(){
        extraRequirements = new ArrayList<>();
        message = new ArrayList<>();
        message.add("&6You need $5000 to get this kit.");
        message.add("&8Status: &7$%vault_eco_balance% &8- %status_symbol_price%");
        actionsOnBuy = new ArrayList<>();
    }

    public boolean isOneTimeRequirements() {
        return oneTimeRequirements;
    }

    public void setOneTimeRequirements(boolean oneTimeRequirements) {
        this.oneTimeRequirements = oneTimeRequirements;
    }


    public List<String> getMessage() {
        return message;
    }

    public void setMessage(List<String> message) {
        this.message = message;
    }

    public List<String> getActionsOnBuy() {
        return actionsOnBuy;
    }

    public void setActionsOnBuy(List<String> actionsOnBuy) {
        this.actionsOnBuy = actionsOnBuy;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public List<String> getExtraRequirements() {
        return extraRequirements;
    }

    public void setExtraRequirements(List<String> extraRequirements) {
        this.extraRequirements = extraRequirements;
    }
}
