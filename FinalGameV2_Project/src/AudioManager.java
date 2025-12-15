 package src;

import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * AudioManager handles loading and playing sound effects for the game.
 * Uses Java's sound API to play .wav and .ogg audio files.
 */
public class AudioManager {
    private final Map<String, Clip> soundClips = new HashMap<>();
    private boolean soundEnabled = true;

    public AudioManager() {
        // Load all sound files
        loadSound("click", "audio/click.wav");
        loadSound("level_next", "audio/level_next.wav");
        loadSound("lose", "audio/lose.wav");
        loadSound("mage", "audio/mage.wav");
        loadSound("slash", "audio/slash.wav");
        loadSound("won", "audio/won.wav");
        loadSound("boss_hit", "audio/boss_hit.wav");
        loadSound("damage", "audio/damage.wav");
        loadSound("pick_uped", "audio/pick_uped.wav");
        loadSound("boom", "audio/boom.wav");
        loadSound("dash", "audio/dash.wav");
    }

    /**
     * Load a sound file and store it in the sound clips map.
     * @param name The identifier for the sound
     * @param path The relative path to the audio file
     */
    private void loadSound(String name, String path) {
        try {
            File soundFile = new File(path);
            if (!soundFile.exists()) {
                System.err.println("Warning: Sound file not found: " + path);
                return;
            }
            
            AudioInputStream audioStream = AudioSystem.getAudioInputStream(soundFile);
            AudioFormat sourceFormat = audioStream.getFormat();
            
            // Convert 24-bit audio to 16-bit if necessary
            if (sourceFormat.getSampleSizeInBits() == 24 || sourceFormat.getSampleSizeInBits() == 32) {
                AudioFormat targetFormat = new AudioFormat(
                    AudioFormat.Encoding.PCM_SIGNED,
                    sourceFormat.getSampleRate(),
                    16, // Convert to 16-bit
                    sourceFormat.getChannels(),
                    sourceFormat.getChannels() * 2, // 16-bit = 2 bytes per sample
                    sourceFormat.getSampleRate(),
                    false // little-endian
                );
                
                audioStream = AudioSystem.getAudioInputStream(targetFormat, audioStream);
            }
            
            Clip clip = AudioSystem.getClip();
            clip.open(audioStream);
            soundClips.put(name, clip);
        } catch (UnsupportedAudioFileException e) {
            System.err.println("Warning: Unsupported audio format for " + path + " (skipping)");
        } catch (IOException | LineUnavailableException e) {
            System.err.println("Error loading sound: " + path + " - " + e.getMessage());
        }
    }

    /**
     * Play a sound effect by name.
     * @param name The identifier of the sound to play
     */
    public void playSound(String name) {
        if (!soundEnabled) return;
        
        Clip clip = soundClips.get(name);
        if (clip != null) {
            // Stop and reset the clip if it's already playing
            if (clip.isRunning()) {
                clip.stop();
            }
            clip.setFramePosition(0);
            clip.start();
        } else {
            System.err.println("Sound not found: " + name);
        }
    }

    /**
     * Stop a currently playing sound.
     * @param name The identifier of the sound to stop
     */
    public void stopSound(String name) {
        Clip clip = soundClips.get(name);
        if (clip != null && clip.isRunning()) {
            clip.stop();
            clip.setFramePosition(0);
        }
    }

    /**
     * Stop all currently playing sounds.
     */
    public void stopAllSounds() {
        for (Clip clip : soundClips.values()) {
            if (clip != null && clip.isRunning()) {
                clip.stop();
                clip.setFramePosition(0);
            }
        }
    }

    /**
     * Enable or disable sound playback.
     * @param enabled true to enable sounds, false to disable
     */
    public void setSoundEnabled(boolean enabled) {
        this.soundEnabled = enabled;
        if (!enabled) {
            stopAllSounds();
        }
    }

    /**
     * Clean up resources when the audio manager is no longer needed.
     */
    public void dispose() {
        for (Clip clip : soundClips.values()) {
            if (clip != null) {
                clip.close();
            }
        }
        soundClips.clear();
    }
}
