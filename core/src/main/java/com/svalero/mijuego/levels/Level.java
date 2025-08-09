package com.svalero.mijuego.levels;

import com.badlogic.gdx.math.Rectangle;
import java.util.ArrayList;
import java.util.List;

public class Level {
    public final List<Rectangle> solids = new ArrayList<>();
    public Rectangle exit;
    public float widthUnits = 40f;

    public static Level create(int number){
        Level L = new Level();
        L.solids.add(new Rectangle(0, 0, 200, 1));
        if (number == 1){
            L.solids.add(new Rectangle(4, 2.5f, 4, 0.6f));
            L.solids.add(new Rectangle(10, 4.0f, 3, 0.6f));
            L.solids.add(new Rectangle(15, 5.5f, 3, 0.6f));
            L.widthUnits = 28f;
            L.exit = new Rectangle(L.widthUnits - 1.5f, 1f, 1.0f, 2f);
        } else {
            L.solids.add(new Rectangle(6, 2.8f, 3.5f, 0.6f));
            L.solids.add(new Rectangle(11, 4.2f, 3.0f, 0.6f));
            L.solids.add(new Rectangle(16, 6.0f, 2.5f, 0.6f));
            L.solids.add(new Rectangle(21, 3.6f, 4.0f, 0.6f));
            L.widthUnits = 30f;
            L.exit = new Rectangle(L.widthUnits - 1.5f, 1f, 1.0f, 2f);
        }
        return L;
    }
}

