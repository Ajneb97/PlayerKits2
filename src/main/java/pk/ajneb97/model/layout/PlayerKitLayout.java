package pk.ajneb97.model.layout;

import java.util.Arrays;

public final class PlayerKitLayout {

    public static final int UNSET = -1;
    public static final int MAX_SLOT = 35;
    public static final int MAX_ITEMS = 54;

    private final String kitName;
    private int[] slots;

    public PlayerKitLayout(String kitName, int[] slots) {
        this.kitName = kitName;
        this.slots = slots == null ? new int[0] : slots;
    }

    public String getKitName() {
        return kitName;
    }

    public int[] getSlots() {
        return slots;
    }

    public boolean isEmpty() {
        for (int slot : slots) {
            if (slot != UNSET) {
                return false;
            }
        }
        return true;
    }

    public int getSlot(int itemIndex) {
        if (itemIndex < 0 || itemIndex >= slots.length) {
            return UNSET;
        }
        return slots[itemIndex];
    }

    public void setSlot(int itemIndex, int slot) {
        if (itemIndex < 0 || itemIndex >= MAX_ITEMS) {
            return;
        }
        if (slot != UNSET && (slot < 0 || slot > MAX_SLOT)) {
            return;
        }
        ensureCapacity(itemIndex + 1);
        for (int i = 0; i < slots.length; i++) {
            if (i != itemIndex && slot != UNSET && slots[i] == slot) {
                slots[i] = UNSET;
            }
        }
        slots[itemIndex] = slot;
    }

    public void swap(int firstIndex, int secondIndex) {
        int max = Math.max(firstIndex, secondIndex) + 1;
        ensureCapacity(max);
        int temp = slots[firstIndex];
        slots[firstIndex] = slots[secondIndex];
        slots[secondIndex] = temp;
    }

    public void clear() {
        Arrays.fill(slots, UNSET);
    }

    public String serialize() {
        if (slots.length == 0) {
            return "";
        }
        StringBuilder builder = new StringBuilder(slots.length * 3);
        for (int i = 0; i < slots.length; i++) {
            if (i > 0) {
                builder.append(',');
            }
            builder.append(slots[i]);
        }
        return builder.toString();
    }

    public static PlayerKitLayout deserialize(String kitName, String raw) {
        if (kitName == null || raw == null || raw.isEmpty()) {
            return null;
        }
        String[] parts = raw.split(",");
        int length = Math.min(parts.length, MAX_ITEMS);
        int[] parsed = new int[length];
        boolean[] used = new boolean[MAX_SLOT + 1];
        for (int i = 0; i < length; i++) {
            int slot = UNSET;
            try {
                slot = Integer.parseInt(parts[i].trim());
            } catch (NumberFormatException ignored) {
                slot = UNSET;
            }
            if (slot < 0 || slot > MAX_SLOT || used[slot]) {
                slot = UNSET;
            } else {
                used[slot] = true;
            }
            parsed[i] = slot;
        }
        PlayerKitLayout layout = new PlayerKitLayout(kitName, parsed);
        return layout.isEmpty() ? null : layout;
    }

    private void ensureCapacity(int size) {
        if (size <= slots.length) {
            return;
        }
        int capped = Math.min(size, MAX_ITEMS);
        int[] resized = new int[capped];
        Arrays.fill(resized, UNSET);
        System.arraycopy(slots, 0, resized, 0, Math.min(slots.length, capped));
        slots = resized;
    }
}
