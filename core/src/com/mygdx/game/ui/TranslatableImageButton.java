package com.mygdx.game.ui;

import com.badlogic.gdx.scenes.scene2d.ui.ImageTextButton;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.mygdx.game.Main;

/**
 * Class that represents a image text button with
 * volatile text that depends on current language chosen
 */
public class TranslatableImageButton extends ImageTextButton{

    String langKey;

    public TranslatableImageButton(String langKey, Skin skin) {
        super(Main.getInstance().getLang().get(langKey), skin);

        this.langKey = langKey;
    }

    public void updateLanguage() {
        this.setText(Main.getInstance().getLang().get(langKey));
    }

}
