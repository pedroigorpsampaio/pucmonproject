package com.mygdx.game.states.game.standard.item.factory;

import java.util.Comparator;

/**
 * Available boots in the game must be
 * stored in this enum with the correct slot index
 * Indexes must be unique within this enum!
 *
 * @author Pedro Sampaio
 * @since 1.3
 */
public enum Boots {
    // Name_With_Spaces_Same_As_Data_File (SlotIndexValueSameAsEquipmentDataFile)
    Golden_Skull_Boots(0), Leather_Boots(1), Scale_Boots(2), Royal_Knight_Boots(3);

    private final int index;

    Boots(int index) {
        this.index = index;
    }

    // return the equipment correct index in data structure
    int getIndex(){ return this.index; }

    public static Comparator<Boots> enumComparator = new Comparator<Boots>() {
        @Override
        public int compare(Boots e1, Boots e2) {
            return e1.getIndex() - e2.getIndex();
        }
    };
}