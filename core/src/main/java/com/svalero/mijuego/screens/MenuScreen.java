package com.svalero.mijuego.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.svalero.mijuego.MiJuego;
import com.svalero.mijuego.ui.UiStyles;
import com.svalero.mijuego.util.Constants;

public class MenuScreen implements Screen {

    private final MiJuego game;
    private Stage stage;
    private Skin skin;
    private boolean built = false;   // <- evita construir dos veces

    public MenuScreen(MiJuego game) {
        this.game = game;
    }

    @Override
    public void show() {
        if (stage == null) {
            stage = new Stage(new FitViewport(Constants.VIRTUAL_WIDTH, Constants.VIRTUAL_HEIGHT), game.batch);
        }
        if (skin == null) {
            skin = UiStyles.makeSkin();
        }

        if (!built) {            // <- solo la primera vez
            stage.clear();       // por si acaso
            buildUI();
            built = true;
        }

        Gdx.input.setInputProcessor(stage);
    }

    private void buildUI() {
        Table root = new Table(skin);
        root.setFillParent(true);
        stage.addActor(root);

        Label title = new Label("Cat Courier", skin, "title");

        TextButton btnPlay   = new TextButton("Jugar", skin);
        TextButton btnConf   = new TextButton("Configuración", skin);
        TextButton btnInstr  = new TextButton("Instrucciones", skin);
        TextButton btnExit   = new TextButton("Salir", skin);

        root.add(title).padBottom(30).row();
        root.add(btnPlay).width(320).height(64).pad(8).row();
        root.add(btnConf).width(320).height(64).pad(8).row();
        root.add(btnInstr).width(320).height(64).pad(8).row();
        root.add(btnExit).width(320).height(64).pad(22).row();

        btnPlay.addListener(e -> { if (!btnPlay.isPressed()) return false; game.setScreen(new GameScreen(game, 1)); return true; });
        btnConf.addListener(e -> { if (!btnConf.isPressed()) return false; game.setScreen(new SettingsScreen(game)); return true; });
        btnInstr.addListener(e -> { if (!btnInstr.isPressed()) return false; game.setScreen(new InstructionsScreen(game)); return true; });
        btnExit.addListener(e -> { if (!btnExit.isPressed()) return false; Gdx.app.exit(); return true; });
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
