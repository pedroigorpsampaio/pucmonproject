package com.mygdx.game.states.game.standard.item.factory;

import java.util.Comparator;

/**
 * Available armors in the game must be
 * stored in this enum with the correct slot index
 * Indexes must be unique within this enum!
 *
 * @author Pedro Sampaio
 * @since 1.3
 */
public enum Armor {
    // Name_With_Spaces_Same_As_Data_File (SlotIndexValueSameAsEquipmentDataFile)
    Golden_Skull_Armor(0), Leather_Armor(1), Scale_Armor(2), Royal_Knight_Armor(3);

    private final int index;

    Armor(int index) {
        this.index = index;
    }

    // return the equipment correct index in data structure
    int getIndex(){ return this.index; }

    public static Comparator<Armor> enumComparator = new Comparator<Armor>() {
        @Override
        public int compare(Armor e1, Armor e2) {
            return e1.getIndex() - e2.getIndex();
        }
    };
}