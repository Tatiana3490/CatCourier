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

public class SettingsScreen implements Screen {
    private final MiJuego game;
    private Stage stage;
    private Skin skin;
    private OrthographicCamera camera;

    // Opciones configurables mínimas para la 1ª entrega (placeholder)
    private boolean musicEnabled = true;
    private boolean sfxEnabled = true;

    public SettingsScreen(MiJuego game) {
        this.game = game;
    }

    @Override
    public void show() {
        camera = new OrthographicCamera();
        stage = new Stage(new FitViewport(Constants.VIRTUAL_WIDTH, Constants.VIRTUAL_HEIGHT, camera), game.batch);
        skin = UiStyles.makeSkin();

        Table root = new Table();
        root.setFillParent(true);
        stage.addActor(root);

        Label title = new Label("Configuración", skin);
        TextButton musicBtn = new TextButton("Música: ON", skin);
        TextButton sfxBtn = new TextButton("Efectos: ON", skin);
        TextButton back = new TextButton("Volver", skin);

        root.add(title).padBottom(40).row();
        root.add(musicBtn).width(260).height(52).pad(8).row();
        root.add(sfxBtn).width(260).height(52).pad(8).row();
        root.add(back).width(260).height(52).pad(20).row();

        musicBtn.addListener(new ClickListener(){
            @Override public void clicked(InputEvent event, float x, float y) {
                musicEnabled = !musicEnabled;
                musicBtn.setText("Música: " + (musicEnabled?"ON":"OFF"));
            }
        });
        sfxBtn.addListener(new ClickListener(){
            @Override public void clicked(InputEvent event, float x, float y) {
                sfxEnabled = !sfxEnabled;
                sfxBtn.setText("Efectos: " + (sfxEnabled?"ON":"OFF"));
            }
        });
        back.addListener(new ClickListener(){
            @Override public void clicked(InputEvent event, float x, float y) {
                game.setScreen(new MenuScreen(game));
            }
        });

        Gdx.input.setInputProcessor(stage);
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0.12f,0.09f,0.09f,1f);
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

