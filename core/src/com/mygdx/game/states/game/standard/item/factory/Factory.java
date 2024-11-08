package com.mygdx.game.states.game.standard.item.factory;


import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.mygdx.game.Main;
import com.mygdx.game.states.game.standard.architecture.Resource;
import com.mygdx.game.states.game.standard.item.Effect;
import com.mygdx.game.states.game.standard.item.Equipment;
import com.mygdx.game.states.game.standard.item.Item;
import com.mygdx.game.util.Common;
import com.mygdx.game.util.Config;
import com.mygdx.game.util.Pair;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

/**
 * Class responsible for creating game items
 *
 * @author  Pedro Sampaio
 * @since   1.2
 */
public class Factory {

    private static Factory instance = null; // singleton instance
    Texture equipSheet; // equipment sprite sheet

    // TODO - Add special item that increases an equipment level

    /***********************
     * Equipment constants *
     ***********************/

    /**
     * Equipments id prefix to concatenate and form uniqueID
     */
    static int helmetFirstID = 3000; // helmet first id
    static int armorFirstID = 5000; // armor first id
    static int legsFirstID = 7000; // legs first id
    static int bootsFirstID = 9000; // boots first id
    static int weaponFirstID = 11000; // weapon first id
    static int shieldFirstID = 13000; // shield first id
    static int ringFirstID = 15000; // ring first id
    static int amuletFirstID = 17000; // amulet first id

    /**
     * Equipments available following enum indexes
     * that will be the base for cloning with createItem
     */
    private HashMap<Integer, Equipment> weapons;
    private HashMap<Integer, Equipment> shields;
    private HashMap<Integer, Equipment> armors;
    private HashMap<Integer, Equipment> legs;
    private HashMap<Integer, Equipment> helmets;
    private HashMap<Integer, Equipment> boots;
    private HashMap<Integer, Equipment> amulets;
    private HashMap<Integer, Equipment> rings;

    /**
     * All equipments stored in a list to help drop method
     */
    private ArrayList<Pair<HashMap<Integer, Equipment>, Equipment.Slot>> equipments;

    /**
     * Constructor initializes (defeats external instantiation)
     */
    private Factory() {
        equipSheet = Resource.equipSheet; // gets equip sheet
        // initialize base equipment maps
        helmets = new HashMap<Integer, Equipment>();
        armors = new HashMap<Integer, Equipment>();
        legs = new HashMap<Integer, Equipment>();
        boots = new HashMap<Integer, Equipment>();
        amulets = new HashMap<Integer, Equipment>();
        rings = new HashMap<Integer, Equipment>();
        weapons = new HashMap<Integer, Equipment>();
        shields = new HashMap<Integer, Equipment>();

        // prepare items enabling them to be copied on creation later
        prepareItems();

        // stores all equipments in a list to help drop method
        equipments = new ArrayList<Pair<HashMap<Integer, Equipment>, Equipment.Slot>>();
        equipments.add(new Pair<HashMap<Integer, Equipment>, Equipment.Slot>(helmets, Equipment.Slot.HELMET));
        equipments.add(new Pair<HashMap<Integer, Equipment>, Equipment.Slot>(armors, Equipment.Slot.ARMOR));
        equipments.add(new Pair<HashMap<Integer, Equipment>, Equipment.Slot>(legs, Equipment.Slot.LEGS));
        equipments.add(new Pair<HashMap<Integer, Equipment>, Equipment.Slot>(boots, Equipment.Slot.BOOTS));
        equipments.add(new Pair<HashMap<Integer, Equipment>, Equipment.Slot>(amulets, Equipment.Slot.AMULET));
        equipments.add(new Pair<HashMap<Integer, Equipment>, Equipment.Slot>(rings, Equipment.Slot.RING));
        equipments.add(new Pair<HashMap<Integer, Equipment>, Equipment.Slot>(weapons, Equipment.Slot.WEAPON));
        equipments.add(new Pair<HashMap<Integer, Equipment>, Equipment.Slot>(shields, Equipment.Slot.SHIELD));
    }

