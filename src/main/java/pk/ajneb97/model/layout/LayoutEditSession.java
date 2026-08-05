package pk.ajneb97.model.layout;

import java.util.Arrays;

public final class LayoutEditSession {

    private final String kitName;
    private final int itemCount;
    private final int[] slots;
    private int selectedIndex = -1;

    public LayoutEditSession(String kitName, int itemCount, PlayerKitLayout existing, int[] defaults) {
        this.kitName = kitName;
        this.itemCount = itemCount;
        this.slots = new int[itemCount];
        Arrays.fill(this.slots, PlayerKitLayout.UNSET);

        boolean[] used = new boolean[PlayerKitLayout.MAX_SLOT + 1];
        for (int i = 0; i < itemCount; i++) {
            int slot = existing != null ? existing.getSlot(i) : PlayerKitLayout.UNSET;
            if (slot < 0 || slot > PlayerKitLayout.MAX_SLOT || used[slot]) {
                continue;
            }
            used[slot] = true;
            slots[i] = slot;
        }
        for (int i = 0; i < itemCount; i++) {
            if (slots[i] != PlayerKitLayout.UNSET) {
                continue;
            }
            int fallback = i < defaults.length ? defaults[i] : PlayerKitLayout.UNSET;
            if (fallback >= 0 && fallback <= PlayerKitLayout.MAX_SLOT && !used[fallback]) {
                used[fallback] = true;
                slots[i] = fallback;
            }
        }
    }

    public String getKitName() {
        return kitName;
    }

    public int getItemCount() {
        return itemCount;
    }

    public int getSlot(int index) {
        return index < 0 || index >= slots.length ? PlayerKitLayout.UNSET : slots[index];
    }

    public int getIndexAtSlot(int slot) {
        for (int i = 0; i < slots.length; i++) {
            if (slots[i] == slot) {
                return i;
            }
        }
        return -1;
    }

    public int getSelectedIndex() {
        return selectedIndex;
    }

    public void setSelectedIndex(int selectedIndex) {
        this.selectedIndex = selectedIndex;
    }

    public void moveTo(int index, int targetSlot) {
        if (index < 0 || index >= slots.length) {
            return;
        }
        if (targetSlot < 0 || targetSlot > PlayerKitLayout.MAX_SLOT) {
            return;
        }
        int occupant = getIndexAtSlot(targetSlot);
        int origin = slots[index];
        slots[index] = targetSlot;
        if (occupant != -1 && occupant != index) {
            slots[occupant] = origin;
        }
    }

    public void reset(int[] defaults) {
        Arrays.fill(slots, PlayerKitLayout.UNSET);
        boolean[] used = new boolean[PlayerKitLayout.MAX_SLOT + 1];
        for (int i = 0; i < slots.length && i < defaults.length; i++) {
            int fallback = defaults[i];
            if (fallback >= 0 && fallback <= PlayerKitLayout.MAX_SLOT && !used[fallback]) {
                used[fallback] = true;
                slots[i] = fallback;
            }
        }
        selectedIndex = -1;
    }

    public PlayerKitLayout toLayout() {
        return new PlayerKitLayout(kitName, Arrays.copyOf(slots, slots.length));
    }

    public boolean matchesDefaults(int[] defaults) {
        for (int i = 0; i < slots.length; i++) {
            int expected = i < defaults.length ? defaults[i] : PlayerKitLayout.UNSET;
            if (slots[i] != expected) {
                return false;
            }
        }
        return true;
    }
}
