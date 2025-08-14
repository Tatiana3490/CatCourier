package com.svalero.mijuego.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.svalero.mijuego.MiJuego;
import com.svalero.mijuego.scores.ScoreEntry;
import com.svalero.mijuego.scores.ScoreManager;
import com.svalero.mijuego.ui.UiStyles;
import com.svalero.mijuego.util.Constants;

import java.util.List;

public class HighScoresScreen implements Screen {

    private final MiJuego game;
    private Stage stage;
    private Skin skin;

    public HighScoresScreen(MiJuego game) {
        this.game = game;
    }

    @Override
    public void show() {
        stage = new Stage(new FitViewport(Constants.VIRTUAL_WIDTH, Constants.VIRTUAL_HEIGHT), game.batch);
        skin  = UiStyles.makeSkin();
        Gdx.input.setInputProcessor(stage);

        Table root = new Table();  // sin skin para que no busque drawables por defecto
        root.setFillParent(true);
        root.pad(24);
        stage.addActor(root);

        // "Tarjeta" contenedora
        Table card = new Table();
        card.defaults().pad(6);

        // Fondo seguro: intenta varios nombres comunes; si no hay ninguno, no pone fondo.
        try {
            if (skin.has("window", Drawable.class)) {
                card.setBackground(skin.getDrawable("window"));
            } else if (skin.has("panel", Drawable.class)) {
                card.setBackground(skin.getDrawable("panel"));
            } else if (skin.has("button", Drawable.class)) {
                card.setBackground(skin.getDrawable("button"));
            }
        } catch (Exception ignore) { }

        root.add(card).width(900).height(520).center();

        Label title = new Label("Mejores Puntuaciones", skin, "title");
        card.add(title).padBottom(16).row();

        Table header = new Table();
        header.defaults().pad(4).left();
        header.add(new Label("#",      skin)).width(40);
        header.add(new Label("Nombre", skin)).width(280);
        header.add(new Label("Puntos", skin)).width(140);
        header.add(new Label("Fecha",  skin)).width(220);
        card.add(header).growX().row();

        Table rows = new Table();
        rows.defaults().pad(2).left();

        java.util.List<com.svalero.mijuego.scores.ScoreEntry> top = com.svalero.mijuego.scores.ScoreManager.getTop(10);
        int i = 1;
        for (com.svalero.mijuego.scores.ScoreEntry s : top) {
            rows.add(new Label(String.valueOf(i++), skin)).width(40);
            rows.add(new Label(s.name,  skin)).width(280);
            rows.add(new Label(String.valueOf(s.score), skin)).width(140);
            rows.add(new Label(s.date,  skin)).width(220);
            rows.row();
        }

        ScrollPane sp = new ScrollPane(rows, skin);
        sp.setFadeScrollBars(false);
        card.add(sp).growX().height(330).padBottom(12).row();

        TextButton back = new TextButton("Volver", skin);
        back.addListener(e -> { if (!back.isPressed()) return false; game.setScreen(new MenuScreen(game)); return true; });
        card.add(back).width(220).height(52).padTop(6);
    }


    @Override public void render(float delta) {
        Gdx.gl.glClearColor(0,0,0,1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        stage.act(delta);
        stage.draw();
    }

    @Override public void resize(int w, int h) { stage.getViewport().update(w, h, true); }
    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}
    @Override public void dispose() { if (stage != null) stage.dispose(); if (skin != null) skin.dispose(); }
}
