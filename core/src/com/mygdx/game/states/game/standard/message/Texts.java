package com.mygdx.game.states.game.standard.message;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.mygdx.game.Main;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Class that act as a helper to build text messages
 * of game to be displayed at any time
 *
 * @author  Pedro Sampaio
 * @since   1.6
 */
public class Texts {

    /**
     * Returns a TextMessage of default message retrieved via key parameter
     * @param key       the message content key in language properties files
     * @param anchor    the anchor position of text message
     * @return  the text message object to be rendered at any time
     */
    public static TextMessage msgDefault(String key, TextMessage.Anchor anchor) {
        return new TextMessage(Main.getInstance().getLang().get(key), anchor);
    }

    /**
     * Returns a TextMessage of enemy reward information
     * @param exp       the exp reward
     * @param gold      the gold reward
     * @param items     the hash map of items dropped with their quantities
     * @param anchor    the anchor position of text message
     * @return  the text message object to be rendered at any time
     */
    public static TextMessage msgReward(int exp, int gold, HashMap<String, Integer> items, TextMessage.Anchor anchor) {
        String msgText = Main.getInstance().getLang().get("msgReward") + "\n"; // reward string
        msgText +=  Main.getInstance().getLang().get("statsExp") + ": " + exp + "\n"; // exp info
        msgText +=  Main.getInstance().getLang().get("goldStr") + ": " + gold + "\n"; // gold info
        msgText += "\\"; // force new page in message text

        // inventory is full, no more items can be obtained
        if(items == null) {
            msgText += Main.getInstance().getLang().get("msgInvFull");
            return new TextMessage(msgText, anchor); // returns built message
        }

        // no items were dropped
        if(items.size() <= 0) {
            msgText += Main.getInstance().getLang().get("msgNoDrop"); // no drop message
            return new TextMessage(msgText, anchor); // returns built message
        }

        // else, items were dropped and their info should be added
        msgText += Main.getInstance().getLang().get("msgDrop"); // drop message
        Iterator it = items.entrySet().iterator();
        // for each item, concatenates msg string with quantities and item names
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry) it.next();
            msgText += '\n';
            msgText += pair.getValue()+"x " + pair.getKey() + " ";
        }
        return new TextMessage(msgText, anchor);
    }

    /**
     * Test message
     * @param anchor    the anchor position of text message
     * @return  the text message object to be rendered at any time
     */
    public static TextMessage msgTest(TextMessage.Anchor anchor) {
        String msgTest = "Lorem ipsum dolor sit amet, tacimates expetendis ei nec, qui at assum erant honestatis. His everti utroque eu, mel adhuc eripuit moderatius no, et eam tibique perpetua qualisque. Ut laudem vocibus pro, at vel oporteat theophrastus, at pro accusamus referrentur. Qui cu equidem similique, ius ea dolores adipisci reformidans.\n" +
                "Rebum tation prompta te nec, ea numquam bonorum pri. Ea quis moderatius mea, his no unum fabulas volumus. Veniam alterum theophrastus ut ius, te nec rebum solet cotidieque. Torquatos democritum eos cu. Labore commune sed et, ponderum maiestatis percipitur est an. An dico dolor homero eos, in eam duis perpetua, nam te vocent scriptorem theophrastus.\n" +
                "Dicant decore ea nam. In nonumy quodsi fabellas pro, mazim rationibus id nec. Elitr cotidieque no sit, quem fugit te eam. Ne aeque mucius eam, vix ad euismod nusquam, elitr aeterno no vis. Ex pri audire indoctum vulputate, sea eius similique cu.\n" +
                "Sed latine necessitatibus ei, pri at legere gloriatur. Sonet honestatis referrentur an has. An pro amet solet, vis ei postulant necessitatibus. Ludus deserunt interesset per te.\n" +
                "Choro accusam ne usu. Solum posse animal mea ei. Sed ei oportere moderatius, eum stet utroque mnesarchum an. Probo atqui dissentiet vix id, dolorem vivendum duo ex.\n" +
                "Lorem novum efficiendi id cum, an has enim clita maiorum. Vix aliquip volutpat ne. Ea gloriatur delicatissimi sea, augue viderer nostrum pro an. Eum tollit oporteat delicata an, modo liber aeterno et cum, eum id quando utroque posidonium. At offendit assueverit mel.\n" +
                "Tacimates splendide dissentiet mei no. Ne est vide vocibus officiis, ea enim salutatus eum. Discere periculis salutatus cu vim, duo no ubique perfecto scripserit. Movet aliquip aperiri vim ne, usu te consul aliquam, quo ei simul accusam.\n" +
                "His meis fabellas consectetuer ex, est id tota iriure. No omittam oporteat mel, sanctus dolores sea id. Viris ocurreret repudiandae usu ad, duo at tempor blandit. Eros soleat singulis eam an, sea aperiri suscipiantur deterruisset te. Diceret fabellas et vim, ignota postulant ex usu. Ut voluptaria accommodare per, graece audiam option usu et.\n" +
                "Has cu cibo mazim nostrum, melius constituto ad pri. Te vis timeam invidunt liberavisse, eu nec fugit scriptorem, ei maiorum disputationi consectetuer vix. Te sed impedit atomorum temporibus, ut ius exerci appellantur. Ad dolore putent disputando mea, qui ubique feugait et, iuvaret civibus oportere his ei.\n" +
                "Ex legere eruditi quo, amet wisi duo in, ex nec modo feugiat commune. Omnes vivendum deterruisset id usu, eum mucius partiendo te, meliore salutandi qualisque eum an. Vidit discere ad sit. Ei epicurei partiendo pri.";
        return new TextMessage(msgTest, anchor);
    }
}
