package com.svalero.mijuego.items;

import com.badlogic.gdx.math.Rectangle;

public class Collectible {
    public float x, y;
    public float w = 0.6f, h = 0.6f;
    public int value = 10;
    private boolean collected = false;
    private final Rectangle bounds = new Rectangle();

    public Collectible(float x, float y){ this.x = x; this.y = y; updateBounds(); }

    public boolean isCollected(){ return collected; }
    public void collect(){ collected = true; }

    public Rectangle getBounds(){ updateBounds(); return bounds; }
    private void updateBounds(){ bounds.set(x, y, w, h); }
}
