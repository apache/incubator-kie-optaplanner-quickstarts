package org.acme.orderpicking.domain;

import java.util.HashMap;
import java.util.Map;

import static java.lang.Math.abs;
import static org.acme.orderpicking.domain.Shelving.newShelvingId;

/**
 * Models the warehouse where the order picking problem is formulated and is a static structure composed of shelvings
 * organized in columns and rows. A shelving can be identified by the warehouse column and row number where it's placed.
 * The store products are located on the shelvings, each shelving can contain products on the LEFT and RIGHT sides, and
 * on a row on the particular shelving side.
 *
 * Note: This warehouse structure is completely static and aligned with the "Graphical Map" structure represented in
 * the order picking webapp UI, changes on this structure might require UI adjustments.
 *
 *  -----------------------------------------------------------------------------------> x
 *  |    |--------|   |--------|   |--------|   |--------|   |--------|
 *  |    |        |   |        |   |        |   |        |   |        |
 *  |    |        |   |        |   |        |   |        |   |        |
 *  |    | (A,1)  |   | (B,1)  |   | (C,1)  |   | (D,1)  |   | (E,1)  |
 *  |    |        |   |        |   |        |   |        |   |        |
 *  |    |        |   |        |   |        |   |        |   |        |
 *  |    |--------|   |--------|   |--------|   |--------|   |--------|
 *  |
 *  |    |--------|   |--------|   |--------|   |--------|   |--------|
 *  |    |        |   |        |   |        |   |        |   |        |
 *  |    |        |   |        |   |        |   |        |   |        |
 *  |    | (A,2)  |   | (B,2)  |   | (C,2)  |   | (D,2)  |   | (E,2)  |
 *  |    |        |   |        |   |        |   |        |   |        |
 *  |    |        |   |        |   |        |   |        |   |        |
 *  |    |--------|   |--------|   |--------|   |--------|   |--------|
 *  |
 *  |    |--------|   |--------|   |--------|   |--------|   |--------|
 *  |    |        |   |        |   |        |   |        |   |        |
 *  |    |        |   |        |   |        |   |        |   |        |
 *  |    | (A,3)  |   | (B,3)  |   | (C,3)  |   | (D,3)  |   | (E,3)  |
 *  |    |        |   |        |   |        |   |        |   |        |
 *  |    |        |   |        |   |        |   |        |   |        |
 *  |    |--------|   |--------|   |--------|   |--------|   |--------|
 *  |
 *  y
 * @see Shelving
 */
public class Warehouse {

    /**
     * Defines the warehouse columns.
     */
    public enum Column {
        COL_A('A'),
        COL_B('B'),
        COL_C('C'),
        COL_D('D'),
        COL_E('E');

        private final char colId;

        Column(char colId) {
            this.colId = colId;
        }

        @Override
        public String toString() {
            return Character.toString(colId);
        }
    }

    /**
     * Defines the warehouse rows.
     */
    public enum Row {
        ROW_1(1),
        ROW_2(2),
        ROW_3(3);

        private final int rowId;

        Row(int rowId) {
            this.rowId = rowId;
        }

        @Override
        public String toString() {
            return Integer.toString(rowId);
        }
    }

    /**
     * The shelving width in meters.
     */
    private static final int SHELVING_WIDTH = 2;
    /**
     * The shelving height in meters.
     */
    private static final int SHELVING_HEIGHT = 10;
    /**
     * Spacing between the shelving rows and columns in meters.
     */
    private static final int SHELVING_PADDING = 3;

    private static final Map<String, Shelving> SHELVING_MAP = new HashMap<>();

    private static final String SHELVING_NOT_FOUND_ERROR = "Shelving: %s was not found in current Warehouse structure.";

