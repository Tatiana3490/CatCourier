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
import com.svalero.mijuego.entities.AllyHealer;
import com.svalero.mijuego.entities.EnemyWalker;
import com.svalero.mijuego.entities.Player;
import com.svalero.mijuego.items.Collectible;
import com.svalero.mijuego.levels.Level;
import com.svalero.mijuego.ui.Hud;
import com.svalero.mijuego.util.Constants;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class GameScreen implements Screen {
    private final MiJuego game;
    private final int levelNumber;

    // Mundo
    private OrthographicCamera worldCam;
    private ExtendViewport worldViewport;
    private ShapeRenderer shapes;
    private Player player;
    private Level level;

    // Entidades
    private final List<EnemyWalker> enemies = new ArrayList<>();
    private final List<Collectible> items = new ArrayList<>();
    private final List<AllyHealer> allies = new ArrayList<>();

    // HUD
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

        // Spawns de ejemplo (ajústalos a tu gusto)
        enemies.add(new EnemyWalker(6f, 1f, 6f, 10f));
        allies.add(new AllyHealer(8.5f, 1f));
        items.add(new Collectible(10.5f, 4.7f));
    }

    @Override
    public void render(float delta) {
        // --- LÓGICA ---
        // 1) Actualizar jugador
        player.update(delta, level.solids);

        // 2) Actualizar enemigos
        for (EnemyWalker e : enemies) e.update(delta);

        // 3) Colisiones jugador ↔ enemigos
        for (EnemyWalker e : enemies) {
            if (player.getBounds().overlaps(e.getBounds())) {
                player.energy -= 10; // daño simple
                if (player.energy < 0) player.energy = 0;
            }
        }

        // 4) Colisiones jugador ↔ coleccionables
        for (Iterator<Collectible> it = items.iterator(); it.hasNext();) {
            Collectible c = it.next();
            if (!c.isCollected() && player.getBounds().overlaps(c.getBounds())) {
                c.collect();
                player.score += c.value;
            }
        }
        items.removeIf(Collectible::isCollected);

        // 5) Colisiones jugador ↔ aliado
        for (AllyHealer a : allies) {
            if (!a.isUsed() && player.getBounds().overlaps(a.getBounds())) {
                player.energy = Math.min(100, player.energy + a.healAmount);
                a.markUsed();
            }
        }

        // 6) Cámara siguiendo al jugador + límites del nivel
        worldCam.position.set(
            player.pos.x + Constants.PLAYER_W / 2f,
            Math.max(player.pos.y, 4.5f),
            0
        );
        float halfW = worldViewport.getWorldWidth() / 2f;
        if (worldCam.position.x < halfW) worldCam.position.x = halfW;
        if (worldCam.position.x > level.widthUnits - halfW) worldCam.position.x = level.widthUnits - halfW;
        worldCam.update();

        // 7) Cambio de nivel o Game Over
        if (player.getBounds().overlaps(level.exit)) {
            if (levelNumber == 1) game.setScreen(new GameScreen(game, 2));
            else game.setScreen(new GameOverScreen(game, player.score));
            return;
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            game.setScreen(new GameOverScreen(game, player.score));
            return;
        }

        // --- RENDER ---
        Gdx.gl.glClearColor(0.12f, 0.14f, 0.18f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        shapes.setProjectionMatrix(worldCam.combined);
        shapes.begin(ShapeRenderer.ShapeType.Filled);

        // Suelo / plataformas (blanco por defecto)
        for (Rectangle r : level.solids) {
            shapes.rect(r.x, r.y, r.width, r.height);
        }

        // Salida (verde)
        shapes.setColor(0.1f, 0.7f, 0.3f, 1f);
        shapes.rect(level.exit.x, level.exit.y, level.exit.width, level.exit.height);

        // Jugador (amarillo)
        shapes.setColor(0.9f, 0.9f, 0.1f, 1f);
        shapes.rect(player.pos.x, player.pos.y, Constants.PLAYER_W, Constants.PLAYER_H);

        // Enemigos (rojo)
        shapes.setColor(0.85f, 0.2f, 0.2f, 1f);
        for (EnemyWalker e : enemies) {
            shapes.rect(e.getBounds().x, e.getBounds().y, e.getBounds().width, e.getBounds().height);
        }

        // Aliados (azul; no dibujar si ya se usó)
        shapes.setColor(0.2f, 0.4f, 0.95f, 1f);
        for (AllyHealer a : allies) {
            if (!a.isUsed()) {
                shapes.rect(a.getBounds().x, a.getBounds().y, a.getBounds().width, a.getBounds().height);
            }
        }

        // Coleccionables (naranja)
        shapes.setColor(0.95f, 0.55f, 0.1f, 1f);
        for (Collectible c : items) {
            if (!c.isCollected()) {
                shapes.rect(c.getBounds().x, c.getBounds().y, c.getBounds().width, c.getBounds().height);
            }
        }

        shapes.end();

        // HUD
        hud.setScore(player.score);
        hud.setEnergy(player.energy);
        hud.stage.act(delta);
        hud.stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        worldViewport.update(width, height, true);
        hud.stage.getViewport().update(width, height, true);
    }

    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}

    @Override
    public void dispose() {
        if (shapes != null) shapes.dispose();
        if (hud != null) hud.stage.dispose();

    }
}
