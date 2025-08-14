package com.svalero.mijuego;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.svalero.mijuego.audio.AudioManager;
import com.svalero.mijuego.screens.GameScreen;
import com.svalero.mijuego.screens.MenuScreen;

/**
 * Clase principal del juego.
 * - Mantiene la SpriteBatch global (para dibujar en todas las pantallas).
 * - Gestiona la música global (bgm).
 * - Guarda el marcador acumulado de toda la partida (totalScore).
 */
public class MiJuego extends Game {

    // Batch global para dibujado (HUD, sprites, etc.)
    public SpriteBatch batch;

    // Música de fondo global (opcional si existe el archivo)
    public Music bgm;

    // === Marcador global acumulado de toda la partida ===
    // Lo irás sumando desde GameScreen (collectibles, bonus, etc.)
    public int totalScore = 0;

    @Override
    public void create() {
        batch = new SpriteBatch();

        // Cargar y reproducir música global si existe
        if (Gdx.files.internal("music/bgm.ogg").exists()) {
            bgm = Gdx.audio.newMusic(Gdx.files.internal("music/bgm.ogg"));
            bgm.setLooping(true);
            bgm.setVolume(0.5f);
            bgm.play();
            // Registrar en tu AudioManager para que respete mute/volumen
            AudioManager.registerMusic(bgm);
        }

        // Pantalla inicial del juego (menú principal)
        setScreen(new MenuScreen(this));
    }

    /**
     * Llamar cuando empiece una nueva partida (por ejemplo desde el menú).
     * - Resetea el totalScore a 0
     * - Entra al nivel 1
     */
    public void startNewGame() {
        totalScore = 0;
        setScreen(new GameScreen(this, 1));
    }

    /**
     * Suma puntos al marcador global con una pequeña protección (no baja de 0).
     * Útil si en algún momento quieres restar (pasa un valor negativo).
     */
    public void addToTotalScore(int amount) {
        totalScore = Math.max(0, totalScore + amount);
    }

    @Override
    public void dispose() {
        // primero dejar que la pantalla actual se libere
        if (getScreen() != null) getScreen().dispose();

        // Parar y liberar música
        if (bgm != null) {
            bgm.stop();
            bgm.dispose();
        }

        // Liberar batch global
        if (batch != null) batch.dispose();
    }
}
