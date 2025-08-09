package com.svalero.mijuego.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;

public class UiStyles {
    public static Skin makeSkin() {
        Skin skin = new Skin();

        // Fuente por defecto
        BitmapFont font = new BitmapFont();
        skin.add("default-font", font, BitmapFont.class);

        // Drawables: rectángulos simples con colores
        Drawable up = colorDrawable(0x2D, 0x6A, 0x4F, 255);     // verde
        Drawable down = colorDrawable(0x1E, 0x46, 0x36, 255);   // verde oscuro
        Drawable over = colorDrawable(0x3B, 0x8F, 0x6B, 255);   // verde claro
        Drawable bg = colorDrawable(20, 20, 24, 200);            // fondo semitransparente

        // Label
        Label.LabelStyle labelStyle = new Label.LabelStyle();
        labelStyle.font = font;
        labelStyle.fontColor = Color.WHITE;
        skin.add("default", labelStyle);

        // TextButton
        TextButton.TextButtonStyle tbs = new TextButton.TextButtonStyle();
        tbs.font = font;
        tbs.up = up;
        tbs.down = down;
        tbs.over = over;
        tbs.fontColor = Color.WHITE;
        skin.add("default", tbs);

        // Fondo genérico para paneles
        skin.add("panel-bg", bg);
        return skin;
    }

    private static Drawable colorDrawable(int r, int g, int b, int a) {
        Pixmap pm = new Pixmap(10, 10, Pixmap.Format.RGBA8888);
        pm.setColor(r/255f, g/255f, b/255f, a/255f);
        pm.fill();
        Texture tx = new Texture(pm);
        pm.dispose();
        return new NinePatchDrawable(new NinePatch(tx, 3,3,3,3));
    }
}
