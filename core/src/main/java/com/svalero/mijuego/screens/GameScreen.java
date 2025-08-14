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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Function;

/*Pantalla principal del juego */
public class GameScreen implements Screen {

    // ==========================
    //   REFERENCIAS / ESTADO
    // ==========================
    private final MiJuego game;
    private final int levelNumber;

    // Mundo/cámara (pinto en unidades de mundo, no en píxeles)
    private OrthographicCamera worldCam;
    private ExtendViewport worldViewport;
    private ShapeRenderer shapes;

    // Entidades principales
    private Player player;
    private Level level;

    // Listas de NPCs/ítems (muy básico)
    private final List<EnemyWalker>    enemiesWalk  = new ArrayList<>();
    private final List<EnemyFlyer>     enemiesFly   = new ArrayList<>();
    private final List<EnemyJumper>    enemiesJump  = new ArrayList<>();
    private final List<EnemyDog>       enemiesDog   = new ArrayList<>();
    private final List<EnemyMouse>     enemiesMouse = new ArrayList<>();
    private final List<EnemyCheese>    enemiesCheese= new ArrayList<>();
    private final List<Collectible>    items        = new ArrayList<>();
    private final List<AllyHealer>     alliesHeal   = new ArrayList<>();
    private final List<AllyScoreBonus> alliesBonus  = new ArrayList<>();
    private final List<AllyShield>     alliesShield = new ArrayList<>();

    // HUD (marcadores)
    private Hud hud;

    // Objetivo para abrir la puerta de salida
    private int requiredPackages;
    private int collectedPackages;

    // Pausa / UI
    private boolean paused = false;
    private Stage pauseStage;
    private Skin  pauseSkin;

    // Sonidos
    private Sound sJump, sCollect, sHit;

    // Animación jugador (si no hay sprite, pinto un rectángulo y ya)
    private Texture playerSheet;
    private TextureRegion[][] playerFrames;
    private TextureRegion currentFrame;
    private com.badlogic.gdx.graphics.g2d.Animation<TextureRegion> animIdle, animWalk, animJump;
    private float  stateTime = 0f;
    private boolean facingRight = true;

    // Texturas (todas opcionales)
    private Texture texEnemyWalker, texEnemyFlyer, texEnemyJumper;
    private Texture texAllyHealer, texAllyBonus, texAllyShield;
    private Texture texCollectible;
    private Texture texEnemyDog, texEnemyMouse, texEnemyCheese;
    private Texture texDoorClosed, texDoorOpen;

    // Escala y ajuste visual de la puerta
    private static final float DOOR_DRAW_SCALE_X = 1.8f;
    private static final float DOOR_DRAW_SCALE_Y = 1.8f;
    private static final float DOOR_FLOOR_EPS    = 1.0f;

    // Volúmenes globales muy básicos (0 o 1)
    private boolean muteMusic = false;
    private boolean muteSfx   = false;

    public GameScreen(MiJuego game, int levelNumber){
        this.game = game;
        this.levelNumber = levelNumber;
    }

