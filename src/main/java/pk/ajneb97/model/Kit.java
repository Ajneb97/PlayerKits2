package pk.ajneb97.model;

import java.util.Objects;

import org.bukkit.command.CommandSender;
import pk.ajneb97.model.item.KitItem;
import pk.ajneb97.utils.PlayerUtils;

import java.util.ArrayList;

public class Kit {
    private final String name;
    private int cooldown;
    private boolean permissionRequired;
    private boolean clearInventory;
    private String customPermission;
    private boolean oneTime;
    private ArrayList<KitItem> items;
    private ArrayList<KitAction> claimActions;
    private ArrayList<KitAction> errorActions;
    private boolean saveOriginalItems;
    private boolean allowPlaceholdersOnOriginalItems;

    private KitItem displayItemDefault;
    private KitItem displayItemNoPermission;
    private KitItem displayItemCooldown;
    private KitItem displayItemOneTime;
    private KitItem displayItemOneTimeRequirements;
    private KitRequirements requirements;

    private boolean autoArmor;

    public Kit(String name){
        this.name = name;
        this.cooldown = 0;
        this.autoArmor = false;
        this.oneTime = false;
        this.saveOriginalItems = false;
    }

    public String getName() {
        return name;
    }

    public int getCooldown() {
        return cooldown;
    }

    public void setCooldown(int cooldown) {
        this.cooldown = cooldown;
    }

    public boolean isPermissionRequired() {
        return permissionRequired;
    }

    public void setPermissionRequired(boolean permissionRequired) {
        this.permissionRequired = permissionRequired;
    }

    public ArrayList<KitItem> getItems() {
        return items;
    }

    public void setItems(ArrayList<KitItem> items) {
        this.items = items;
    }

    public KitItem getDisplayItemDefault() {
        return displayItemDefault;
    }

    public void setDisplayItemDefault(KitItem displayItemDefault) {
        this.displayItemDefault = displayItemDefault;
    }

    public KitItem getDisplayItemNoPermission() {
        return displayItemNoPermission;
    }

    public void setDisplayItemNoPermission(KitItem displayItemNoPermission) {
        this.displayItemNoPermission = displayItemNoPermission;
    }

    public KitItem getDisplayItemCooldown() {
        return displayItemCooldown;
    }

    public void setDisplayItemCooldown(KitItem displayItemCooldown) {
        this.displayItemCooldown = displayItemCooldown;
    }

    public boolean isAutoArmor() {
        return autoArmor;
    }

    public void setAutoArmor(boolean autoArmor) {
        this.autoArmor = autoArmor;
    }

    public boolean isOneTime() {
        return oneTime;
    }

    public void setOneTime(boolean oneTime) {
        this.oneTime = oneTime;
    }

    public ArrayList<KitAction> getClaimActions() {
        return claimActions;
    }

    public void setClaimActions(ArrayList<KitAction> claimActions) {
        this.claimActions = claimActions;
    }

    public ArrayList<KitAction> getErrorActions() {
        return errorActions;
    }

    public void setErrorActions(ArrayList<KitAction> errorActions) {
        this.errorActions = errorActions;
    }

    public KitItem getDisplayItemOneTime() {
        return displayItemOneTime;
    }

    public void setDisplayItemOneTime(KitItem displayItemOneTime) {
        this.displayItemOneTime = displayItemOneTime;
    }

    public KitItem getDisplayItemOneTimeRequirements() {
        return displayItemOneTimeRequirements;
    }

    public void setDisplayItemOneTimeRequirements(KitItem displayItemOneTimeRequirements) {
        this.displayItemOneTimeRequirements = displayItemOneTimeRequirements;
    }

    public KitRequirements getRequirements() {
        return requirements;
    }

    public void setRequirements(KitRequirements requirements) {
        this.requirements = requirements;
    }

    public String getCustomPermission() {
        return customPermission;
    }

    public void setCustomPermission(String customPermission) {
        this.customPermission = customPermission;
    }

    public boolean isSaveOriginalItems() {
        return saveOriginalItems;
    }

    public void setSaveOriginalItems(boolean saveOriginalItems) {
        this.saveOriginalItems = saveOriginalItems;
    }

    public boolean isClearInventory() {
        return clearInventory;
    }

    public void setClearInventory(boolean clearInventory) {
        this.clearInventory = clearInventory;
    }

    public boolean isAllowPlaceholdersOnOriginalItems() {
        return allowPlaceholdersOnOriginalItems;
    }

    public void setAllowPlaceholdersOnOriginalItems(boolean allowPlaceholdersOnOriginalItems) {
        this.allowPlaceholdersOnOriginalItems = allowPlaceholdersOnOriginalItems;
    }

    public boolean playerHasPermission(CommandSender player){
        if(permissionRequired){
            return PlayerUtils.isPlayerKitsAdmin(player)
                    || player.hasPermission(Objects.requireNonNullElse(customPermission, "playerkits.kit."+name));
        }
        return true;
    }

    public void setDefaults(Kit defaultKit){
        cooldown = defaultKit.getCooldown();
        oneTime = defaultKit.isOneTime();
        clearInventory = defaultKit.isClearInventory();
        permissionRequired = defaultKit.isPermissionRequired();
        if(defaultKit.getClaimActions() != null){
            ArrayList<KitAction> actions = new ArrayList<>();
            for(KitAction action : defaultKit.getClaimActions()){
                actions.add(action.copy());
            }
            claimActions = actions;
        }
        if(defaultKit.getErrorActions() != null){
            ArrayList<KitAction> actions = new ArrayList<>();
            for(KitAction action : defaultKit.getErrorActions()){
                actions.add(action.copy());
            }
            errorActions = actions;
        }
        displayItemDefault = defaultKit.getDisplayItemDefault() != null ? defaultKit.getDisplayItemDefault().copy() : null;
        displayItemNoPermission = defaultKit.getDisplayItemNoPermission() != null ? defaultKit.getDisplayItemNoPermission().copy() : null;
        displayItemCooldown = defaultKit.getDisplayItemCooldown() != null ? defaultKit.getDisplayItemCooldown().copy() : null;
        displayItemOneTime = defaultKit.getDisplayItemOneTime() != null ? defaultKit.getDisplayItemOneTime().copy() : null;
        displayItemOneTimeRequirements = defaultKit.getDisplayItemOneTimeRequirements() != null ? defaultKit.getDisplayItemOneTimeRequirements().copy() : null;
        autoArmor = defaultKit.isAutoArmor();
    }
}
