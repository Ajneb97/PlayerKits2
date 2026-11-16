package pk.ajneb97.managers;

import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;

public class PreviewLayoutTest {

    @Test
    public void movesOutOfBoundsItemsIntoFreeSlots() {
        assertArrayEquals(new int[]{0, 1}, PreviewLayout.assignSlots(
                new int[]{50, -2}, new boolean[45]));
    }

    @Test
    public void keepsClaimAndBackSlotsFreeOfRewards() {
        boolean[] reserved = new boolean[54];
        reserved[45] = true;
        reserved[49] = true;

        assertArrayEquals(new int[]{0, 1}, PreviewLayout.assignSlots(
                new int[]{45, 49}, reserved));
    }

    @Test
    public void keepsExplicitPositionsAndMovesCollidingItems() {
        assertArrayEquals(new int[]{0, 2, 1, 3}, PreviewLayout.assignSlots(
                new int[]{-1, 2, 2, -1}, new boolean[9]));
    }

    @Test
    public void givesExplicitPositionsPriorityOverImplicitPositions() {
        assertArrayEquals(new int[]{1, 0, 2}, PreviewLayout.assignSlots(
                new int[]{-1, 0, -1}, new boolean[9]));
    }

    @Test
    public void retainsLegacyPositionsWhenTheyDoNotConflict() {
        assertArrayEquals(new int[]{5, 1, 2}, PreviewLayout.assignSlots(
                new int[]{5, -1, -1}, new boolean[9]));
    }

    @Test
    public void omitsItemsThatDoNotFit() {
        assertArrayEquals(new int[]{1, -1, -1}, PreviewLayout.assignSlots(
                new int[]{-1, -1, -1}, new boolean[]{true, false}));
    }
}
