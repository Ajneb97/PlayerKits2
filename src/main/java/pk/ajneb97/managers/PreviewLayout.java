package pk.ajneb97.managers;

import java.util.Arrays;

final class PreviewLayout {

    private PreviewLayout() {
    }

    static int[] assignSlots(int[] preferredSlots, boolean[] reservedSlots) {
        int[] assigned = new int[preferredSlots.length];
        Arrays.fill(assigned, -1);
        boolean[] occupied = reservedSlots.clone();

        for (int index = 0; index < preferredSlots.length; index++) {
            int slot = preferredSlots[index];
            if (slot >= 0 && slot < occupied.length && !occupied[slot]) {
                assigned[index] = slot;
                occupied[slot] = true;
            }
        }

        int next = 0;
        for (int index = 0; index < assigned.length; index++) {
            if (assigned[index] != -1) {
                continue;
            }
            // Use the legacy sequential position when an implicit slot is still free.
            if (preferredSlots[index] == -1 && index < occupied.length && !occupied[index]) {
                assigned[index] = index;
                occupied[index] = true;
                continue;
            }
            while (next < occupied.length && occupied[next]) {
                next++;
            }
            if (next == occupied.length) {
                break;
            }
            assigned[index] = next;
            occupied[next] = true;
        }
        return assigned;
    }
}
