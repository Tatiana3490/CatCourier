package com.svalero.mijuego.entities;

import com.badlogic.gdx.math.Rectangle;

public class AllyHealer {
    public float x, y;
    public float w = 0.9f, h = 1.1f;
    public int healAmount = 20;
    private boolean used = false;

    private final Rectangle bounds = new Rectangle();

    public AllyHealer(float x, float y){ this.x = x; this.y = y; updateBounds(); }

    public boolean canHeal(){ return !used; }
    public void markUsed(){ used = true; }
    public boolean isUsed(){ return used; }

    public Rectangle getBounds(){ updateBounds(); return bounds; }
    private void updateBounds(){ bounds.set(x, y, w, h); }
}
