package com.svalero.mijuego.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.svalero.mijuego.MiJuego;
import com.svalero.mijuego.ui.UiStyles;
import com.svalero.mijuego.util.Constants;

public class MenuScreen implements Screen {
    private final MiJuego game;
    private Stage stage;
    private Skin skin;
    private OrthographicCamera camera;

    public MenuScreen(MiJuego game) {
        this.game = game;
    }

    @Override
    public void show() {
        camera = new OrthographicCamera();
        stage = new Stage(new FitViewport(Constants.VIRTUAL_WIDTH, Constants.VIRTUAL_HEIGHT, camera), game.batch);
        skin = UiStyles.makeSkin();

        // UI
        Table root = new Table();
        root.setFillParent(true);
        stage.addActor(root);

        Label title = new Label("CAT COURIER", skin);
        TextButton play = new TextButton("Jugar", skin);
        TextButton settings = new TextButton("Configuración", skin);
        TextButton help = new TextButton("Instrucciones", skin);
        TextButton exit = new TextButton("Salir", skin);

        root.add(title).padBottom(40f).row();
        root.add(play).width(260).height(52).pad(10).row();
        root.add(settings).width(260).height(52).pad(10).row();
        root.add(help).width(260).height(52).pad(10).row();
        root.add(exit).width(260).height(52).pad(10).row();

        // Listeners
        play.addListener(new ClickListener(){
            @Override public void clicked(InputEvent event, float x, float y) {
                game.setScreen(new GameScreen(game, 1));
            }
        });
        settings.addListener(new ClickListener(){
            @Override public void clicked(InputEvent event, float x, float y) {
                game.setScreen(new SettingsScreen(game));
            }
        });
        help.addListener(new ClickListener(){
            @Override public void clicked(InputEvent event, float x, float y) {
                // De momento mostramos instrucciones simples en consola
                Gdx.app.log("Instrucciones", "Recoge paquetes y llega a la salida antes de quedarte sin energía.");
            }
        });
        exit.addListener(new ClickListener(){
            @Override public void clicked(InputEvent event, float x, float y) {
                Gdx.app.exit();
            }
        });

        Gdx.input.setInputProcessor(stage);
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0.1f,0.12f,0.15f,1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        stage.act(delta);
        stage.draw();
    }

    @Override public void resize(int width, int height) { stage.getViewport().update(width, height, true); }
    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}

    @Override
    public void dispose() {
        if (stage != null) stage.dispose();
        if (skin != null) skin.dispose();
    }
}

