package com.mygdx.game.states.game.standard.battle;

import com.mygdx.game.util.Common;

/**
 * Class that will hold all battle
 * damages formulas and calculations
 *
 * @author  Pedro Sampaio
 * @since   1.0
 */
public class Damage {

    /*******************
     * Battle Formulas *
     *******************/
    static int normalDmgFormula (int atk, int def) {return atk+def>0 ? (atk * atk / (atk + def)): 0; }
    static int linearDmgFormula (int atk, int def) {return atk-(def/10)>0 ? atk-(def/10) : 1;}

    static float minAtkPercent = 0.8f;
    static float maxAtkPercent = 1.2f;
    static float minDefPercent = 0.9f;
    static float maxDefPercent = 1.1f;

    /**
     * Receives attributes from both attacker
     * and defender and calculates the damage
     * dealt by the attacker returning it
     * @param attacker  the attributes of the attacker
     * @param defender  the attributes of the defender
     * @return the calculated damage dealt by the attacker
     */
    public static int getNormalDmg(Attributes attacker, Attributes defender) {
        return normalDmgFormula((int)Common.randFloat(attacker.getAttack()* minAtkPercent,
                                    attacker.getAttack()*maxAtkPercent),
                                (int)Common.randFloat(defender.getDefense()* minDefPercent,
                                        defender.getDefense()*maxDefPercent));
    }

    /**
     * Receives attributes from both attacker
     * and defender and calculates the auto attack damage
     * dealt by the attacker returning it
     * @param attacker the attributes of the attacker
     * @param defender the attributes of the defender
     * @return the calculated auto attack damage dealt by the attacker
     */
    public static int getAutoDmg(Attributes attacker, Attributes defender) {
        return linearDmgFormula(attacker.getAutoAttack(), defender.getDefense());
    }
}