    // ==========================
    //   CICLO DE VIDA SCREEN
    // ==========================
    @Override
    public void show() {
        // --- Cámara/viewport: 16x9 unidades de mundo ---
        worldCam = new OrthographicCamera();
        worldViewport = new ExtendViewport(16f, 9f, worldCam);
        shapes = new ShapeRenderer();

        // --- Cargar nivel y crear jugador ---
        level = Level.create(levelNumber);
        player = new Player(2f, 2f);

        // Si entro directo al nivel 1 sin pasar por menú, reseteo el acumulado por si acaso.
        if (levelNumber == 1) game.totalScore = 0;

        // --- Spawns muy simples por nivel (a mano) ---
        switch (levelNumber) {
            case 1:
                enemiesWalk.add(new EnemyWalker(6f, 1f, 6f, 10f));
                enemiesFly.add(new EnemyFlyer(12f, 3f));
                items.add(new Collectible(10.5f, 4.7f));
                items.add(new Collectible(16.0f, 5.9f));
                alliesHeal.add(new AllyHealer(8.5f, 1f));
                alliesShield.add(new AllyShield(13.2f, 1f, 6f)); // 6 s de escudo
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
                alliesHeal.add(new AllyHealer(21.5f, 1f));
                alliesBonus.add(new AllyScoreBonus(25.0f, 1f, 150));
                requiredPackages = 3;
                break;

                case 3:
                enemiesWalk.add(new EnemyWalker(5.5f, 1f, 5.5f, 9f));
                enemiesWalk.add(new EnemyWalker(18f, 1f, 18f, 23f));
                enemiesFly.add(new EnemyFlyer(25f, 4.0f));

                enemiesJump.add(new EnemyJumper(13.0f, 1.2f));

                items.add(new Collectible(6.5f, 2.7f));
                items.add(new Collectible(12.2f, 3.2f));
                items.add(new Collectible(19.0f, 4.1f));

                alliesHeal.add(new AllyHealer(22.5f, 1f));
                alliesBonus.add(new AllyScoreBonus(27.2f, 3.6f, 150));
                alliesShield.add(new AllyShield(9.5f, 1f, 6f));

                requiredPackages = 3;
                break;


            case 4:
                // Enemigos base
                enemiesWalk.add(new EnemyWalker(3.5f, 1f, 3.5f, 9f));
                enemiesWalk.add(new EnemyWalker(21f, 1f, 21f, 27f));
                enemiesWalk.add(new EnemyWalker(33f, 1f, 33f, 38f));
                enemiesFly.add(new EnemyFlyer(16f, 4.0f));
                enemiesFly.add(new EnemyFlyer(28f, 3.6f));
                enemiesJump.add(new EnemyJumper(24.0f, 1.2f));
                // Enemigos nuevos
                enemiesDog.add(new EnemyDog(10f, 1f, 3f, 6f));
                enemiesMouse.add(new EnemyMouse(14f, 1f, 0.5f, 5f));
                enemiesCheese.add(new EnemyCheese(18f, 1f, 2f, true));
                // Ítems sobre plataformas (ligero offset para que “no se claven”)
                final float EPS = 0.05f;
                items.add(new Collectible( 6.5f, 2.8f + EPS));
                items.add(new Collectible(16.5f, 3.4f + EPS));
                items.add(new Collectible(24.5f, 3.8f + EPS));
                items.add(new Collectible(35.5f, 3.4f + EPS));
                // Aliados
                alliesHeal.add(new AllyHealer(31f, 1f));
                alliesShield.add(new AllyShield(26.0f, 1f, 6f));
                alliesBonus.add(new AllyScoreBonus(36.0f, 1f, 200));
                requiredPackages = 4;
                break;
        }
        collectedPackages = 0;

        // --- Animación del jugador ---
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

        // --- SFX (defensivo) ---
        try {
            if (Gdx.files.internal("sfx/jump.wav").exists())    sJump    = Gdx.audio.newSound(Gdx.files.internal("sfx/jump.wav"));
            if (Gdx.files.internal("sfx/collect.wav").exists()) sCollect = Gdx.audio.newSound(Gdx.files.internal("sfx/collect.wav"));
            if (Gdx.files.internal("sfx/hit.wav").exists())     sHit     = Gdx.audio.newSound(Gdx.files.internal("sfx/hit.wav"));
        } catch (Exception e) {
            Gdx.app.error("SFX", "Error cargando SFX: " + e.getMessage(), e);
        }

        // --- Texturas PNG (opcionales) ---
        if (Gdx.files.internal("enemy_walker.png").exists()) texEnemyWalker = new Texture("enemy_walker.png");
        if (Gdx.files.internal("enemy_flyer.png").exists())  texEnemyFlyer  = new Texture("enemy_flyer.png");
        if (Gdx.files.internal("enemy_jumper.png").exists()) texEnemyJumper = new Texture("enemy_jumper.png");
        if (Gdx.files.internal("ally_healer.png").exists())  texAllyHealer  = new Texture("ally_healer.png");
        if (Gdx.files.internal("ally_bonus.png").exists())   texAllyBonus   = new Texture("ally_bonus.png");
        if (Gdx.files.internal("ally_shield.png").exists())  texAllyShield  = new Texture("ally_shield.png");
        if (Gdx.files.internal("collectible.png").exists())  texCollectible = new Texture("collectible.png");
        if (Gdx.files.internal("door_closed.png").exists())  texDoorClosed  = new Texture("door_closed.png");
        if (Gdx.files.internal("door_open.png").exists())    texDoorOpen    = new Texture("door_open.png");
        if (Gdx.files.internal("enemy_dog.png").exists())    texEnemyDog    = new Texture("enemy_dog.png");
        if (Gdx.files.internal("enemy_mouse.png").exists())  texEnemyMouse  = new Texture("enemy_mouse.png");
        if (Gdx.files.internal("enemy_cheese.png").exists()) texEnemyCheese = new Texture("enemy_cheese.png");

        // --- HUD (muestro el score GLOBAL) ---
        hud = new Hud(game.batch);
        hud.setLevel(levelNumber);
        hud.setGoal(collectedPackages, requiredPackages);
        hud.setScore(game.totalScore); // uso el acumulado
        Gdx.input.setInputProcessor(hud.stage);

        // --- UI de pausa ---
        setupPauseUI();
    }

