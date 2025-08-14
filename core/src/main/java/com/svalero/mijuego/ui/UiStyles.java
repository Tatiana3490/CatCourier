package com.svalero.mijuego.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;

/**
 * Skin con fuente TTF (FreeType). Incluye estilos:
 * Label (default/title), TextButton, Button, Window,
 * Slider (default-horizontal), CheckBox y ScrollPane.
 */
public class UiStyles {

    public static Skin makeSkin() {
        Skin skin = new Skin();

        // === 1) Genera una BitmapFont desde TTF ===
        FreeTypeFontGenerator gen = new FreeTypeFontGenerator(Gdx.files.internal("fonts/NotoSans-Regular.ttf"));

        // Fuente normal (UI)
        FreeTypeFontGenerator.FreeTypeFontParameter p = new FreeTypeFontGenerator.FreeTypeFontParameter();
        p.size = 18; // tamaño en píxeles

        p.characters = FreeTypeFontGenerator.DEFAULT_CHARS + "←→↑↓[]";
        BitmapFont font = gen.generateFont(p);

        // Fuente de título (un poco más grande)
        FreeTypeFontGenerator.FreeTypeFontParameter pTitle = new FreeTypeFontGenerator.FreeTypeFontParameter();
        pTitle.size = 22;
        pTitle.characters = p.characters;
        BitmapFont fontTitle = gen.generateFont(pTitle);

        gen.dispose();

        // Registramos fuentes en el Skin
        skin.add("default-font", font, BitmapFont.class);
        skin.add("title-font",   fontTitle, BitmapFont.class);

        // === 2) Drawables planos para botones/slider/panel ===
        Drawable btnUp    = colored(0x2d2f39ff);
        Drawable btnDown  = colored(0x22242bff);
        Drawable btnChk   = colored(0x3a3d49ff);
        Drawable panelBg  = colored(0x1e2027ff);
        Drawable sliderBg = colored(0x3a3d49ff);
        Drawable sliderKn = colored(0xf9a11bff);

        // === 3) Label styles ===
        skin.add("default", new Label.LabelStyle(font, Color.WHITE));
        skin.add("title",   new Label.LabelStyle(fontTitle, Color.valueOf("ffd37a")));

        // === 4) TextButton ===
        TextButton.TextButtonStyle tbs = new TextButton.TextButtonStyle();
        tbs.up = btnUp; tbs.down = btnDown; tbs.checked = btnChk;
        tbs.font = font; tbs.fontColor = Color.WHITE;
        skin.add("default", tbs);

        // === 5) Button (sin texto) ===
        Button.ButtonStyle bs = new Button.ButtonStyle();
        bs.up = btnUp; bs.down = btnDown; bs.checked = btnChk;
        skin.add("default", bs);

        // === 6) Window / Panel ===
        Window.WindowStyle ws = new Window.WindowStyle();
        ws.background = panelBg;
        ws.titleFont = fontTitle;
        ws.titleFontColor = Color.WHITE;
        skin.add("default", ws);

        // === 7) Slider horizontal (clave: "default-horizontal") ===
        Slider.SliderStyle sss = new Slider.SliderStyle();
        sss.background  = sliderBg;
        sss.knob        = sliderKn;
        sss.knobBefore  = colored(0xf4c97bff);
        sss.knobAfter   = colored(0x2d2f39ff);
        skin.add("default-horizontal", sss);

        // === 8) CheckBox (opcional) ===
        CheckBox.CheckBoxStyle cbs = new CheckBox.CheckBoxStyle();
        cbs.checkboxOn  = colored(0xf9a11bff);
        cbs.checkboxOff = colored(0x3a3d49ff);
        cbs.font = font; cbs.fontColor = Color.WHITE;
        skin.add("default", cbs);

        // === 9) ScrollPane (opcional) ===
        ScrollPane.ScrollPaneStyle sps = new ScrollPane.ScrollPaneStyle();
        sps.background = panelBg;
        skin.add("default", sps);

        return skin;
    }

    // Drawable de color sólido con Pixmap (8x8) y NinePatch (bordes 2px).
    private static Drawable colored(int rgba8888) {
        Pixmap pm = new Pixmap(8, 8, Pixmap.Format.RGBA8888);
        Color temp = new Color();
        Color.rgba8888ToColor(temp, rgba8888);
        pm.setColor(temp);
        pm.fill();
        NinePatch np = new NinePatch(new Texture(pm), 2, 2, 2, 2);
        pm.dispose();
        return new NinePatchDrawable(np);
    }
}
