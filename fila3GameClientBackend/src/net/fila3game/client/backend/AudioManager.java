package net.fila3game.client.backend;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import java.io.File;
import java.net.URL;
import java.util.HashMap;

/**
 * Created by codecadet on 2/25/17.
 */
public class AudioManager {

    public static HashMap<String, Clip> soundClips;
    private static AudioInputStream inputStream;

    private final static boolean ENABLE = false;


    public static void load(String[] soundNames) {

        if (!ENABLE) {
            return;
        }

        soundClips = new HashMap<>(soundNames.length);

        for (String soundName : soundNames) {
            try {
                soundClips.put(soundName, AudioSystem.getClip());

                // load sound from jar
                String pathStr = soundName + ".wav";
                URL soundURL = GameClient.class.getClassLoader().getResource(pathStr);

                if (soundURL == null) {
                    File f = new File("resources" + File.separator + soundName + ".wav");
                    soundURL = f.toURI().toURL();

                }


                inputStream = AudioSystem.getAudioInputStream(soundURL);
                soundClips.get(soundName).open(inputStream);

            } catch (Exception e) {
                System.err.println(e);
            }
        }
    }

    public static void start(String soundName) {

        if (soundClips == null) {
            return;
        }

        if (!soundClips.get(soundName).isRunning()) {
            soundClips.get(soundName).start();
            soundClips.get(soundName).setFramePosition(0);
        }
    }

    public static void loop(String soundName, int time) {

        if (soundClips == null) {
            return;
        }

        if (!soundClips.get(soundName).isRunning()) {
            soundClips.get(soundName).loop(time);
            soundClips.get(soundName).setFramePosition(0);
        }
    }

    public static void stopAll() {

        if (soundClips == null) {
            return;
        }

        for (String soundName : soundClips.keySet()) {
            soundClips.get(soundName).stop();
        }
    }

    public static void stop(String soundName) {
        if (soundClips == null) {
            return;
        }
        soundClips.get(soundName).stop();
    }

}

