package com.gokhanozg.ww.controller;

import org.apache.log4j.Logger;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;

/**
 * Created by mephala on 7/7/17.
 */
public class PlayCatSound {
    private static Logger logger = Logger.getLogger(PlayCatSound.class);

    public static void play() {
        try {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Clip clip = AudioSystem.getClip();
                        AudioInputStream ais = AudioSystem.getAudioInputStream(this.getClass().getResourceAsStream("/cat.wav"));
                        clip.open(ais);
                        clip.start();
                        logger.info("Playing cat sound...");
                    } catch (Throwable t) {
                        logger.error("!!! Failed to play cat voice !!!", t);
                    }
                }
            }).start();

        } catch (Throwable t) {

        }
    }
}
