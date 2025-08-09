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

public class GameOverScreen implements Screen {
    private final MiJuego game;
    private final int score;

    private Stage stage;
    private Skin skin;
    private OrthographicCamera camera;

    public GameOverScreen(MiJuego game, int score) {
        this.game = game;
        this.score = score;
    }

    @Override
    public void show() {
        camera = new OrthographicCamera();
        stage = new Stage(new FitViewport(Constants.VIRTUAL_WIDTH, Constants.VIRTUAL_HEIGHT, camera), game.batch);
        skin = UiStyles.makeSkin();

        Table root = new Table();
        root.setFillParent(true);
        stage.addActor(root);

        Label title = new Label("GAME OVER", skin);
        Label scoreLabel = new Label("Puntuación: " + score, skin);
        TextButton retry = new TextButton("Volver a jugar", skin);
        TextButton menu = new TextButton("Menú principal", skin);

        root.add(title).padBottom(20).row();
        root.add(scoreLabel).padBottom(30).row();
        root.add(retry).width(260).height(52).pad(8).row();
        root.add(menu).width(260).height(52).pad(8).row();

        retry.addListener(new ClickListener(){
            @Override public void clicked(InputEvent event, float x, float y) {
                game.setScreen(new GameScreen(game, 1));
            }
        });
        menu.addListener(new ClickListener(){
            @Override public void clicked(InputEvent event, float x, float y) {
                game.setScreen(new MenuScreen(game));
            }
        });

        Gdx.input.setInputProcessor(stage);
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0.08f,0.08f,0.08f,1f);
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
