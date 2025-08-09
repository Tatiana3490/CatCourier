package com.svalero.mijuego.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.svalero.mijuego.MiJuego;
import com.svalero.mijuego.util.Constants;

/**
 * Versión mínima: sólo muestra "Nivel X" y permite ir a GameOver con ESC
 * y pasar a siguiente nivel con ENTER. Más adelante aquí irá el mundo, player, HUD, etc.
 */
public class GameScreen implements Screen {
    private final MiJuego game;
    private final int level; // 1 o 2 para la primera entrega

    private OrthographicCamera camera;
    private Stage stage; // reservado para HUD/overlays más adelante
    private BitmapFont font;
    private GlyphLayout layout;

    public GameScreen(MiJuego game, int level) {
        this.game = game;
        this.level = level;
    }

    @Override
    public void show() {
        camera = new OrthographicCamera();
        stage = new Stage(new FitViewport(Constants.VIRTUAL_WIDTH, Constants.VIRTUAL_HEIGHT, camera), game.batch);
        font = new BitmapFont();
        layout = new GlyphLayout();
        Gdx.input.setInputProcessor(stage); // por ahora, aunque no hay UI
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0.05f, 0.09f, 0.13f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // Entrada mínima para navegar entre estados mientras no hay lógica
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            game.setScreen(new GameOverScreen(game, /*score=*/0));
            return;
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
            if (level == 1) game.setScreen(new GameScreen(game, 2));
            else game.setScreen(new GameOverScreen(game, 0));
            return;
        }

        // Texto centrado: "Nivel X"
        String text = "Nivel " + level + " (ENTER → siguiente / ESC → Game Over)";
        layout.setText(font, text);
        float x = (Constants.VIRTUAL_WIDTH - layout.width) / 2f;
        float y = (Constants.VIRTUAL_HEIGHT + layout.height) / 2f;

        game.batch.begin();
        font.draw(game.batch, text, x, y);
        game.batch.end();

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
        if (font != null) font.dispose();
    }
}
