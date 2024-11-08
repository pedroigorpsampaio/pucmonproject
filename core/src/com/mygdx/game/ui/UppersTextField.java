package com.mygdx.game.ui;

import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;

/**
 * Created by fmmarzoa on 12/23/16.
 * Modified by Pedro Sampaio
 */
public class UppersTextField extends TextField {
    public UppersTextField(String text, Skin style) {
        super(text, style);
    }

    @Override
    protected InputListener createInputListener() {
        return new TextFieldClickListener() {
            @Override
            public boolean keyTyped(InputEvent event, char character) {
                return super.keyTyped(event, Character.toUpperCase(character));
            }
        };
    }

    @Override
    public void setText(String str) {
        super.setText(str.toUpperCase());
    }
}