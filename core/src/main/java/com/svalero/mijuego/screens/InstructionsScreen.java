package com.svalero.mijuego.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.svalero.mijuego.MiJuego;
import com.svalero.mijuego.ui.UiStyles;
import com.svalero.mijuego.util.Constants;

public class InstructionsScreen implements Screen {
    private final MiJuego game;
    private Stage stage;
    private Skin skin;

    public InstructionsScreen(MiJuego game){ this.game = game; }

    @Override
    public void show(){
        skin = UiStyles.makeSkin();
        stage = new Stage(new FitViewport(Constants.VIRTUAL_WIDTH, Constants.VIRTUAL_HEIGHT), game.batch);
        Gdx.input.setInputProcessor(stage);

        Table root = new Table(skin);
        root.setFillParent(true);
        root.top().pad(24);
        stage.addActor(root);

        // Título con estilo "title"
        Label title = new Label("Instrucciones", skin, "title");

        // Texto con flechas (requiere la TTF configurada en UiStyles)
        String txt =
            "Objetivo: recoge los paquetes requeridos para abrir la salida.\n" +
                "Controles: FLECHA IZQ. /FELCHA DRCH. o A/D para moverte, ESPACIO para saltar.\n" +
                "P o ESC para pausar.\n" +
                "Evita enemigos rojos/magenta y toca el aliado azul para curarte.";
        Label body = new Label(txt, skin);
        body.setWrap(true);
        body.setAlignment(Align.topLeft);

        TextButton back = new TextButton("Volver", skin);

        // Layout
        root.add(title).left().padBottom(18).row();
        root.add(body).width(720).left().padBottom(24).row();
        root.add(back).width(220).height(48).left();

        back.addListener(new ClickListener(){
            @Override public void clicked(InputEvent event, float x, float y){
                game.setScreen(new MenuScreen(game));
            }
        });
    }

    @Override
    public void render(float delta){
        // Limpiar pantalla
        Gdx.gl.glClearColor(0, 0, 0, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        stage.act(delta);
        stage.draw();
    }

    @Override public void resize(int w, int h){ stage.getViewport().update(w, h, true); }
    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}

    @Override
    public void dispose(){
        if (stage != null) stage.dispose();
        if (skin  != null) skin.dispose();
    }
}
