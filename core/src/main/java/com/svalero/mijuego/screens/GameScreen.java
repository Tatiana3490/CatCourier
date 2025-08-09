package com.svalero.mijuego.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.svalero.mijuego.MiJuego;
import com.svalero.mijuego.entities.Player;
import com.svalero.mijuego.levels.Level;
import com.svalero.mijuego.ui.Hud;
import com.svalero.mijuego.util.Constants;

public class GameScreen implements Screen {
    private final MiJuego game;
    private final int levelNumber;
    private OrthographicCamera worldCam;
    private ExtendViewport worldViewport;
    private ShapeRenderer shapes;
    private Player player;
    private Level level;
    private Hud hud;

    public GameScreen(MiJuego game, int levelNumber){
        this.game = game;
        this.levelNumber = levelNumber;
    }

    @Override
    public void show() {
        worldCam = new OrthographicCamera();
        worldViewport = new ExtendViewport(16f, 9f, worldCam);
        shapes = new ShapeRenderer();
        level = Level.create(levelNumber);
        player = new Player(2f, 2f);
        hud = new Hud(game.batch);
        hud.setLevel(levelNumber);
        Gdx.input.setInputProcessor(hud.stage);
    }

    @Override
    public void render(float delta) {
        player.update(delta, level.solids);
        worldCam.position.set(player.pos.x + Constants.PLAYER_W/2f, Math.max(player.pos.y, 4.5f), 0);
        float halfW = worldViewport.getWorldWidth()/2f;
        if (worldCam.position.x < halfW) worldCam.position.x = halfW;
        if (worldCam.position.x > level.widthUnits - halfW) worldCam.position.x = level.widthUnits - halfW;
        worldCam.update();
        if (player.getBounds().overlaps(level.exit)){
            if (levelNumber == 1) game.setScreen(new GameScreen(game, 2));
            else game.setScreen(new GameOverScreen(game, player.score));
            return;
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)){
            game.setScreen(new GameOverScreen(game, player.score));
            return;
        }
        Gdx.gl.glClearColor(0.12f,0.14f,0.18f,1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        shapes.setProjectionMatrix(worldCam.combined);
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        for (Rectangle r : level.solids) shapes.rect(r.x, r.y, r.width, r.height);
        shapes.setColor(0.1f, 0.7f, 0.3f, 1f);
        shapes.rect(level.exit.x, level.exit.y, level.exit.width, level.exit.height);
        shapes.setColor(0.9f, 0.9f, 0.1f, 1f);
        shapes.rect(player.pos.x, player.pos.y, Constants.PLAYER_W, Constants.PLAYER_H);
        shapes.end();
        shapes.setColor(1,1,1,1);
        hud.setScore(player.score);
        hud.setEnergy(player.energy);
        hud.stage.act(delta);
        hud.stage.draw();
    }

    @Override public void resize(int width, int height){
        worldViewport.update(width, height, true);
        hud.stage.getViewport().update(width, height, true);
    }
    @Override public void pause(){}
    @Override public void resume(){}
    @Override public void hide(){}
    @Override public void dispose() {
        if (shapes != null) shapes.dispose();
        if (hud != null) hud.stage.dispose();
    }
}
