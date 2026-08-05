package pk.ajneb97.model;

import pk.ajneb97.model.layout.PlayerKitLayout;

import java.util.Collection;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

public class PlayerData {

    private volatile String name;
    private final UUID uuid;

    private final Map<String, PlayerDataKit> kits;
    private final Map<String, PlayerKitLayout> layouts;
    private final AtomicBoolean modified;
    private final AtomicBoolean loaded;

    public PlayerData(UUID uuid, String name) {
        this.name = name;
        this.uuid = uuid;
        this.kits = new ConcurrentHashMap<>();
        this.layouts = new ConcurrentHashMap<>();
        this.modified = new AtomicBoolean(false);
        this.loaded = new AtomicBoolean(false);
    }

    public UUID getUuid() {
        return uuid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Collection<PlayerDataKit> getKits() {
        return kits.values();
    }

    public void addKit(PlayerDataKit kit) {
        if (kit != null && kit.getName() != null) {
            kits.put(kit.getName(), kit);
        }
    }

    public boolean isModified() {
        return modified.get();
    }

    public void setModified(boolean value) {
        modified.set(value);
    }

    public boolean isLoaded() {
        return loaded.get();
    }

    public void setLoaded(boolean value) {
        loaded.set(value);
    }

    public PlayerDataKit getKit(String kitName) {
        return kitName == null ? null : kits.get(kitName);
    }

    private PlayerDataKit getOrCreateKit(String kitName, boolean[] created) {
        PlayerDataKit existing = kits.get(kitName);
        if (existing != null) {
            created[0] = false;
            return existing;
        }
        PlayerDataKit fresh = new PlayerDataKit(kitName);
        PlayerDataKit race = kits.putIfAbsent(kitName, fresh);
        if (race != null) {
            created[0] = false;
            return race;
        }
        created[0] = true;
        return fresh;
    }

    public boolean setKitCooldown(String kitName, long cooldown) {
        boolean[] created = new boolean[1];
        getOrCreateKit(kitName, created).setCooldown(cooldown);
        return created[0];
    }

    public long getKitCooldown(String kitName) {
        PlayerDataKit kit = getKit(kitName);
        return kit == null ? 0 : kit.getCooldown();
    }

    public boolean setKitOneTime(String kitName) {
        boolean[] created = new boolean[1];
        getOrCreateKit(kitName, created).setOneTime(true);
        return created[0];
    }

    public boolean getKitOneTime(String kitName) {
        PlayerDataKit kit = getKit(kitName);
        return kit != null && kit.isOneTime();
    }

    public boolean setKitBought(String kitName) {
        boolean[] created = new boolean[1];
        getOrCreateKit(kitName, created).setBought(true);
        return created[0];
    }

    public boolean getKitHasBought(String kitName) {
        PlayerDataKit kit = getKit(kitName);
        return kit != null && kit.isBought();
    }

    public void resetKit(String kitName) {
        if (kits.remove(kitName) != null) {
            modified.set(true);
        }
    }

    public Collection<PlayerKitLayout> getLayouts() {
        return layouts.values();
    }

    public PlayerKitLayout getLayout(String kitName) {
        return kitName == null ? null : layouts.get(kitName);
    }

    public void putLayout(PlayerKitLayout layout) {
        if (layout == null || layout.getKitName() == null) {
            return;
        }
        if (layout.isEmpty()) {
            layouts.remove(layout.getKitName());
        } else {
            layouts.put(layout.getKitName(), layout);
        }
        modified.set(true);
    }

    public void removeLayout(String kitName) {
        if (kitName != null && layouts.remove(kitName) != null) {
            modified.set(true);
        }
    }

    public void mergeFrom(PlayerData stored) {
        if (stored == null) {
            return;
        }
        for (PlayerDataKit storedKit : stored.getKits()) {
            PlayerDataKit current = kits.putIfAbsent(storedKit.getName(), storedKit);
            if (current == null) {
                continue;
            }
            if (storedKit.getCooldown() > current.getCooldown()) {
                current.setCooldown(storedKit.getCooldown());
            }
            if (storedKit.isOneTime()) {
                current.setOneTime(true);
            }
            if (storedKit.isBought()) {
                current.setBought(true);
            }
        }
        for (PlayerKitLayout layout : stored.getLayouts()) {
            layouts.putIfAbsent(layout.getKitName(), layout);
        }
        modified.set(true);
    }
}
