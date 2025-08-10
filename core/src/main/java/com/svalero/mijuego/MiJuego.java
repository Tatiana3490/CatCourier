package com.svalero.mijuego;


import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.svalero.mijuego.screens.MenuScreen;

public class MiJuego extends Game {
    public SpriteBatch batch;
    public Music bgm; // ← música global

    @Override
    public void create() {
        batch = new SpriteBatch();

        // Cargar y reproducir una sola vez
        if (Gdx.files.internal("music/bgm.ogg").exists()) {
            bgm = Gdx.audio.newMusic(Gdx.files.internal("music/bgm.ogg"));
            bgm.setLooping(true);
            bgm.setVolume(0.5f);
            bgm.play();
        }

        setScreen(new MenuScreen(this));
    }

    @Override
    public void dispose() {
        if (batch != null) batch.dispose();
        if (bgm != null) bgm.dispose(); // se destruye al cerrar el juego
        if (getScreen() != null) getScreen().dispose();
    }
}
