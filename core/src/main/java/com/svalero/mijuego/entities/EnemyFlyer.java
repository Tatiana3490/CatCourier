package com.svalero.mijuego.entities;

import com.badlogic.gdx.math.Rectangle;

/** Enemigo volador con movimiento senoidal vertical. */
public class EnemyFlyer {
    public float x, baseY;            // posición base
    public float w = 0.9f, h = 0.9f;
    public float amplitude = 1.2f;    // amplitud del "vuelo"
    public float speed = 2.5f;        // velocidad angular
    private float t = 0f;             // tiempo acumulado

    private final Rectangle bounds = new Rectangle();

    public EnemyFlyer(float x, float y){
        this.x = x; this.baseY = y; updateBounds(getY());
    }

    public void update(float dt){
        t += dt;
        updateBounds(getY());
    }

    public float getY(){
        return baseY + (float)Math.sin(t * speed) * amplitude;
    }

    private void updateBounds(float y){ bounds.set(x, y, w, h); }
    public Rectangle getBounds(){ updateBounds(getY()); return bounds; }
}