    static {
        int shelvingX = 0;
        int shelvingY;
        Shelving shelving;

        for (Column col : Column.values()) {
            shelvingY = 0;
            for (Row row : Row.values()) {
                shelving = new Shelving(newShelvingId(col, row), shelvingX, shelvingY);
                SHELVING_MAP.put(shelving.getId(), shelving);
                shelvingY = shelvingY + SHELVING_HEIGHT + SHELVING_PADDING;
            }
            shelvingX = shelvingX + SHELVING_WIDTH + SHELVING_PADDING;
        }
    }

    private Warehouse() {
    }

    /**
     * Calculates the distance in meters between two locations considering the warehouse structure.
     */
    public static int calculateDistance(WarehouseLocation start, WarehouseLocation end) {
        final Shelving startShelving = SHELVING_MAP.get(start.getShelvingId());
        if (startShelving == null) {
            throw new IndexOutOfBoundsException(String.format(SHELVING_NOT_FOUND_ERROR, start.getShelvingId()));
        }
        final Shelving endShelving = SHELVING_MAP.get(end.getShelvingId());
        if (endShelving == null) {
            throw new IndexOutOfBoundsException(String.format(SHELVING_NOT_FOUND_ERROR, end.getShelvingId()));
        }
        int deltaX = 0;
        int deltaY;

        final int startX = getAbsoluteX(startShelving, start);
        final int startY = getAbsoluteY(startShelving, start);
        final int endX = getAbsoluteX(endShelving, end);
        final int endY = getAbsoluteY(endShelving, end);

        if (startShelving == endShelving) {
            //same shelving
            if (start.getSide() == end.getSide()) {
                //same side
                deltaY = abs(startY - endY);
            } else {
                //different side, calculate shortest walk.
                deltaX = SHELVING_WIDTH;
                deltaY = calculateBestYDistanceInShelvingRow(start.getRow(), end.getRow());
            }
        } else if (startShelving.getY() == endShelving.getY()) {
            //distinct shelvings but on the same warehouse row
            if (abs(startX - endX) == SHELVING_PADDING) {
                //neighbor shelvings, but also contiguous side
                deltaX = SHELVING_PADDING;
                deltaY = abs(startY - endY);
            } else {
                //any other combination of shelvings but in the same warehouse row
                deltaX = abs(startX - endX);
                deltaY = calculateBestYDistanceInShelvingRow(start.getRow(), end.getRow());
            }
        } else {
            //shelvings on different warehouse rows
            deltaX = abs(startX - endX);
            deltaY = abs(startY - endY);
        }
        return deltaX + deltaY;
    }

    public static int calculateDistanceToTravel(Trolley trolley) {
        int distance = 0;
        WarehouseLocation previousLocation = trolley.getLocation();
        TrolleyStep nextElement = trolley.getNextElement();
        while (nextElement != null) {
            distance += calculateDistance(previousLocation, nextElement.getLocation());
            previousLocation = nextElement.getLocation();
            nextElement = nextElement.getNextElement();
        }
        distance += calculateDistance(previousLocation, trolley.getLocation());
        return distance;
    }

    private static int calculateBestYDistanceInShelvingRow(int startY, int endY) {
        final int northDirectionDistance = startY + endY;
        final int southDirectionDistance = (SHELVING_HEIGHT - startY) + (SHELVING_HEIGHT - endY);
        return Math.min(northDirectionDistance, southDirectionDistance);
    }

    /**
     * Calculates the absolute X position of a location considering the warehouse structure and the shelving where it's
     * contained.
     */
    private static int getAbsoluteX(Shelving shelving, WarehouseLocation location) {
        if (location.getSide() == Shelving.Side.LEFT) {
            return shelving.getX();
        } else {
            return shelving.getX() + SHELVING_WIDTH;
        }
    }

    /**
     * Calculates the absolute Y position of a location considering the warehouse structure and the shelving where it's
     * contained.
     */
    private static int getAbsoluteY(Shelving shelving, WarehouseLocation location) {
        return shelving.getY() + location.getRow();
    }
}