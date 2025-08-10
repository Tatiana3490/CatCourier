package com.svalero.mijuego.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.svalero.mijuego.MiJuego;
import com.svalero.mijuego.entities.*;
import com.svalero.mijuego.items.Collectible;
import com.svalero.mijuego.levels.Level;
import com.svalero.mijuego.ui.Hud;
import com.svalero.mijuego.ui.UiStyles;
import com.svalero.mijuego.util.Constants;

import java.util.*;

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
    private final List<EnemyWalker> enemiesWalk = new ArrayList<>();
    private final List<EnemyFlyer> enemiesFly  = new ArrayList<>();
    private final List<Collectible> items      = new ArrayList<>();
    private final List<AllyHealer> allies     = new ArrayList<>();

    // HUD
    private Hud hud;

    // Objetivo de nivel
    private int requiredPackages;
    private int collectedPackages;

    // PAUSA
    private boolean paused = false;
    private Stage pauseStage;
    private Skin pauseSkin;

    // Audio (SFX)
    private Sound sJump, sCollect, sHit;
    private float hitCooldown = 0f; // evita spam de sonido de golpe

    // Animación del player
    private Texture playerSheet;
    private TextureRegion[][] playerFrames;
    private TextureRegion currentFrame;
    private com.badlogic.gdx.graphics.g2d.Animation<TextureRegion> animIdle, animWalk, animJump;
    private float stateTime = 0f;
    private boolean facingRight = true;
    private boolean lastOnGround = true; // para detectar inicio del salto

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

        // Spawns por nivel
        if (levelNumber == 1){
            enemiesWalk.add(new EnemyWalker(6f, 1f, 6f, 10f));
            enemiesFly.add(new EnemyFlyer(12f, 3.0f));
            items.add(new Collectible(10.5f, 4.7f));
            items.add(new Collectible(16.0f, 5.9f));
            allies.add(new AllyHealer(8.5f, 1f));
            requiredPackages = 2;
        } else {
            enemiesWalk.add(new EnemyWalker(7f, 1f, 7f, 12f));
            enemiesWalk.add(new EnemyWalker(19f, 1f, 19f, 23f));
            enemiesFly.add(new EnemyFlyer(15f, 4.2f));
            enemiesFly.add(new EnemyFlyer(24f, 5.0f));
            items.add(new Collectible(11.2f, 4.5f));
            items.add(new Collectible(17.0f, 6.3f));
            items.add(new Collectible(22.2f, 3.9f));
            allies.add(new AllyHealer(21.5f, 1f));
            requiredPackages = 3;
        }
        collectedPackages = 0;

        // ----- ANIMACIONES -----
        if (Gdx.files.internal("player.png").exists()) {
            playerSheet = new Texture(Gdx.files.internal("player.png"));
            int rows = 3, cols = 6; // ajusta a tu spritesheet real
            playerFrames = TextureRegion.split(
                playerSheet,
                playerSheet.getWidth() / cols,
                playerSheet.getHeight() / rows
            );
            float frameDur = 0.10f;
            animIdle = new com.badlogic.gdx.graphics.g2d.Animation<>(0.15f, playerFrames[0]);
            animIdle.setPlayMode(com.badlogic.gdx.graphics.g2d.Animation.PlayMode.LOOP);
            animWalk = new com.badlogic.gdx.graphics.g2d.Animation<>(frameDur, playerFrames[1]);
            animWalk.setPlayMode(com.badlogic.gdx.graphics.g2d.Animation.PlayMode.LOOP);
            animJump = new com.badlogic.gdx.graphics.g2d.Animation<>(0.12f, playerFrames[2]);
            animJump.setPlayMode(com.badlogic.gdx.graphics.g2d.Animation.PlayMode.NORMAL);
        }

        // ----- CARGA SFX (con logs) -----
        Gdx.app.log("SFX", "jump exists? " + Gdx.files.internal("sfx/jump.wav").exists());
        Gdx.app.log("SFX", "collect exists? " + Gdx.files.internal("sfx/collect.wav").exists());
        Gdx.app.log("SFX", "hit exists? " + Gdx.files.internal("sfx/hit.wav").exists());
        try {
            if (Gdx.files.internal("sfx/jump.wav").exists())
                sJump = Gdx.audio.newSound(Gdx.files.internal("sfx/jump.wav"));
            if (Gdx.files.internal("sfx/collect.wav").exists())
                sCollect = Gdx.audio.newSound(Gdx.files.internal("sfx/collect.wav"));
            if (Gdx.files.internal("sfx/hit.wav").exists())
                sHit = Gdx.audio.newSound(Gdx.files.internal("sfx/hit.wav"));
        } catch (Exception e) {
            Gdx.app.error("SFX", "Error cargando SFX: " + e.getMessage(), e);
        }

        hud = new Hud(game.batch);
        hud.setLevel(levelNumber);
        hud.setGoal(collectedPackages, requiredPackages);

        // Input por defecto al HUD (cuando no está pausado)
        Gdx.input.setInputProcessor(hud.stage);

        // Preparar UI de pausa
        setupPauseUI();
    }

    private void setupPauseUI(){
        pauseSkin = UiStyles.makeSkin();
        pauseStage = new Stage(new FitViewport(Constants.VIRTUAL_WIDTH, Constants.VIRTUAL_HEIGHT), game.batch);

        Table panel = new Table(pauseSkin);
        panel.setFillParent(true);
        panel.center();

        Label title = new Label("PAUSA", pauseSkin);
        TextButton btnResume = new TextButton("Continuar", pauseSkin);
        TextButton btnMenu   = new TextButton("Menú principal", pauseSkin);

        panel.add(title).padBottom(30).row();
        panel.add(btnResume).width(260).height(52).pad(6).row();
        panel.add(btnMenu).width(260).height(52).pad(6).row();

        pauseStage.addActor(panel);

        btnResume.addListener(new ClickListener(){
            @Override public void clicked(InputEvent event, float x, float y) {
                paused = false;
                Gdx.input.setInputProcessor(hud.stage);
                if (game.bgm != null) game.bgm.play();
            }
        });
        btnMenu.addListener(new ClickListener(){
            @Override public void clicked(InputEvent event, float x, float y) {
                paused = false;
                game.setScreen(new MenuScreen(game));
            }
        });
    }

    private void togglePause(){
        paused = !paused;
        if (paused) {
            Gdx.input.setInputProcessor(pauseStage);
            if (game.bgm != null) game.bgm.pause();
        } else {
            Gdx.input.setInputProcessor(hud.stage);
            if (game.bgm != null) game.bgm.play();
        }
    }

    @Override
    public void render(float delta) {
        // Teclas de pausa (P o ESC)
        if (Gdx.input.isKeyJustPressed(Input.Keys.P) || Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            togglePause();
        }

        // TEST TECLAS para SFX (quitar cuando confirmes que suenan)
        if (Gdx.input.isKeyJustPressed(Input.Keys.J) && sJump != null)    sJump.play(0.8f);
        if (Gdx.input.isKeyJustPressed(Input.Keys.C) && sCollect != null) sCollect.play(0.8f);
        if (Gdx.input.isKeyJustPressed(Input.Keys.H) && sHit != null)     sHit.play(0.8f);

        if (!paused) {
            // --- LÓGICA SOLO SI NO ESTÁ PAUSADO ---
            player.update(delta, level.solids);

            // Cooldown daño
            if (hitCooldown > 0f) hitCooldown -= delta;

            // Detectar inicio de salto
            if (lastOnGround && !player.onGround && player.vel.y > 0f && sJump != null) {
                sJump.play(0.6f);
            }
            lastOnGround = player.onGround;

            // Enemigos
            for (EnemyWalker e : enemiesWalk) e.update(delta);
            for (EnemyFlyer f : enemiesFly) f.update(delta);

            // Daño con sonido y cooldown
            for (EnemyWalker e : enemiesWalk) {
                if (player.getBounds().overlaps(e.getBounds())) {
                    int prev = player.energy;
                    player.energy = Math.max(0, player.energy - 10);
                    if (player.energy < prev && hitCooldown <= 0f && sHit != null) {
                        sHit.play(0.6f);
                        hitCooldown = 0.4f;
                    }
                }
            }
            for (EnemyFlyer f : enemiesFly)  {
                if (player.getBounds().overlaps(f.getBounds())) {
                    int prev = player.energy;
                    player.energy = Math.max(0, player.energy - 10);
                    if (player.energy < prev && hitCooldown <= 0f && sHit != null) {
                        sHit.play(0.6f);
                        hitCooldown = 0.4f;
                    }
                }
            }

            // Coleccionables con sonido
            for (Iterator<Collectible> it = items.iterator(); it.hasNext();) {
                Collectible c = it.next();
                if (!c.isCollected() && player.getBounds().overlaps(c.getBounds())){
                    c.collect();
                    player.score += c.value;
                    collectedPackages++;
                    if (sCollect != null) sCollect.play(0.6f);
                }
            }
            items.removeIf(Collectible::isCollected);

            // Aliado
            for (AllyHealer a : allies){
                if (!a.isUsed() && player.getBounds().overlaps(a.getBounds())){
                    player.energy = Math.min(100, player.energy + a.healAmount);
                    a.markUsed();
                }
            }

            // Cámara
            worldCam.position.set(player.pos.x + Constants.PLAYER_W/2f, Math.max(player.pos.y, 4.5f), 0);
            float halfW = worldViewport.getWorldWidth()/2f;
            if (worldCam.position.x < halfW) worldCam.position.x = halfW;
            if (worldCam.position.x > level.widthUnits - halfW) worldCam.position.x = level.widthUnits - halfW;
            worldCam.update();

            // Salida condicionada
            boolean canExit = collectedPackages >= requiredPackages;
            if (canExit && player.getBounds().overlaps(level.exit)){
                if (levelNumber == 1) game.setScreen(new GameScreen(game, 2));
                else game.setScreen(new GameOverScreen(game, player.score));
                return;
            }

            // Animación del jugador: elegir frame
            stateTime += delta;
            if (playerSheet != null) {
                boolean moving = Math.abs(player.vel.x) > 0.05f;
                boolean airborne = !player.onGround;
                if (airborne)      currentFrame = animJump.getKeyFrame(stateTime);
                else if (moving)   currentFrame = animWalk.getKeyFrame(stateTime);
                else               currentFrame = animIdle.getKeyFrame(stateTime);
                if (moving) facingRight = player.vel.x >= 0f;
                if (currentFrame != null && currentFrame.isFlipX() == !facingRight) {
                    currentFrame.flip(true, false);
                }
            }
        }

        // --- RENDER (siempre dibuja) ---
        Gdx.gl.glClearColor(0.12f,0.14f,0.18f,1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // Mundo geométrico
        shapes.setProjectionMatrix(worldCam.combined);
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        for (Rectangle r : level.solids) shapes.rect(r.x, r.y, r.width, r.height);
        boolean canExitNow = collectedPackages >= requiredPackages;
        if (canExitNow) shapes.setColor(0.1f, 0.7f, 0.3f, 1f); else shapes.setColor(0.05f, 0.35f, 0.15f, 1f);
        shapes.rect(level.exit.x, level.exit.y, level.exit.width, level.exit.height);

        // Si no hay sprite, dibuja rectángulo del player
        if (playerSheet == null) {
            shapes.setColor(0.9f, 0.9f, 0.1f, 1f);
            shapes.rect(player.pos.x, player.pos.y, Constants.PLAYER_W, Constants.PLAYER_H);
        }

        // Enemigos / Aliados / Items
        shapes.setColor(0.85f, 0.2f, 0.2f, 1f);
        for (EnemyWalker e : enemiesWalk) shapes.rect(e.getBounds().x, e.getBounds().y, e.getBounds().width, e.getBounds().height);
        shapes.setColor(0.85f, 0.2f, 0.75f, 1f);
        for (EnemyFlyer f : enemiesFly)  shapes.rect(f.getBounds().x, f.getBounds().y, f.getBounds().width, f.getBounds().height);
        shapes.setColor(0.2f, 0.4f, 0.95f, 1f);
        for (AllyHealer a : allies) if (!a.isUsed()) shapes.rect(a.getBounds().x, a.getBounds().y, a.getBounds().width, a.getBounds().height);
        shapes.setColor(0.95f, 0.55f, 0.1f, 1f);
        for (Collectible c : items) if (!c.isCollected()) shapes.rect(c.getBounds().x, c.getBounds().y, c.getBounds().width, c.getBounds().height);
        shapes.end();

        // Dibujo del jugador con sprite (si existe)
        if (playerSheet != null && currentFrame != null) {
            game.batch.setProjectionMatrix(worldCam.combined);
            game.batch.begin();
            game.batch.draw(currentFrame, player.pos.x, player.pos.y, Constants.PLAYER_W, Constants.PLAYER_H);
            game.batch.end();
        }

        // HUD
        hud.setScore(player.score);
        hud.setEnergy(player.energy);
        hud.setGoal(collectedPackages, requiredPackages);
        hud.stage.act(paused ? 0f : Gdx.graphics.getDeltaTime());
        hud.stage.draw();

        // Overlay de pausa
        if (paused){
            Gdx.gl.glEnable(GL20.GL_BLEND);
            shapes.begin(ShapeRenderer.ShapeType.Filled);
            shapes.setProjectionMatrix(hud.stage.getViewport().getCamera().combined);
            shapes.setColor(0,0,0,0.45f);
            shapes.rect(0,0, Constants.VIRTUAL_WIDTH, Constants.VIRTUAL_HEIGHT);
            shapes.end();
            Gdx.gl.glDisable(GL20.GL_BLEND);

            pauseStage.act(Gdx.graphics.getDeltaTime());
            pauseStage.draw();
        }
    }

    @Override public void resize(int width, int height){
        worldViewport.update(width, height, true);
        hud.stage.getViewport().update(width, height, true);
        if (pauseStage != null) pauseStage.getViewport().update(width, height, true);
    }
    @Override public void pause(){}
    @Override public void resume(){}
    @Override public void hide(){}

    @Override
    public void dispose(){
        if (shapes != null) shapes.dispose();
        if (hud != null) hud.stage.dispose();
        if (pauseStage != null) pauseStage.dispose();
        if (pauseSkin != null) pauseSkin.dispose();
        if (sJump != null) sJump.dispose();
        if (sCollect != null) sCollect.dispose();
        if (sHit != null) sHit.dispose();
        if (playerSheet != null) playerSheet.dispose();
    }
}
