package com.svalero.mijuego.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.svalero.mijuego.MiJuego;
import com.svalero.mijuego.ui.UiStyles;
import com.svalero.mijuego.util.Constants;

public class MenuScreen implements Screen {

    private final MiJuego game;
    private Stage stage;
    private Skin skin;
    private boolean built = false;

    public MenuScreen(MiJuego game) {
        this.game = game;
    }

    @Override
    public void show() {
        // Creamos stage y skin si aún no existen
        if (stage == null) {
            stage = new Stage(new FitViewport(Constants.VIRTUAL_WIDTH, Constants.VIRTUAL_HEIGHT), game.batch);
        }
        if (skin == null) {
            skin = UiStyles.makeSkin();
        }

        // Construimos la interfaz solo una vez
        if (!built) {
            stage.clear();
            buildUI();
            built = true;
        }

        // Enviamos el input al stage del menú
        Gdx.input.setInputProcessor(stage);
    }

    //Construye todos los widgets del menú
    private void buildUI() {
        Table root = new Table(skin);
        root.setFillParent(true);
        stage.addActor(root);

        // Título y botones
        Label title       = new Label("Cat Courier", skin, "title");

        TextButton btnPlay    = new TextButton("Jugar", skin);
        TextButton btnRanking = new TextButton("Ranking", skin);
        TextButton btnConf    = new TextButton("Configuración", skin);
        TextButton btnInstr   = new TextButton("Instrucciones", skin);
        TextButton btnExit    = new TextButton("Salir", skin);

        // Layout (una fila por botón)
        root.add(title).padBottom(30).row();
        root.add(btnPlay).width(320).height(64).pad(8).row();
        root.add(btnRanking).width(320).height(64).pad(8).row();
        root.add(btnConf).width(320).height(64).pad(8).row();
        root.add(btnInstr).width(320).height(64).pad(8).row();
        root.add(btnExit).width(320).height(64).pad(22).row();

        // Listeners con ClickListener.clicked
        btnPlay.addListener(new ClickListener() {
            @Override public void clicked(InputEvent event, float x, float y) {
                game.setScreen(new GameScreen(game, 1));
            }
        });

        btnRanking.addListener(new ClickListener() {
            @Override public void clicked(InputEvent event, float x, float y) {
                game.setScreen(new HighScoresScreen(game));
            }
        });

        btnConf.addListener(new ClickListener() {
            @Override public void clicked(InputEvent event, float x, float y) {
                game.setScreen(new SettingsScreen(game));
            }
        });

        btnInstr.addListener(new ClickListener() {
            @Override public void clicked(InputEvent event, float x, float y) {
                game.setScreen(new InstructionsScreen(game));
            }
        });

        btnExit.addListener(new ClickListener() {
            @Override public void clicked(InputEvent event, float x, float y) {
                Gdx.app.exit();
            }
        });
    }

    @Override
    public void render(float delta) {
        // Limpiamos la pantalla y dibujamos el stage
        Gdx.gl.glClearColor(0, 0, 0, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        stage.act(delta);
        stage.draw();
    }

    @Override public void resize(int w, int h) { stage.getViewport().update(w, h, true); }
    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}

    @Override
    public void dispose() {
        if (stage != null) stage.dispose();
        if (skin  != null) skin.dispose();
    }
}
