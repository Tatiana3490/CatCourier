package com.svalero.mijuego.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.svalero.mijuego.MiJuego;
import com.svalero.mijuego.audio.AudioManager;
import com.svalero.mijuego.ui.UiStyles;
import com.svalero.mijuego.util.Constants;

public class SettingsScreen implements Screen {
    private final MiJuego game;
    private Stage stage;// Escenario donde “viven” los widgets (botones, labels, sliders, etc.)
    private Skin skin;// “Skin” con estilos (colores, fuentes, etc.) que nos da UiStyles
    private Sound testSfx; // Sonido de prueba para el botón “Probar efecto”

    public SettingsScreen(MiJuego game){ this.game = game; }

    @Override
    public void show() {
        // 1) Crear skin y stage propios de esta pantalla
        skin  = UiStyles.makeSkin();
        stage = new Stage(new FitViewport(Constants.VIRTUAL_WIDTH, Constants.VIRTUAL_HEIGHT), game.batch);

        // Enviar el input (teclado/ratón) a este stage
        Gdx.input.setInputProcessor(stage);

        // 2) Cargar un SFX de prueba (si existe)
        if (Gdx.files.internal("sfx/jump.wav").exists()) {
            testSfx = Gdx.audio.newSound(Gdx.files.internal("sfx/jump.wav"));
        }

        // 3) Table raíz centrado y ocupando todo
        Table root = new Table(skin);
        root.setFillParent(true);
        root.center();
        stage.addActor(root);

        // 4) Widgets
        Label title  = new Label("Configuración", skin, "title");
        Label lMusic = new Label("Volumen Música", skin);
        Label lSfx   = new Label("Volumen Efectos", skin);

        // Sliders de 0 a 1 con paso 0.01. Les ponemos el valor guardado.
        final Slider sMusic = new Slider(0f, 1f, 0.01f, false, skin);
        sMusic.setValue(AudioManager.getMusicVolume());

        final Slider sSfx = new Slider(0f, 1f, 0.01f, false, skin);
        sSfx.setValue(AudioManager.getSfxVolume());

        TextButton bTest = new TextButton("Probar efecto", skin);
        TextButton bBack = new TextButton("Volver", skin);

        // 5) Layout (orden y tamaños)
        root.add(title).padBottom(32).row();

        root.add(lMusic).left().padBottom(6).row();
        root.add(sMusic).width(520).padBottom(16).row();

        root.add(lSfx).left().padBottom(6).row();
        root.add(sSfx).width(520).padBottom(24).row();

        root.add(bTest).width(260).height(52).padBottom(16).row();
        root.add(bBack).width(260).height(52);

        // 6) Listeners
        // Cuando mueves el slider de música, guardamos el volumen y lo aplicamos al BGM activo
        sMusic.addListener(new ChangeListener() {
            @Override public void changed(ChangeEvent event, Actor actor) {
                AudioManager.setMusicVolume(sMusic.getValue());
            }
        });

        // Cuando mueves el slider de efectos, guardamos el volumen para futuros SFX
        sSfx.addListener(new ChangeListener() {
            @Override public void changed(ChangeEvent event, Actor actor) {
                AudioManager.setSfxVolume(sSfx.getValue());
            }
        });

        // Botón de prueba: reproduce un efecto a volumen actual de SFX
        bTest.addListener(new ClickListener() {
            @Override public void clicked(InputEvent event, float x, float y) {
                if (testSfx != null) AudioManager.play(testSfx);
            }
        });

        // Volver al menú principal
        bBack.addListener(new ClickListener() {
            @Override public void clicked(InputEvent event, float x, float y) {
                game.setScreen(new MenuScreen(game));
            }
        });
    }

    @Override
    public void render(float delta) {
        // IMPORTANTE: limpiar la pantalla para que no se “vea” el menú anterior debajo
        Gdx.gl.glClearColor(0, 0, 0, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        stage.act(delta);
        stage.draw();
    }

    @Override public void resize(int w, int h) { stage.getViewport().update(w, h, true); }
    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}

    @Override
    public void dispose() {
        // Cerrar y liberar recursos creados por esta pantalla
        if (stage   != null) stage.dispose();
        if (skin    != null) skin.dispose();
        if (testSfx != null) testSfx.dispose();
    }
}
