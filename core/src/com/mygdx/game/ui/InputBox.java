package com.mygdx.game.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.mygdx.game.Main;

/**
 * Class that helps input retrieving for text fields
 * so that an external box pop up and help data retrieving
 * while allowing each input given to be seen always
 *
 * @author  Pedro Sampaio
 * @since   2.0
 */
public class InputBox implements TextField.OnscreenKeyboard {

    private TextField tf;
    private String title;
    private String defaultStr;
    private String hint;

    /**
     * Receives necessary data to open external input box
     * @param tf            the textfield that will receive this input
     * @param title         the title of this input box
     * @param defaultStr    the default input for this input box if there is any
     * @param hint          the hint of this input box
     */
    public InputBox (TextField tf, String title, String defaultStr, String hint) {
        this.tf = tf;
        this.title = title;
        this.defaultStr = defaultStr;
        this.hint = hint;
    }

    @Override
    public void show(boolean visible) {
        Gdx.input.getTextInput(new Input.TextInputListener() {

            @Override
            public void input(String text) {
                tf.setText(text);
            }

            @Override
            public void canceled() {
                System.out.println("Cancelled.");
            }
        }, this.title, this.defaultStr, this.hint);
    }
}
