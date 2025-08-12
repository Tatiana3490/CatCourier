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
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
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
import java.util.List;

/*Pantalla principal del juego.*/
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
    private static final float DOOR_DRAW_SCALE_X = 1.8f;
    private static final float DOOR_DRAW_SCALE_Y = 1.8f;
    private static final float DOOR_FLOOR_EPS    = 1.0f;

    // --- Mute global desde el menú de pausa ---
    private boolean muted = false;
    private float prevMusicVol = 1f;
    private float prevSfxVol   = 1f;
    private boolean muteMusic  = false;
    private boolean muteSfx    = false;

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

        // 3) Spawns por nivel (enemigos, items, aliados y paquetes requeridos)
        switch (levelNumber) {
            case 1:
                enemiesWalk.add(new EnemyWalker(6f, 1f, 6f, 10f));
                enemiesFly.add(new EnemyFlyer(12f, 3.0f));
                items.add(new Collectible(10.5f, 4.7f));
                items.add(new Collectible(16.0f, 5.9f));
                allies.add(new AllyHealer(8.5f, 1f));
                requiredPackages = 2;
                break;

            case 2:
                enemiesWalk.add(new EnemyWalker(7f, 1f, 7f, 12f));
                enemiesWalk.add(new EnemyWalker(19f, 1f, 19f, 23f));
                enemiesFly.add(new EnemyFlyer(15f, 4.2f));
                enemiesFly.add(new EnemyFlyer(24f, 5.0f));
                items.add(new Collectible(11.2f, 4.5f));
                items.add(new Collectible(17.0f, 6.3f));
                items.add(new Collectible(22.2f, 3.9f));
                allies.add(new AllyHealer(21.5f, 1f));
                requiredPackages = 3;
                break;

            case 3:
                enemiesWalk.add(new EnemyWalker(5.5f, 1f, 5.5f, 9f));
                enemiesWalk.add(new EnemyWalker(18f, 1f, 18f, 23f));
                enemiesFly.add(new EnemyFlyer(13f, 3.2f));
                enemiesFly.add(new EnemyFlyer(25f, 4.0f));
                items.add(new Collectible(6.5f, 2.7f));
                items.add(new Collectible(12.2f, 3.2f));
                items.add(new Collectible(19.0f, 4.1f));
                allies.add(new AllyHealer(22.5f, 1f));
                requiredPackages = 3;
                break;

            case 4:
                // Enemigos
                enemiesWalk.add(new EnemyWalker(3.5f, 1f, 3.5f, 9f));
                enemiesWalk.add(new EnemyWalker(21f, 1f, 21f, 27f));
                enemiesWalk.add(new EnemyWalker(33f, 1f, 33f, 38f));
                enemiesFly.add(new EnemyFlyer(16f, 4.0f));
                enemiesFly.add(new EnemyFlyer(28f, 3.6f));

                // === Cajas ENCIMA de las plataformas del Level 4 ===
                // Plataformas Level 4 (de tu Level.java):
                // (5, 2.2, 3, 0.6)   -> top = 2.8   -> centro X = 6.5
                // (15, 2.8, 3, 0.6)  -> top = 3.4   -> centro X = 16.5
                // (23, 3.2, 3, 0.6)  -> top = 3.8   -> centro X = 24.5   (AJUSTADA)
                // (34, 2.8, 3, 0.6)  -> top = 3.4   -> centro X = 35.5
                final float EPS = 0.05f; // para que no se “clave” visualmente

                items.add(new Collectible( 6.5f, 2.8f + EPS));
                items.add(new Collectible(16.5f, 3.4f + EPS));
                items.add(new Collectible(24.5f, 3.8f + EPS)); // <- esta era la conflictiva
                items.add(new Collectible(35.5f, 3.4f + EPS));

                allies.add(new AllyHealer(31f, 1f));
                requiredPackages = 4;
                break;
        }
        collectedPackages = 0;

        // 4) Animaciones del jugador (si falta player.png, dibujaremos un rectángulo)
        if (Gdx.files.internal("player.png").exists()) {
            playerSheet = new Texture("player.png");
            int rows = 3, cols = 6;
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

        // 5) Sonidos (cargamos solo si existen)
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

        // 6) Sprites PNG (fallback geométrico si faltan)
        if (Gdx.files.internal("enemy_walker.png").exists()) texEnemyWalker = new Texture("enemy_walker.png");
        if (Gdx.files.internal("enemy_flyer.png").exists())  texEnemyFlyer  = new Texture("enemy_flyer.png");
        if (Gdx.files.internal("ally_healer.png").exists())  texAllyHealer  = new Texture("ally_healer.png");
        if (Gdx.files.internal("collectible.png").exists())  texCollectible = new Texture("collectible.png");

        // Puerta (cerrada/abierta)
        if (Gdx.files.internal("door_closed.png").exists()) texDoorClosed = new Texture("door_closed.png");
        if (Gdx.files.internal("door_open.png").exists())   texDoorOpen   = new Texture("door_open.png");

        // 7) HUD
        hud = new Hud(game.batch);
        hud.setLevel(levelNumber);
        hud.setGoal(collectedPackages, requiredPackages);
        Gdx.input.setInputProcessor(hud.stage);

        // 8) Ventana de pausa
        setupPauseUI();
    }

    /** Construye la ventana de pausa con controles de sonido y navegación. */
    private void setupPauseUI(){
        pauseSkin = UiStyles.makeSkin();
        pauseStage = new Stage(new FitViewport(Constants.VIRTUAL_WIDTH, Constants.VIRTUAL_HEIGHT), game.batch);

        Table panel = new Table(pauseSkin);
        panel.setFillParent(true);
        panel.center().pad(20);
        pauseStage.addActor(panel);

        Label title = new Label("PAUSA", pauseSkin, "title");

        // CheckBoxes de sonido
        CheckBox cbMuteAll   = new CheckBox(" Silencio total", pauseSkin);
        CheckBox cbMuteMusic = new CheckBox(" Silenciar música", pauseSkin);
        CheckBox cbMuteSfx   = new CheckBox(" Silenciar efectos", pauseSkin);

        cbMuteAll.setChecked(muteMusic && muteSfx);
        cbMuteMusic.setChecked(muteMusic);
        cbMuteSfx.setChecked(muteSfx);

        // Botones
        TextButton btnResume = new TextButton("Continuar", pauseSkin);
        TextButton btnMenu   = new TextButton("Menú principal", pauseSkin);
        TextButton btnExit   = new TextButton("Salir del juego", pauseSkin);

        // Layout
        panel.add(title).padBottom(24).row();
        panel.add(cbMuteAll).left().padBottom(10).row();
        panel.add(cbMuteMusic).left().padBottom(10).row();
        panel.add(cbMuteSfx).left().padBottom(18).row();
        panel.add(btnResume).width(260).height(52).pad(6).row();
        panel.add(btnMenu).width(260).height(52).pad(6).row();
        panel.add(btnExit).width(260).height(52).padTop(16).row();

        // Listeners de sonido
        cbMuteAll.addListener(new com.badlogic.gdx.scenes.scene2d.utils.ChangeListener() {
            @Override public void changed(ChangeEvent event, Actor actor) {
                setMuteAll(cbMuteAll.isChecked());
                cbMuteMusic.setChecked(muteMusic);
                cbMuteSfx.setChecked(muteSfx);
            }
        });
        cbMuteMusic.addListener(new com.badlogic.gdx.scenes.scene2d.utils.ChangeListener() {
            @Override public void changed(ChangeEvent event, Actor actor) {
                setMuteMusic(cbMuteMusic.isChecked());
                cbMuteAll.setChecked(muteMusic && muteSfx);
            }
        });
        cbMuteSfx.addListener(new com.badlogic.gdx.scenes.scene2d.utils.ChangeListener() {
            @Override public void changed(ChangeEvent event, Actor actor) {
                setMuteSfx(cbMuteSfx.isChecked());
                cbMuteAll.setChecked(muteMusic && muteSfx);
            }
        });

        // Botones de control
        btnResume.addListener(new ClickListener(){
            @Override public void clicked(InputEvent event, float x, float y) {
                paused = false;
                Gdx.input.setInputProcessor(hud.stage);
                if (!muteMusic && game.bgm != null) game.bgm.play();
                else if (game.bgm != null) game.bgm.pause();
            }
        });
        btnMenu.addListener(new ClickListener(){
            @Override public void clicked(InputEvent event, float x, float y) {
                paused = false;
                game.setScreen(new MenuScreen(game));
            }
        });
        btnExit.addListener(new ClickListener(){
            @Override public void clicked(InputEvent event, float x, float y) {
                Gdx.app.exit();
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

    /** Activa/desactiva silencio total (música + efectos). */
    private void setMuted(boolean value) {
        muted = value;
        if (muted) {
            prevMusicVol = AudioManager.getMusicVolume();
            prevSfxVol   = AudioManager.getSfxVolume();
            AudioManager.setMusicVolume(0f);
            AudioManager.setSfxVolume(0f);
            if (game.bgm != null) game.bgm.setVolume(0f);
        } else {
            AudioManager.setMusicVolume(prevMusicVol);
            AudioManager.setSfxVolume(prevSfxVol);
            if (game.bgm != null) game.bgm.setVolume(prevMusicVol);
        }
    }

    @Override
    public void render(float delta) {
        // Tecla de pausa (P o ESC)
        if (Gdx.input.isKeyJustPressed(Input.Keys.P) || Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            togglePause();
        }

        // Teclas test SFX
        if (Gdx.input.isKeyJustPressed(Input.Keys.J) && sJump != null)    AudioManager.play(sJump);
        if (Gdx.input.isKeyJustPressed(Input.Keys.C) && sCollect != null) AudioManager.play(sCollect);
        if (Gdx.input.isKeyJustPressed(Input.Keys.H) && sHit != null)     AudioManager.play(sHit);

        // ===== UPDATE =====
        if (!paused) {
            player.update(delta, level.solids);

            // Muerte por caída
            if (player.pos.y < -2f) {
                game.setScreen(new GameOverScreen(game, player.score));
                return;
            }

            // Cooldown hit
            if (hitCooldown > 0f) hitCooldown -= delta;

            // Sonido al iniciar salto
            if (lastOnGround && !player.onGround && player.vel.y > 0f && sJump != null) {
                AudioManager.play(sJump);
            }
            lastOnGround = player.onGround;

            // Mover enemigos
            for (EnemyWalker e : enemiesWalk) e.update(delta);
            for (EnemyFlyer  f : enemiesFly)  f.update(delta);

            // Colisiones con enemigos
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

            // Recoger ítems
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

            // Aliado (curación una vez)
            for (AllyHealer a : allies){
                if (!a.isUsed() && player.getBounds().overlaps(a.getBounds())){
                    player.energy = Math.min(100, player.energy + a.healAmount);
                    a.markUsed();
                    if (sCollect != null) AudioManager.play(sCollect);
                }
            }

            // Cámara sigue al jugador (con límites)
            worldCam.position.set(player.pos.x + Constants.PLAYER_W/2f, Math.max(player.pos.y, 4.5f), 0);
            float halfW = worldViewport.getWorldWidth()/2f;
            if (worldCam.position.x < halfW) worldCam.position.x = halfW;
            if (worldCam.position.x > level.widthUnits - halfW) worldCam.position.x = level.widthUnits - halfW;
            worldCam.update();

            // ¿Puede salir?
            boolean canExit = collectedPackages >= requiredPackages;
            if (canExit && player.getBounds().overlaps(level.exit)){
                int nextLevel = levelNumber + 1;
                int MAX_LEVEL = 4; // niveles 1..4
                if (nextLevel <= MAX_LEVEL) {
                    game.setScreen(new GameScreen(game, nextLevel));
                } else {
                    game.setScreen(new GameOverScreen(game, player.score));
                }
                return;
            }

            // Animación jugador
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

        // ===== DRAW / RENDER =====
        Gdx.gl.glClearColor(0.12f,0.14f,0.18f,1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        boolean canExitNow = collectedPackages >= requiredPackages;
        Rectangle exit = level.exit;

        // 1) Geometría básica
        shapes.setProjectionMatrix(worldCam.combined);
        shapes.begin(ShapeRenderer.ShapeType.Filled);

        shapes.setColor(0.30f, 0.35f, 0.42f, 1f);
        for (Rectangle r : level.solids) shapes.rect(r.x, r.y, r.width, r.height);

        // Fallback de puerta (si faltan PNGs)
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

        // 2) Sprites PNG
        game.batch.setProjectionMatrix(worldCam.combined);
        game.batch.begin();

        // Puerta alineada al suelo
        if (texDoorOpen != null || texDoorClosed != null) {
            float drawW = exit.width  * DOOR_DRAW_SCALE_X;
            float drawH = exit.height * DOOR_DRAW_SCALE_Y;
            float drawX = exit.x + (exit.width - drawW) / 2f;
            float drawY = exit.y - DOOR_FLOOR_EPS;

            if (canExitNow && texDoorOpen != null)
                game.batch.draw(texDoorOpen, drawX, drawY, drawW, drawH);
            else if (!canExitNow && texDoorClosed != null)
                game.batch.draw(texDoorClosed, drawX, drawY, drawW, drawH);
        }

        // Enemigos
        if (texEnemyWalker != null) {
            for (EnemyWalker e : enemiesWalk) {
                Rectangle b = e.getBounds();
                game.batch.draw(texEnemyWalker, b.x, b.y, b.width, b.height);
            }
        }
        if (texEnemyFlyer != null) {
            for (EnemyFlyer f : enemiesFly) {
                Rectangle b = f.getBounds();
                game.batch.draw(texEnemyFlyer, b.x, b.y, b.width, b.height);
            }
        }

        // Aliado (si sigue disponible)
        if (texAllyHealer != null) {
            for (AllyHealer a : allies) {
                if (!a.isUsed()) {
                    Rectangle b = a.getBounds();
                    game.batch.draw(texAllyHealer, b.x, b.y, b.width, b.height);
                }
            }
        }

        // Ítems
        if (texCollectible != null) {
            for (Collectible c : items) {
                if (!c.isCollected()) {
                    Rectangle b = c.getBounds();
                    game.batch.draw(texCollectible, b.x, b.y, b.width, b.height);
                }
            }
        }

        // Jugador animado
        if (playerSheet != null && currentFrame != null) {
            game.batch.draw(currentFrame, player.pos.x, player.pos.y, Constants.PLAYER_W, Constants.PLAYER_H);
        }

        game.batch.end();

        // 3) HUD
        hud.setScore(player.score);
        hud.setEnergy(player.energy);
        hud.setGoal(collectedPackages, requiredPackages);
        hud.stage.act(paused ? 0f : Gdx.graphics.getDeltaTime());
        hud.stage.draw();

        // 4) Pausa
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
        if (texEnemyWalker != null) texEnemyWalker.dispose();
        if (texEnemyFlyer  != null) texEnemyFlyer.dispose();
        if (texAllyHealer  != null) texAllyHealer.dispose();
        if (texCollectible != null) texCollectible.dispose();
        if (texDoorClosed != null) texDoorClosed.dispose();
        if (texDoorOpen   != null) texDoorOpen.dispose();
    }

    private void updateVolumes() {
        AudioManager.setMusicVolume(muteMusic ? 0f : 1f);
        AudioManager.setSfxVolume(muteSfx ? 0f : 1f);
        if (game.bgm != null) game.bgm.setVolume(muteMusic ? 0f : 1f);
    }

    private void setMuteMusic(boolean value) {
        muteMusic = value;
        updateVolumes();
    }

    private void setMuteSfx(boolean value) {
        muteSfx = value;
        updateVolumes();
    }

    private void setMuteAll(boolean value) {
        muteMusic = value;
        muteSfx = value;
        updateVolumes();
    }
}
