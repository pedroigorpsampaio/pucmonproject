package com.mygdx.game.states.game.standard.item.factory;

import java.util.Comparator;

/**
 * Available shields in the game must be
 * stored in this enum with the correct slot index
 * Indexes must be unique within this enum!
 *
 * @author Pedro Sampaio
 * @since 1.3
 */
public enum Shield {
    // Name_With_Spaces_Same_As_Data_File (SlotIndexValueSameAsEquipmentDataFile)
    Golden_Shield(0), Wooden_Shield(1), Plate_Shield(2), Royal_Shield(3);

    private final int index;

    Shield(int index) {
        this.index = index;
    }

    // return the equipment correct index in data structure
    int getIndex(){ return this.index; }

    public static Comparator<Shield> enumComparator = new Comparator<Shield>() {
        @Override
        public int compare(Shield e1, Shield e2) {
            return e1.getIndex() - e2.getIndex();
        }
    };
}