package com.svalero.mijuego;


import com.badlogic.gdx.Game;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.ScreenUtils;
import com.svalero.mijuego.screens.MenuScreen;

public class MiJuego extends Game {
    public SpriteBatch batch; // Usable en todas las pantallas

    @Override
    public void create() {
        batch = new SpriteBatch();
        setScreen(new MenuScreen(this));
    }

    @Override
    public void render() {
        ScreenUtils.clear(0.07f, 0.07f, 0.09f, 1f); // Fondo oscuro por defecto
        super.render(); // delega en la screen actual
    }

    @Override
    public void dispose() {
        if (batch != null) batch.dispose();
        if (getScreen() != null) getScreen().dispose();
    }
}
