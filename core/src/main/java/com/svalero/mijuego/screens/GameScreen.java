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
import com.svalero.mijuego.audio.AudioManager;
import com.svalero.mijuego.entities.*;
import com.svalero.mijuego.items.Collectible;
import com.svalero.mijuego.levels.Level;
import com.svalero.mijuego.ui.Hud;
import com.svalero.mijuego.ui.UiStyles;
import com.svalero.mijuego.util.Constants;

import java.util.*;

/**
 * Pantalla principal del juego.
 * - Actualiza y dibuja jugador, enemigos, aliados y coleccionables
 * - Muestra HUD y gestiona la pausa
 * - Dibuja sprites PNG (si existen) o “rectángulos fallback” si no los tienes.
 */
public class GameScreen implements Screen {

    private final MiJuego game;
    private final int levelNumber;

    // --- Mundo y cámara (vista en unidades de juego, no en píxeles) ---
    private OrthographicCamera worldCam;
    private ExtendViewport worldViewport;
    private ShapeRenderer shapes;

    // --- Entidades que viven en el mundo ---
    private Player player;
    private Level level;
    private final List<EnemyWalker> enemiesWalk = new ArrayList<>();
    private final List<EnemyFlyer>  enemiesFly  = new ArrayList<>();
    private final List<Collectible> items       = new ArrayList<>();
    private final List<AllyHealer>  allies      = new ArrayList<>();

    // --- HUD (marcadores y textos superpuestos) ---
    private Hud hud;

    // --- Objetivo del nivel (para abrir la salida) ---
    private int requiredPackages;
    private int collectedPackages;

    // --- Pausa / UI de pausa ---
    private boolean paused = false;
    private Stage pauseStage;
    private Skin  pauseSkin;

    // --- Sonidos puntuales (efectos) ---
    private Sound sJump, sCollect, sHit;
    private float hitCooldown = 0f; // evita reproducir "hit" muchas veces por segundo

    // --- Animación del jugador con spritesheet ---
    private Texture playerSheet;                       // imagen grande con varias celdas
    private TextureRegion[][] playerFrames;           // celdas separadas
    private TextureRegion currentFrame;               // frame a dibujar ahora
    private com.badlogic.gdx.graphics.g2d.Animation<TextureRegion> animIdle, animWalk, animJump;
    private float stateTime = 0f;                     // tiempo acumulado para animaciones
    private boolean facingRight = true;               // hacia dónde mira
    private boolean lastOnGround = true;              // para detectar inicio del salto

    // --- Texturas (PNG) para NPCs e ítems (no animados) ---
    private Texture texEnemyWalker;
    private Texture texEnemyFlyer;
    private Texture texAllyHealer;
    private Texture texCollectible;

    // --- Texturas de puerta (cerrada/abierta) ---
    private Texture texDoorClosed;
    private Texture texDoorOpen;

    // --- Escala visual de la puerta y ajuste contra el suelo ---
    //     * X e Y se escalan por separado (ancho/alto)
    //     * DOOR_FLOOR_EPS empuja ligeramente hacia abajo para “apoyarla” en el suelo
    private static final float DOOR_DRAW_SCALE_X = 1.8f;
    private static final float DOOR_DRAW_SCALE_Y = 1.8f;
    private static final float DOOR_FLOOR_EPS    = 1.0f;

    public GameScreen(MiJuego game, int levelNumber){
        this.game = game;
        this.levelNumber = levelNumber;
    }