    /** Ventana de pausa con mute de música/efectos y navegación básica. */
    private void setupPauseUI(){
        pauseSkin = UiStyles.makeSkin();
        pauseStage = new Stage(new FitViewport(Constants.VIRTUAL_WIDTH, Constants.VIRTUAL_HEIGHT), game.batch);

        Table panel = new Table(pauseSkin);
        panel.setFillParent(true);
        panel.center().pad(20);
        pauseStage.addActor(panel);

        Label title = new Label("PAUSA", pauseSkin, "title");
        CheckBox cbMuteMusic = new CheckBox(" Silenciar música", pauseSkin);
        CheckBox cbMuteSfx   = new CheckBox(" Silenciar efectos", pauseSkin);
        cbMuteMusic.setChecked(muteMusic);
        cbMuteSfx.setChecked(muteSfx);

        TextButton btnResume = new TextButton("Continuar", pauseSkin);
        TextButton btnMenu   = new TextButton("Menú principal", pauseSkin);
        TextButton btnExit   = new TextButton("Salir del juego", pauseSkin);

        panel.add(title).padBottom(24).row();
        panel.add(cbMuteMusic).left().padBottom(10).row();
        panel.add(cbMuteSfx).left().padBottom(18).row();
        panel.add(btnResume).width(260).height(52).pad(6).row();
        panel.add(btnMenu).width(260).height(52).pad(6).row();
        panel.add(btnExit).width(260).height(52).padTop(16).row();

        cbMuteMusic.addListener(new com.badlogic.gdx.scenes.scene2d.utils.ChangeListener() {
            @Override public void changed(ChangeEvent event, Actor actor) { setMuteMusic(cbMuteMusic.isChecked()); }
        });
        cbMuteSfx.addListener(new com.badlogic.gdx.scenes.scene2d.utils.ChangeListener() {
            @Override public void changed(ChangeEvent event, Actor actor) { setMuteSfx(cbMuteSfx.isChecked()); }
        });

        btnResume.addListener(new ClickListener(){
            @Override public void clicked(InputEvent event, float x, float y) {
                paused = false;
                Gdx.input.setInputProcessor(hud.stage);
                if (!muteMusic && game.bgm != null) game.bgm.play(); else if (game.bgm != null) game.bgm.pause();
            }
        });
        btnMenu.addListener(new ClickListener(){
            @Override public void clicked(InputEvent event, float x, float y) {
                paused = false;
                game.setScreen(new MenuScreen(game));
            }
        });
        btnExit.addListener(new ClickListener(){
            @Override public void clicked(InputEvent event, float x, float y) { Gdx.app.exit(); }
        });
    }

    /** Alterno pausa y paso el input al stage que toca. */
    private void togglePause(){
        paused = !paused;
        if (paused) {
            Gdx.input.setInputProcessor(pauseStage);
            if (game.bgm != null) game.bgm.pause();
        } else {
            Gdx.input.setInputProcessor(hud.stage);
            if (game.bgm != null && !muteMusic) game.bgm.play();
        }
    }

