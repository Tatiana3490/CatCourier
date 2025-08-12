package com.svalero.mijuego.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.svalero.mijuego.MiJuego;
import com.svalero.mijuego.scores.ScoreManager;   // gestor de puntuaciones (lee/escribe JSON)
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
        stage  = new Stage(new FitViewport(Constants.VIRTUAL_WIDTH, Constants.VIRTUAL_HEIGHT, camera), game.batch);
        skin   = UiStyles.makeSkin();
        Gdx.input.setInputProcessor(stage);

        // Tabla raíz para maquetar todo centrado
        Table root = new Table(skin);
        root.setFillParent(true);
        root.pad(20);
        stage.addActor(root);

        // Título y puntuación
        Label title = new Label("¡Fin de la partida!", skin, "title");
        Label scoreLabel = new Label("Puntuación: " + score, skin);
        title.setAlignment(Align.center);

        // --- TextField para el nombre ---
        // Nuestro UiStyles no tiene TextFieldStyle, así que lo creamos aquí.
        TextField.TextFieldStyle tfs = makeTextFieldStyle();
        TextField tfName = new TextField("", tfs);
        tfName.setMessageText("Escribe tu nombre");   // placeholder (gris)
        tfName.setFocusTraversal(true);

        // Botones
        TextButton bSave = new TextButton("Guardar y ver Ranking", skin);
        TextButton bRetry = new TextButton("Volver a jugar", skin);
        TextButton bMenu = new TextButton("Menú principal", skin);

        // Layout
        root.add(title).padBottom(18).row();
        root.add(scoreLabel).padBottom(14).row();
        root.add(new Label("Nombre:", skin)).left().padBottom(6).row();
        root.add(tfName).width(360).padBottom(12).row();
        root.add(bSave).width(360).height(52).padBottom(12).row();
        root.add(bRetry).width(260).height(48).pad(6).row();
        root.add(bMenu).width(260).height(48).pad(6).row();

        // Guardar y abrir ranking
        bSave.addListener(new ClickListener() {
            @Override public void clicked(InputEvent event, float x, float y) {
                String name = sanitizeName(tfName.getText());
                ScoreManager.addScore(name, score);
                game.setScreen(new HighScoresScreen(game));
            }
        });

        // Enter en el TextField también guarda y abre ranking
        tfName.setTextFieldListener((field, c) -> {
            if (c == '\n' || c == '\r') {
                String name = sanitizeName(field.getText());
                ScoreManager.addScore(name, score);
                game.setScreen(new HighScoresScreen(game));
            }
        });

        // Volver a jugar (reinicia en nivel 1)
        bRetry.addListener(new ClickListener() {
            @Override public void clicked(InputEvent event, float x, float y) {
                game.setScreen(new GameScreen(game, 1));
            }
        });

        // Menú principal
        bMenu.addListener(new ClickListener() {
            @Override public void clicked(InputEvent event, float x, float y) {
                game.setScreen(new MenuScreen(game));
            }
        });

        // Foco inicial en el campo de texto para teclear directamente
        stage.setKeyboardFocus(tfName);
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0.08f, 0.08f, 0.08f, 1f);
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
        if (skin  != null) skin.dispose();
    }

    // =======================
    // Helpers
    // =======================

    /** Asegura un nombre válido: quita espacios extremos, recorta largo máximo y pone "Jugador" si queda vacío. */
    private String sanitizeName(String raw) {
        String s = raw == null ? "" : raw.trim();
        if (s.isEmpty()) s = "Jugador";
        // Evita nombres larguísimos en pantalla y fichero
        if (s.length() > 20) s = s.substring(0, 20);
        return s;
    }

    /** Crea un TextFieldStyle muy básico con fondos/cursores dibujados a color. */
    private TextField.TextFieldStyle makeTextFieldStyle() {
        // Usamos la fuente por defecto que guardamos en el Skin
        BitmapFont font = skin.getFont("default-font");

        // Drawables simples (fondo, selección y cursor)
        NinePatchDrawable bg     = solidDrawable(0x2d2f39ff, 6); // gris oscuro con padding
        NinePatchDrawable bgFocus= solidDrawable(0x3a3d49ff, 6); // un poco más claro al enfocar
        NinePatchDrawable sel    = solidDrawable(0x5588ffff, 0); // selección azul semitransparente
        NinePatchDrawable cursor = solidDrawable(0xffffffff, 0); // cursor blanco

        TextField.TextFieldStyle style = new TextField.TextFieldStyle();
        style.font = font;
        style.fontColor = Color.WHITE;
        style.messageFont = font;
        style.messageFontColor = new Color(1,1,1,0.4f); // placeholder gris
        style.background = bg;
        style.focusedBackground = bgFocus;
        style.cursor = cursor;
        style.selection = sel;

        return style;
    }

    /** Crea un NinePatchDrawable sólido del color indicado. padPx añade “relleno interno” (padding visual). */
    private NinePatchDrawable solidDrawable(int rgba8888, int padPx) {
        // Mini lienzo 8x8 del color pedido
        Pixmap pm = new Pixmap(8, 8, Pixmap.Format.RGBA8888);
        Color temp = new Color();
        Color.rgba8888ToColor(temp, rgba8888);
        pm.setColor(temp);
        pm.fill();

        // NinePatch con bordes de 2px para poder estirar sin deformar
        NinePatch np = new NinePatch(new Texture(pm), 2, 2, 2, 2);
        pm.dispose();

        NinePatchDrawable nd = new NinePatchDrawable(np);
        if (padPx > 0) {
            nd.setLeftWidth(padPx);
            nd.setRightWidth(padPx);
            nd.setTopHeight(padPx);
            nd.setBottomHeight(padPx);
        }
        return nd;
    }
}