    @Override
    public void show() {
        // 1) Cámara y viewport: 16x9 unidades de mundo visibles
        worldCam = new OrthographicCamera();
        worldViewport = new ExtendViewport(16f, 9f, worldCam);
        shapes = new ShapeRenderer();

        // 2) Cargar el nivel y crear al jugador
        level = Level.create(levelNumber);
        player = new Player(2f, 2f);

        // 3) Spawns rápidos por nivel (puedes extraer a Level si quieres más limpio)
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

        // 4) Animaciones del jugador (si falta player.png, dibujaremos un rectángulo)
        if (Gdx.files.internal("player.png").exists()) {
            playerSheet = new Texture("player.png");
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

        // 5) Sonidos (cargamos solo si existen, para no romper nada)
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

        // 6) Sprites PNG (si faltan, simplemente no se dibujan y usamos fallback geométrico)
        if (Gdx.files.internal("enemy_walker.png").exists()) texEnemyWalker = new Texture("enemy_walker.png");
        if (Gdx.files.internal("enemy_flyer.png").exists())  texEnemyFlyer  = new Texture("enemy_flyer.png");
        if (Gdx.files.internal("ally_healer.png").exists())  texAllyHealer  = new Texture("ally_healer.png");
        if (Gdx.files.internal("collectible.png").exists())  texCollectible = new Texture("collectible.png");

        // Puerta (cerrada/abierta)
        if (Gdx.files.internal("door_closed.png").exists()) texDoorClosed = new Texture("door_closed.png");
        if (Gdx.files.internal("door_open.png").exists())   texDoorOpen   = new Texture("door_open.png");

        // 7) HUD arriba del todo; por defecto el input va al HUD
        hud = new Hud(game.batch);
        hud.setLevel(levelNumber);
        hud.setGoal(collectedPackages, requiredPackages);
        Gdx.input.setInputProcessor(hud.stage);

        // 8) Ventana de pausa (otro stage independiente)
        setupPauseUI();
    }

    /** Construye la ventana de pausa con dos botones. */
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

    /** Cambia pausa y envía el input al stage correcto. */
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

        // Tecla de pausa (P o ESC)
        if (Gdx.input.isKeyJustPressed(Input.Keys.P) || Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            togglePause();
        }

        // Teclas de test para oír SFX (puedes quitarlas cuando no las necesites)
        if (Gdx.input.isKeyJustPressed(Input.Keys.J) && sJump != null)    AudioManager.play(sJump);
        if (Gdx.input.isKeyJustPressed(Input.Keys.C) && sCollect != null) AudioManager.play(sCollect);
        if (Gdx.input.isKeyJustPressed(Input.Keys.H) && sHit != null)     AudioManager.play(sHit);

        // ===== UPDATE (lógica; solo si no está pausado) =====
        if (!paused) {
            player.update(delta, level.solids);

            // Enfriamiento del “hit”
            if (hitCooldown > 0f) hitCooldown -= delta;

            // Sonido al iniciar salto (pasar de en suelo -> en el aire)
            if (lastOnGround && !player.onGround && player.vel.y > 0f && sJump != null) {
                AudioManager.play(sJump);
            }
            lastOnGround = player.onGround;

            // Mover enemigos
            for (EnemyWalker e : enemiesWalk) e.update(delta);
            for (EnemyFlyer  f : enemiesFly)  f.update(delta);

            // Colisiones con enemigos (quitan energía + sonido con cooldown)
            for (EnemyWalker e : enemiesWalk) {
                if (player.getBounds().overlaps(e.getBounds())) {
                    int prev = player.energy;
                    player.energy = Math.max(0, player.energy - 10);
                    if (player.energy < prev && hitCooldown <= 0f && sHit != null) {
                        AudioManager.play(sHit);
                        hitCooldown = 0.4f;
                    }
                }
            }
            for (EnemyFlyer f : enemiesFly)  {
                if (player.getBounds().overlaps(f.getBounds())) {
                    int prev = player.energy;
                    player.energy = Math.max(0, player.energy - 10);
                    if (player.energy < prev && hitCooldown <= 0f && sHit != null) {
                        AudioManager.play(sHit);
                        hitCooldown = 0.4f;
                    }
                }
            }

            // Recoger ítems (suben score y cuentan para abrir la puerta)
            for (Iterator<Collectible> it = items.iterator(); it.hasNext();) {
                Collectible c = it.next();
                if (!c.isCollected() && player.getBounds().overlaps(c.getBounds())){
                    c.collect();
                    player.score += c.value;
                    collectedPackages++;
                    if (sCollect != null) AudioManager.play(sCollect);
                }
            }
            items.removeIf(Collectible::isCollected);

            // Aliado que cura una sola vez al tocarlo
            for (AllyHealer a : allies){
                if (!a.isUsed() && player.getBounds().overlaps(a.getBounds())){
                    player.energy = Math.min(100, player.energy + a.healAmount);
                    a.markUsed();
                    if (sCollect != null) AudioManager.play(sCollect);
                }
            }

            // Cámara sigue al jugador (con límites del nivel)
            worldCam.position.set(player.pos.x + Constants.PLAYER_W/2f, Math.max(player.pos.y, 4.5f), 0);
            float halfW = worldViewport.getWorldWidth()/2f;
            if (worldCam.position.x < halfW) worldCam.position.x = halfW;
            if (worldCam.position.x > level.widthUnits - halfW) worldCam.position.x = level.widthUnits - halfW;
            worldCam.update();

            // Cambio de nivel / fin del juego al tocar la salida si ya está abierta
            boolean canExit = collectedPackages >= requiredPackages;
            if (canExit && player.getBounds().overlaps(level.exit)){
                if (levelNumber == 1) game.setScreen(new GameScreen(game, 2));
                else game.setScreen(new GameOverScreen(game, player.score));
                return; // muy importante cortar aquí
            }

            // Elegir frame de animación del jugador
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

        // ===== DRAW / RENDER (pintar) =====
        Gdx.gl.glClearColor(0.12f,0.14f,0.18f,1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // Calcular una vez si ya podemos salir (lo usaremos varias veces)
        boolean canExitNow = collectedPackages >= requiredPackages;
        Rectangle exit = level.exit;

        // 1) Geometría básica: suelos + (fallback de puerta si faltan PNGs)
        shapes.setProjectionMatrix(worldCam.combined);
        shapes.begin(ShapeRenderer.ShapeType.Filled);

        // Suelos/plataformas
        shapes.setColor(0.30f, 0.35f, 0.42f, 1f);
        for (Rectangle r : level.solids) shapes.rect(r.x, r.y, r.width, r.height);

        // Fallback de puerta (solo si NO hay imágenes adecuadas cargadas)
        if ((texDoorOpen == null && texDoorClosed == null) ||
            (canExitNow && texDoorOpen == null) ||
            (!canExitNow && texDoorClosed == null)) {

            shapes.setColor(canExitNow ? 0.1f : 0.05f, canExitNow ? 0.7f : 0.35f, canExitNow ? 0.3f : 0.15f, 1f);
            shapes.rect(exit.x, exit.y, exit.width, exit.height);
        }

        // Placeholder del jugador si no hay spritesheet
        if (playerSheet == null) {
            shapes.setColor(0.9f, 0.9f, 0.1f, 1f);
            shapes.rect(player.pos.x, player.pos.y, Constants.PLAYER_W, Constants.PLAYER_H);
        }
        shapes.end();

        // 2) Sprites (PNG): puerta, NPCs, ítems y jugador
        game.batch.setProjectionMatrix(worldCam.combined);
        game.batch.begin();

        // Puerta alineada al suelo (si hay PNGs)
        if (texDoorOpen != null || texDoorClosed != null) {
            // tamaño de dibujo escalado
            float drawW = exit.width  * DOOR_DRAW_SCALE_X;
            float drawH = exit.height * DOOR_DRAW_SCALE_Y;

            // X: centrada respecto al rectángulo de colisión
            float drawX = exit.x + (exit.width - drawW) / 2f;

            // Y: base del sprite apoyada en el suelo (exit.y)
            float drawY = exit.y - DOOR_FLOOR_EPS;

            if (canExitNow && texDoorOpen != null) {
                game.batch.draw(texDoorOpen, drawX, drawY, drawW, drawH);
            } else if (!canExitNow && texDoorClosed != null) {
                game.batch.draw(texDoorClosed, drawX, drawY, drawW, drawH);
            }
        }

        // Enemigo caminante
        if (texEnemyWalker != null) {
            for (EnemyWalker e : enemiesWalk) {
                Rectangle b = e.getBounds();
                game.batch.draw(texEnemyWalker, b.x, b.y, b.width, b.height);
            }
        }

        // Enemigo volador
        if (texEnemyFlyer != null) {
            for (EnemyFlyer f : enemiesFly) {
                Rectangle b = f.getBounds();
                game.batch.draw(texEnemyFlyer, b.x, b.y, b.width, b.height);
            }
        }

        // Aliado (solo si sigue disponible)
        if (texAllyHealer != null) {
            for (AllyHealer a : allies) {
                if (!a.isUsed()) {
                    Rectangle b = a.getBounds();
                    game.batch.draw(texAllyHealer, b.x, b.y, b.width, b.height);
                }
            }
        }

        // Ítems no recogidos
        if (texCollectible != null) {
            for (Collectible c : items) {
                if (!c.isCollected()) {
                    Rectangle b = c.getBounds();
                    game.batch.draw(texCollectible, b.x, b.y, b.width, b.height);
                }
            }
        }

        // Jugador animado (si hay spritesheet)
        if (playerSheet != null && currentFrame != null) {
            game.batch.draw(currentFrame, player.pos.x, player.pos.y, Constants.PLAYER_W, Constants.PLAYER_H);
        }

        game.batch.end();

        // 3) HUD siempre encima
        hud.setScore(player.score);
        hud.setEnergy(player.energy);
        hud.setGoal(collectedPackages, requiredPackages);
        hud.stage.act(paused ? 0f : Gdx.graphics.getDeltaTime());
        hud.stage.draw();

        // 4) Si está pausado: oscurecer fondo + dibujar ventana de pausa
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
        // Libera todo lo que tenga recursos nativos (GPU/Audio/etc.)
        if (shapes != null) shapes.dispose();
        if (hud != null) hud.stage.dispose();
        if (pauseStage != null) pauseStage.dispose();
        if (pauseSkin != null) pauseSkin.dispose();

        if (sJump != null) sJump.dispose();
        if (sCollect != null) sCollect.dispose();
        if (sHit != null) sHit.dispose();

        if (playerSheet != null) playerSheet.dispose();
        if (texEnemyWalker != null) texEnemyWalker.dispose();
        if (texEnemyFlyer  != null) texEnemyFlyer.dispose();
        if (texAllyHealer  != null) texAllyHealer.dispose();
        if (texCollectible != null) texCollectible.dispose();
        if (texDoorClosed != null) texDoorClosed.dispose();
        if (texDoorOpen   != null) texDoorOpen.dispose();
    }
}
