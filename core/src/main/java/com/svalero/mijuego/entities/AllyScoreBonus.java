package com.svalero.mijuego.entities;

import com.badlogic.gdx.math.Rectangle;

/** Aliado que otorga puntos una única vez al tocarlo. */
public class AllyScoreBonus {
    private final float x, y;
    private final float w = 0.9f, h = 1.0f;
    private boolean used = false;
    public final int bonus; // puntos que da
    private final Rectangle bounds = new Rectangle();

    public AllyScoreBonus(float x, float y, int bonus){
        this.x = x;
        this.y = y;
        this.bonus = bonus;
        bounds.set(x, y, w, h);
    }

    public boolean isUsed(){ return used; }
    public void markUsed(){ used = true; }

    public Rectangle getBounds(){ return bounds; }
}
