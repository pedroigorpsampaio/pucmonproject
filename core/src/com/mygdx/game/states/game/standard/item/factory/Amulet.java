package com.mygdx.game.states.game.standard.item.factory;

import java.util.Comparator;

/**
 * Available amulets in the game must be
 * stored in this enum with the correct slot index
 * Indexes must be unique within this enum!
 *
 * @author Pedro Sampaio
 * @since 1.3
 */
public enum Amulet {
    // Name_With_Spaces_Same_As_Data_File (SlotIndexValueSameAsEquipmentDataFile)
    Golden_Amulet(0), Tribal_Amulet(1), Amethyst_Amulet(2), Royal_Amulet(3);

    private final int index;

    Amulet(int index) {
        this.index = index;
    }

    // return the equipment correct index in data structure
    int getIndex(){ return this.index; }

    public static Comparator<Amulet> enumComparator = new Comparator<Amulet>() {
        @Override
        public int compare(Amulet e1, Amulet e2) {
            return e1.getIndex() - e2.getIndex();
        }
    };
}