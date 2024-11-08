package com.mygdx.game.states.game.standard.item.factory;

import java.util.Comparator;

/**
 * Available helmets in the game must be
 * stored in this enum with the correct slot index
 * Indexes must be unique within this enum!
 *
 * @author Pedro Sampaio
 * @since 1.3
 */
public enum Helmet {
    // Name_With_Spaces_Same_As_Data_File (SlotIndexValueSameAsEquipmentDataFile)
    Golden_Skull_Helmet(0), Leather_Helmet(1), Scale_Helmet(2), Royal_Knight_Helmet(3);

    private final int index;

    Helmet(int index) {
        this.index = index;
    }

    // return the equipment correct index in data structure
    int getIndex(){ return this.index; }

    public static Comparator<Helmet> enumComparator = new Comparator<Helmet>() {
        @Override
        public int compare(Helmet e1, Helmet e2) {
            return e1.getIndex() - e2.getIndex();
        }
    };
}