    // ==========================
    //   BUCLE PRINCIPAL
    // ==========================
    @Override
    public void render(float delta) {
        // Pausa con P o ESC
        if (Gdx.input.isKeyJustPressed(Input.Keys.P) || Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            togglePause();
        }

        // Teclas de prueba para oír SFX
        if (Gdx.input.isKeyJustPressed(Input.Keys.J) && sJump != null)    AudioManager.play(sJump);
        if (Gdx.input.isKeyJustPressed(Input.Keys.C) && sCollect != null) AudioManager.play(sCollect);
        if (Gdx.input.isKeyJustPressed(Input.Keys.H) && sHit != null)     AudioManager.play(sHit);

        // ========== UPDATE ==========
        if (!paused) {
            // Físicas y colisiones del jugador con el mapa
            player.update(delta, level.solids);
            player.tickShield(delta); // escudo baja aquí

            // Sonido de salto (sólo el frame en el que empezó el salto)
            if (player.consumeJustJumped() && sJump != null) AudioManager.play(sJump);

            // Caída fuera del mundo = Game Over con score acumulado
            if (player.pos.y < -2f) {
                game.setScreen(new GameOverScreen(game, game.totalScore));
                return;
            }

            // Mover NPCs
            for (EnemyWalker e : enemiesWalk) e.update(delta);
            for (EnemyFlyer  f : enemiesFly)  f.update(delta);
            for (EnemyJumper j : enemiesJump) j.update(delta);
            for (EnemyDog    d : enemiesDog)  d.update(delta, player.pos.x);
            for (EnemyMouse  m : enemiesMouse) m.update(delta);

            // Queso: si entro en rango, cuenta como golpe
            for (EnemyCheese c : enemiesCheese) {
                if (c.checkTrigger(player.pos.x, player.pos.y)) {
                    if (player.onHit() && sHit != null) AudioManager.play(sHit);
                }
            }

            // Daño por contacto (uso helper genérico con referencia a método)
            checkTouch(enemiesWalk, EnemyWalker::getBounds);
            checkTouch(enemiesFly,  EnemyFlyer::getBounds);
            checkTouch(enemiesJump, EnemyJumper::getBounds);
            checkTouch(enemiesDog,  EnemyDog::getBounds);
            checkTouch(enemiesMouse, EnemyMouse::getBounds);

            // ¿Muerto por 2º golpe? fin con score acumulado
            if (player.isDead()) {
                game.setScreen(new GameOverScreen(game, game.totalScore));
                return;
            }

            // Aliados: bonus, curación y escudo (una vez) —> TODOS suman al marcador global
            for (AllyScoreBonus b : alliesBonus) {
                if (!b.isUsed() && player.getBounds().overlaps(b.getBounds())) {
                    b.markUsed();
                    game.totalScore += b.bonus;
                    if (sCollect != null) AudioManager.play(sCollect);
                }
            }
            for (AllyHealer a : alliesHeal){
                if (!a.isUsed() && player.getBounds().overlaps(a.getBounds())){
                    player.energy = Math.min(100, player.energy + a.healAmount);
                    a.markUsed();
                    if (sCollect != null) AudioManager.play(sCollect);
                }
            }
            for (AllyShield s : alliesShield) {
                if (!s.isUsed() && player.getBounds().overlaps(s.getBounds())) {
                    s.markUsed();
                    player.activateShield(s.duration);
                    if (sCollect != null) AudioManager.play(sCollect);
                }
            }

            // Recoger paquetes (SUMAN AL MARCADOR GLOBAL y cuentan para la puerta)
            for (Iterator<Collectible> it = items.iterator(); it.hasNext();) {
                Collectible c = it.next();
                if (!c.isCollected() && player.getBounds().overlaps(c.getBounds())){
                    c.collect();
                    game.totalScore += c.value;
                    collectedPackages++;
                    if (sCollect != null) AudioManager.play(sCollect);
                }
            }
            items.removeIf(Collectible::isCollected);

            // Actualizo HUD con score acumulado
            hud.setScore(game.totalScore);

            // Cámara sigue al jugador pero no se sale del nivel
            worldCam.position.set(
                player.pos.x + Constants.PLAYER_W/2f,
                Math.max(player.pos.y, 4.5f),
                0
            );
            float halfW = worldViewport.getWorldWidth()/2f;
            if (worldCam.position.x < halfW) worldCam.position.x = halfW;
            if (worldCam.position.x > level.widthUnits - halfW) worldCam.position.x = level.widthUnits - halfW;
            worldCam.update();

            // Si ya tengo suficientes cajas y toco la salida → siguiente nivel o fin
            boolean canExit = collectedPackages >= requiredPackages;
            if (canExit && player.getBounds().overlaps(level.exit)){
                int nextLevel = levelNumber + 1;
                int MAX_LEVEL = 4;
                if (nextLevel <= MAX_LEVEL) game.setScreen(new GameScreen(game, nextLevel));
                else                         game.setScreen(new GameOverScreen(game, game.totalScore));
                return;
            }

            // Selecciono frame del jugador según estado
            stateTime += delta;
            if (playerSheet != null) {
                boolean moving = Math.abs(player.vel.x) > 0.05f;
                boolean airborne = !player.onGround;
                if (airborne)      currentFrame = animJump.getKeyFrame(stateTime);
                else if (moving)   currentFrame = animWalk.getKeyFrame(stateTime);
                else               currentFrame = animIdle.getKeyFrame(stateTime);

                // Flip horizontal según dirección de movimiento
                if (moving) facingRight = player.vel.x >= 0f;
                if (currentFrame != null && currentFrame.isFlipX() == !facingRight) {
                    currentFrame.flip(true, false);
                }
            }
        }

        // ========== DRAW ==========
        Gdx.gl.glClearColor(0.12f,0.14f,0.18f,1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        boolean canExitNow = collectedPackages >= requiredPackages;
        Rectangle exit = level.exit;

        // 1) Geometría (suelos + fallback de puerta y jugador si no hay sprites)
        shapes.setProjectionMatrix(worldCam.combined);
        shapes.begin(ShapeRenderer.ShapeType.Filled);

        // Suelos/plataformas
        shapes.setColor(0.30f, 0.35f, 0.42f, 1f);
        for (Rectangle r : level.solids) shapes.rect(r.x, r.y, r.width, r.height);

        // Puerta fallback (si me faltan las texturas)
        if ((texDoorOpen == null && texDoorClosed == null) ||
            (canExitNow && texDoorOpen == null) ||
            (!canExitNow && texDoorClosed == null)) {
            shapes.setColor(
                canExitNow ? 0.1f : 0.05f,
                canExitNow ? 0.7f : 0.35f,
                canExitNow ? 0.3f : 0.15f,
                1f
            );
            shapes.rect(exit.x, exit.y, exit.width, exit.height);
        }

        // Player fallback (rectángulo amarillo) si no tengo sprite
        if (playerSheet == null) {
            shapes.setColor(0.9f, 0.9f, 0.1f, 1f);
            shapes.rect(player.pos.x, player.pos.y, Constants.PLAYER_W, Constants.PLAYER_H);
        }
        shapes.end();

        // 2) Sprites
        game.batch.setProjectionMatrix(worldCam.combined);
        game.batch.begin();

        // Puerta con imagen, apoyada en el suelo
        if (texDoorOpen != null || texDoorClosed != null) {
            float drawW = exit.width  * DOOR_DRAW_SCALE_X;
            float drawH = exit.height * DOOR_DRAW_SCALE_Y;
            float drawX = exit.x + (exit.width - drawW) / 2f;
            float drawY = exit.y - DOOR_FLOOR_EPS;
            if (canExitNow && texDoorOpen != null)  game.batch.draw(texDoorOpen,  drawX, drawY, drawW, drawH);
            else if (!canExitNow && texDoorClosed != null) game.batch.draw(texDoorClosed, drawX, drawY, drawW, drawH);
        }

        // Enemigos
        if (texEnemyWalker != null)
            for (EnemyWalker e : enemiesWalk) { Rectangle b = e.getBounds(); game.batch.draw(texEnemyWalker, b.x, b.y, b.width, b.height); }
        if (texEnemyFlyer != null)
            for (EnemyFlyer f : enemiesFly)   { Rectangle b = f.getBounds(); game.batch.draw(texEnemyFlyer,  b.x, b.y, b.width, b.height); }
        if (texEnemyJumper != null)
            for (EnemyJumper j : enemiesJump) { Rectangle b = j.getBounds(); game.batch.draw(texEnemyJumper, b.x, b.y, b.width, b.height); }
        if (texEnemyDog != null)
            for (EnemyDog d : enemiesDog)     { Rectangle b = d.getBounds(); game.batch.draw(texEnemyDog, b.x, b.y, b.width, b.height); }
        if (texEnemyMouse != null)
            for (EnemyMouse m : enemiesMouse) { Rectangle b = m.getBounds(); game.batch.draw(texEnemyMouse, b.x, b.y, b.width, b.height); }
        if (texEnemyCheese != null)
            for (EnemyCheese c : enemiesCheese){ Rectangle b = c.getBounds(); game.batch.draw(texEnemyCheese, b.x, b.y, b.width, b.height); }

        // Aliados (sólo si no están “gastados”)
        if (texAllyHealer != null)
            for (AllyHealer a : alliesHeal)   if (!a.isUsed()) { Rectangle b = a.getBounds(); game.batch.draw(texAllyHealer, b.x, b.y, b.width, b.height); }
        if (texAllyBonus != null)
            for (AllyScoreBonus a : alliesBonus) if (!a.isUsed()) { Rectangle b = a.getBounds(); game.batch.draw(texAllyBonus, b.x, b.y, b.width, b.height); }
        if (texAllyShield != null)
            for (AllyShield a : alliesShield) if (!a.isUsed()) { Rectangle b = a.getBounds(); game.batch.draw(texAllyShield, b.x, b.y, b.width, b.height); }

        // Ítems (cajas)
        if (texCollectible != null)
            for (Collectible c : items) if (!c.isCollected()) { Rectangle b = c.getBounds(); game.batch.draw(texCollectible, b.x, b.y, b.width, b.height); }

        // Jugador animado
        if (playerSheet != null && currentFrame != null)
            game.batch.draw(currentFrame, player.pos.x, player.pos.y, Constants.PLAYER_W, Constants.PLAYER_H);

        game.batch.end();

        // 3) HUD encima de todo (score global ya actualizado más arriba)
        hud.setEnergy(player.energy);
        hud.setGoal(collectedPackages, requiredPackages);
        hud.stage.act(paused ? 0f : Gdx.graphics.getDeltaTime());
        hud.stage.draw();

        // 4) Pausa (overlay oscuro + ventana)
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

    // ==========================
    //   HELPERS DE COLISIÓN
    // ==========================
    /** Helper genérico: le paso una lista de cualquier tipo y una función para obtener su Rectangle. */
    private <T> boolean checkTouch(List<T> list, Function<T, Rectangle> getBounds) {
        Rectangle pb = player.getBounds();
        boolean hit = false;
        for (T e : list) {
            if (pb.overlaps(getBounds.apply(e))) {
                // Aquí mismo registro el golpe e incluso disparo el SFX
                if (player.onHit() && sHit != null) AudioManager.play(sHit);
                hit = true;
            }
        }
        return hit;
    }

    // ==========================
    //   EVENTOS SECUNDARIOS
    // ==========================
    @Override public void resize(int width, int height){
        worldViewport.update(width, height, true);
        hud.stage.getViewport().update(width, height, true);
        if (pauseStage != null) pauseStage.getViewport().update(width, height, true);
    }
    @Override public void pause(){}
    @Override public void resume(){}
    @Override public void hide(){}

    // ==========================
    //   LIMPIEZA DE RECURSOS
    // ==========================
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
        if (texEnemyJumper != null) texEnemyJumper.dispose();
        if (texAllyHealer  != null) texAllyHealer.dispose();
        if (texAllyBonus   != null) texAllyBonus.dispose();
        if (texAllyShield  != null) texAllyShield.dispose();
        if (texCollectible != null) texCollectible.dispose();
        if (texDoorClosed  != null) texDoorClosed.dispose();
        if (texDoorOpen    != null) texDoorOpen.dispose();
        if (texEnemyDog    != null) texEnemyDog.dispose();
        if (texEnemyMouse  != null) texEnemyMouse.dispose();
        if (texEnemyCheese != null) texEnemyCheese.dispose();
    }

    // ==========================
    //   AUDIO
    // ==========================
    private void updateVolumes() {
        AudioManager.setMusicVolume(muteMusic ? 0f : 1f);
        AudioManager.setSfxVolume(muteSfx ? 0f : 1f);
        if (game.bgm != null) game.bgm.setVolume(muteMusic ? 0f : 1f);
    }
    private void setMuteMusic(boolean value) { muteMusic = value; updateVolumes(); }
    private void setMuteSfx(boolean value)   { muteSfx   = value; updateVolumes(); }
}
