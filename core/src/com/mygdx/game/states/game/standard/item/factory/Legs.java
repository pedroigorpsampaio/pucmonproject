package com.mygdx.game.states.game.standard.item.factory;

import java.util.Comparator;

/**
 * Available legs in the game must be
 * stored in this enum with the correct slot index
 * Indexes must be unique within this enum!
 *
 * @author Pedro Sampaio
 * @since 1.3
 */
public enum Legs {
    // Name_With_Spaces_Same_As_Data_File (SlotIndexValueSameAsEquipmentDataFile)
    Golden_Skull_Legs(0), Leather_Legs(1), Scale_Legs(2), Royal_Knight_Legs(3);

    private final int index;

    Legs(int index) {
        this.index = index;
    }

    // return the equipment correct index in data structure
    int getIndex(){ return this.index; }

    public static Comparator<Legs> enumComparator = new Comparator<Legs>() {
        @Override
        public int compare(Legs e1, Legs e2) {
            return e1.getIndex() - e2.getIndex();
        }
    };
}