package pk.ajneb97.model;

import pk.ajneb97.model.layout.PlayerKitLayout;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class PlayerData {

    private String name;
    private UUID uuid;

    private ArrayList<PlayerDataKit> kits;
    private Map<String, PlayerKitLayout> layouts;
    private boolean modified;

    public PlayerData(UUID uuid, String name){
        this.name = name;
        this.uuid = uuid;
        this.kits = new ArrayList<>();
        this.layouts = new ConcurrentHashMap<>();
        this.modified = false;
    }

    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ArrayList<PlayerDataKit> getKits() {
        return kits;
    }

    public void setKits(ArrayList<PlayerDataKit> kits) {
        this.kits = kits;
    }

    public void addKit(PlayerDataKit kit){
        this.kits.add(kit);
    }

    public boolean isModified() {
        return modified;
    }

    public void setModified(boolean modified) {
        this.modified = modified;
    }

    public PlayerDataKit getKit(String kitName){
        for(PlayerDataKit kit : kits){
            if(kit.getName().equals(kitName)){
                return kit;
            }
        }
        return null;
    }

    public boolean setKitCooldown(String kitName, long cooldown){
        PlayerDataKit playerDataKit = getKit(kitName);
        boolean creating = false;
        if(playerDataKit == null){
            playerDataKit = new PlayerDataKit(kitName);
            kits.add(playerDataKit);
            creating = true;
        }

        playerDataKit.setCooldown(cooldown);
        return creating;
    }

    public long getKitCooldown(String kitName){
        PlayerDataKit playerDataKit = getKit(kitName);
        if(playerDataKit == null){
            return 0;
        }else{
            return playerDataKit.getCooldown();
        }
    }

    public boolean setKitOneTime(String kitName){
        PlayerDataKit playerDataKit = getKit(kitName);
        boolean creating = false;
        if(playerDataKit == null){
            playerDataKit = new PlayerDataKit(kitName);
            kits.add(playerDataKit);
            creating = true;
        }

        playerDataKit.setOneTime(true);
        return creating;
    }

    public boolean getKitOneTime(String kitName){
        PlayerDataKit playerDataKit = getKit(kitName);
        if(playerDataKit == null){
            return false;
        }else{
            return playerDataKit.isOneTime();
        }
    }

    public boolean setKitBought(String kitName){
        PlayerDataKit playerDataKit = getKit(kitName);
        boolean creating = false;
        if(playerDataKit == null){
            playerDataKit = new PlayerDataKit(kitName);
            kits.add(playerDataKit);
            creating = true;
        }

        playerDataKit.setBought(true);
        return creating;
    }

    public boolean getKitHasBought(String kitName){
        PlayerDataKit playerDataKit = getKit(kitName);
        if(playerDataKit == null){
            return false;
        }else{
            return playerDataKit.isBought();
        }
    }

    public Collection<PlayerKitLayout> getLayouts(){
        return layouts.values();
    }

    public PlayerKitLayout getLayout(String kitName){
        return kitName == null ? null : layouts.get(kitName);
    }

    public void putLayout(PlayerKitLayout layout){
        if(layout == null || layout.getKitName() == null){
            return;
        }
        if(layout.isEmpty()){
            layouts.remove(layout.getKitName());
        }else{
            layouts.put(layout.getKitName(),layout);
        }
    }

    public void removeLayout(String kitName){
        if(kitName != null){
            layouts.remove(kitName);
        }
    }

    public void resetKit(String kitName){
        for(int i=0;i<kits.size();i++){
            if(kits.get(i).getName().equals(kitName)){
                kits.remove(i);
                modified = true;
            }
        }
    }
}
