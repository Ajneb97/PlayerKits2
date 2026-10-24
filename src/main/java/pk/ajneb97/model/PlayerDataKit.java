package pk.ajneb97.model;

public class PlayerDataKit {

    private final String name;
    private volatile long cooldown;
    private volatile boolean oneTime;
    private volatile boolean bought;

    public PlayerDataKit(String name) {
        this.name = name;
        this.cooldown = 0;
        this.oneTime = false;
        this.bought = false;
    }

    public String getName() {
        return name;
    }

    public long getCooldown() {
        return cooldown;
    }

    public void setCooldown(long cooldown) {
        this.cooldown = cooldown;
    }

    public boolean isOneTime() {
        return oneTime;
    }

    public void setOneTime(boolean oneTime) {
        this.oneTime = oneTime;
    }

    public boolean isBought() {
        return bought;
    }

    public void setBought(boolean bought) {
        this.bought = bought;
    }
}
