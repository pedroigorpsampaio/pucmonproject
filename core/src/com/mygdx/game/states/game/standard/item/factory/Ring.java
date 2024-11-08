package com.mygdx.game.states.game.standard.item.factory;

import java.util.Comparator;

/**
 * Available rings in the game must be
 * stored in this enum with the correct slot index
 * Indexes must be unique within this enum!
 *
 * @author Pedro Sampaio
 * @since 1.3
 */
public enum Ring {
    // Name_With_Spaces_Same_As_Data_File (SlotIndexValueSameAsEquipmentDataFile)
    Golden_Ring(0), Tribal_Ring(1), Amethyst_Ring(2), Royal_Ring(3);

    private final int index;

    Ring(int index) {
        this.index = index;
    }

    // return the equipment correct index in data structure
    int getIndex(){ return this.index; }

    public static Comparator<Ring> enumComparator = new Comparator<Ring>() {
        @Override
        public int compare(Ring e1, Ring e2) {
            return e1.getIndex() - e2.getIndex();
        }
    };
}