    /**
     * Prepare items to be created
     * whenever there is a need to
     */
    private void prepareItems() {
        // gets equipment data file
        InputStream eDataFile = Gdx.files.internal("data/equipments.data").read();
        // buffered reader to read equipment data file
        BufferedReader eDataReader = new BufferedReader(new InputStreamReader(eDataFile));
        // iterates through lines and columns to get equipment data
        String line;
        try {
            while ((line = eDataReader.readLine()) != null) {
                if(line.contains("--")) // ignores any line that has "--" in any position (commentary)
                    continue;

                // end of a equipment data
                if(line.contains("}"))
                    continue;

                // start of a new equipment data
                if(line.contains("{")) {
                    if(Config.debug)
                        System.out.println("####### NEW EQUIPMENT #######");

                    // the name of the equipment
                    String name = line.substring(0, line.indexOf('{')).trim();
                    if(Config.debug)
                        System.out.println("New Equip Name: "+name);
                    // the effect of the equipment
                    Effect effect = new Effect();
                    // gets equipment effects data
                    while((line = eDataReader.readLine()).contains("--")); // ignore all comments until next data
                    effect.attack = Integer.parseInt(line.substring(line.indexOf(":")+1).trim()); // get equipment attack effect
                    while((line = eDataReader.readLine()).contains("--")); // ignore all comments until next data
                    effect.defense = Integer.parseInt(line.substring(line.indexOf(":")+1).trim()); // get equipment defense effect
                    while((line = eDataReader.readLine()).contains("--")); // ignore all comments until next data
                    effect.autoAttack = Integer.parseInt(line.substring(line.indexOf(":")+1).trim()); // get equipment auto attack effect
                    while((line = eDataReader.readLine()).contains("--")); // ignore all comments until next data
                    effect.maxHealth = Integer.parseInt(line.substring(line.indexOf(":")+1).trim()); // get equipment max health effect
                    while((line = eDataReader.readLine()).contains("--")); // ignore all comments until next data
                    effect.critChance = Float.parseFloat(line.substring(line.indexOf(":")+1).trim()); // get equipment crit chance effect
                    while((line = eDataReader.readLine()).contains("--")); // ignore all comments until next data
                    effect.critMult = Float.parseFloat(line.substring(line.indexOf(":")+1).trim()); // get equipment crit mult effect
                    while((line = eDataReader.readLine()).contains("--")); // ignore all comments until next data
                    effect.autoSpeed = Float.parseFloat(line.substring(line.indexOf(":")+1).trim()); // get equipment auto speed effect
                    while((line = eDataReader.readLine()).contains("--")); // ignore all comments until next data
                    effect.speed = Float.parseFloat(line.substring(line.indexOf(":")+1).trim()); // get equipment speed effect
                    while((line = eDataReader.readLine()).contains("--")); // ignore all comments until next data
                    effect.rateDrop = Float.parseFloat(line.substring(line.indexOf(":")+1).trim()); // get equipment rate drop effect
                    while((line = eDataReader.readLine()).contains("--")); // ignore all comments until next data
                    effect.rateExp = Float.parseFloat(line.substring(line.indexOf(":")+1).trim()); // get equipment rate exp effect
                    while((line = eDataReader.readLine()).contains("--")); // ignore all comments until next data
                    effect.rateGold = Float.parseFloat(line.substring(line.indexOf(":")+1).trim()); // get equipment rate gold effect

                    if(Config.debug)
                        System.out.println("New Equip Effects: {attack: " + effect.attack + ", defense: "+effect.defense
                          +  ", autoAttack: "+effect.autoAttack +  ", maxHealth: "+effect.maxHealth +  ", critChance: "+effect.critChance
                            + ", critMult: "+effect.critMult+ ", autoSpeed: "+effect.autoSpeed+ ", speed: "+effect.speed
                            + ", rateDrop: "+effect.rateDrop + ", rateExp: "+effect.rateExp  + ", rateGold: "+effect.rateGold);

                    // get equipment chance of drop
                    while((line = eDataReader.readLine()).contains("--")); // ignore all comments until next data
                    float chance = Float.parseFloat(line.substring(line.indexOf(":")+1).trim()); // get equipment drop chance

                    if(Config.debug)
                        System.out.println("New Equip Drop Chance: "+chance);

                    // get equipment quality data
                    while((line = eDataReader.readLine()).contains("--")); // ignore all comments until next data
                    int q = Integer.parseInt(line.substring(line.indexOf(":")+1).trim()); // get equipment quality
                    q = MathUtils.clamp(q, 0, Item.Quality.values().length-1); // clamps to fit existent qualities
                    Item.Quality quality = Item.Quality.values()[q];
                    if(Config.debug)
                        System.out.println("New Equip Quality: "+q+ " -> " + quality);

                    // get equipment slot data
                    while((line = eDataReader.readLine()).contains("--")); // ignore all comments until next data
                    String slotStr = line.substring(line.indexOf(":")+1).trim(); // get string representing slot
                    Equipment.Slot slot = Equipment.Slot.valueOf(slotStr); // get equipment slot via string

                    if(Config.debug)
                        System.out.println("New Equip Slot: "+slot);

                    // get equipment index data (very important !!)
                    while((line = eDataReader.readLine()).contains("--")); // ignore all comments until next data
                    int index = Integer.parseInt(line.substring(line.indexOf(":")+1).trim()); // get equipment index

                    if(Config.debug)
                        System.out.println("New Equip Index in Equip Type: "+index);

                    // get equipment sprite position on sprite sheet
                    while((line = eDataReader.readLine()).contains("--")); // ignore all comments until next data
                    Vector2 pos = Common.stringToVector2(line.substring(line.indexOf(":")+1).trim()); // gets position and store in a vector2

                    // get equipment sprite size on sprite sheet
                    while((line = eDataReader.readLine()).contains("--")); // ignore all comments until next data
                    Vector2 size = Common.stringToVector2(line.substring(line.indexOf(":")+1).trim()); // gets size and store in a vector2

                    if(Config.debug) {
                        System.out.println("New Equip Texture Region: " + pos + "," + size);
                        System.out.println("#############################");
                    }
                    // gets strings (name and description)
                    String equipName = Main.getInstance().getLang().get(name+"_Name");
                    String equipDesc = Main.getInstance().getLang().get(name+"_Desc");

                    // gets sprite
                    TextureRegion equipSpr = new TextureRegion(equipSheet, (int)pos.x, (int)pos.y, (int)size.x, (int)size.y);

                    // switch slot types to add to correct hash map
                    switch(slot) {
                        case HELMET:
                            helmets.put(index, new Equipment(index, equipName, equipDesc, effect,
                                                equipSpr, chance, quality, slot));
                            break;
                        case ARMOR:
                            armors.put(index, new Equipment(index, equipName, equipDesc, effect,
                                    equipSpr, chance, quality, slot));
                            break;
                        case LEGS:
                            legs.put(index, new Equipment(index, equipName, equipDesc, effect,
                                    equipSpr, chance, quality, slot));
                            break;
                        case BOOTS:
                            boots.put(index, new Equipment(index, equipName, equipDesc, effect,
                                    equipSpr, chance, quality, slot));
                            break;
                        case WEAPON:
                            weapons.put(index, new Equipment(index, equipName, equipDesc, effect,
                                    equipSpr, chance, quality, slot));
                            break;
                        case SHIELD:
                            shields.put(index, new Equipment(index, equipName, equipDesc, effect,
                                    equipSpr, chance, quality, slot));
                            break;
                        case RING:
                            rings.put(index, new Equipment(index, equipName, equipDesc, effect,
                                    equipSpr, chance, quality, slot));
                            break;
                        case AMULET:
                            amulets.put(index, new Equipment(index, equipName, equipDesc, effect,
                                    equipSpr, chance, quality, slot));
                            break;
                        default:
                            System.err.println("Unkown slot type. Check item/equipments.java enum t" +
                                    "to see the available ones. Unknown Slot: "+slot);
                            break;
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Could not read file: data/equipments.data");
            e.printStackTrace();
        }

    }

    /**
     * gets singleton instance
     * @return singleton instance
     */
    public static Factory getInstance() {
        if(instance == null)
            instance = new Factory();

        return instance;
    }

    /**
     * Creates game items
     * @param item item to be created
     * @param level the level of the created item (in case of equipments)
     * @param scale if item level should be scaled or should be the received level parameter
     * @return the created item received in parameter
     */
    public static Item createItem(Enum<?> item, int level, boolean scale) {

        // get item name from enum
        String itemName = item.toString();
        Item createdItem = null;

        // check item type
        if(itemName.contains("Helmet")) { // creates equipment of helmet type using enum index
            int eid = ((Helmet)item).getIndex();// converts to get correct index
            createdItem = createEquipment(getInstance().helmets.get(eid), helmetFirstID + eid, level, scale);
        }
        else if(itemName.contains("Armor")) { // creates equipment of armor type using enum index
            int eid = ((Armor) item).getIndex();// converts to get correct index
            createdItem = createEquipment(getInstance().armors.get(eid), armorFirstID + eid, level, scale);
        }
        else if(itemName.contains("Legs")) { // creates equipment of legs type using enum index
            int eid = ((Legs) item).getIndex();// converts to get correct index
            createdItem = createEquipment(getInstance().legs.get(eid), legsFirstID + eid, level, scale);
        }
        else if(itemName.contains("Boots")) { // creates equipment of boots type using enum index
            int eid = ((Boots) item).getIndex();// converts to get correct index
            createdItem = createEquipment(getInstance().boots.get(eid), bootsFirstID + eid, level, scale);
        }
        else if(itemName.contains("Weapon")) { // creates equipment of weapon type using enum index
            int eid = ((Weapon) item).getIndex();// converts to get correct index
            createdItem = createEquipment(getInstance().weapons.get(eid), weaponFirstID + eid, level, scale);
        }
        else if(itemName.contains("Shield")) { // creates equipment of shield type using enum index
            int eid = ((Shield) item).getIndex();// converts to get correct index
            createdItem = createEquipment(getInstance().shields.get(eid), shieldFirstID + eid, level, scale);
        }
        else if(itemName.contains("Ring")) { // creates equipment of ring type using enum index
            int eid = ((Ring) item).getIndex();// converts to get correct index
            createdItem = createEquipment(getInstance().rings.get(eid), ringFirstID + eid, level, scale);
        }
        else if(itemName.contains("Amulet")) { // creates equipment of amulet type using enum index
            int eid = ((Amulet) item).getIndex();// converts to get correct index
            createdItem = createEquipment(getInstance().amulets.get(eid), amuletFirstID + eid, level, scale);
        }
        else
            System.err.println("Unknown type of equipment. Equipments should contain their types in their names" +
                                "in data file to be properly identified (ex. Golden_Skull_Helmet ");

        return createdItem;
    }

    /**
     * Creates game items based on its UniqueID
     * @param uniqueID unique ID of ite
     * @param level the level of the created item (in case of equipments)
     * @param scale if item level should be scaled or should be the received level parameter
     * @return the created item of uniqueID received in parameter
     */
    public static Item createItem(int uniqueID, int level, boolean scale) {

        Item createdItem = null;

        // check item type
        if(uniqueID < helmetFirstID) {
            // creates item
        }
        else if(uniqueID < armorFirstID) { // creates equipment of helmet type using uniqueID
            int eid = uniqueID - helmetFirstID;
            createdItem = createEquipment(getInstance().helmets.get(eid),uniqueID, level, scale);
        }
        else if(uniqueID < legsFirstID) { // creates equipment of armor type using uniqueID
            int eid = uniqueID - armorFirstID;
            createdItem = createEquipment(getInstance().armors.get(eid),uniqueID, level, scale);
        }
        else if(uniqueID < bootsFirstID) { // creates equipment of legs type using uniqueID
            int eid = uniqueID - legsFirstID;
            createdItem = createEquipment(getInstance().legs.get(eid),uniqueID, level, scale);
        }
        else if(uniqueID < weaponFirstID) { // creates equipment of boots type using uniqueID
            int eid = uniqueID - bootsFirstID;
            createdItem = createEquipment(getInstance().boots.get(eid),uniqueID, level, scale);
        }
        else if(uniqueID < shieldFirstID) { // creates equipment of weapon type using uniqueID
            int eid = uniqueID - weaponFirstID;
            createdItem = createEquipment(getInstance().weapons.get(eid),uniqueID, level, scale);
        }
        else if(uniqueID < ringFirstID) { // creates equipment of shield type using uniqueID
            int eid = uniqueID - shieldFirstID;
            createdItem = createEquipment(getInstance().shields.get(eid),uniqueID, level, scale);
        }
        else if(uniqueID < amuletFirstID) { // creates equipment of rings type using uniqueID
            int eid = uniqueID - ringFirstID;
            createdItem = createEquipment(getInstance().rings.get(eid),uniqueID, level, scale);
        }
        else { // creates equipment of amulet type using uniqueID
            int eid = uniqueID - amuletFirstID;
            createdItem = createEquipment(getInstance().amulets.get(eid),uniqueID, level, scale);
        }

        return createdItem;
    }

    /**
     * Creates game equipments
     * @param cloneableEquip  the equipment to be cloned
     * @param uniqueID   the uniqueID of this equipment
     * @param level the level to scale this item effectiveness
     * @param scale if equipment level should be scaled or should be the received level parameter
     * @return  the generated item with all its information
     */
    private static Equipment createEquipment(Equipment cloneableEquip, int uniqueID, int level, boolean scale) {
        // creates equipment
        Equipment equip = new Equipment(cloneableEquip);

        // sets equipment uniqueID
        equip.setUniqueID(uniqueID);

        // scale equipment with level if wanted
        if(scale)
            equip.scale(level);
        else // do not scale, just set level
            equip.setLevel(level);

        return equip;
    }

    /**
     * Tries to drop any type of equipment based on
     * equipments drop chance and game current drop rate
     * @param level the level to scale equipment, if drop chance is achieved
     * @param depth the number of equipments to try dropping of each type
     * @return the equipment if drop chance is achieved, null otherwise
     */
    public static Equipment rollEquipment(int level, int depth) {
        // shuffles list of equips to try to drop in random order of equipment types
        Collections.shuffle(getInstance().equipments);

        // iterates through each type of equipment in shuffle order
        for(int i = 0 ; i < getInstance().equipments.size(); i++) {
            // current type of equipment
            Equipment.Slot equipType = getInstance().equipments.get(i).getRight();
            // current type of equipments equips
            HashMap<Integer, Equipment> equips = getInstance().equipments.get(i).getLeft();
            // tries to drop depth times equipments of each type
            for(int j = 0; j < depth; j++) {
                // random number between bounds of equipment type
                int equipIdx = Common.randInt(0, equips.size());
                // clamp for safety
                equipIdx = MathUtils.clamp(equipIdx, 0, equips.size()-1);
                // calculates current chance of dropping equipment based on
                // drop rate chances and also number of tries, maximum number of drops
                float chance = ((equips.get(equipIdx).getChance() * Config.dropRate) /
                                ((Config.maxNumberOfDrops/equips.size()) * Config.maxDropTries *
                                        (level * Config.levelEquipmentDropFactor))) * 0.216257f;

                // tries to drop equip of shuffled index
                if(Common.rollDice(chance)) { // successfully achieved drop chances
                    // lets get the correct first ID that depends of equipment type
                    int firstID;
                    switch (equipType) {
                        case HELMET:
                            firstID = helmetFirstID;
                            break;
                        case ARMOR:
                            firstID = armorFirstID;
                            break;
                        case LEGS:
                            firstID = legsFirstID;
                            break;
                        case BOOTS:
                            firstID = bootsFirstID;
                            break;
                        case AMULET:
                            firstID = amuletFirstID;
                            break;
                        case RING:
                            firstID = ringFirstID;
                            break;
                        case WEAPON:
                            firstID = weaponFirstID;
                            break;
                        case SHIELD:
                            firstID = shieldFirstID;
                            break;
                        default:
                            System.err.println("Unknown type of equipment to drop: "+equipType);
                            return null;
                    }
                    // lets create the equipment and return it
                    return createEquipment(equips.get(equipIdx), firstID + equipIdx, level, true);
                }
            }
        }

        // no drops were rolled, return null representing it
        return null;
    }

    /**
     * Gets an item name via its unique ID
     * @param uniqueID unique ID of item
     * @return the name of the item related to uniqueID received in parameters
     */
    public static String getItemName(int uniqueID) {
        String name = null;

        // check item type
        if(uniqueID < helmetFirstID) {

        }
        else if(uniqueID < armorFirstID) { // gets item name of helmet type using uniqueID
            int eid = uniqueID - helmetFirstID;
            name = getInstance().helmets.get(eid).getName();
        }
        else if(uniqueID < legsFirstID) { // gets item name of armor type using uniqueID
            int eid = uniqueID - armorFirstID;
            name = getInstance().armors.get(eid).getName();
        }
        else if(uniqueID < bootsFirstID) { // gets item name of legs type using uniqueID
            int eid = uniqueID - legsFirstID;
            name = getInstance().legs.get(eid).getName();
        }
        else if(uniqueID < weaponFirstID) { // gets item name of boots type using uniqueID
            int eid = uniqueID - bootsFirstID;
            name = getInstance().boots.get(eid).getName();
        }
        else if(uniqueID < shieldFirstID) { // gets item name of weapon type using uniqueID
            int eid = uniqueID - weaponFirstID;
            name = getInstance().weapons.get(eid).getName();
        }
        else if(uniqueID < ringFirstID) { // gets item name of shield type using uniqueID
            int eid = uniqueID - shieldFirstID;
            name = getInstance().shields.get(eid).getName();
        }
        else if(uniqueID < amuletFirstID) { // gets item name of rings type using uniqueID
            int eid = uniqueID - ringFirstID;
            name = getInstance().rings.get(eid).getName();
        }
        else { // gets item name of amulet type using uniqueID
            int eid = uniqueID - amuletFirstID;
            name = getInstance().amulets.get(eid).getName();
        }

        return name;
    }

    /**
     * Gets an item sprite via its unique ID
     * @param uniqueID unique ID of item
     * @return the sprite of the item related to uniqueID received in parameters
     */
    public static TextureRegion getItemSprite(int uniqueID) {
        TextureRegion sprite = null;

        // check item type
        if(uniqueID < helmetFirstID) {

        }
        else if(uniqueID < armorFirstID) { // gets item sprite of helmet type using uniqueID
            int eid = uniqueID - helmetFirstID;
            sprite = getInstance().helmets.get(eid).getSprite();
        }
        else if(uniqueID < legsFirstID) { // gets item sprite of armor type using uniqueID
            int eid = uniqueID - armorFirstID;
            sprite = getInstance().armors.get(eid).getSprite();
        }
        else if(uniqueID < bootsFirstID) { // gets item sprite of legs type using uniqueID
            int eid = uniqueID - legsFirstID;
            sprite = getInstance().legs.get(eid).getSprite();
        }
        else if(uniqueID < weaponFirstID) { // gets item sprite of boots type using uniqueID
            int eid = uniqueID - bootsFirstID;
            sprite = getInstance().boots.get(eid).getSprite();
        }
        else if(uniqueID < shieldFirstID) { // gets item sprite of weapon type using uniqueID
            int eid = uniqueID - weaponFirstID;
            sprite = getInstance().weapons.get(eid).getSprite();
        }
        else if(uniqueID < ringFirstID) { // gets item sprite of shield type using uniqueID
            int eid = uniqueID - shieldFirstID;
            sprite = getInstance().shields.get(eid).getSprite();
        }
        else if(uniqueID < amuletFirstID) { // gets item sprite of rings type using uniqueID
            int eid = uniqueID - ringFirstID;
            sprite = getInstance().rings.get(eid).getSprite();
        }
        else { // gets item sprite of amulet type using uniqueID
            int eid = uniqueID - amuletFirstID;
            sprite = getInstance().amulets.get(eid).getSprite();
        }

        return sprite;
    }

    /**
     * Gets and item description via its unique ID and level
     * @param uniqueID unique ID of item
     * @param level level of item to adjust description effects
     * @return the description of the item related to uniqueID and level received in parameters
     */
    public static String getItemDescription(int uniqueID, int level) {
        String desc = null;

        // check item type
        if(uniqueID < helmetFirstID) {

        }
        else if(uniqueID < armorFirstID) { // gets item description of helmet type using uniqueID
            int eid = uniqueID - helmetFirstID;
            Equipment equip = new Equipment(getInstance().helmets.get(eid)); // clones factory equipment
            equip.setLevel(level); // sets level to adjust description
            desc = equip.getEquipmentDescription(); // gets adjusted description
        }
        else if(uniqueID < legsFirstID) { // gets item description of armor type using uniqueID
            int eid = uniqueID - armorFirstID;
            Equipment equip = new Equipment(getInstance().armors.get(eid)); // clones factory equipment
            equip.setLevel(level); // sets level to adjust description
            desc = equip.getEquipmentDescription(); // gets adjusted description
        }
        else if(uniqueID < bootsFirstID) { // gets item description of legs type using uniqueID
            int eid = uniqueID - legsFirstID;
            Equipment equip = new Equipment(getInstance().legs.get(eid)); // clones factory equipment
            equip.setLevel(level); // sets level to adjust description
            desc = equip.getEquipmentDescription(); // gets adjusted description
        }
        else if(uniqueID < weaponFirstID) { // gets item description of boots type using uniqueID
            int eid = uniqueID - bootsFirstID;
            Equipment equip = new Equipment(getInstance().boots.get(eid)); // clones factory equipment
            equip.setLevel(level); // sets level to adjust description
            desc = equip.getEquipmentDescription(); // gets adjusted description
        }
        else if(uniqueID < shieldFirstID) { // gets item description of weapon type using uniqueID
            int eid = uniqueID - weaponFirstID;
            Equipment equip = new Equipment(getInstance().weapons.get(eid)); // clones factory equipment
            equip.setLevel(level); // sets level to adjust description
            desc = equip.getEquipmentDescription(); // gets adjusted description
        }
        else if(uniqueID < ringFirstID) { // gets item description of shield type using uniqueID
            int eid = uniqueID - shieldFirstID;
            Equipment equip = new Equipment(getInstance().shields.get(eid)); // clones factory equipment
            equip.setLevel(level); // sets level to adjust description
            desc = equip.getEquipmentDescription(); // gets adjusted description
        }
        else if(uniqueID < amuletFirstID) { // gets item description of rings type using uniqueID
            int eid = uniqueID - ringFirstID;
            Equipment equip = new Equipment(getInstance().rings.get(eid)); // clones factory equipment
            equip.setLevel(level); // sets level to adjust description
            desc = equip.getEquipmentDescription(); // gets adjusted description
        }
        else { // gets item description of amulet type using uniqueID
            int eid = uniqueID - amuletFirstID;
            Equipment equip = new Equipment(getInstance().amulets.get(eid)); // clones factory equipment
            equip.setLevel(level); // sets level to adjust description
            desc = equip.getEquipmentDescription(); // gets adjusted description
        }

        return desc;
    }

    /**
     * disposes resources
     */
    public void dispose() {
        equipSheet.dispose(); //disposes equipment sprite sheet
    }
}
