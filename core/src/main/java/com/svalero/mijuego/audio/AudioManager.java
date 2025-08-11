package com.svalero.mijuego.audio;

import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;

/* Gestor sencillo de audio para controlar volumen global. */
public class AudioManager {
    private static Music bgm;
    private static float musicVolume = 0.5f; // 0..1
    private static float sfxVolume = 0.8f;   // 0..1

    public static void registerMusic(Music music){
        bgm = music;
        applyMusicVolume();
    }

    public static void setMusicVolume(float v){
        musicVolume = clamp01(v);
        applyMusicVolume();
    }
    public static float getMusicVolume(){ return musicVolume; }

    private static void applyMusicVolume(){ if (bgm != null) bgm.setVolume(musicVolume); }

    public static void setSfxVolume(float v){ sfxVolume = clamp01(v); }
    public static float getSfxVolume(){ return sfxVolume; }

    /* Reproduce un SFX respetando el volumen global de efectos. */
    public static long play(Sound sfx){ return sfx != null ? sfx.play(sfxVolume) : -1; }

    private static float clamp01(float x){ return Math.max(0f, Math.min(1f, x)); }
}

