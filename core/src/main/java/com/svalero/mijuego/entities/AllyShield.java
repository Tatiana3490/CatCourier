package com.svalero.mijuego.entities;

import com.badlogic.gdx.math.Rectangle;

//Aliado que otorga invulnerabilidad temporal contra enemigos.
public class AllyShield {
    private final float x, y;
    private final float w = 0.9f, h = 1.0f;
    private boolean used = false;
    public final float duration; // en segundos
    private final Rectangle bounds = new Rectangle();

    public AllyShield(float x, float y, float duration) {
        this.x = x;
        this.y = y;
        this.duration = duration;
        bounds.set(x, y, w, h);
    }

    public boolean isUsed() { return used; }
    public void markUsed() { used = true; }
    public Rectangle getBounds() { return bounds; }
}
