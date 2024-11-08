package com.mygdx.game.states.game.standard.item.factory;

import java.util.Comparator;

/**
 * Available weapons in the game must be
 * stored in this enum with the correct slot index
 * Indexes must be unique within this enum!
 *
 * @author Pedro Sampaio
 * @since 1.3
 */
public enum Weapon {
    // Name_With_Spaces_Same_As_Data_File (SlotIndexValueSameAsEquipmentDataFile)
    Golden_Axe_Weapon(0), Club_Weapon(1), Amethyst_Sword_Weapon(2), Royal_Spear_Weapon(3);

    private final int index;

    Weapon(int index) {
        this.index = index;
    }

    // return the equipment correct index in data structure
    int getIndex(){ return this.index; }

    public static Comparator<Weapon> enumComparator = new Comparator<Weapon>() {
        @Override
        public int compare(Weapon e1, Weapon e2) {
            return e1.getIndex() - e2.getIndex();
        }
    };
}