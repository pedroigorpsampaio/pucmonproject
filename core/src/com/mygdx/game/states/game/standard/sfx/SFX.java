package com.mygdx.game.states.game.standard.sfx;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.math.MathUtils;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Class that holds all game sound effects
 *
 * @author Pedro Sampaio
 * @since 1.0
 */
public class SFX {
    // enum with all avaiable sound effects
    public enum Effect {Hit_Enemy, Critical_Hit_1}

    // enum with all avaiable bgms
    public enum BGM {Fanfare_Victory, Fanfare_Defeat}


    private HashMap<Effect, Sound> stationSFX; // pairs of effects and sounds for playing
    private HashMap<BGM, Music> stationBGM; // pairs of BGMS and musics for playing
    private static SFX instance = null;

    /**
     * Constructor o defeat instantiation
     */
    private SFX() {
        //initializes stations
        stationSFX = new HashMap<Effect, Sound>();
        stationBGM = new HashMap<BGM, Music>();
    }

    /**
     * gets singleton instance
     * @return the singleton instance of SFX
     */
    public static SFX getInstance() {
        if(instance == null)
            instance = new SFX();

        return instance;
    }

    /**
     * Loads all sound effects of game
     */
    public void loadSFX() {
        // load all sounds
        Sound sfxCritHit1 = Gdx.audio.newSound(Gdx.files.internal("sfx/Critical_Hit_1.wav"));
        Sound sfxHitEnemy = Gdx.audio.newSound(Gdx.files.internal("sfx/Hit_Enemy.ogg"));
        Music sfxVictoryFanfare = Gdx.audio.newMusic(Gdx.files.internal("sfx/Fanfare_Victory.ogg"));
        Music sfxDefeatFanfare = Gdx.audio.newMusic(Gdx.files.internal("sfx/Fanfare_Defeat.ogg"));

        // adds sounds to station attached to effects key
        stationSFX.put(Effect.Critical_Hit_1, sfxCritHit1);
        stationSFX.put(Effect.Hit_Enemy, sfxHitEnemy);
        stationBGM.put(BGM.Fanfare_Victory, sfxVictoryFanfare);
        stationBGM.put(BGM.Fanfare_Defeat, sfxDefeatFanfare);
    }

    /**
     * Disposes sounds and bgms of game
     */
    public void dispose() {
        // iterates through stations disposing all sounds and bgms
        Iterator it = stationSFX.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry) it.next();
            ((Sound)pair.getValue()).dispose();
        }
        it = stationBGM.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry) it.next();
            ((Music)pair.getValue()).dispose();
        }
    }

    /**
     * Plays the sound effect at desired volume
     * @param sfx   the sound effect to be played
     * @param volume the volume to set sound effect (between 0.0f and 1.0f)
     */
    public static void playSFX(Effect sfx, float volume) {
        // clamp volume
        volume = MathUtils.clamp(volume, 0.0f, 1.0f);
        // plays sound effect
        getInstance().stationSFX.get(sfx).play(volume);
    }

    /**
     * Plays the bgm at desired volume
     * @param bgm       the bgm to be played
     * @param volume    the volume
     * @param loop      if this bgm should be looped
     */
    public static void playBGM(BGM bgm, float volume, boolean loop) {
        // clamp volume
        volume = MathUtils.clamp(volume, 0.0f, 1.0f);
        // plays bgm if not playing yet
        if(!getInstance().stationBGM.get(bgm).isPlaying()) {
            getInstance().stationBGM.get(bgm).setVolume(volume);
            getInstance().stationBGM.get(bgm).setLooping(loop);
            getInstance().stationBGM.get(bgm).play();
        }
    }

    /**
     * Stops the the bgm received in parameter
     * @param bgm       the bgm to be played
     */
    public static void stopBGM(BGM bgm) {
        // stops bgm it it is playing
        if(getInstance().stationBGM.get(bgm).isPlaying())
            getInstance().stationBGM.get(bgm).stop();
    }
}
