package com.svalero.mijuego.entities;

import com.badlogic.gdx.math.Rectangle;

public class EnemyWalker {
    public float x, y;
    public float w = 0.9f, h = 0.9f;
    public float minX, maxX;
    public float speed = 3.5f;
    private int dir = 1;

    private final Rectangle bounds = new Rectangle();

    public EnemyWalker(float x, float y, float minX, float maxX){
        this.x = x; this.y = y; this.minX = minX; this.maxX = maxX;
        updateBounds();
    }

    public void update(float dt){
        x += dir * speed * dt;
        if (x < minX){ x = minX; dir = 1; }
        if (x + w > maxX){ x = maxX - w; dir = -1; }
        updateBounds();
    }

    public Rectangle getBounds(){ updateBounds(); return bounds; }
    private void updateBounds(){ bounds.set(x, y, w, h); }
}
